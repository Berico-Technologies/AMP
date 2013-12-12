package amp.gel.dao.impl.derby;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import cmf.bus.EnvelopeHeaderConstants;

/**
 * This class adapts the cmf.bus.Envelope class as a POJO with JPA annotations.
 * Instead of a map of headers, the individual fields are specifically
 * identified. This means that if keys to the envelope headers were to change,
 * then this class should be modified accordingly.
 * 
 */
@Entity
public class Envelope implements Serializable {

	private static final long serialVersionUID = 1198815409520506136L;

	@Id
	@GeneratedValue
	@Column(name = "envelope_id")
	private Long id;

	@Version
	private Integer version;

	@Column(unique = false, nullable = true)
	private Date creationTime;

	@Column(unique = false, nullable = true)
	private Date receiptTime;

	@Column(unique = false, nullable = true)
	private String correlationId;

	@Column(unique = false, nullable = true)
	private String messageId;

	@Column(unique = false, nullable = true)
	private String pattern;

	@Column(unique = false, nullable = true)
	private String patternPubSub;

	@Column(unique = false, nullable = true)
	private String patternRpc;

	@Column(unique = false, nullable = true)
	private String patternRpcTimeout;

	@Column(unique = false, nullable = true)
	private String senderIdentity;

	@Column(unique = false, nullable = true)
	private String senderSignature;

	@Column(unique = false, nullable = true, columnDefinition = "VARCHAR(32672)")
	private String topic;

	@Column(unique = false, nullable = true, columnDefinition = "VARCHAR(32672)")
	private String type;

	@Column(unique = false, nullable = true, columnDefinition = "BLOB")
	private byte[] payload;

	public Envelope() {
		super();
	}

	public Envelope(cmf.bus.Envelope envelope) {
		super();

		this.payload = envelope.getPayload();

		this.creationTime = timeMillisToDate(envelope
				.getHeader(EnvelopeHeaderConstants.ENVELOPE_CREATION_TIME));
		this.receiptTime = timeMillisToDate(envelope
				.getHeader(EnvelopeHeaderConstants.ENVELOPE_RECEIPT_TIME));
		this.correlationId = envelope
				.getHeader(EnvelopeHeaderConstants.MESSAGE_CORRELATION_ID);
		this.messageId = envelope.getHeader(EnvelopeHeaderConstants.MESSAGE_ID);
		this.pattern = envelope
				.getHeader(EnvelopeHeaderConstants.MESSAGE_PATTERN);
		this.patternPubSub = envelope
				.getHeader(EnvelopeHeaderConstants.MESSAGE_PATTERN_PUBSUB);
		this.patternRpc = envelope
				.getHeader(EnvelopeHeaderConstants.MESSAGE_PATTERN_RPC);
		this.patternRpcTimeout = envelope
				.getHeader(EnvelopeHeaderConstants.MESSAGE_PATTERN_RPC_TIMEOUT);
		this.senderIdentity = envelope
				.getHeader(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY);
		this.senderSignature = envelope
				.getHeader(EnvelopeHeaderConstants.MESSAGE_SENDER_SIGNATURE);
		this.topic = envelope.getHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC);
		this.type = envelope.getHeader(EnvelopeHeaderConstants.MESSAGE_TYPE);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getReceiptTime() {
		return receiptTime;
	}

	public void setReceiptTime(Date receiptTime) {
		this.receiptTime = receiptTime;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getPatternPubSub() {
		return patternPubSub;
	}

	public void setPatternPubSub(String patternPubSub) {
		this.patternPubSub = patternPubSub;
	}

	public String getPatternRpc() {
		return patternRpc;
	}

	public void setPatternRpc(String patternRpc) {
		this.patternRpc = patternRpc;
	}

	public String getPatternRpcTimeout() {
		return patternRpcTimeout;
	}

	public void setPatternRpcTimeout(String patternRpcTimeout) {
		this.patternRpcTimeout = patternRpcTimeout;
	}

	public String getSenderIdentity() {
		return senderIdentity;
	}

	public void setSenderIdentity(String senderIdentity) {
		this.senderIdentity = senderIdentity;
	}

	public String getSenderSignature() {
		return senderSignature;
	}

	public void setSenderSignature(String senderSignature) {
		this.senderSignature = senderSignature;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	private Date timeMillisToDate(String timeMillis) {
		if (StringUtils.isBlank(timeMillis)
				|| !StringUtils.isNumeric(timeMillis))
			return null;

		long instant = Long.valueOf(timeMillis);
		DateTime dateTime = new DateTime(instant, DateTimeZone.UTC);
		return dateTime.toDate();
	}
}
