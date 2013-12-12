
using System.Collections.Generic;

namespace amp.rabbit.topology
{
    public class Queue : AmqpBaseModel
    {
        public bool IsExclusive { get; private set; }

        public Queue(string name, 
            bool isAutoDelete = false, 
            bool isDurable = false, 
            bool isExclusive = true, 
            bool shouldDeclare = false, 
            IDictionary<string, object> arguments = null) 
            : base(name, isAutoDelete, isDurable, shouldDeclare, arguments)
        {
            IsExclusive = isExclusive;
        }

        public override string ToString()
        {
            return string.Format("Queue [isExclusive={0}, name={1}, isAutoDelete={2}, isDurable={3}, shouldDeclare={4}, arguments={5}]", 
                IsExclusive, Name, IsAutoDelete, IsDurable, ShouldDeclare, Arguments);
        }

        protected bool Equals(Queue other)
        {
            return base.Equals(other) 
                && IsExclusive.Equals(other.IsExclusive);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((Queue) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return (base.GetHashCode()*397) ^ IsExclusive.GetHashCode();
            }
        }
    }
}
