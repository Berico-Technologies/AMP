package amp.topology.snapshot.impl;

import org.joda.time.DateTime;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class FilenameSnapshotDescriptorTest {

    @Test
    public void test_getBaseFilename() throws Exception {

        DateTime dtg = DateTime.now();

        String id = "abc123";

        FilenameSnapshotDescriptor descriptor = new FilenameSnapshotDescriptor(id, dtg.getMillis());

        String baseName = descriptor.getBaseFilename();

        assertEquals(baseName, "snapshot.abc123." + dtg.toString(FilenameSnapshotDescriptor.DATETIME_FORMATTER));
    }

    @Test
    public void test_fromFile(){

        DateTime dtg = DateTime.now();

        String friendlyTime = dtg.toString(FilenameSnapshotDescriptor.DATETIME_FORMATTER);

        File file = new File("./snapshot.abc123." + friendlyTime + ".xml");

        FilenameSnapshotDescriptor descriptor = FilenameSnapshotDescriptor.fromFile(file);

        assertEquals("abc123", descriptor.getId());

        assertEquals(friendlyTime, descriptor.getFriendlyTimestamp());

        assertEquals(dtg.getMillis(), descriptor.getTimestamp());
    }

    @Test(expected = IllegalStateException.class)
    public void test_fromFile__throws_exception_if_id_not_present(){

        File file = new File("./snapshot.xml");

        FilenameSnapshotDescriptor descriptor = FilenameSnapshotDescriptor.fromFile(file);

        fail();
    }

    @Test(expected = IllegalStateException.class)
    public void test_fromFile__throws_exception_if_timestamp_not_present(){

        File file = new File("./snapshot.abc123.xml");

        FilenameSnapshotDescriptor descriptor = FilenameSnapshotDescriptor.fromFile(file);

        fail();
    }
}
