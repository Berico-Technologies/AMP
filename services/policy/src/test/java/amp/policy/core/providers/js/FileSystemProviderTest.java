package amp.policy.core.providers.js;

import amp.policy.core.adjudicators.javascript.ScriptConfiguration;
import cmf.bus.EnvelopeHeaderConstants;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class FileSystemProviderTest {

    private static final long MAX_WAIT_TIME = FileSystemProvider.DELAY_BETWEEN_CHECKS_ON_FILE_SYSTEM + 100;

    private static final ArrayList<String> MOCK_SCRIPT =  Lists.newArrayList(
            " // NAME:  My Script                                   ",
            " // ENTRY: obj.myFunction                              ",
            " // amp.test.property: my.test.Class123                ",
            " // topic: my.topic.Event                              ",
            " // sender: rclayton@beri.co                           ",
            "                                                       ",
            " function adjudicate(envelope, enforcer){              ",
            "      enforcer.approve(envelope);                      ",
            " }                                                     "
    );

    private static final ArrayList<String> APPEND_TO_MOCK =  Lists.newArrayList(
            "                               ",
            " function identify(i){         ",
            "      return i;                ",
            " }                             "
    );

    private static final String MOCK_SCRIPT_BODY = Joiner.on("\n").join(MOCK_SCRIPT);

    private static final String APPEND_TO_MOCK_BODY = Joiner.on("\n").join(APPEND_TO_MOCK);

    @Test
    public void parse_correctly_extracts_script_configuration(){

        ScriptConfiguration configuration = FileSystemProvider.parse(MOCK_SCRIPT);

        assertEquals("My Script", configuration.getName());
        assertEquals("obj", configuration.getObjectEntry());
        assertEquals("myFunction", configuration.getFunctionEntry());
        assertEquals("my.topic.Event", configuration.getRegistrationInfo().get(EnvelopeHeaderConstants.MESSAGE_TOPIC));
        assertEquals("rclayton@beri.co", configuration.getRegistrationInfo().get(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY));
        assertEquals("my.test.Class123", configuration.getRegistrationInfo().get("amp.test.property"));
        assertEquals(MOCK_SCRIPT_BODY, configuration.getBody());
    }

    private static Path getResourcePath(String path) throws URISyntaxException {

        URL url = FileSystemProvider.class.getResource(path);

        return Paths.get(url.toURI());
    }

    private void assertHasScript(
            Map<Path, ScriptConfiguration> scriptBag,
            BaseScriptProvider bsp,
            String filePath,
            String expectedScriptName)
            throws URISyntaxException {

        Path expectedPath = getResourcePath(filePath);

        assertTrue(Files.exists(expectedPath, LinkOption.NOFOLLOW_LINKS));

        assertTrue(scriptBag.containsKey(expectedPath));

        ScriptConfiguration configuration = scriptBag.get(expectedPath);

        assertEquals(expectedScriptName, configuration.getName());

        verify(bsp).fireScriptAdded(configuration);
    }

    private void assertNotHasScript(Map<Path, ScriptConfiguration> scriptBag, String filePath){

        Path notExpectedPath = Paths.get(filePath);

        assertFalse(scriptBag.containsKey(notExpectedPath));
    }

    @Test
    public void correctly_loads_directory_of_scripts() throws IOException, URISyntaxException {

        Path p = getResourcePath("/scriptTestFiles/fileSystemProvider_load");

        Map<Path, ScriptConfiguration> scriptBag = Maps.newHashMap();

        BaseScriptProvider bsp = mock(BaseScriptProvider.class);

        FileSystemProvider.loadInitialDirectory(p, scriptBag, bsp);

        assertHasScript(scriptBag, bsp, "/scriptTestFiles/fileSystemProvider_load/testa.js", "testa.js");
        assertHasScript(scriptBag, bsp, "/scriptTestFiles/fileSystemProvider_load/testb.js", "testb.js");
        assertHasScript(scriptBag, bsp, "/scriptTestFiles/fileSystemProvider_load/dir1/test1.js", "test1.js");
        assertHasScript(scriptBag, bsp, "/scriptTestFiles/fileSystemProvider_load/dir2/test2.js", "test2.js");
        assertHasScript(scriptBag, bsp, "/scriptTestFiles/fileSystemProvider_load/dir2/dir21/test21.js", "test21.js");
        assertNotHasScript(scriptBag, "/scriptTestFiles/fileSystemProvider_load/dir2/dontRead.txt");
        assertNotHasScript(scriptBag, "/scriptTestFiles/fileSystemProvider_load/dontRead.json");
    }

    @Test
    public void file_system_watcher_reacts_to_file_created_updated_and_removed() throws Exception {

        Path baseWatchDirectory = Files.createTempDirectory(null, new FileAttribute[0]);

        final Path targetFile =  Paths.get(baseWatchDirectory.toString() + "/script.js");

        final AtomicBoolean hasBeenCreated = new AtomicBoolean(false);
        final AtomicBoolean hasBeenModified = new AtomicBoolean(false);
        final AtomicBoolean hasBeenDeleted = new AtomicBoolean(false);

        FileAlterationListenerAdaptor handler = new FileAlterationListenerAdaptor() {

            @Override
            public void onFileCreate(File file) {

                hasBeenCreated.set(true);
            }

            @Override
            public void onFileChange(File file) {

                hasBeenModified.set(true);
            }

            @Override
            public void onFileDelete(File file) {

                hasBeenDeleted.set(true);
            }
        };

        FileSystemProvider.startDirectoryWatcher(baseWatchDirectory, handler);

        Thread.sleep(500);

        Files.write(targetFile, MOCK_SCRIPT_BODY.getBytes(), StandardOpenOption.CREATE);

        await().atMost(MAX_WAIT_TIME, TimeUnit.MILLISECONDS).untilTrue(hasBeenCreated);

        assertTrue(hasBeenCreated.get());

        Files.write(targetFile, APPEND_TO_MOCK_BODY.getBytes(), StandardOpenOption.APPEND);

        await().atMost(MAX_WAIT_TIME, TimeUnit.MILLISECONDS).untilTrue(hasBeenModified);

        assertTrue(hasBeenModified.get());

        Files.delete(targetFile);

        await().atMost(MAX_WAIT_TIME, TimeUnit.MILLISECONDS).untilTrue(hasBeenDeleted);

        assertTrue(hasBeenDeleted.get());
    }

    @Test
    public void ensure_script_create_is_called_when_new_script_appears_in_directory() throws Exception {

        Path baseWatchDirectory = Files.createTempDirectory("create_", new FileAttribute[0]);

        FileSystemProvider fsp = new FileSystemProvider(baseWatchDirectory.toString());

        assertEquals(0, fsp.scripts.size());

        Path targetFile =  Paths.get(baseWatchDirectory.toString() + "/script.js");

        Files.write(targetFile, MOCK_SCRIPT_BODY.getBytes(), StandardOpenOption.CREATE);

        Thread.sleep(MAX_WAIT_TIME);

        assertEquals(1, fsp.scripts.size());

        assertEquals(MOCK_SCRIPT_BODY, fsp.scripts.get(targetFile).getBody());
    }

    @Test
    public void ensure_script_update_is_called_when_script_is_updated_in_directory() throws Exception {

        Path baseWatchDirectory = Files.createTempDirectory("update_", new FileAttribute[0]);

        Path targetFile =  Paths.get(baseWatchDirectory.toString() + "/script.js");

        Path createdFile = Files.write(targetFile, MOCK_SCRIPT_BODY.getBytes(), StandardOpenOption.CREATE);

        FileSystemProvider fsp = new FileSystemProvider(baseWatchDirectory.toString());

        assertEquals(1, fsp.scripts.size());

        assertEquals(MOCK_SCRIPT_BODY, fsp.scripts.get(createdFile).getBody());

        Files.write(createdFile, APPEND_TO_MOCK_BODY.getBytes(), StandardOpenOption.APPEND);

        assertTrue(Files.exists(createdFile, LinkOption.NOFOLLOW_LINKS));

        Thread.sleep(MAX_WAIT_TIME);

        assertEquals(1, fsp.scripts.size());

        assertEquals(MOCK_SCRIPT_BODY + APPEND_TO_MOCK_BODY, fsp.scripts.get(createdFile).getBody());
    }

    @Test
    public void ensure_script_remove_is_called_when_script_is_deleted_from_directory() throws Exception {

        Path baseWatchDirectory = Files.createTempDirectory("remove_", new FileAttribute[0]);

        Path targetFile =  Paths.get(baseWatchDirectory.toString() + "/script.js");

        Files.write(targetFile, MOCK_SCRIPT_BODY.getBytes(), StandardOpenOption.CREATE);

        FileSystemProvider fsp = new FileSystemProvider(baseWatchDirectory.toString());

        assertEquals(1, fsp.scripts.size());

        assertEquals(MOCK_SCRIPT_BODY, fsp.scripts.get(targetFile).getBody());

        Files.delete(targetFile);

        Thread.sleep(MAX_WAIT_TIME);

        assertEquals(0, fsp.scripts.size());
    }

}
