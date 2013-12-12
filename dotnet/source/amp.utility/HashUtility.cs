using System.Collections;
using System.Linq;

namespace amp.utility
{
    public class HashUtility
    {
        private const int InitialHash = 17; // Prime number
        private const int Multiplier = 23; // Different prime number

        public static int GetHashCode(IEnumerable values)
        {
            unchecked // overflow is fine
            {
                int hash = InitialHash;

                if (values != null)
                    hash = values.Cast<object>().Aggregate(hash, (currentHash, currentValue) => 
                        currentHash*Multiplier + (currentValue != null ? currentValue.GetHashCode() : 0));

                return hash;
            }
        }

        public static bool Compare(IEnumerable left, IEnumerable right)
        {
            if (left == null && right == null) return true;
            if (left == null || right == null) return false;

            var l = left.Cast<object>().ToArray();
            var r = right.Cast<object>().ToArray();

            if (l.Length != r.Length) return false;

            for (int i = 0; i < r.Length; i++)
            {
                if (l[i] == null && r[i] == null) continue;
                if (l[i] == null || r[i] == null) return false;
                if (!l[i].Equals(r[i])) return false;
            }
            return true;
        }
    }
}
