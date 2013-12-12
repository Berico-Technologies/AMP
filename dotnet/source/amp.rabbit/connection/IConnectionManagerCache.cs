using System;
using System.Collections.Generic;
using amp.rabbit.topology;

namespace amp.rabbit.connection
{
    public interface IConnectionManagerCache : IDisposable
    {
        IEnumerable<IConnectionManager> GetConnectionManagersFor(BaseRoute route);
    }
}