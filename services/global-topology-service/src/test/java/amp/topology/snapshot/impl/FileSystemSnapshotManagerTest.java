package amp.topology.snapshot.impl;

import amp.topology.global.impl.BasicTopic;
import amp.topology.global.TopicRegistry;
import amp.topology.snapshot.Snapshot;
import amp.topology.snapshot.SnapshotDescriptor;
import amp.topology.snapshot.exceptions.SnapshotDoesNotExistException;
import amp.topology.snapshot.exceptions.TopicChangeExceptionRollup;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class FileSystemSnapshotManagerTest {

    @Test
    public void test_validate__if_directory_not_exists_its_created(){

        TopicRegistry topicRegistry = mock(TopicRegistry.class);

        SnapshotSerializer serializer = mock(SnapshotSerializer.class);

        File basedir = createMockFile(false, true, true, true);

        new FileSystemSnapshotManager(topicRegistry, serializer, basedir);

        verify(basedir).mkdirs();
    }

    @Test(expected = IllegalStateException.class)
    public void test_validate__fail_if_snapshot_dir_is_not_a_directory(){

        TopicRegistry topicRegistry = mock(TopicRegistry.class);

        SnapshotSerializer serializer = mock(SnapshotSerializer.class);

        File basedir = createMockFile(true, false, true, true);

        new FileSystemSnapshotManager(topicRegistry, serializer, basedir);
    }

    @Test(expected = IllegalStateException.class)
    public void test_validate__fail_if_snapshot_dir_cant_be_read(){

        TopicRegistry topicRegistry = mock(TopicRegistry.class);

        SnapshotSerializer serializer = mock(SnapshotSerializer.class);

        File basedir = createMockFile(true, true, false, true);

        new FileSystemSnapshotManager(topicRegistry, serializer, basedir);
    }

    @Test(expected = IllegalStateException.class)
    public void test_validate__fail_if_snapshot_dir_cant_be_write_to(){

        TopicRegistry topicRegistry = mock(TopicRegistry.class);

        SnapshotSerializer serializer = mock(SnapshotSerializer.class);

        File basedir = createMockFile(true, true, true, false);

        new FileSystemSnapshotManager(topicRegistry, serializer, basedir);
    }

    @Test(expected = NullPointerException.class)
    public void test_validate__fail_if_topic_registry_is_null(){

        TopicRegistry topicRegistry = null;

        SnapshotSerializer serializer = mock(SnapshotSerializer.class);

        File basedir = createMockFile(true, true, true, true);

        new FileSystemSnapshotManager(topicRegistry, serializer, basedir);
    }

    @Test(expected = NullPointerException.class)
    public void test_validate__fail_if_snapshot_dir_is_null(){

        TopicRegistry topicRegistry = mock(TopicRegistry.class);

        SnapshotSerializer serializer = mock(SnapshotSerializer.class);

        File basedir = null;

        new FileSystemSnapshotManager(topicRegistry, serializer, basedir);
    }

    @Test(expected = NullPointerException.class)
    public void test_validate__fail_if_serializer_is_null(){

        TopicRegistry topicRegistry = mock(TopicRegistry.class);

        SnapshotSerializer serializer = null;

        File basedir = createMockFile(true, true, true, true);

        new FileSystemSnapshotManager(topicRegistry, serializer, basedir);
    }

    @Test
    public void test_locateById(){

        amp.topology.global.Topic topicConfiguration1 = mock(BasicTopic.class);

        when(topicConfiguration1.getTopicId()).thenReturn("amp.test.Event1");

        amp.topology.global.Topic topicConfiguration2 = mock(BasicTopic.class);

        when(topicConfiguration2.getTopicId()).thenReturn("amp.test.Event2");

        amp.topology.global.Topic topicConfiguration3 = mock(BasicTopic.class);

        when(topicConfiguration3.getTopicId()).thenReturn("amp.test.Event3");

        Snapshot snapshot = mock(Snapshot.class);

        when(snapshot.getTopics())
                .thenReturn(Arrays.asList(topicConfiguration1, topicConfiguration2, topicConfiguration3));

        assertNull(FileSystemSnapshotManager.locateById(snapshot, "nonexistent"));

        assertEquals(topicConfiguration1, FileSystemSnapshotManager.locateById(snapshot, "amp.test.Event1"));
        assertEquals(topicConfiguration2, FileSystemSnapshotManager.locateById(snapshot, "amp.test.Event2"));
        assertEquals(topicConfiguration3, FileSystemSnapshotManager.locateById(snapshot, "amp.test.Event3"));
    }

    @Test
    public void test_getLatestFile(){

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        File latestFile = manager.getLatestFile();

        assertEquals("/temp/snapshots/snapshot.latest.xml", latestFile.getAbsolutePath());
    }

    @Test
    public void test_getInstanceFile(){

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        Snapshot snapshot = mock(Snapshot.class);

        DateTime dtg = DateTime.now();

        when(snapshot.getId()).thenReturn("abc123");

        when(snapshot.getTimestamp()).thenReturn(dtg.getMillis());

        File instanceFile = manager.getInstanceFile(snapshot);

        String EXPECTED = "/temp/snapshots/snapshot.abc123."
                + dtg.toString(FilenameSnapshotDescriptor.DATETIME_FORMATTER)
                + ".xml";

        assertEquals(EXPECTED, instanceFile.getAbsolutePath());
    }

    @Test
    public void test_dehydrateSnapshot__snapshot_successfully_returned() throws Exception {

        // The forward slash in front of the file means "relative to the working directory".
        URL fakeFile = this.getClass().getResource("/data/snapshots/snapshot.fake.20140102080012123.xml");

        File snapshotFile = new File(fakeFile.toURI());

        Snapshot EXPECTED = mock(Snapshot.class);

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        when(manager.serializer.deserialize(anyString())).thenReturn(EXPECTED);

        Snapshot ACTUAL = manager.dehydrateSnapshot(snapshotFile);

        verify(manager.serializer).deserialize(anyString());

        assertEquals(EXPECTED, ACTUAL);
    }

    @Test(expected = SnapshotFileNotExistException.class)
    public void test_dehydrateSnapshot__exception_thrown_if_file_not_exist() throws Exception {

        File snapshotFile = mock(File.class);

        when(snapshotFile.exists()).thenReturn(false);

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        manager.dehydrateSnapshot(snapshotFile);

        fail();
    }

    @Test
    public void test_list() throws Exception {

        FileSystemSnapshotManager manager = createInstanceWithMocks(getTestListSnapshotDir());

        Collection<SnapshotDescriptor> descriptors = manager.list();

        assertEquals(3, descriptors.size());

        assertHasSnapshotDescriptor(descriptors, "abc123", 1388958311345l);
        assertHasSnapshotDescriptor(descriptors, "def456", 1359101554500l);
        assertHasSnapshotDescriptor(descriptors, "ghi789", 1359100752678l);
    }

    @Test
    public void test_locateInstanceFile() throws Exception {

        File baseDir = getTestListSnapshotDir();

        FileSystemSnapshotManager manager = createInstanceWithMocks(baseDir);

        File SHOULD_BE_NULL = manager.locateInstanceFile("shouldBeNull");

        assertNull(SHOULD_BE_NULL);

        File ACTUAL = manager.locateInstanceFile("def456");

        assertNotNull(ACTUAL);

        String EXPECTED = baseDir.getAbsolutePath() + File.separator + "snapshot.def456.201312250012345.xml";

        assertEquals(EXPECTED, ACTUAL.getAbsolutePath());
    }

    @Test
    public void test_lastPersisted__latest_should_be_null_and_return_default_value() throws Exception {

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        long lastPersisted = manager.lastPersisted();

        assertTrue(lastPersisted < 0);
    }

    @Test
    public void test_lastPersisted__latest_not_null_should_return_value_from_snapshot() throws Exception {

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        Snapshot latest = mock(Snapshot.class);

        long EXPECTED = 1234567890l;

        when(latest.getTimestamp()).thenReturn(EXPECTED);

        manager.currentSnapshot = latest;

        long lastPersisted = manager.lastPersisted();

        verify(latest).getTimestamp();

        assertEquals(EXPECTED, lastPersisted);
    }

    @Test
    public void test_latest__returns_null_if_there_is_no_latest_snapshot(){

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        assertNull(manager.latest());
    }

    @Test
    public void test_latest__looks_to_file_system_if_currentSnapshot_not_set() throws Exception {

        FileSystemSnapshotManager manager = createInstanceWithMocks(getTestLatestSnapshotDir());

        Snapshot EXPECTED = mock(Snapshot.class);

        when(manager.serializer.deserialize(anyString())).thenReturn(EXPECTED);

        Snapshot ACTUAL = manager.latest();

        verify(manager.serializer).deserialize(anyString());

        assertEquals(EXPECTED, ACTUAL);
    }

    @Test
    public void test_latest__references_latest_if_set() throws Exception {

        FileSystemSnapshotManager manager = createInstanceWithMocks(getTestLatestSnapshotDir());

        Snapshot EXPECTED = mock(Snapshot.class);

        manager.currentSnapshot = EXPECTED;

        Snapshot ACTUAL = manager.latest();

        verifyZeroInteractions(manager.serializer);

        assertEquals(EXPECTED, ACTUAL);
    }

    @Test
    public void test_get__retrieve_snapshot_from_filesystem() throws Exception {

        File baseDir = getTestListSnapshotDir();

        FileSystemSnapshotManager manager = createInstanceWithMocks(baseDir);

        Snapshot EXPECTED = mock(Snapshot.class);

        when(manager.serializer.deserialize(anyString())).thenReturn(EXPECTED);

        Snapshot ACTUAL = manager.get("abc123");

        assertEquals(EXPECTED, ACTUAL);
    }

    @Test(expected = SnapshotDoesNotExistException.class)
    public void test_get__nonexistent_snapshot_throws_exception() throws Exception {

        File baseDir = getTestListSnapshotDir();

        FileSystemSnapshotManager manager = createInstanceWithMocks(baseDir);

        manager.get("nonexistent");
    }

    @Test
    public void test_synchronizeConfiguration__does_not_update_if_state_is_equivalent() throws Exception {

        BasicTopic TOPIC = mock(BasicTopic.class);

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        // Gross, but easiest way to ensure a mock is equal since we can't
        // stub Object.equals()
        manager.synchronizeTopic(TOPIC, TOPIC, false);

        verifyZeroInteractions(manager.topicRegistry);
    }

    @Test
    public void test_synchronizeConfiguration__replaces_existing_state_if_not_equivalent() throws Exception {

        amp.topology.global.Topic TOPICA_CURRENT = mock(BasicTopic.class);

        when(TOPICA_CURRENT.getTopicId()).thenReturn("abc123");

        BasicTopic TOPICA_MUTATION = mock(BasicTopic.class);

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        manager.synchronizeTopic(TOPICA_CURRENT, TOPICA_MUTATION, false);

        verify(manager.topicRegistry).unregister("abc123");

        verify(manager.topicRegistry).register(TOPICA_MUTATION);
    }

    @Test
    public void test_synchronizeConfiguration__remove_if_no_mutation_and_dont_keep_entries() throws Exception {

        amp.topology.global.Topic TOPICA_CURRENT = mock(BasicTopic.class);

        when(TOPICA_CURRENT.getTopicId()).thenReturn("abc123");

        BasicTopic TOPICA_MUTATION = null;

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        manager.synchronizeTopic(TOPICA_CURRENT, TOPICA_MUTATION, true);

        verify(manager.topicRegistry).unregister("abc123");
    }

    @Test
    public void test_synchronizeConfiguration__do_not_remove_if_no_mutation_and_keep_entries() throws Exception {

        amp.topology.global.Topic TOPICA_CURRENT = mock(BasicTopic.class);

        BasicTopic TOPICA_MUTATION = null;

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        manager.synchronizeTopic(TOPICA_CURRENT, TOPICA_MUTATION, false);

        verifyZeroInteractions(manager.topicRegistry);
    }

    @Test
    public void test_synchronizeTopology__successfully_completes_without_errors() throws Exception {

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        amp.topology.global.Topic TC1 = mock(BasicTopic.class);

        when(TC1.getTopicId()).thenReturn("amp.test.Event1");

        amp.topology.global.Topic TC2 = mock(BasicTopic.class);

        when(TC2.getTopicId()).thenReturn("amp.test.Event2");

        amp.topology.global.Topic TC3 = mock(BasicTopic.class);

        when(TC3.getTopicId()).thenReturn("amp.test.Event3");

        Collection<amp.topology.global.Topic> TOPICS = Arrays.asList(TC1, TC2, TC3);

        when(manager.topicRegistry.entries()).thenReturn(TOPICS);

        Snapshot mockSnapshot = mock(Snapshot.class);

        when(mockSnapshot.getTopics()).thenReturn(TOPICS);

        manager.synchronizeTopology(mockSnapshot, false);
    }

    @Test
    public void test_synchronizeTopology__throws_rollup_when_errors_encountered_synchronizing_state() throws Exception {

        FileSystemSnapshotManager manager = createInstanceWithMocks();

        amp.topology.global.Topic TC1 = mock(BasicTopic.class);

        when(TC1.getTopicId()).thenReturn("amp.test.Event1");

        amp.topology.global.Topic TC2_I1 = mock(BasicTopic.class);

        when(TC2_I1.getTopicId()).thenReturn("amp.test.Event2");

        BasicTopic TC2_I2 = mock(BasicTopic.class);

        when(TC2_I2.getTopicId()).thenReturn("amp.test.Event2");

        amp.topology.global.Topic TC3 = mock(BasicTopic.class);

        when(TC3.getTopicId()).thenReturn("amp.test.Event3");

        when(manager.topicRegistry.entries()).thenReturn(Arrays.asList(TC1, TC2_I1, TC3));

        Snapshot mockSnapshot = mock(Snapshot.class);

        when(mockSnapshot.getTopics()).thenReturn(Arrays.asList(TC1, TC2_I2));

        Exception EXPECTED_EXCEPTION_1 = new Exception();

        doThrow(EXPECTED_EXCEPTION_1).when(manager.topicRegistry).register(TC2_I2);

        Exception EXPECTED_EXCEPTION_2 = new Exception();

        doThrow(EXPECTED_EXCEPTION_2).when(manager.topicRegistry).unregister("amp.test.Event3");

        try {

            manager.synchronizeTopology(mockSnapshot, true);

            fail("A Rollup Exception should have been thrown.");

        } catch (TopicChangeExceptionRollup rollup){

            assertTrue(rollup.hasErrors());
            assertEquals(2, rollup.getRollup().size());
            assertRollupContainsException(rollup, EXPECTED_EXCEPTION_1);
            assertRollupContainsException(rollup, EXPECTED_EXCEPTION_2);
        }
    }

    @Test
    public void test_export() throws Exception {

        File baseDir = new File("target/test-snapshots/" + UUID.randomUUID().toString());

        FileSystemSnapshotManager manager = createInstanceWithMocks(baseDir);

        when(manager.serializer.serialize(any(Snapshot.class))).thenAnswer(new SerializeSnapshotAnswer());

        amp.topology.global.Topic TC1 = mock(BasicTopic.class);

        when(TC1.getTopicId()).thenReturn("amp.test.Event1");

        amp.topology.global.Topic TC2_I1 = mock(BasicTopic.class);

        when(TC2_I1.getTopicId()).thenReturn("amp.test.Event2");

        amp.topology.global.Topic TC2_I2 = mock(BasicTopic.class);

        when(TC2_I2.getTopicId()).thenReturn("amp.test.Event2");

        amp.topology.global.Topic TC3 = mock(BasicTopic.class);

        when(TC3.getTopicId()).thenReturn("amp.test.Event3");

        when(manager.topicRegistry.entries()).thenReturn(Arrays.asList(TC1, TC2_I1, TC3));

        Snapshot snapshot = manager.export("Some description");

        File latest = manager.getLatestFile();

        assertTrue(latest.exists());

        File instance = manager.getInstanceFile(snapshot);

        assertTrue(instance.exists());
    }

    private void assertRollupContainsException(TopicChangeExceptionRollup rollup, Exception target){

        for (TopicChangeExceptionRollup.TopicChangeException exception : rollup.getRollup()){

            if (exception.getCause().equals(target)) return;
        }

        fail("Rollup does not contain exception: " + target);
    }

    private void assertHasSnapshotDescriptor(Collection<SnapshotDescriptor> descriptors, String id, long timestamp){

        boolean foundDescriptor = false;

        for (SnapshotDescriptor descriptor : descriptors){

            if (descriptor.getId().equals(id)){

                assertEquals(timestamp, descriptor.getTimestamp());

                foundDescriptor = true;
            }
        }

        assertTrue(foundDescriptor);
    }

    private static File createMockFile(boolean exists, boolean isDirectory, boolean canRead, boolean canWrite){

        File mockFile = mock(File.class);

        when(mockFile.exists()).thenReturn(exists);

        when(mockFile.isDirectory()).thenReturn(isDirectory);

        when(mockFile.canRead()).thenReturn(canRead);

        when(mockFile.canWrite()).thenReturn(canWrite);

        return mockFile;
    }

    private static FileSystemSnapshotManager createInstanceWithMocks(){

        File baseDir = createMockFile(true, true, true, true);

        when(baseDir.getAbsolutePath()).thenReturn("/temp/snapshots");

        return createInstanceWithMocks(baseDir);
    }

    private static FileSystemSnapshotManager createInstanceWithMocks(File baseDir){

        SnapshotSerializer serializer = mock(SnapshotSerializer.class);

        when(serializer.serializedFileExtension()).thenReturn("xml");

        TopicRegistry topicRegistry = mock(TopicRegistry.class);

        return new FileSystemSnapshotManager(topicRegistry, serializer, baseDir);
    }

    private File getTestListSnapshotDir() throws Exception {

        URL baseDirUrl = this.getClass().getResource("/data/snapshots/list");

        return new File(baseDirUrl.toURI());
    }

    private File getTestLatestSnapshotDir() throws Exception {

        URL baseDirUrl = this.getClass().getResource("/data/snapshots/latest");

        return new File(baseDirUrl.toURI());
    }

    private static class SerializeSnapshotAnswer implements Answer<String> {

        @Override
        public String answer(InvocationOnMock invocationOnMock) throws Throwable {

            Snapshot snapshot = (Snapshot)invocationOnMock.getArguments()[0];

            StringBuilder sb = new StringBuilder();

            sb.append("<snapshot id='").append(snapshot.getId()).append("'>\n");
            sb.append("\t<description>").append(snapshot.getDescription()).append("</description>\n");
            sb.append("\t<timestamp>").append(snapshot.getTimestamp()).append("</timestamp>\n");

            sb.append("\t<topics>\n");

            for(amp.topology.global.Topic topicConfiguration : snapshot.getTopics()){

                sb.append("\t\t<topic id='").append(topicConfiguration.getTopicId()).append("'>\n");
                sb.append("\t\t\t").append(topicConfiguration.toString()).append("\n");
                sb.append("\t\t</topic>\n");
            }

            sb.append("</snapshot>");

            return sb.toString();
        }
    }
}
