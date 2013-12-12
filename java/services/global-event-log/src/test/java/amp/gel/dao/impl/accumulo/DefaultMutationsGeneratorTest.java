package amp.gel.dao.impl.accumulo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Mutation;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import amp.gel.dao.impl.accumulo.DefaultMutationsGenerator;
import amp.gel.dao.impl.accumulo.MutationsGenerator;
import amp.gel.dao.impl.accumulo.MutationsGeneratorUtils;

import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;

public class DefaultMutationsGeneratorTest {

	private static final String USER = "mjordan";

	private static final String TOPIC = "com.example.event.ExampleEvent";

	private static final long TIME_MILLIS = 1366384297854L;

	private static final byte[] EMPTY_PAYLOAD = new byte[0];

	private static byte[] PAYLOAD = MutationsGeneratorUtils
			.toBytes("{\"attribute1\":\"Michael Jordan\",\"attribute2\":23}");

	private MutationsGenerator mutationsGenerator = new DefaultMutationsGenerator();

	private Envelope envelope = mock(Envelope.class);

	@Test
	public void testNullEnvelope() {
		List<Mutation> mutations = mutationsGenerator.generate(null);
		assertNumberOfMutations(mutations, 0);
	}

	@Test
	public void testNullHeaders() {
		when(envelope.getHeaders()).thenReturn(null);

		List<Mutation> mutations = mutationsGenerator.generate(envelope);
		assertNumberOfMutations(mutations, 0);
	}

	@Test
	public void testEmptyHeaders() {
		when(envelope.getHeaders()).thenReturn(new HashMap<String, String>());

		List<Mutation> mutations = mutationsGenerator.generate(envelope);
		assertNumberOfMutations(mutations, 0);
	}

