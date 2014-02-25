using amp.anubis.client.security;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace amp.anubis.client
{
    public interface IAnubisClient
    {
        NamedToken AuthenticateWithCurrentUserCertificate();

        NamedToken AuthenticateWithUser(string userDistinguishedName);

        bool IsAuthenticated(string identity, string token);
    }
}
