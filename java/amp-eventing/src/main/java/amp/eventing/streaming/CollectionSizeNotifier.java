package amp.eventing.streaming;

public class CollectionSizeNotifier {
    private int size;
    private String collectionType;
    private String sequenceId;

    public int getSize() {
        return this.size;
    }
    public void setSize(int size) {
        this.size = size;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public CollectionSizeNotifier() {
        this.setSize(0);
        this.setCollectionType(null);
        this.setSequenceId(null);
    }

    public CollectionSizeNotifier(int size, String collectionType, String sequenceId) {
        setSize(size);
        setCollectionType(collectionType);
        setSequenceId(sequenceId);
    }



}
