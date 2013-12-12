package amp.bus;


import java.util.Map.Entry;
import java.util.UUID;

import cmf.bus.Envelope;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.Duration;


public class EnvelopeHelper {

    private Envelope env;

    public EnvelopeHelper() {
        env = new Envelope();
    }

    public EnvelopeHelper(Envelope envelope) {
        env = envelope;
    }

    public String flatten() {
        return this.flatten(",");
    }

    public String flatten(String separator) {
        StringBuilder sb = new StringBuilder();

        sb.append("[");

        for (Entry<String, String> kvp : env.getHeaders().entrySet()) {
            sb.append(String.format("%s{%s:%s}", separator, kvp.getKey(), kvp.getValue()));
        }

        if (sb.length() > 1) {
            sb.delete(1, 1 + separator.length());
        }

        sb.append("]");

        return sb.toString();
    }

    public Envelope getEnvelope() {
        return env;
    }

    public String getHeader(String key) {
        return env.getHeader(key);
    }

    public byte[] getPayload() {
        return env.getPayload();
    }

    public EnvelopeHelper setHeader(String key, String value) {
        env.setHeader(key, value);
        return this;
    }

    public EnvelopeHelper setPayload(byte[] payload) {
        env.setPayload(payload);
        return this;
    }
}
