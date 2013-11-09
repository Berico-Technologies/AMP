package amp.policy.core.adjudicators;

import amp.policy.core.EnvelopeAdjudicator;
import amp.policy.core.PolicyEnforcer;
import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import com.google.common.base.Strings;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class JavaScriptAdjudicator implements EnvelopeAdjudicator {

    private static final Logger LOG = LoggerFactory.getLogger(JavaScriptAdjudicator.class);

    private static final String JAVASCRIPT = "JavaScript";

    protected static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    protected ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(JAVASCRIPT);

    protected ScriptContext scriptContext;

    public JavaScriptAdjudicator(ScriptContext scriptContext) throws ScriptException {

        this(scriptContext, new HashMap<String, Object>());
    }

    public JavaScriptAdjudicator(
            ScriptContext scriptContext, Map<String, Object> sessionContext) throws ScriptException {

        this.scriptContext = scriptContext;

        scriptEngine.eval(scriptContext.getScriptBody());

        for (Map.Entry<String, Object> object : sessionContext.entrySet()){

            scriptEngine.put(object.getKey(), object.getValue());
        }
    }

    /**
     * Use the script as the context for evaluating and enforcing policy.
     *
     * @param envelope Envelope to evaluate.
     * @param enforcer Mechanism to enforce policy.
     */
    @Override
    public void adjudicate(Envelope envelope, PolicyEnforcer enforcer) {

        try {

            if(scriptContext.isObjectEntry()){

                callMethod(
                    scriptContext.getObjectEntry(), scriptContext.getFunctionEntry(), envelope, enforcer);
            }
            else {

                callFunction(scriptContext.getFunctionEntry(), envelope, enforcer);
            }
        }
        catch (Exception e){

            LOG.error("Error executing the script function.", e);

            enforcer.log(envelope, PolicyEnforcer.LogTypes.ERROR, "Error executing the script function.");
        }

    }

    /**
     * Is the body of the Envelope a JSON object?
     * @param e Envelope with the payload.
     * @return True if the payload's content type is JSON or if the content-type is null.
     */
    static boolean isJsonPayload(Envelope e){

        String ct = e.getHeader("Content-Type");

        return (Strings.isNullOrEmpty(ct) || ct.equals("application/json"));
    }

    /**
     * Call a function on the Script Engine.
     * @param functionName Name of the function to call
     * @param args An array of arguments to pass to the function
     *        representing the function argument signature.
     * @returns The result (if any) from the script function
     */
    Object callFunction(String functionName, Object... args) throws Exception {

        return ((Invocable)scriptEngine).invokeFunction(functionName, args);
    }

    /**
     * Call a method on a Script object within the Script Engine.
     * @param objectName The reference name of the object with
     *        the method that will be called (e.g.: in foo->bar(),
     *        we want 'foo').
     * @param methodName Name of the method to call (bar())
     * @param args An array of arguments to pass to the function
     *        representing the function argument signature.
     * @returns The result (if any) from the script method
     */
    Object callMethod(String objectName, String methodName, Object... args) throws Exception {

        return ((Invocable)scriptEngine).invokeMethod(objectName, methodName, args);
    }
}
