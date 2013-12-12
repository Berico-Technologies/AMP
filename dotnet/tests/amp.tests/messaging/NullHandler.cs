using System;
using System.Collections.Generic;
using amp.messaging;

namespace amp.tests.messaging
{
    class NullHandler : IMessageHandler
    {
        public string Topic
        {
            get { return "NULL"; }
        }

        public object Handle(object message, IDictionary<string, string> headers)
        {
            return null;
        }

        public object HandleFailed(cmf.bus.Envelope env, Exception ex)
        {
            return null;
        }
    }
}
