package amp.policy.core.adjudicators.javascript;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class JavaScriptLibrary {

    private String libraryName;

    private String librarySource;

    private String version;

    public JavaScriptLibrary(String libraryName, String librarySource, String version) {
        this.libraryName = libraryName;
        this.librarySource = librarySource;
        this.version = version;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public String getLibrarySource() {
        return librarySource;
    }

    public String getVersion() {
        return version;
    }
}
