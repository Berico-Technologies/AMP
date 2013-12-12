using System.Collections.Generic;
using System.Text;
using amp.utility;

namespace amp.rabbit.topology
{
    public class Exchange : AmqpBaseModel
    {
        public string ExchangeType { get; private set; }

        public Exchange(
            string name, 
            string exchangeType = "topic", 
            bool isDurable = false,
            bool autoDelete = false,
            bool shouldDeclare = false,
            IDictionary<string, object> arguments = null)
            : base(name, autoDelete, isDurable, shouldDeclare, arguments)
        {
            this.ExchangeType = exchangeType;
        }


        public override string ToString()
        {
            StringBuilder sb = new StringBuilder();

            sb.Append("{");
            sb.AppendFormat("Name:{0},", this.Name);
            sb.AppendFormat("ExchangeType:{0},", this.ExchangeType);
            sb.AppendFormat("IsDurable:{0},", this.IsDurable);
            sb.AppendFormat("IsAutoDelete:{0},", this.IsAutoDelete);
            sb.AppendFormat("ShouldDeclare:{0},", this.ShouldDeclare);

            sb.Append("}");
            return sb.ToString();
        }

        public bool Equals(Exchange other)
        {
            return string.Equals(Name, other.Name) 
                && string.Equals(ExchangeType, other.ExchangeType) 
                && IsDurable.Equals(other.IsDurable) 
                && IsAutoDelete.Equals(other.IsAutoDelete) 
                && ShouldDeclare.Equals(other.ShouldDeclare)
                && HashUtility.Compare(Arguments, other.Arguments);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            return obj is Exchange && Equals((Exchange) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                var hashCode = (Name != null ? Name.GetHashCode() : 0);
                hashCode = (hashCode*397) ^ (ExchangeType != null ? ExchangeType.GetHashCode() : 0);
                hashCode = (hashCode*397) ^ IsDurable.GetHashCode();
                hashCode = (hashCode*397) ^ IsAutoDelete.GetHashCode();
                hashCode = (hashCode*397) ^ ShouldDeclare.GetHashCode();
                hashCode = (hashCode*397) ^ (Arguments != null ? HashUtility.GetHashCode(Arguments) : 0);
                return hashCode;
            }
        }
    }
}
