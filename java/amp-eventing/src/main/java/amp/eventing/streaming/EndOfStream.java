package amp.eventing.streaming;

public class EndOfStream {
    private String streamType;
    private String sequenceId;

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public EndOfStream() {
        setStreamType(null);
    }

    public EndOfStream(String streamType, String sequenceId) {
        setStreamType(streamType);
        setSequenceId(sequenceId);
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }
}
