package amp.tests.integration;

public class Config {

	public class Bus{
		public final static String All ="config/AllBussesConfig.xml";
	}

	public class Authorization{
		public final static String Basic ="config/authorization/BasicAuthRabbitConfig.xml";
		public final static String AnubisBasic ="config/authorization/AnubisAndBasicAuthRabbitConfig.xml";
		public final static String AnubisTwoWaySsl ="config/authorization/AnubisAndTwoWaySSLRabbitConfig.xml";
	}

	public class Topology{
		public final static String Simple ="config/topology/SimpleTopologyConfig.xml";
		public final static String Gts ="config/topology/GtsConfig.xml";
		public final static String GtsSSL ="config/topology/GtsConfigSSL.xml";
	}
}
