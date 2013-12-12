using System;
using System.Collections;
using System.Collections.Generic;
using amp.utility;

namespace amp.rabbit.topology
{
    /// <summary>
    /// Queues and Exchanges share a number of properties. The shared behavior is
    /// captured here.
    /// </summary>
    public abstract class AmqpBaseModel
    {
        public string Name { get; private set; }

        /// <summary>
        /// Delete queue/exchange when there are no consumers/queues (respectively)
        /// using the queue/exchange.
        /// </summary>
        public bool IsAutoDelete { get; private set; }

        /// <summary>
        /// Indicates that the queue/exchange persists when the server restarts.
        /// </summary>
        public bool IsDurable { get; private set; }

        /// <summary>
        /// Indicates if the client should create the queue/exchange if it doesn't exist.
        /// </summary>
        public bool ShouldDeclare { get; private set; }
        
        public IDictionary<string, object> Arguments { get; private set; }

        /// <summary>
        /// Returns the sames value as Arguments but as a non-generic Dictionary 
        /// as required for use with the the RabbitMQ .Net client.
        /// </summary>
        public IDictionary ArgumentsAsDictionary
        {
            get
            {
                return Arguments == null
                    ? null
                    : Arguments as IDictionary ?? new Dictionary<string, object>(Arguments);
            }
        } 

        protected AmqpBaseModel(string name, bool isAutoDelete, bool isDurable,
            bool shouldDeclare,
            IDictionary<string, Object> arguments)
        {
            Name = name;
            IsAutoDelete = isAutoDelete;
            IsDurable = isDurable;
            ShouldDeclare = shouldDeclare;
            Arguments = arguments;
        }

        protected bool Equals(AmqpBaseModel other)
        {
            return string.Equals(Name, other.Name) 
                && IsAutoDelete.Equals(other.IsAutoDelete) 
                && IsDurable.Equals(other.IsDurable) 
                && ShouldDeclare.Equals(other.ShouldDeclare)
                && HashUtility.Compare(Arguments, other.Arguments);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((AmqpBaseModel) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                var hashCode = (Name != null ? Name.GetHashCode() : 0);
                hashCode = (hashCode*397) ^ IsAutoDelete.GetHashCode();
                hashCode = (hashCode*397) ^ IsDurable.GetHashCode();
                hashCode = (hashCode*397) ^ ShouldDeclare.GetHashCode();
                hashCode = (hashCode * 397) ^ (Arguments != null ? HashUtility.GetHashCode(Arguments) : 0);
                return hashCode;
            }
        }
    }
}
