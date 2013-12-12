namespace amp.rabbit.topology
{
    public class Broker
    {
        public string ClusterId { get; private set; }
        public string Hostname { get; private set; }
        public int Port { get; private set; }
        public string VirtualHost { get; private set; }
        public string ConnectionStrategy { get; private set; }

        public Broker(string hostname, int port)
            : this(null, hostname, port)
        {
        }

        public Broker(string clusterId, string hostname, int port)
            : this(clusterId, hostname, port, "")
        {
        }

        public Broker(string hostname, int port, string connectionStrategy)
            : this(null, hostname, port, "/", connectionStrategy)
        {
        }

        public Broker(string clusterId, string hostname, int port, string connectionStrategy)
            : this(clusterId, hostname, port, "/", connectionStrategy)
        {
        }

        public Broker(string clusterId, string hostname, int port, string virtualHost, string connectionStrategy)
        {
            ClusterId = clusterId;
            Hostname = hostname;
            Port = port;
            VirtualHost = virtualHost;
            ConnectionStrategy = connectionStrategy;
        }

        protected bool Equals(Broker other)
        {
            return string.Equals(ClusterId, other.ClusterId) 
                && string.Equals(Hostname, other.Hostname) 
                && Port == other.Port
                && string.Equals(VirtualHost, other.VirtualHost)
                && ConnectionStrategy.Equals(other.ConnectionStrategy);
        }

        public override string ToString()
        {
            return string.Format("Broker [ClusterId={0}, Hostname={1}, Port={2}, VHost={3}, ConnectionStrategy={4}]", 
                ClusterId, Hostname, Port, VirtualHost, ConnectionStrategy);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((Broker) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                var hashCode = (ClusterId != null ? ClusterId.GetHashCode() : 0);
                hashCode = (hashCode*397) ^ (Hostname != null ? Hostname.GetHashCode() : 0);
                hashCode = (hashCode*397) ^ Port;
                hashCode = (hashCode * 397) ^ (VirtualHost != null ? VirtualHost.GetHashCode() : 0);
                hashCode = (hashCode * 397) ^ (ConnectionStrategy != null ? ConnectionStrategy.GetHashCode() : 0);
                return hashCode;
            }
        }
    }
}