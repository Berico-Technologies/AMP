package amp.gel.service;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import amp.gel.dao.BaseTopicLoggerIntegrationTest.SimplePojo;
import cmf.bus.Envelope;
import amp.messaging.EnvelopeHeaderConstants;
import cmf.bus.IEnvelopeFilterPredicate;
import cmf.bus.IRegistration;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Counts the # of envelopes that have been received with a specific topic.
 * 
 */
public class TopicCounter implements IRegistration {

    private static final Logger LOG = LoggerFactory.getLogger(TopicCounter.class);

	private String topic;


	public TopicCounter(String topic) {
		super();
		this.topic = topic;
	}

	private volatile AtomicLong count = new AtomicLong();

	private Set<Integer> ids = Collections
			.synchronizedSet(new TreeSet<Integer>());

	private Gson gson = new Gson();

	public long getCount() {
		return count.longValue();
	}

	public Set<Integer> getMissingIds() {
		Set<Integer> missingIds = new HashSet<Integer>();

		for (int i = 0; i < ids.size(); i++) {
			if (!ids.contains(i)) {
				missingIds.add(i);
			}
		}

		return missingIds;
	}

	public IEnvelopeFilterPredicate getFilterPredicate() {
		return null;
	}

	public Map<String, String> getRegistrationInfo() {
		Map<String, String> info = new HashMap<String, String>();
		info.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, topic);
		return info;
	}

	public Object handle(Envelope envelope) throws Exception {

		addId(envelope);
        long current = count.incrementAndGet();

        LOG.debug("Received a SimplePojo, count currently at {}", current);
		return true;
	}

	public Object handleFailed(Envelope envelope, Exception ex)
			throws Exception {
		return ex;
	}

	private void addId(Envelope envelope) throws UnsupportedEncodingException {
		SimplePojo pojo = gson.fromJson(new String(envelope.getPayload(),
				"UTF-8"), SimplePojo.class);
		ids.add(pojo.getId());
	}
}