	@Test
	public void testAllNullHeaderValues() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(EnvelopeHeaderConstants.ENVELOPE_RECEIPT_TIME, null);
		headers.put(EnvelopeHeaderConstants.MESSAGE_ID, null);
		headers.put(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY, null);
		headers.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, null);
		headers.put(EnvelopeHeaderConstants.MESSAGE_TYPE, null);

		when(envelope.getHeaders()).thenReturn(headers);

		List<Mutation> mutations = mutationsGenerator.generate(envelope);
		assertNumberOfMutations(mutations, 0);
	}

	@Test
	public void testIgnoreNullHeaderValues() {
		// one non-null header value
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY, null);
		headers.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, null);
		headers.put(EnvelopeHeaderConstants.ENVELOPE_RECEIPT_TIME,
				String.valueOf(TIME_MILLIS));

		when(envelope.getHeaders()).thenReturn(headers);

		List<Mutation> mutations = mutationsGenerator.generate(envelope);
		assertNumberOfMutations(mutations, 1);

		Mutation mutation = mutations.get(0);
		assertNotNull(mutation);

		// one column update for the one non-null header value
		List<ColumnUpdate> columnUpdates = mutation.getUpdates();
		assertNotNull(columnUpdates);
		assertEquals(1, columnUpdates.size());

		for (ColumnUpdate columnUpdate : columnUpdates) {
			assertEquals(
					DefaultMutationsGenerator.HEADERS_COLUMN_FAMILY.toString(),
					MutationsGeneratorUtils.toString(columnUpdate
							.getColumnFamily()));
			assertTrue(headers.containsKey(MutationsGeneratorUtils
					.toString(columnUpdate.getColumnQualifier())));
			assertTrue(headers.containsValue(MutationsGeneratorUtils
					.toString(columnUpdate.getValue())));
			assertEquals(TIME_MILLIS, columnUpdate.getTimestamp());
		}
	}

	@Test
	public void testRowIdAsUniqueId() {
		/*
		 * No receipt time header, so the row ID should be just a unique ID and
		 * the timestamp should be the current time.
		 */
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY, USER);
		headers.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, TOPIC);

		when(envelope.getHeaders()).thenReturn(headers);
		when(envelope.getPayload()).thenReturn(EMPTY_PAYLOAD);

		long currentTime = System.currentTimeMillis();

		List<Mutation> mutations = mutationsGenerator.generate(envelope);
		assertNumberOfMutations(mutations, 1);

		Mutation mutation = mutations.get(0);

		// check that row ID is a UUID
		String rowId = getRowId(mutation);
		UUID uuid = UUID.fromString(rowId);
		assertNotNull(uuid);

		// column updates should match the headers
		List<ColumnUpdate> columnUpdates = mutation.getUpdates();
		assertNotNull(columnUpdates);
		assertEquals(headers.size(), columnUpdates.size());

		for (ColumnUpdate columnUpdate : columnUpdates) {
			assertEquals(
					DefaultMutationsGenerator.HEADERS_COLUMN_FAMILY.toString(),
					MutationsGeneratorUtils.toString(columnUpdate
							.getColumnFamily()));
			assertTrue(headers.containsKey(MutationsGeneratorUtils
					.toString(columnUpdate.getColumnQualifier())));
			assertTrue(headers.containsValue(MutationsGeneratorUtils
					.toString(columnUpdate.getValue())));

			// check that the timestamp is close enough
			assertTimestampsAreAlmostEqual(currentTime,
					columnUpdate.getTimestamp());
		}
	}

	@Test
	public void testRowIdAsCompositeId() {
		Map<String, String> headers = generateNormalHeaders();

		when(envelope.getHeaders()).thenReturn(headers);
		when(envelope.getPayload()).thenReturn(EMPTY_PAYLOAD);

		List<Mutation> mutations = mutationsGenerator.generate(envelope);
		assertNumberOfMutations(mutations, 1);

		Mutation mutation = mutations.get(0);
		String rowId = getRowId(mutation);

		/*
		 * Header includes sender ID, topic, and receipt time, so the row ID
		 * should be a composite of these header values.
		 */
		String formattedTimestamp = MutationsGeneratorUtils
				.longToFormattedTimestamp(TIME_MILLIS);
		String compositeRowId = StringUtils.join(
				Arrays.asList(USER, TOPIC, formattedTimestamp),
				DefaultMutationsGenerator.ROW_ID_DELIMETER);
		assertTrue(rowId.startsWith(compositeRowId));

		// column updates should match the headers
		List<ColumnUpdate> columnUpdates = mutation.getUpdates();
		assertNotNull(columnUpdates);
		assertEquals(headers.size(), columnUpdates.size());

		for (ColumnUpdate columnUpdate : columnUpdates) {
			assertEquals(
					DefaultMutationsGenerator.HEADERS_COLUMN_FAMILY.toString(),
					MutationsGeneratorUtils.toString(columnUpdate
							.getColumnFamily()));
			assertTrue(headers.containsKey(MutationsGeneratorUtils
					.toString(columnUpdate.getColumnQualifier())));
			assertTrue(headers.containsValue(MutationsGeneratorUtils
					.toString(columnUpdate.getValue())));

			// check that the timestamp matches
			assertEquals(TIME_MILLIS, columnUpdate.getTimestamp());
		}
	}

	/**
	 * The row ID is a combination of sender ID, the topic, and receipt time.
	 * Since the receipt time is only accurate down to the millisecond and there
	 * can be > 1K messages / s, there can be multiple messages per millisecond.
	 * As such, a unique ID is appended to the row ID to guarantee uniqueness.
	 * This test checks for unique row IDs even when the envelopes are the same.
	 */
	@Test
	public void testRowIdsAreUnique() {
		Envelope envelope1 = mock(Envelope.class);
		Envelope envelope2 = mock(Envelope.class);

		Map<String, String> headers = generateNormalHeaders();

		when(envelope1.getHeaders()).thenReturn(headers);
		when(envelope1.getPayload()).thenReturn(EMPTY_PAYLOAD);

		when(envelope2.getHeaders()).thenReturn(headers);
		when(envelope2.getPayload()).thenReturn(EMPTY_PAYLOAD);

		List<Mutation> mutations = mutationsGenerator.generate(envelope1);
		String rowId1 = getRowId(mutations.get(0));

		mutations = mutationsGenerator.generate(envelope2);
		String rowId2 = getRowId(mutations.get(0));

		assertEquals(rowId1.length(), rowId2.length());
		int lastIndexOfDelimiter = rowId1
				.lastIndexOf(DefaultMutationsGenerator.ROW_ID_DELIMETER);
		/*
		 * Since the envelopes have the same headers, the row IDs should match
		 * up to the last field: the unique ID.
		 */
		assertEquals(rowId1.substring(0, lastIndexOfDelimiter),
				rowId2.substring(0, lastIndexOfDelimiter));
		assertNotEquals(rowId1.substring(lastIndexOfDelimiter),
				rowId2.substring(lastIndexOfDelimiter));
	}

	@Test
	public void testHeadersWithPayload() {
		Map<String, String> headers = generateNormalHeaders();

		when(envelope.getHeaders()).thenReturn(headers);
		when(envelope.getPayload()).thenReturn(PAYLOAD);

		List<Mutation> mutations = mutationsGenerator.generate(envelope);
		assertNumberOfMutations(mutations, 1);

		Mutation mutation = mutations.get(0);
		assertNotNull(mutation);

		// column updates should match the headers + 1 for the payload
		List<ColumnUpdate> columnUpdates = mutation.getUpdates();
		assertNotNull(columnUpdates);
		assertEquals(headers.size() + 1, columnUpdates.size());

		for (ColumnUpdate columnUpdate : columnUpdates) {
			String columnFamily = MutationsGeneratorUtils.toString(columnUpdate
					.getColumnFamily());

			if (columnFamily
					.equals(DefaultMutationsGenerator.HEADERS_COLUMN_FAMILY
							.toString())) {
				assertTrue(headers.containsKey(MutationsGeneratorUtils
						.toString(columnUpdate.getColumnQualifier())));
				assertTrue(headers.containsValue(MutationsGeneratorUtils
						.toString(columnUpdate.getValue())));
			} else {
				assertEquals(
						DefaultMutationsGenerator.PAYLOAD_COLUMN_FAMILY
								.toString(),
						columnFamily);
				assertEquals(
						DefaultMutationsGenerator.RAW_PAYLOAD_COLUMN_QUALIFIER
								.toString(),
						MutationsGeneratorUtils.toString(columnUpdate
								.getColumnQualifier()));
				assertArrayEquals(PAYLOAD, columnUpdate.getValue());
			}

			assertEquals(TIME_MILLIS, columnUpdate.getTimestamp());
		}
	}

	private String getRowId(Mutation mutation) {
		assertNotNull(mutation);
		byte[] row = mutation.getRow();
		assertNotNull(row);
		return MutationsGeneratorUtils.toString(row);
	}

	/**
	 * Create a header that includes sender ID, topic, and receipt time. With
	 * such a header, the row ID should be a composite of these header values
	 * and the timestamp should be the receipt time.
	 */
	private Map<String, String> generateNormalHeaders() {
		Map<String, String> headers;
		headers = new HashMap<String, String>();
		headers.put(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY, USER);
		headers.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, TOPIC);
		headers.put(EnvelopeHeaderConstants.ENVELOPE_RECEIPT_TIME,
				String.valueOf(TIME_MILLIS));
		return headers;
	}

	public void assertNumberOfMutations(List<Mutation> mutations,
			int expectedSize) {
		assertNotNull(mutations);
		assertEquals(expectedSize, mutations.size());
	}

	private void assertTimestampsAreAlmostEqual(long time1, long time2) {
		assertTrue(Math.abs(time1 - time2) < 1000);
	}
}
