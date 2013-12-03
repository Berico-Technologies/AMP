using System.Reflection;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;

// Global Descriptive Stuff
[assembly: AssemblyProduct("Advanced Modular Platform")]
[assembly: AssemblyCompany("Berico Technologies, LLC")]
[assembly: AssemblyCopyright("Copyright ©  2013")]
[assembly: AssemblyTrademark("")]
[assembly: AssemblyCulture("")]

// Global Versioning
[assembly: AssemblyVersion("3.1.2.*")]
/* let MSBuild automatically update this:
 * [assembly: AssemblyFileVersion("3.0.0.0")]
 * see: 
 *   http://stackoverflow.com/questions/356543/can-i-automatically-increment-the-file-build-version-when-using-visual-studio
 */

// Global Configuration
#if DEBUG
[assembly: AssemblyConfiguration("Debug")]
#else
[assembly: AssemblyConfiguration("Release")]
#endif