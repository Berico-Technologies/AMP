package amp.topology.global.persistence.mapdb;

import amp.topology.global.impl.TopologyState;
import com.google.common.base.Optional;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class MapDbPersister<STATE extends TopologyState> {

    private Map<String, STATE> persistentMap;

    private DB configuredDatabase;

    public abstract String getCollectionName();

    public MapDbPersister(DB configuredDatabase){

        this.configuredDatabase = configuredDatabase;

        this.persistentMap = configuredDatabase.getHashMap(getCollectionName());
    }

    public Map<String, STATE> map(){

        return this.persistentMap;
    }

    public void compact(){

        configuredDatabase.compact();
    }

    public void close(){

        configuredDatabase.close();
    }
}
