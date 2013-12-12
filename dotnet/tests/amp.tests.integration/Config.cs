namespace amp.tests.integration
{
    internal class Config
    {
        internal class Bus
        {
            public const string All = "assembly://amp.tests.integration/amp.tests.integration.Config/AllBussesConfig.xml";
        }

        internal class Authorization
        {
            public const string Basic = "assembly://amp.tests.integration/amp.tests.integration.Config.Authorization/BasicAuthRabbitConfig.xml";
            public const string AnubisBasic = "assembly://amp.tests.integration/amp.tests.integration.Config.Authorization/AnubisAndBasicAuthRabbitConfig.xml";
            public const string AnubisOneWaySsl = "assembly://amp.tests.integration/amp.tests.integration.Config.Authorization/AnubisAndOneWaySSLRabbitConfig.xml";
            public const string AnubisTwoWaySsl = "assembly://amp.tests.integration/amp.tests.integration.Config.Authorization/AnubisAndTwoWaySSLRabbitConfig.xml";
        }

        internal class Topology
        {
            public const string Simple = "assembly://amp.tests.integration/amp.tests.integration.Config.Topology/SimpleTopologyConfig.xml";
            public const string Gts = "assembly://amp.tests.integration/amp.tests.integration.Config.Topology/GtsConfig.xml";
            public const string GtsSSL = "assembly://amp.tests.integration/amp.tests.integration.Config.Topology/GtsConfigSSL.xml";
        }
    }
}
