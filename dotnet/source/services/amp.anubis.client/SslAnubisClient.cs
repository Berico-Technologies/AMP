﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using amp.bus.security;
using System.Security.Cryptography.X509Certificates;
using System.Net;
using System.IO;
using amp.anubis.client.security;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using Common.Logging;

namespace amp.anubis.client
{
    public class SslAnubisClient : IAnubisClient
    {
        public ICertificateProvider CertificateProvider { get; set; }
        private readonly ILog _log = LogManager.GetLogger(typeof(SslAnubisClient).FullName);
        private const string AnubisVerifyMethod = "/anubis/rabbitmq/user?username={0}&password={1}";
        private const string AnubisAuthenticationMethod = "/anubis/identity/authenticate";
        private string _verifyTokenUri;
        private string _authenticationUri;
        protected readonly JsonSerializerSettings _settings;

        public SslAnubisClient(ICertificateProvider certProvider, string anubisEndpointHost)
        {
            CertificateProvider = certProvider;
            _verifyTokenUri = anubisEndpointHost + AnubisVerifyMethod;
            _authenticationUri = anubisEndpointHost + AnubisAuthenticationMethod;
            _settings = new JsonSerializerSettings
            {
                ContractResolver = new LowercaseContractResolver(),
                TypeNameHandling = TypeNameHandling.Objects
            };
        }

        #region Interface Implementation for IAnubisClient
        public bool IsAuthenticated(string identity, string token)
        {
            _log.Debug("Enter IsAuthenticated");
            bool isAuthenticated = true;
            HttpWebResponse response = CheckCredentials(identity, token);
            string anubisResponse = GetContentFromResponse(response);
            if ("deny" == anubisResponse)
            {
                _log.Debug(string.Format("User: {0} has a failed or expired authentication token", identity));
                isAuthenticated = false;
            }
            
            _log.Debug("Leave IsAuthenticated");
            return isAuthenticated;
        }

        public NamedToken AuthenticateWithCurrentUserCertificate()
        {
            _log.Debug("Enter AuthenticateWithCurrentUserCertificate");
            X509Certificate2 userCert = CertificateProvider.GetCertificate();
            NamedToken credentials = AuthenticateWithCertificate(userCert);
            _log.Debug("Leave AuthenticateWithCurrentUserCertificate");
            return credentials;
        }

        public NamedToken AuthenticateWithUser(string userDistinguishedName)
        {
            _log.Debug("Enter AuthenticateWithUser");
            X509Certificate2 userCert = CertificateProvider.GetCertificateFor(userDistinguishedName);
            if (userCert == null)
            {
                throw new Exception("Unable to find certificate for " + userDistinguishedName);
            }
            NamedToken token = AuthenticateWithCertificate(userCert);
            _log.Debug("Leave AuthenticateWithUser");
            return token;
        }
        #endregion

        #region Private Methods

        private NamedToken AuthenticateWithCertificate(X509Certificate2 userCert)
        {
            _log.Debug("Enter AuthenticateWithCertificate");
            HttpWebResponse response = RequestAuthentication(userCert);
            string rawJsonResponse = GetContentFromResponse(response);

            NamedToken credentials = null;
            if (false == string.IsNullOrEmpty(rawJsonResponse))
            {
                _log.Debug(string.Format("Attempting to deserialize JSON response into a NamedToken:\n{0}", rawJsonResponse));
                credentials = JsonConvert.DeserializeObject<NamedToken>(rawJsonResponse);
                _log.Debug("Response successfully converted into NamedToken");
            }

            _log.Debug("Leave AuthenticateWithCurrentUserCertificate");
            return credentials;
        }

        private static string GetContentFromResponse(HttpWebResponse response)
        {
            StringBuilder contentBody = new StringBuilder();

            using (Stream contentBodyStream = response.GetResponseStream())
            {

                int numCharsToReadAtATime = 256;

                Encoding encoding = System.Text.Encoding.GetEncoding("utf-8");
                using (StreamReader contentReader = new StreamReader(contentBodyStream, encoding))
                {
                    Char[] read = new Char[numCharsToReadAtATime];
                    int charsRead = contentReader.Read(read, 0, numCharsToReadAtATime);
                    while (charsRead > 0)
                    {
                        contentBody.Append(new string(read, 0, charsRead));
                        charsRead = contentReader.Read(read, 0, numCharsToReadAtATime);
                    }
                    contentReader.Close();
                }
            }
            byte[] contentBytes = new UTF8Encoding().GetBytes(contentBody.ToString());
            string utf8Content = new UTF8Encoding().GetString(contentBytes);
            return utf8Content;
        }

        

        private HttpWebResponse RequestAuthentication(X509Certificate2 userCert)
        {
            return RequestFromWeb(_authenticationUri, userCert);
        }

        private HttpWebResponse CheckCredentials(string identity, string token)
        {
            string parameterizedUri = string.Format(_verifyTokenUri, identity, token);
            return RequestFromWeb(parameterizedUri);
        }

        private HttpWebResponse RequestFromWeb(string uri, X509Certificate2 optionalCert = null)
        {
            HttpWebRequest request = HttpWebRequest.Create(uri) as HttpWebRequest;
            if (null != optionalCert)
            {
                request.ClientCertificates.Add(optionalCert);
            }
            return request.GetResponse() as HttpWebResponse;
        }
        #endregion

        /// <summary>
        /// We need to resolve properties to lowercase in order to be interoperable
        /// with java and other json serializers.
        /// </summary>
        protected class LowercaseContractResolver : DefaultContractResolver
        {
            protected override string ResolvePropertyName(string propertyName)
            {
                string firstChar = propertyName.Substring(0, 1).ToLower();
                string camelName = string.Format("{0}{1}", firstChar, propertyName.Substring(1));

                return camelName;
            }
        }
    }
}
