package amp.esp.monitors;

import amp.esp.InferredEvent;

import javax.script.ScriptException;

public class JsEnvelopeCounter extends JavascriptDetector {

    private static final String IE_CLASSNAME = InferredEvent.class.getName();

    private static final String BODY =
            "" +

                    "var clock = {" +
                    "  getTime : function() { return new Date().getTime(); }," +
                    "  getElapsed : function(prev) { return this.getTime() - prev; }" +
                    "};" +

                    "var searchCounter = {" +
                    "  count : 0," +
                    "  start : clock.getTime()," +
                    "  curRate : 0," +
                    "  receive : function(env) {" +
                    // The following statement can be put in the Javascript to target certain
                    // envelope types or conditions
                    // "    if (env.getEventType() != 'Search') return null;" +
                    "    this.count++;" +
                    "    var elapsed = clock.getElapsed(this.start);" +
                    "    this.curRate = (1000 * this.count) / elapsed;" +
                    "    var ie = new Packages." + IE_CLASSNAME +
                    "('BusEvents','JsEnvelopeCounter');" +
                    "    ie.putData('Count', '' + this.count);" +
                    "    ie.putData('Elapsed', '' + elapsed);" +
                    "    ie.putData('Rate', '' + this.curRate);" +
                    "    return ie;" +
                    "  }" +
                    "};" +

                    "// test calls\n" +
                    "searchCounter;" ;


        public JsEnvelopeCounter() throws ScriptException {
                super(BODY);
        }
}
