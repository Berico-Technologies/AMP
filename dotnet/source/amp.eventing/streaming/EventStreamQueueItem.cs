using cmf.bus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace amp.eventing.streaming
{
    /// <summary>
    /// Contains the elements needed to send an event in an event stream.
    /// </summary>
    public class EventStreamQueueItem
    {
        private readonly EventContext _eventContext;

        public EventStreamQueueItem(EventContext eventContext)
        {
            _eventContext = eventContext;
        }

        public Envelope Envelope { get { return _eventContext.Envelope; } }

        public EventContext EventContext { get { return _eventContext; } }
    }
}
