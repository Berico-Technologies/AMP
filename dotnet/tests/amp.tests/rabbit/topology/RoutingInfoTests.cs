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
    public class RoutingInfoTests
    {
        protected RoutingInfo _referenceRoute;
        protected RoutingInfo _equivelentRoute;
        protected RoutingInfo _producingRoutesNotEquivelentRoute;
        protected RoutingInfo _consumingRoutesNotEquivelent;

        [SetUp]
        public void SetUp()
        {
            _referenceRoute = NewRoutingInfo("prodRoute1", "conRoute1");
            _equivelentRoute = NewRoutingInfo("prodRoute1", "conRoute1");
            _producingRoutesNotEquivelentRoute = NewRoutingInfo("prodRoute2", "conRoute1");
            _consumingRoutesNotEquivelent = NewRoutingInfo("prodRoute1", "conRoute2");
        }

        private RoutingInfo NewRoutingInfo(string producingRouteKey, string consumingRouteKey)
        {
            return new RoutingInfo(
                new[] { new ProducingRoute(new[] { new Broker("host", 0) }, new Exchange("exchange"), new[] { producingRouteKey }) },
                new[] { new ConsumingRoute(new[] { new Broker("host", 0) }, new Exchange("exchange"), new Queue("queue"), new[] { consumingRouteKey }) }
                );
        }

        [Test]
        public void Equals_should_return_true_for_equivelent_routes()
        {
            Assert.That(_equivelentRoute.Equals(_referenceRoute), Is.True);
        }

        [Test]
        public void Equals_should_return_false_if_consuming_routes_differ()
        {
            Assert.That(_consumingRoutesNotEquivelent.Equals(_referenceRoute), Is.False);
        }

        [Test]
        public void Equals_should_return_false_if_producing_routes_differ()
        {
            Assert.That(_producingRoutesNotEquivelentRoute.Equals(_referenceRoute), Is.False);
        }

        [Test]
        public void Hashcodes_should_not_differ_for_equivelent_routes()
        {
            Assert.That(_equivelentRoute.GetHashCode(), Is.EqualTo(_referenceRoute.GetHashCode()));
        }

        [Test]
        public void Hashcodes_should_differ_if_consuming_routes_differ()
        {
            Assert.That(_consumingRoutesNotEquivelent.GetHashCode(), Is.Not.EqualTo(_referenceRoute.GetHashCode()));
        }

        [Test]
        public void Hashcodes_should_differ_if_producing_routes_differ()
        {
            Assert.That(_producingRoutesNotEquivelentRoute.GetHashCode(), Is.Not.EqualTo(_referenceRoute.GetHashCode()));
        }
    }
}
