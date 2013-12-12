using System;
using System.Runtime.Caching;
using amp.rabbit.topology;

namespace amp.rabbit.transport
{
    public class SimpleRoutingInfoCache : IRoutingInfoCache
    {
        protected readonly MemoryCache _routingInfoCache;
        protected readonly int _cacheExpiryInSeconds;
        protected readonly object _cacheLock = new object();

        protected SimpleRoutingInfoCache(int cacheExpiryInSeconds)
        {
            _routingInfoCache = new MemoryCache("amp.bus.rabbit.cache");
            _cacheExpiryInSeconds = cacheExpiryInSeconds;
        }

        public RoutingInfo GetIfPresent(String topic) {

            // I don't think we should lock this call, otherwise we'll lock everytime we try to read the cache, which
            // is going to kill performance.  The worst that happens is we make an extraneous call to the GTS.
            return _routingInfoCache.Get(topic) as RoutingInfo;
        }

        public void Put(String topic, RoutingInfo routingInfo)
        {
            lock (_cacheLock)
            {
                // creating a new cache policy each time is annoying, but necessary
                // see: http://stackoverflow.com/questions/16972641/expiring-a-cached-item-via-cacheitempolicy-in-net-memorycache
                CacheItemPolicy expirationPolicy = new CacheItemPolicy()
                {
                    AbsoluteExpiration = new DateTimeOffset(DateTime.UtcNow.AddSeconds(_cacheExpiryInSeconds))
                };

                _routingInfoCache.Add(new CacheItem(topic, routingInfo), expirationPolicy);
            }
        }

        public virtual void Dispose()
        {
            
        }
    }
}