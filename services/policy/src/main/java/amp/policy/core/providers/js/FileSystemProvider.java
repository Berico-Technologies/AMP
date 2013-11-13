package amp.policy.core.providers.js;

import amp.policy.core.adjudicators.javascript.ScriptConfiguration;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class FileSystemProvider extends BaseScriptProvider {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemProvider.class);

    ConcurrentMap<Path, ScriptConfiguration> scripts = Maps.newConcurrentMap();

    /**
     * Time between files system check intervals.
     */
    public static long DELAY_BETWEEN_CHECKS_ON_FILE_SYSTEM = 1000;

    /**
     * Example:
     * // NAME: My Script
     * // ENTRY: object.function
     * // amd.asdf.asdf12: asdf223.23d2
     */
    private static final String COMMENT_KVP_PATTERN = "^\\s*\\/\\/\\s*(?:\\w|\\.)+[:]\\s*.+$";

    /**
     * Base Path for Scripts
     */
    private final Path scriptsBasePath;


    /**
     * Instantiate the repository by specifying the location of the repository.
     * @param scriptDirectory Repository location.
     * @throws IOException
     */
    public FileSystemProvider(String scriptDirectory) throws Exception {

        Path scriptBasePath = Paths.get(scriptDirectory);

        //Preconditions.checkState(Files.isDirectory(scriptBasePath, null));

        scriptsBasePath = scriptBasePath;

        initialize();
    }

    /**
     * Get all known scripts.
     * @return All scripts known to, and managed by, the FileSystemProvider.
     */
    @Override
    public Collection<ScriptConfiguration> get() {

        return scripts.values();
    }

    /**
     * Initialize the File System Monitor
     * @throws FileSystemException
     */
    private void initialize() throws Exception {

        loadInitialDirectory(scriptsBasePath, scripts, this);

        startDirectoryWatcher(scriptsBasePath, fileAlterationListener);
    }

    /**
     * Start watching the directory.
     * @param baseDirectory Directory to watch.
     * @param listener Handles changes on the file system.
     * @throws Exception
     */
    static void startDirectoryWatcher(Path baseDirectory, FileAlterationListener listener) throws Exception {

        FileAlterationObserver fao = new FileAlterationObserver(baseDirectory.toFile());

        FileAlterationMonitor fam = new FileAlterationMonitor(DELAY_BETWEEN_CHECKS_ON_FILE_SYSTEM);

        fao.addListener(listener);

        fam.addObserver(fao);

        fam.start();
    }

    /**
     * Load the initial directory.
     * @param path Base path
     * @param scriptBag the bag of known scripts
     * @param scriptProvider the provider we'll call to notify registered listeners of script changes
     * @throws IOException
     */
    static void loadInitialDirectory(Path path, Map<Path, ScriptConfiguration> scriptBag, BaseScriptProvider scriptProvider) throws IOException {

        getValidPaths(path, scriptBag, scriptProvider);
    }

    /**
     * Collect valid scripts and insert them into the script bag.  If the target is a directory, recursively scan the
     * directory for more scripts.
     * @param path Directory in scope
     * @param scriptBag Bag of Scripts
     * @param scriptProvider Service we'll call to notify script changes
     * @throws IOException
     */
    static void getValidPaths(Path path, Map<Path, ScriptConfiguration> scriptBag, BaseScriptProvider scriptProvider) throws IOException {

        DirectoryStream<Path> ds = Files.newDirectoryStream(path, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {

                return Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)
                       || (!Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS) && entry.toString().endsWith(".js"));
            }
        });

        for (Path p : ds){

            if (Files.isDirectory(p)){

                getValidPaths(p, scriptBag, scriptProvider);
            }
            else {

                ScriptConfiguration conf = getConfigurationForPath(p);

                scriptBag.put(p, conf);

                scriptProvider.fireScriptAdded(conf);
            }
        }
    }

    /**
     * Get the configuration for the given path.
     * @param fileToGetConfigFor Path to parse configuration info for
     * @return Script Configuration
     * @throws IOException
     */
    static ScriptConfiguration getConfigurationForPath(Path fileToGetConfigFor) throws IOException {

        List<String> lines = Files.readAllLines(fileToGetConfigFor, Charset.defaultCharset());

        return parse(lines);
    }

    /**
     * Update a script at the given path.
     * @param path Path of the update
     * @param configuration The actual update
     */
    public void updateScript(Path path, ScriptConfiguration configuration){

        removeScript(path);

        addScript(path, configuration);
    }

    /**
     * Add a new script to the provider
     * @param path Path of script
     * @param configuration The new script
     */
    public void addScript(Path path, ScriptConfiguration configuration){

        scripts.put(path, configuration);

        fireScriptAdded(configuration);
    }

    /**
     * Remove a script from the provider
     * @param path Path of script
     */
    public void removeScript(Path path){

        ScriptConfiguration configuration = scripts.remove(path);

        fireScriptRemoved(configuration);
    }

    /**
     * Extract the key-value pair from the line.
     * @param line Line to parse
     * @return Key-value pair representing the property.
     */
    static Map.Entry<String, String> parseLine(String line){

        final String key = line.substring(0, line.indexOf(":")).replace("/", "").trim();
        final String value = line.substring(line.indexOf(":") + 1).trim();

        return new Map.Entry<String, String>() {
            @Override
            public String getKey() { return key; }

            @Override
            public String getValue() { return value; }

            @Override
            public String setValue(String value) { return null; }
        };
    }

    /**
     * Parse the metadata from the script file and convert it to a ScriptConfiguration.
     * @param scriptLines Lines representing file.
     * @return ScriptConfiguration
     */
    static ScriptConfiguration parse(List<String> scriptLines){

        String name = null;
        String entry = null;

        HashMap<String, String> registrationInfo = Maps.newHashMap();

        for (String line : scriptLines){

            if (line.matches(COMMENT_KVP_PATTERN)) {

                Map.Entry<String, String> kvp = parseLine(line);

                if (kvp.getKey().toLowerCase().equals("name"))
                    name = kvp.getValue();
                else if (kvp.getKey().toLowerCase().equals("entry"))
                    entry = kvp.getValue();
                else
                    registrationInfo.put(kvp.getKey(), kvp.getValue());
            }
        }

        String body = Joiner.on("\n").join(scriptLines);

        checkNotNull(entry);

        return new ScriptConfiguration(name, body, entry, registrationInfo);
    }

    /**
     * Will listen to changes on the file system and call the correct method.
     */
    FileAlterationListenerAdaptor fileAlterationListener = new FileAlterationListenerAdaptor(){

        @Override
        public void onFileCreate(File file) {

            Path targetPath = Paths.get(file.getAbsolutePath());

            ScriptConfiguration configuration = null;

            try {

                configuration = getConfigurationForPath(targetPath);

            } catch (IOException e) {

                LOG.error("Unable to parse configuration.", e);
            }

            addScript(targetPath, configuration);
        }

        @Override
        public void onFileChange(File file) {

            Path targetPath = Paths.get(file.getAbsolutePath());

            // This method is erroneously called twice during an append, which
            // will make appear absent to the file system.  If we can not
            // locate the file during this process, we will ignore.
            if (!Files.exists(targetPath, LinkOption.NOFOLLOW_LINKS)) return;

            ScriptConfiguration configuration = null;

            try {
                configuration = getConfigurationForPath(targetPath);

            } catch (IOException e) {

                LOG.error("Unable to parse configuration.", e);
            }

            updateScript(targetPath, configuration);
        }

        @Override
        public void onFileDelete(File file) {

            Path targetPath = Paths.get(file.getAbsolutePath());

            removeScript(targetPath);
        }
    };
}
