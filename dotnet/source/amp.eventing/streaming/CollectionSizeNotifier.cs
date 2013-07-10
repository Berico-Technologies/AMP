using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace amp.eventing.streaming
{
    public class CollectionSizeNotifier
    {
        public int Size { get; set; }
        public string CollectionType { get; set; }
        public string SequenceId { get; set; }

        public CollectionSizeNotifier() 
        {
            Size = 0;
            CollectionType = null;
            SequenceId = null;
        }

        public CollectionSizeNotifier(int size, string collectionType, string sequenceId)
        {
            Size = size;
            CollectionType = collectionType;
            SequenceId = sequenceId;
        }
    }
}
