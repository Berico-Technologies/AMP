package amp.gel.dao.impl.accumulo;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.Text;

import amp.gel.dao.EnvelopeValidationUtils;

import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;

/**
 * Generates Accumulo mutations from envelopes.
 * 
 * There are 2 column families: one for the headers and one for the payload.
 * Separating parts of the envelope into different column families as a logical
 * separation but also as a possible performance optimization since the headers
 * will likely be accessed most often.
 * 
 */
public class DefaultMutationsGenerator implements MutationsGenerator {

	public static final Text HEADERS_COLUMN_FAMILY = new Text("headers");

	public static final Text PAYLOAD_COLUMN_FAMILY = new Text("payload");

	public static final Text RAW_PAYLOAD_COLUMN_QUALIFIER = new Text("raw");

	public static final String ROW_ID_DELIMETER = "|";

	public List<Mutation> generate(Envelope envelope) {
		if (envelope == null)
			return new ArrayList<Mutation>();

		Map<String, String> headers = envelope.getHeaders();
		if (!EnvelopeValidationUtils.areHeadersValid(headers))
			return new ArrayList<Mutation>();

		String rowId = generateRowId(headers);
		long timestamp = generateTimestamp(headers);

		Mutation mutation = new Mutation(new Text(rowId));
		addHeaders(headers, timestamp, mutation);
		addPayload(envelope, timestamp, mutation);

		return Arrays.asList(mutation);
	}

	/**
	 * Ideally, the row ID is a combination of sender ID, the topic, and receipt
	 * time. Since the receipt time is only accurate down to the millisecond and
	 * there can be > 1K messages / s, there can be multiple messages per
	 * millisecond. As such, a unique ID is appended to the row ID to guarantee
	 * uniqueness. Here is an example row ID:
	 * 
	 * <pre>
	 * mnguyen|gel.wiretap.SimplePojo|2013-04-23T18:50:36.281Z|1ba46158-e3f3-408b-b75a-d185504ac762
	 * </pre> 
	 */
	private String generateRowId(Map<String, String> headers) {
		String uniqueId = DefaultMutationsGenerator.generateUniqueId();

		String senderId = headers
				.get(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY);
		String topic = headers.get(EnvelopeHeaderConstants.MESSAGE_TOPIC);
		String receiptTime = headers
				.get(EnvelopeHeaderConstants.ENVELOPE_RECEIPT_TIME);

		String rowId;
		if (StringUtils.isNotBlank(senderId) && StringUtils.isNotBlank(topic)
				&& EnvelopeValidationUtils.isValidTimeMillis(receiptTime)) {
			String formattedTimestamp = MutationsGeneratorUtils
					.longToFormattedTimestamp(Long.valueOf(receiptTime));
			List<String> rowIdElements = Arrays.asList(senderId, topic,
					formattedTimestamp, uniqueId);
			rowId = StringUtils.join(rowIdElements, ROW_ID_DELIMETER);
		} else {
			rowId = uniqueId;
		}

		return rowId;
	}

	/**
	 * Ideally, the timestamp for Accumulo is just the receipt time from the
	 * envelope header. If this isn't available, then just use the current time.
	 */
	private long generateTimestamp(Map<String, String> headers) {
		long timestamp = System.currentTimeMillis();

		if (headers != null) {
			String receiptTime = headers
					.get(EnvelopeHeaderConstants.ENVELOPE_RECEIPT_TIME);
			if (EnvelopeValidationUtils.isValidTimeMillis(receiptTime)) {
				timestamp = Long.valueOf(receiptTime);
			}
		}

		return timestamp;
	}

	/**
	 * Adds the envelope's headers to the mutation for the row. Each header from
	 * the envelope is its own column in the header's column family.
	 * 
	 * <pre>
	 * Column Family: 		HEADERS_COLUMN_FAMILY
	 * Column Qualifier:	header key
	 * Value: 				header value
	 * </pre>
	 */
	private void addHeaders(Map<String, String> headers, long timestamp,
			Mutation mutation) {
		for (String headerKey : headers.keySet()) {
			String headerValue = headers.get(headerKey);
			if (StringUtils.isNotBlank(headerValue)) {
				Text colQual = new Text(headerKey);
				Value value = new Value(
						MutationsGeneratorUtils.toBytes(headerValue));
				mutation.put(HEADERS_COLUMN_FAMILY, colQual, timestamp, value);
			}
		}
	}

	/**
	 * Adds the envelope's payload to the mutation for the row. The payload is
	 * its own column in its own column family.
	 */
	private void addPayload(Envelope envelope, long timestamp, Mutation mutation) {
		byte[] payload = envelope.getPayload();
		if (payload != null && payload.length > 0) {
			mutation.put(PAYLOAD_COLUMN_FAMILY, RAW_PAYLOAD_COLUMN_QUALIFIER,
					timestamp, new Value(payload));
		}
	}

	private static String generateUniqueId() {
		return UUID.randomUUID().toString();
	}
}