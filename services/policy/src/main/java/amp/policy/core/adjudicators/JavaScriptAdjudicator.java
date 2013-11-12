package amp.policy.core.adjudicators;

import amp.policy.core.Enforcer;
import amp.policy.core.EnvelopeAdjudicator;
import amp.policy.core.adjudicators.javascript.JavaScriptLibrary;
import amp.policy.core.adjudicators.javascript.ScriptConfiguration;
import cmf.bus.Envelope;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.mozilla.javascript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Delegates adjudication to JavaScript written by developers.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class JavaScriptAdjudicator implements EnvelopeAdjudicator {

    private static final Logger LOG = LoggerFactory.getLogger(JavaScriptAdjudicator.class);

    /**
     * Core libraries that will be available by default within the script runtime.
     */
    static final String[] coreLibraries = new String[]{ "/scripts/lodash.js", "/scripts/helpers.js" };

    /**
     * This is the JavaScript scope.  The scope is specific to each instantiation; therefore, states
     * is only maintained per class instance.
     */
    protected ScriptableObject scriptScope;

    /**
     * Script configuration (name, script source, entry point).
     */
    protected ScriptConfiguration scriptConfiguration;

    /**
     * Libraries that should be loaded into the script runtime (provided by instantiator).
     */
    protected List<JavaScriptLibrary> libraries = Lists.newArrayList();

    /**
     * External services injected into the runtime.
     */
    protected Map<String, Object> sessionObjects = Maps.newHashMap();

    /**
     * Has the adjudicator been instantiated yet?
     */
    protected boolean wasInitialized = false;

    /**
     * Instantiate the adjudicator.  Use the setter methods to configure options for the runtime.
     * Call "initialize" if you don't want to lazy instantiate the instance.
     *
     * Note: if you do not set the ScriptConfiguration, an exception will be raised!
     */
    public JavaScriptAdjudicator(){}

    /**
     * Instantiate the essential Script Configuration.  Call "initialize" if you don't want to lazy
     * instantiate the instance.
     * @param scriptConfiguration
     */
    public JavaScriptAdjudicator(ScriptConfiguration scriptConfiguration) {

        this.scriptConfiguration = scriptConfiguration;
    }

    /**
     * Set the script configuration.
     * @param scriptConfiguration Represents the handler that will be called by
     *                            adjudicator.
     */
    public void setScriptConfiguration(ScriptConfiguration scriptConfiguration) {
        this.scriptConfiguration = scriptConfiguration;
    }

    /**
     * Set the libraries that should be used with the script runtime.
     * @param libraries Libraries to load
     */
    public void setLibraries(List<JavaScriptLibrary> libraries) {
        this.libraries = libraries;
    }

    /**
     * Set the Java objects/services that should be available within the API.
     * @param sessionObjects Objects to inject into the runtime.
     */
    public void setSessionObjects(Map<String, Object> sessionObjects) {
        this.sessionObjects = sessionObjects;
    }

    /**
     * Initialized the adjudicator instance by loading applicable contexts, the supplied script,
     * libraries, and services.
     *
     * @throws NullPointerException if script configuration, libraries, or session objects are null.  By default,
     * libraries and session objects are empty collections (already not null).
     */
    public void initialize(){

        checkNotNull(scriptConfiguration);
        checkNotNull(libraries);
        checkNotNull(sessionObjects);

        Context loadingContext = Context.enter();

        scriptScope = loadingContext.initStandardObjects();

        loadConstants(loadingContext, scriptScope);

        loadCoreLibraries(loadingContext, scriptScope, coreLibraries);

        loadSessionObjects(scriptScope, sessionObjects);

        loadUserLibraries(loadingContext, scriptScope, libraries);

        loadScript(
                loadingContext, scriptScope, scriptConfiguration.getScriptBody(), scriptConfiguration.getScriptName());

        Context.exit();

        wasInitialized = true;
    }

    /**
     * Load essential constants into the script runtime.
     * @param context Rhino Context
     * @param scope Execution Scope
     */
    static void loadConstants(Context context, ScriptableObject scope) {

        scope.putConst("INFO", scope, Enforcer.LogTypes.INFO);
        scope.putConst("ERROR", scope, Enforcer.LogTypes.ERROR);
        scope.putConst("DEBUG", scope, Enforcer.LogTypes.DEBUG);
        scope.putConst("WARN", scope, Enforcer.LogTypes.WARN);
    }

    /**
     * Load the core libraries into the script runtime.
     * @param context Rhino Context
     * @param scope Execution Scope
     * @param libraryFiles Libraries (as embedded resources in the resources folder) to load.
     */
    static void loadCoreLibraries(Context context, Scriptable scope, String[] libraryFiles){

        for (String library : libraryFiles){

            try {

                System.out.println("Loading " + library + "...");

                loadResourceFile(context, scope, library);

            } catch (IOException ex){

                throw new RuntimeException("Essential script library " + library + " was not found.");
            }
        }
    }

    /**
     * Load user libraries into the script runtime.
     * @param context Rhino Context
     * @param scope Execution Scope
     * @param libraries User Libraries to Load
     */
    static void loadUserLibraries(Context context, Scriptable scope, List<JavaScriptLibrary> libraries){

        for (JavaScriptLibrary library : libraries){

            loadScript(context, scope, library.getLibrarySource(), library.getLibraryName());
        }
    }

    /**
     * Load the session objects into the script runtime.
     * @param scope Execution Scope
     * @param sessionObjects Objects to insert
     */
    static void loadSessionObjects(Scriptable scope, Map<String, Object> sessionObjects){

        for (Map.Entry<String, Object> sessionObject : sessionObjects.entrySet()){

            Object jsObj = Context.javaToJS(sessionObject.getValue(), scope);

            scope.put(sessionObject.getKey(), scope, jsObj);
        }
    }

    /**
     * Use the script as the context for evaluating and enforcing policy.
     *
     * @param envelope Envelope to evaluate.
     * @param enforcer Mechanism to enforce policy.
     */
    @Override
    public void adjudicate(Envelope envelope, Enforcer enforcer) {

        if (!wasInitialized) initialize();

        Context runContext = Context.enter();

        Object jsEnvelope = Context.javaToJS(envelope, scriptScope);

        Object jsEnforcer = Context.javaToJS(enforcer, scriptScope);

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

                callMethod(
                        runContext,
                        scriptScope,
                        scriptConfiguration.getObjectEntry(),
                        scriptConfiguration.getFunctionEntry(),
                        jsEnvelope,
                        jsEnforcer);
            }
            else {

                callFunction(
                        runContext,
                        scriptScope,
                        scriptConfiguration.getFunctionEntry(),
                        jsEnvelope,
                        jsEnforcer);
            }
        }
        catch (IllegalStateException ise){

            throw new RuntimeException("Execution must stop, there's something wrong with the environment.", ise);
        }
        catch (Exception e){

            LOG.error("Error executing the script function: {}.", e);

            enforcer.log(envelope, Enforcer.LogTypes.ERROR, "Error executing the script function.");

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
