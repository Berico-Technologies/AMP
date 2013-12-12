package amp.topology.core.repo.snapshot;

import amp.topology.core.ExtendedExchange;
import amp.topology.core.ExtendedRouteInfo;

import com.thoughtworks.xstream.XStream;

/**
 * XML Serializer for Topology Snapshots using XStream.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class XmlTopologySnapshotSerializer implements ITopologySnapshotSerializer {

	XStream xstream = null;
	
	public XmlTopologySnapshotSerializer(){
		
		this.xstream = new XStream();
		
		// Alias Exchange and RouteInfo classes so they are
		// not tedious to write in XML.
		this.xstream.alias("topology", TopologySnapshot.class);
		this.xstream.alias("exchange", ExtendedExchange.class);
		this.xstream.alias("route", ExtendedRouteInfo.class);
	}
	
	public XmlTopologySnapshotSerializer(XStream xstream){
		
		this.xstream = xstream;
	}
	
	@Override
	public String serialize(TopologySnapshot snapshot) {
		
		return xstream.toXML(snapshot);
	}

	@Override
	public TopologySnapshot deserialize(String serializedSnapshot) {
		
		return (TopologySnapshot) xstream.fromXML(serializedSnapshot);
	}

	@Override
	public String getExtension() {
		
		return "xml";
	}
}
