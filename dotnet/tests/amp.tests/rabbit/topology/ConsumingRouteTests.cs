using amp.rabbit.topology;
using NUnit.Framework;

namespace amp.tests.rabbit.topology
{
    /// <summary>
    /// These tests assert that equality and hashcode generation use value semantics
    /// not just in-so-far as properties have the same reference values but that 
    /// properties themselves are compared using a value semantic.  
    /// </summary>
    public class ConsumingRouteTests : BaseRouteTests
    {
        protected ConsumingRoute _queueNotEquivelentModel;

        public override void SetUp()
        {
            _referenceModel = NewConsummingRoute("broker1", "exchange1", "queue1", "route1");
            _equivelentModel = NewConsummingRoute("broker1", "exchange1", "queue1", "route1");
            _brokerNotEquivelentModel = NewConsummingRoute("broker2", "exchange1", "queue1", "route1");
            _exchangeNotEquivelentModel = NewConsummingRoute("broker1", "exchange2", "queue1", "route1");
            _routeNotEquivelentModel = NewConsummingRoute("broker1", "exchange1", "queue1", "route2");
            _queueNotEquivelentModel = NewConsummingRoute("broker1", "exchange1", "queue2", "route1");
        }

        private ConsumingRoute NewConsummingRoute(string host, string exchange, string queue, string routeKey)
        {
            return new ConsumingRoute(new[] { new Broker(host, 0) }, new Exchange(exchange), new Queue(queue), new[] { routeKey });
        }


        [Test]
        public void Equals_should_return_false_if_queue_values_differ()
        {
            Assert.That(_queueNotEquivelentModel.Equals(_referenceModel), Is.False);
        }

        [Test]
        public void Hashcodes_should_differ_if_queue_entries_differ()
        {
            Assert.That(_queueNotEquivelentModel.GetHashCode(), Is.Not.EqualTo(_equivelentModel.GetHashCode()));
        }

    }
}
