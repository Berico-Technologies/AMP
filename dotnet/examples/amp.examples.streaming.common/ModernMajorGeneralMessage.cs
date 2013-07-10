using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace amp.examples.streaming.common
{
    public class ModernMajorGeneralMessage
    {
        public string Content { get; protected set; }

        public ModernMajorGeneralMessage(string content)
        {
            Content = content;
        }

    }
}
