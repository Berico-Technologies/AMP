using amp.bus.security;
using amp.anubis.client;
using amp.anubis.client.security;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace amp.anubis.client.tests
{
    [TestFixture]
    public class SslAnubisClientTests
    {
        private ICertificateProvider _certProvider;
        private string _userDn = "CN=dotnethater, OU=Users, OU=Capture, DC=archnet, DC=mil";
        private string _baseUrl = "https://anubis.archnet.mil:15678";

        [SetUp]
        public void SetupForTest()
        {
            _certProvider = new WindowsCertificateStoreCertProvider();

        }

        [TestCase]
        public void Verify_Authentication()
        {
            IAnubisClient client = new SslAnubisClient(_certProvider, _baseUrl);

            NamedToken userCredentials = client.AuthenticateWithUser(_userDn);
            Assert.IsNotNull(userCredentials);
            Assert.IsNotNull(userCredentials.Identity);
            Assert.IsTrue(userCredentials.Token.Length > 0);
        }

        [TestCase]
        public void Verify_IsAuthenticated()
        {
            IAnubisClient client = new SslAnubisClient(_certProvider, _baseUrl);
            NamedToken userCredentials = client.AuthenticateWithUser(_userDn);

            bool isAuthenticated = client.IsAuthenticated(userCredentials.Identity, userCredentials.Token);
            Assert.IsTrue(isAuthenticated);
        }
    }
}
