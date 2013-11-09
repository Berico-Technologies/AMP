package amp.policy.core.adjudicators;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ScriptContext {

    protected String scriptName;

    protected String scriptBody;

    protected String functionEntry;

    protected String objectEntry = null;

    public ScriptContext(String scriptName, String scriptBody, String functionEntry) {
        this.scriptName = scriptName;
        this.scriptBody = scriptBody;
        this.functionEntry = functionEntry;
    }

    public ScriptContext(String scriptName, String scriptBody, String functionEntry, String objectEntry) {
        this.scriptName = scriptName;
        this.scriptBody = scriptBody;
        this.functionEntry = functionEntry;
        this.objectEntry = objectEntry;
    }

    public String getScriptName() {
        return scriptName;
    }

    public String getScriptBody() {
        return scriptBody;
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
}
