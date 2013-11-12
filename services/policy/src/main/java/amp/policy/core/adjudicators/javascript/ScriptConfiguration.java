package amp.policy.core.adjudicators.javascript;

import cmf.bus.EnvelopeHeaderConstants;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ScriptConfiguration {

    protected String name;

    protected String body;

    protected String functionEntry;

    protected String objectEntry = null;

    protected Map<String, String> registrationInfo = Maps.newHashMap();

    public ScriptConfiguration(String name, String body, String entry, Map<String, String> registrationInfo) {

        this.name = name;
        this.body = body;

        parseAndSetEntry(entry);

        normalizeKeys(registrationInfo);
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }

    public String getFunctionEntry() {
        return functionEntry;
    }

    public String getObjectEntry() {
        return objectEntry;
    }

    public boolean isObjectEntry(){
        return objectEntry != null;
    }

    public Map<String, String> getRegistrationInfo(){
        return registrationInfo;
    }

    private void parseAndSetEntry(String entry){

        String[] parts = entry.split("[.]");

        if (parts.length > 1){
            this.objectEntry = parts[0];
            this.functionEntry = parts[1];
        }
        else {

            this.functionEntry = entry;
        }
    }

    private void normalizeKeys(Map<String, String> notNormalizedInfo){

        checkNotNull(notNormalizedInfo);

        for(Map.Entry<String, String> e : notNormalizedInfo.entrySet()){

            String property = e.getKey().toLowerCase();

            if (property.equals("topic"))
                registrationInfo.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, e.getValue());
            else if (property.equals("sender"))
                registrationInfo.put(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY, e.getValue());
            else
                registrationInfo.put(e.getKey(), e.getValue());
        }
    }
}