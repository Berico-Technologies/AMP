package amp.gel.dao.impl.accumulo;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import amp.gel.dao.BaseTopicLoggerIntegrationTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-cmf-context.xml",
		"classpath:test-accumulo-context.xml" })
public class AccumuloTopicLoggerIntegrationTest extends
		BaseTopicLoggerIntegrationTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test @Ignore
	public void test() {
		super.test();
	}
}