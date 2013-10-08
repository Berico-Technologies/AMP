﻿using amp.rabbit.topology;
using RabbitMQ.Client;

namespace amp.rabbit.connection
{
    public class BasicConnectionFactory : BaseConnectionFactory
    {
        protected string _username;
        protected string _password;


        public BasicConnectionFactory(string username = "guest", string password = "guest")
        {
            _username = username;
            _password = password;
        }


        public override void ConfigureConnectionFactory(ConnectionFactory factory, Exchange exchange)
        {
            base.ConfigureConnectionFactory(factory, exchange);

            factory.UserName = _username;
            factory.Password = _password;
        }

    }
}
