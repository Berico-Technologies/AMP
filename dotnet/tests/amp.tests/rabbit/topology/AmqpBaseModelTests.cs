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
    public class AmqpBaseModelTests
    {
	    protected AmqpBaseModel _referenceModel;
	    protected AmqpBaseModel _equivelentModel;
	    protected AmqpBaseModel _notEquivelentModel;

        [SetUp]
        public virtual void SetUp()
        {
            _referenceModel = new TestModel();
            _referenceModel.Arguments.Add("1", "one");

            _equivelentModel = new TestModel();
            _equivelentModel.Arguments.Add("1", "one");

            _notEquivelentModel = new TestModel();
            _notEquivelentModel.Arguments.Add("1", "uno");
        }

        [Test]
        public void Equality_should_consider_argument_contents()
        {
            Assert.That(_equivelentModel.Equals(_referenceModel), Is.True);
        }

        [Test]
        public void Equality_should_consider_argument_contents_not_equals()
        {
            Assert.That(_notEquivelentModel.Equals(_referenceModel), Is.False);
        }

        [Test]
        public void Hashcodes_should_not_differ_if_noting_differs()
        {
            Assert.That(_equivelentModel.GetHashCode(), Is.EqualTo(_referenceModel.GetHashCode()));
        }

        //[Test]
        //We can't assert this in .Net because KeyValuePair<K,V>.GetHashCode() impelementation is too loose.
        //However this test is not critical as hashcode colisions are acceptable, so long as .Equals() returns false.
        //That assumption is also tested in this fixture so we are good.
        public void Hashcodes_should_differ_if_argument_contents_differ()
        {
            Assert.That(_notEquivelentModel.GetHashCode(), Is.Not.EqualTo(_referenceModel.GetHashCode()));
        }

        private class TestModel : AmqpBaseModel
        {
            public TestModel()
                : base("test", true, true, true, new Dictionary<String, Object>())
            {
            }
        }
    }
}
