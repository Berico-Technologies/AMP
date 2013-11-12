package amp.policy.core.providers.js;

import amp.policy.core.adjudicators.javascript.ScriptConfiguration;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.codehaus.plexus.util.DirectoryWalkListener;
import org.codehaus.plexus.util.DirectoryWalker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class FileSystemProvider extends BaseScriptProvider implements FileListener {

    ConcurrentMap<Path, ScriptConfiguration> scripts = Maps.newConcurrentMap();

    public static long DELAY_BETWEEN_CHECKS_ON_FILE_SYSTEM = 2000;

    /**
     * Example:
     * // NAME: My Script
     */
    private static final String NAME_PATTERN = "^\\s*\\/\\/\\s+NAME:\\s+.+$";

    /**
     * Example:
     * // ENTRY: object.function
     */
    private static final String ENTRY_PATTERN = "^\\s*\\/\\/\\s+ENTRY:\\s+.+$";

    private final Path scriptsBaseDirectory;


    /**
     * Instantiate the repository by specifying the location of the repository.
     * @param scriptDirectory Repository location.
     * @throws IOException
     */
    public FileSystemProvider(String scriptDirectory) throws IOException {

        Path scriptBasePath = Paths.get(scriptDirectory);

        Preconditions.checkState(Files.isDirectory(scriptBasePath, null));

        scriptsBaseDirectory = scriptBasePath;

        initialize();
    }

    @Override
    public Collection<ScriptConfiguration> get() {

        return scripts.values();
    }

    /**
     * Initialize the File System Monitor
     * @throws FileSystemException
     */
    private void initialize() throws IOException {

        loadInitialDirectory(scriptsBaseDirectory, scripts, this);

        startFileMonitoring(scriptsBaseDirectory, this);
    }


    static void startFileMonitoring(Path baseDirectory, FileListener listener) throws IOException {

        FileSystemManager fsm = VFS.getManager();

        FileObject foBaseDirectory = fsm.resolveFile(baseDirectory.toString());

        final DefaultFileMonitor fileMonitor = new DefaultFileMonitor(listener);

        fileMonitor.setRecursive(true);

        fileMonitor.addFile(foBaseDirectory);

        fileMonitor.setDelay(DELAY_BETWEEN_CHECKS_ON_FILE_SYSTEM);

        // Ensure the file monitor is stopped when the service shuts down.
        Runtime.getRuntime().addShutdownHook(new Thread(){

            @Override
            public void run() {

                fileMonitor.stop();
            }
        });

        fileMonitor.start();
    }


    static void loadInitialDirectory(Path path, Map<Path, ScriptConfiguration> scriptBag, BaseScriptProvider scriptProvider) throws IOException {

        ArrayList<Path> pathsToBeProcessed = Lists.newArrayList();

        getValidPaths(path, scriptBag, scriptProvider, pathsToBeProcessed);
    }

    static void getValidPaths(Path path, Map<Path, ScriptConfiguration> scriptBag, BaseScriptProvider scriptProvider, List<Path> toBeProcessed) throws IOException {

        DirectoryStream<Path> ds = Files.newDirectoryStream(path, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return Files.isDirectory(entry, null) || (!Files.isDirectory(entry, null) && entry.endsWith(".js"));
            }
        });

        for (Path p : ds){

            if (Files.isDirectory(p)){

                getValidPaths(p, scriptBag, scriptProvider, toBeProcessed);
            }
            else {

                ScriptConfiguration conf = getConfigurationForPath(p);

                scriptBag.put(p, conf);

                scriptProvider.fireScriptAdded(conf);
            }
        }
    }

    static ScriptConfiguration getConfigurationForPath(Path fileToGetConfigFor) throws IOException {

        List<String> lines = Files.readAllLines(fileToGetConfigFor, Charset.defaultCharset());

        return parse(lines);
    }

    protected void updateScript(Path path, ScriptConfiguration configuration){

        removeScript(path);

        addScript(path, configuration);
    }

    protected void addScript(Path path, ScriptConfiguration configuration){

        scripts.put(path, configuration);

        fireScriptAdded(configuration);
    }

    protected void removeScript(Path path){

        ScriptConfiguration configuration = scripts.remove(path);

        fireScriptRemoved(configuration);
    }

    /**
     * Parse the metadata from the script file and convert it to a ScriptConfiguration.
     * @param scriptLines Lines representing file.
     * @return ScriptConfiguration
     */
    static ScriptConfiguration parse(List<String> scriptLines){

        String name = null;
        String entry = null;

        for (String line : scriptLines){

            if (line.matches(NAME_PATTERN))
                name = line.substring(line.indexOf(":") + 1).trim();

            if (line.matches(ENTRY_PATTERN))
                entry = line.substring(line.indexOf(":") + 1).trim();
        }

        String body = Joiner.on("\n").join(scriptLines);

        checkNotNull(entry);

        String[] parts = entry.split("[.]");

        if (parts.length > 1)
            return new ScriptConfiguration(name, body, parts[1], parts[0]);

        return new ScriptConfiguration(name, body, entry);
    }

    @Override
    public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {

        Path fileToBeAdded = Paths.get(fileChangeEvent.getFile().getURL().toURI());

        ScriptConfiguration configuration = getConfigurationForPath(fileToBeAdded);

        addScript(fileToBeAdded, configuration);
    }

    @Override
    public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {

        Path fileToBeRemoved = Paths.get(fileChangeEvent.getFile().getURL().toURI());

        removeScript(fileToBeRemoved);
    }

    @Override
    public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {

        Path fileToBeUpdated = Paths.get(fileChangeEvent.getFile().getURL().toURI());

        ScriptConfiguration configuration = getConfigurationForPath(fileToBeUpdated);

        updateScript(fileToBeUpdated, configuration);
    }
}
