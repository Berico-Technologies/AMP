using System;
using System.Collections.Generic;
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
    public class ExchangeTests : AmqpBaseModelTests
    {
        protected Exchange _typeNotEquivelentModel;

        public override void SetUp()
        {
            _referenceModel = NewExchange("topic", "arg1");
            _equivelentModel = NewExchange("topic", "arg1");
            _notEquivelentModel = NewExchange("topic", "arg2");
            _typeNotEquivelentModel = NewExchange("direct", "arg1");
        }

        private Exchange NewExchange(string exchangeType, string argValue)
        {
            var exchange = new Exchange("ex1", exchangeType, arguments: new Dictionary<String, object>());
            exchange.Arguments.Add("A", argValue);
            return exchange;
        }

        [Test]
        public void Equality_should_consider_exchange_type()
        {
            Assert.That(_typeNotEquivelentModel.Equals(_referenceModel), Is.False);
        }

        [Test]
        public void Hashcodes_should_differ_if_exchange_type_differ()
        {
            Assert.That(_typeNotEquivelentModel.GetHashCode(), Is.Not.EqualTo(_referenceModel.GetHashCode()));
        }
    }
}
