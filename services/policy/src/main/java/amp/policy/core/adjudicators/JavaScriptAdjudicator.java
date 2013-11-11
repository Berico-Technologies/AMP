package amp.policy.core.adjudicators;

import amp.policy.core.EnvelopeAdjudicator;
import amp.policy.core.PolicyEnforcer;
import amp.policy.core.adjudicators.javascript.JavaScriptLibrary;
import amp.policy.core.adjudicators.javascript.ScriptConfiguration;
import cmf.bus.Envelope;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.mozilla.javascript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.*;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class JavaScriptAdjudicator implements EnvelopeAdjudicator {

    private static final Logger LOG = LoggerFactory.getLogger(JavaScriptAdjudicator.class);

    static final String[] coreLibraries = new String[]{ "/scripts/lodash.js", "/scripts/helpers.js" };

    protected ScriptableObject scriptScope;

    protected ScriptConfiguration scriptConfiguration;

    protected List<JavaScriptLibrary> libraries = Lists.newArrayList();

    protected Map<String, Object> sessionObjects = Maps.newHashMap();

    protected boolean wasInitialized = false;

    public JavaScriptAdjudicator(){}

    public JavaScriptAdjudicator(ScriptConfiguration scriptConfiguration) {

        this.scriptConfiguration = scriptConfiguration;

        initialize();
    }

    public void setScriptConfiguration(ScriptConfiguration scriptConfiguration) {
        this.scriptConfiguration = scriptConfiguration;
    }

    public void setLibraries(List<JavaScriptLibrary> libraries) {
        this.libraries = libraries;
    }

    public void setSessionObjects(Map<String, Object> sessionObjects) {
        this.sessionObjects = sessionObjects;
    }

    public void initialize(){

        checkNotNull(scriptConfiguration);
        checkNotNull(libraries);
        checkNotNull(sessionObjects);

        Context loadingContext = Context.enter();

        scriptScope = loadingContext.initStandardObjects();

        loadCoreLibraries(loadingContext, scriptScope, coreLibraries);

        for (Map.Entry<String, Object> sessionObject : sessionObjects.entrySet()){

            Object jsObj = Context.javaToJS(sessionObject.getValue(), scriptScope);

            scriptScope.put(sessionObject.getKey(), scriptScope, jsObj);
        }

        for (JavaScriptLibrary library : libraries){

            loadScript(loadingContext, scriptScope, library.getLibrarySource(), library.getLibraryName());
        }

        loadScript(
                loadingContext, scriptScope, scriptConfiguration.getScriptBody(), scriptConfiguration.getScriptName());

        Context.exit();

        wasInitialized = true;
    }


    static void loadCoreLibraries(Context c, Scriptable s, String[] libraryFiles){

        for (String library : libraryFiles){

            try {

                System.out.println("Loading " + library + "...");

                loadResourceFile(c, s, library);

            } catch (IOException ex){

                throw new RuntimeException("Essential script library " + library + " was not found.");
            }
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

        if (!wasInitialized) initialize();

        Context runContext = Context.enter();

        Scriptable tempScope = runContext.newObject(this.scriptScope);

        tempScope.setPrototype(this.scriptScope);

        tempScope.setParentScope(null);

        Object jsEnvelope = Context.javaToJS(envelope, tempScope);

        Object jsEnforcer = Context.javaToJS(enforcer, tempScope);

        try {

//            // TODO: Figure out why NativeJSON.parse doesn't produce a valid JavaScript object.
//            // After calling NativeJSON.parse and sticking the object into the runtime time,
//            // calls to properties are resulting in null-reference errors.
//            Object event = null;
//
//            if(isJsonPayload(envelope)){
//
//                event = parseJSON(runContext, tempScope, new String(envelope.getPayload()));
//            }

            if(scriptConfiguration.isObjectEntry()){

                callMethod(runContext, tempScope, scriptConfiguration.getObjectEntry(), scriptConfiguration.getFunctionEntry(), jsEnvelope, jsEnforcer);
            }
            else {

                callFunction(runContext, tempScope, scriptConfiguration.getFunctionEntry(), jsEnvelope,  jsEnforcer);
            }
        }
        catch (IllegalStateException ise){

            throw new RuntimeException("Execution must stop, there's something wrong with the environment.", ise);
        }
        catch (Exception e){

            LOG.error("Error executing the script function.", e);

            enforcer.log(envelope, PolicyEnforcer.LogTypes.ERROR, "Error executing the script function.");

        }
        finally {

            runContext.exit();
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
     * Parse a JSON String into an Object.
     * @param c Rhino Context
     * @param scope Execution Scope
     * @param json JSON string
     * @return Representative Java Object of the JSON String
     */
    static NativeObject parseJSON(Context c, Scriptable scope, String json){

        // TODO: This isn't producing the results we would expect.

        return (NativeObject) NativeJSON.parse(c, scope, json, new Callable() {
            @Override
            public Object call(Context context, Scriptable scope, Scriptable holdable, Object[] objects) {

                return holdable;
            }
        });
    }


    /**
     * Call a JavaScript function from within the scope.
     * @param c JavaScript Execution Context
     * @param scope JavaScript Scope
     * @param functionName Name of the function to call
     * @param args (optional) any function arguments.
     * @return Whatever the JavaScript method returns
     * @throws IllegalStateException if the function does not exist in the scope
     */
    static Object callFunction(Context c, Scriptable scope, String functionName, Object... args)
            throws IllegalStateException {

        if (scope.has(functionName, scope)){

            Function function = (Function)scope.get(functionName, scope);

            return function.call(c, scope, scope, args);
        }

        throw new IllegalStateException(
                String.format("Function with name %s does not exist in scope.", functionName));
    }


    /**
     * Call a JavaScript method from within the scope.
     * @param context JavaScript Execution Context
     * @param scope JavaScript Scope
     * @param objectName Name of the object with the method to be called
     * @param methodName Name of the method to call
     * @param args (optional) any function arguments.
     * @return Whatever the JavaScript method returns
     * @throws IllegalStateException if the object does not exist in the scope
     */
    static Object callMethod(Context context, Scriptable scope, String objectName, String methodName, Object... args)
            throws IllegalStateException {

        if (scope.has(objectName, scope)){

            NativeObject object = (NativeObject)scope.get(objectName, scope);

            return callFunction(context, object, methodName, args);
        }

        throw new IllegalStateException(
                String.format("Object with name %s does not exist in scope.", objectName));
    }

    /**
     * Load and evaluate JavaScript code from a String.
     * @param context JavaScript Execution Context
     * @param scope JavaScript Scope
     * @param scriptBody Actual JavaScript code as a String
     * @param scriptName Name of the script
     */
    static void loadScript(
            Context context, Scriptable scope, String scriptBody, String scriptName) {

        context.evaluateString(scope, scriptBody, scriptName, 0, null);
    }

    /**
     * Load a file from within the JAR as a script file.
     * @param context JavaScript Execution Context
     * @param scope JavaScript Scope
     * @param file File to load
     * @throws IOException Occurs if file not found or is unable to read.
     */
    static void loadResourceFile(
            Context context, Scriptable scope, String file) throws IOException {

        InputStream is = JavaScriptAdjudicator.class.getResourceAsStream(file);

        InputStreamReader isr = new InputStreamReader(is);

        Reader resource = new BufferedReader(isr);

        context.evaluateReader(scope, resource, file, 0, null);
    }
}
