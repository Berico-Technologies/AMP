package amp.topology.snapshot.impl.serializers;

import amp.topology.snapshot.Snapshot;
import amp.topology.snapshot.impl.SnapshotSerializer;
import com.thoughtworks.xstream.XStream;

/**
 * Serializes and Deserializes Snapshots to/from XML.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class XmlSnapshotSerializer implements SnapshotSerializer {

    XStream xstream;

    public XmlSnapshotSerializer(){

        this.xstream = new XStream();
    }

    @Override
    public String serializedFileExtension() {

        return "xml";
    }

    @Override
    public String serialize(Snapshot snapshot) {

        return this.xstream.toXML(snapshot);
    }

    @Override
    public Snapshot deserialize(String snapshot) {

        return (Snapshot) xstream.fromXML(snapshot);
    }
}
