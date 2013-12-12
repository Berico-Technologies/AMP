package amp.messaging;

/**
 * Defines a set of constant values to be used as {@link Envelope} header keys for
 * common types of header information.
 */
public class EnvelopeHeaderConstants {

    /**
     * The time at which the envelope was created.  This is the original creation time of the 
     * envelope prior to being sent; not the time at which a particular instance was derserialized.
     */
	public static final String ENVELOPE_CREATION_TIME = "cmf.bus.envelope.creation";
	
	/**
	 * The time at which the envelope was received at the current end-point.
	 */
    public static final String ENVELOPE_RECEIPT_TIME = "cmf.bus.envelope.receipt";
    
    /**
     * If the current envelope is a reply message to a previously received message, 
     * the correlation Id is the MESSAGE_ID of the message envelope being replied to.
     */
    public static final String MESSAGE_CORRELATION_ID = "cmf.bus.message.correlation_id";
    
    /**
     * The unique id of the envelope.  As with ENVELOPE_CREATION_TIME, this id persists 
     * in transit.  It is not unique to the specific envelope object instance.
     */
    public static final String MESSAGE_ID = "cmf.bus.message.id";
    
    /**
     * Indicates the particular messaging pattern being used to deliver the message.  Valid values
     * are {@link Envelope#MESSAGE_PATTERN_PUBSUB} and {@link Envelope#MESSAGE_PATTERN_RPC}
     */
    public static final String MESSAGE_PATTERN = "cmf.bus.message.pattern";
    
    /**
     * The value to which the {@link Envelope#MESSAGE_PATTERN} header is set to indicate 
     * that the message is participating in a pub-sub messaging pattern.
     */
    public static final String MESSAGE_PATTERN_PUBSUB = "cmf.bus.message.pattern#pub_sub";
 
    /**
     * The value to which the {@link Envelope#MESSAGE_PATTERN} header is set to indicate 
     * that the message is participating in a RPC messaging pattern.
     */
    public static final String MESSAGE_PATTERN_RPC = "cmf.bus.message.pattern#rpc";
    
    /**
     * For messages participating in an RPC message pattern, this header indicates the length
     * of time for which the original request is valid and within which replies must be received.
     * 
     * This header must be set to a value parsasble as a long integer representing the timeout 
     * length in milliseconds.
     */
    public static final String MESSAGE_PATTERN_RPC_TIMEOUT = "cmf.bus.message.pattern#rpc.timeout";
    
    /**
     * The identity of the person or service sending the message.  May be used by recipients to 
     * authorize activities requested by command or query messages.
     */
    public static final String MESSAGE_SENDER_IDENTITY = "cmf.bus.message.sender_identity";
    
    /**
     * If the message is digitally signed, this key will contain the base64 encoded signature
     * hash for the envelope payload, as signed by the message sender.
     */
    public static final String MESSAGE_SENDER_SIGNATURE = "cmf.bus.message.sender_signature";
    
    /**
     * The message topic to be used in routing this message to its recipients.
     */
    public static final String MESSAGE_TOPIC = "cmf.bus.message.topic";
    
    /**
     * The type of the message.  Typically this is the fully qualified name of the 
     * data structure of which the payload is a serialized instance.
     */
    public static final String MESSAGE_TYPE = "cmf.bus.message.type";
}
