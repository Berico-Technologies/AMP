using amp.rabbit.topology;
using NUnit.Framework;

namespace amp.tests.rabbit.topology
{
    /// <summary>
    /// These tests assert that equality and hashcode generation use value semantics
    /// not just in-so-far as properties have the same reference values but that 
    /// properties themselves are compared using a value semantic.  
    /// </summary>
    [TestFixture]
    public class BaseRouteTests
    {
        protected BaseRoute _referenceModel;
        protected BaseRoute _equivelentModel;
        protected BaseRoute _brokerNotEquivelentModel;
        protected BaseRoute _exchangeNotEquivelentModel;
        protected BaseRoute _routeNotEquivelentModel;

        [SetUp]
        public virtual void SetUp()
        {
            _referenceModel = new TestRoute("broker1", "exchange1", "route1");
            _equivelentModel = new TestRoute("broker1", "exchange1", "route1");
            _brokerNotEquivelentModel = new TestRoute("broker2", "exchange1", "route1");
            _exchangeNotEquivelentModel = new TestRoute("broker1", "exchange2", "route1");
            _routeNotEquivelentModel = new TestRoute("broker1", "exchange1", "route2");
        }

        [Test]
        public void Equals_should_return_true_for_equivelent_routes()
        {
            Assert.That(_equivelentModel.Equals(_referenceModel), Is.True);
        }

        [Test]
        public void Equals_should_return_false_if_broker_values_differ()
        {
            Assert.That(_brokerNotEquivelentModel.Equals(_referenceModel), Is.False);
        }

        [Test]
        public void Equals_should_return_false_if_exchange_values_differ()
        {
            Assert.That(_exchangeNotEquivelentModel.Equals(_referenceModel), Is.False);
        }

        [Test]
        public void Equals_should_return_false_if_route_values_differ()
        {
            Assert.That(_routeNotEquivelentModel.Equals(_referenceModel), Is.False);
        }

        [Test]
        public void Hashcodes_should_not_differ_for_equivelent_routes()
        {
            Assert.That(_equivelentModel.GetHashCode(), Is.EqualTo(_referenceModel.GetHashCode()));
        }

        [Test]
        public void Hashcodes_should_differ_if_broker_entries_differ()
        {
            Assert.That(_brokerNotEquivelentModel.GetHashCode(), Is.Not.EqualTo(_referenceModel.GetHashCode()));
        }

        [Test]
        public void Hashcodes_should_differ_if_exchanges_differ()
        {
            Assert.That(_exchangeNotEquivelentModel.GetHashCode(), Is.Not.EqualTo(_referenceModel.GetHashCode()));
        }

        [Test]
        public void Hashcodes_should_differ_if_routingKey_entries_differ()
        {
            Assert.That(_routeNotEquivelentModel.GetHashCode(), Is.Not.EqualTo(_referenceModel.GetHashCode()));
        }

        private class TestRoute : BaseRoute
        {
            public TestRoute(string host, string exchange, string routeKey)
                : base(new[] { new Broker(host, 0) }, new Exchange(exchange), new[] { routeKey })
            {
            }
        }

    }
}
