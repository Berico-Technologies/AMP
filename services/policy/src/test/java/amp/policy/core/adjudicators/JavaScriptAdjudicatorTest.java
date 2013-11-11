package amp.policy.core.adjudicators;

import amp.bus.EnvelopeHelper;
import amp.policy.core.adjudicators.javascript.ScriptConfiguration;
import amp.utility.serialization.GsonSerializer;
import cmf.bus.Envelope;
import com.google.common.base.Function;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class JavaScriptAdjudicatorTest {

    private static final GsonSerializer serializer = new GsonSerializer();

    private Envelope createJSONEnvelope(Object object){

        return new EnvelopeHelper()
                .setMessageId(UUID.randomUUID())
                .setSenderIdentity("rclayton")
                .setMessageTopic(object.getClass().toString())
                .setCreationTime(DateTime.now().minus(1000 * 2))
                .setReceiptTime(DateTime.now())
                .setMessageType(object.getClass().toString())
                .setHeader("Content-Type", "application/json")
                .setPayload(serializer.byteSerialize(object))
                .getEnvelope();
    }

    @Test
    public void correctly_detects_json_payload_in_envelope() throws Exception {

        Envelope shouldBeJson = createJSONEnvelope(new Person("Richard", "Clayon", "123", 32));

        assertTrue(JavaScriptAdjudicator.isJsonPayload(shouldBeJson));

        Envelope isNotDefinedButShouldDefaultToJson = new Envelope();

        assertTrue(JavaScriptAdjudicator.isJsonPayload(isNotDefinedButShouldDefaultToJson));

        Envelope shouldNotBeJson = new EnvelopeHelper().setHeader("Content-Type", "application/xml").getEnvelope();

        assertFalse(JavaScriptAdjudicator.isJsonPayload(shouldNotBeJson));
    }

    @Test
    public void correctly_calls_javascript_functions_included_in_scope(){

        wrapInScriptContext(new Function<ScriptRuntime, Void>() {

            @Nullable @Override
            public Void apply(@Nullable ScriptRuntime scriptRuntime) {

                try {

                    StringBuilder sb = new StringBuilder();

                    sb.append(" function identity(e){ ");
                    sb.append("     return e;    ");
                    sb.append(" }                 ");

                    JavaScriptAdjudicator.loadScript(
                            scriptRuntime.context, scriptRuntime.scope, sb.toString(), "adhoc");

                    Object result = JavaScriptAdjudicator.callFunction(
                            scriptRuntime.context, scriptRuntime.scope, "identity", 42);

                    assertEquals(42, result);

                } catch (Exception e){

                    fail();
                }

                return null;
            }
        }, true);
    }

    @Test
    public void correctly_calls_javascript_methods_included_in_scope(){

        wrapInScriptContext(new Function<ScriptRuntime, Void>() {

            @Nullable @Override
            public Void apply(@Nullable ScriptRuntime scriptRuntime) {

                try {

                    StringBuilder sb = new StringBuilder();

                    sb.append(" var i = {};               ");
                    sb.append(" i.identity = function(e){ ");
                    sb.append("     return e;             ");
                    sb.append(" }                         ");

                    JavaScriptAdjudicator.loadScript(
                            scriptRuntime.context, scriptRuntime.scope, sb.toString(), "adhoc");

                    Object result = JavaScriptAdjudicator.callMethod(
                            scriptRuntime.context, scriptRuntime.scope, "i", "identity", 42);

                    assertEquals(42, result);

                } catch (Exception e){

                    fail(e.getMessage());
                }

                return null;
            }
        }, true);
    }

    @Test
    public void call_function_should_throw_an_illegal_state_exception_if_it_cannot_find_the_target_function(){

        wrapInScriptContext(new Function<ScriptRuntime, Void>() {

            @Nullable @Override
            public Void apply(@Nullable ScriptRuntime scriptRuntime) {

                try {

                    JavaScriptAdjudicator.callFunction(
                            scriptRuntime.context, scriptRuntime.scope, "unknownFunction", 42);

                    fail("An IllegalStateException should have been thrown.");

                }
                catch (IllegalStateException ise){}  // Success!
                catch (Exception e){ fail(e.getMessage()); } // Huh?  Something else went wrong.

                return null;
            }
        }, true);
    }

    @Test
    public void call_method_should_throw_an_illegal_state_exception_if_it_cannot_find_the_target_object(){

        wrapInScriptContext(new Function<ScriptRuntime, Void>() {

            @Nullable @Override
            public Void apply(@Nullable ScriptRuntime scriptRuntime) {

                try {

                    JavaScriptAdjudicator.callMethod(
                            scriptRuntime.context, scriptRuntime.scope, "unknownObject", "unknownFunction", 42);

                    fail("An IllegalStateException should have been thrown.");

                }
                catch (IllegalStateException ise){}  // Success!
                catch (Exception e){ fail(e.getMessage()); } // Huh?  Something else went wrong.

                return null;
            }
        }, true);
    }

    @Test(expected = NullPointerException.class)
    public void throws_exception_if_libraries_are_null_when_initializing(){

        JavaScriptAdjudicator adj = new JavaScriptAdjudicator();

        adj.setLibraries(null);

        adj.initialize();
    }

    @Test(expected = NullPointerException.class)
    public void throws_exception_if_session_objects_are_null_when_initializing(){

        JavaScriptAdjudicator adj = new JavaScriptAdjudicator();

        adj.setSessionObjects(null);

        adj.initialize();
    }

    @Test(expected = NullPointerException.class)
    public void throws_exception_if_script_configuration_is_null_when_initializing(){

        JavaScriptAdjudicator adj = new JavaScriptAdjudicator();

        adj.setScriptConfiguration(null);

        adj.initialize();
    }

    @Test
    public void helpers_js__script_correctly_detects_json_payload(){

        wrapInScriptContext(new Function<ScriptRuntime, Void>() {

            @Nullable @Override
            public Void apply(@Nullable ScriptRuntime scriptRuntime) {

                try {

                    // This ensures the Helpers library is loaded.
                    JavaScriptAdjudicator.loadCoreLibraries(
                            scriptRuntime.context, scriptRuntime.scope, JavaScriptAdjudicator.coreLibraries);

                    Envelope shouldBeJson = createJSONEnvelope(new Person("Richard", "Clayton", "rclayton", 32));

                    Object actualShouldBeJson = JavaScriptAdjudicator.callFunction(
                            scriptRuntime.context, scriptRuntime.scope, "isJsonPayload", shouldBeJson);

                    assertEquals(true, actualShouldBeJson);

                    Envelope shouldBeJsonByDefault = new Envelope();

                    Object actualShouldBeJsonByDefault = JavaScriptAdjudicator.callFunction(
                            scriptRuntime.context, scriptRuntime.scope, "isJsonPayload", shouldBeJsonByDefault);

                    assertEquals(true, actualShouldBeJsonByDefault);

                    Envelope shouldNotBeJson = new EnvelopeHelper()
                            .setHeader("Content-Type", "application/xml")
                            .getEnvelope();

                    Object actualShouldNotBeJson = JavaScriptAdjudicator.callFunction(
                            scriptRuntime.context, scriptRuntime.scope, "isJsonPayload", shouldNotBeJson);

                    assertEquals(false, actualShouldNotBeJson);

                } catch (Exception e){

                    e.printStackTrace();

                    fail(e.getMessage());
                }

                return null;
            }
        }, true);
    }

    @Test
    public void helpers_js__script_correctly_parses_json_payload(){

        wrapInScriptContext(new Function<ScriptRuntime, Void>() {

            @Nullable @Override
            public Void apply(@Nullable ScriptRuntime scriptRuntime) {

                try {

                    // This ensures the Helpers library is loaded.
                    JavaScriptAdjudicator.loadCoreLibraries(
                            scriptRuntime.context, scriptRuntime.scope, JavaScriptAdjudicator.coreLibraries);

                    Envelope envelope = createJSONEnvelope(new Person("Richard", "Clayton", "rclayton", 32));

                    StringBuilder sb  = new StringBuilder();

                    sb.append(" function makeAssertions(envelope){                        ");
                    sb.append("   var person = convertPayload(envelope);                  ");
                    sb.append("   org.junit.Assert.assertEquals('Richard', person.fname); ");
                    sb.append("   org.junit.Assert.assertEquals('Clayton', person.lname); ");
                    sb.append("   org.junit.Assert.assertEquals('rclayton', person.id);   ");
                    sb.append("   org.junit.Assert.assertEquals(32, person.age, 0.01);    ");
                    sb.append(" }                                                         ");

                    JavaScriptAdjudicator.loadScript(scriptRuntime.context, scriptRuntime.scope, sb.toString(), "makeAssertions");

                    JavaScriptAdjudicator.callFunction(
                            scriptRuntime.context, scriptRuntime.scope, "makeAssertions", envelope);

                } catch (Exception e){

                    e.printStackTrace();

                    fail(e.getMessage());
                }

                return null;
            }
        }, true);
    }



    private void wrapInScriptContext(Function<ScriptRuntime, Void> function, boolean failOnException){

        Context c = Context.enter();

        try {

            Scriptable scope = c.initStandardObjects();

            function.apply(new ScriptRuntime(c, scope));
        }
        catch (Exception e){

            if (failOnException) fail();
        }
        finally {

            c.exit();
        }
    }

    public static class ScriptRuntime {

        public final Context context;
        public final Scriptable scope;

        public ScriptRuntime(Context context, Scriptable scope) {
            this.context = context;
            this.scope = scope;
        }
    }


    public static class Person {

        private String fname;

        private String lname;

        private String id;

        private int age;

        public Person(){}

        public Person(String fname, String lname, String id, int age) {
            this.fname = fname;
            this.lname = lname;
            this.id = id;
            this.age = age;
        }

        public String getFname() {
            return fname;
        }

        public String getLname() {
            return lname;
        }

        public String getId() {
            return id;
        }

        public int getAge() {
            return age;
        }
    }
}
