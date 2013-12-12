using System;
using System.IO;
using System.Net;
using amp.rabbit.topology;
using amp.utility.http;
using amp.utility.serialization;
using RabbitMQ.Client;

namespace amp.rabbit
{
    public class TokenConnectionFactory : BaseConnectionFactory
    {
        private readonly string _anubisUri;
        private readonly IWebRequestFactory _webRequestFactory;
        private readonly IDeserializer<NamedToken> _serializer;
        private readonly BaseConnectionFactory _secureConnectionFactory;

        public TokenConnectionFactory(
            string anubisUri,
            IWebRequestFactory webRequestFactory,
            IDeserializer<NamedToken> serializer)
            : this(anubisUri, webRequestFactory, serializer, null)
        {
        }

        public TokenConnectionFactory(
            string anubisUri,
            IWebRequestFactory webRequestFactory,
            IDeserializer<NamedToken> serializer,
            BaseConnectionFactory secureConnectionFactory)
        {
            _anubisUri = anubisUri;
            _webRequestFactory = webRequestFactory;
            _serializer = serializer;
            _secureConnectionFactory = secureConnectionFactory;
        }

        public override void ConfigureConnectionFactory(ConnectionFactory factory, Exchange exchange)
        {
            NamedToken token = GetNamedToken();

            base.ConfigureConnectionFactory(factory, exchange);

            if (_secureConnectionFactory != null)
            {
                _secureConnectionFactory.ConfigureConnectionFactory(factory, exchange);
            }

            factory.AuthMechanisms = new AuthMechanismFactory[] { new PlainMechanismFactory() };
            factory.UserName = token.Identity;
            factory.Password = token.Token;
        }

        private NamedToken GetNamedToken()
        {
            try
            {
                // use the web request factory to create a web request
                WebRequest request = _webRequestFactory.CreateRequest(_anubisUri);

                // get some response to the request
                using (Stream responseStream = request.GetResponse().GetResponseStream())
                {
                    return _serializer.Deserialize(responseStream);
                }
            }
            catch (Exception ex)
            {
                _log.Error(string.Format("Failed to get token from {0}.", _anubisUri), ex);
                // ReSharper disable once PossibleIntendedRethrow
                throw ex;
            }
        }

        public class NamedToken
        {
            public NamedToken(String identity, String token)
            {
                Identity = identity;
                Token = token;
            }

            public string Identity { get; set; }
            public string Token { get; set; }
        }
    }
}