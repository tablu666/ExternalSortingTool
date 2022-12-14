import student.TestCase;

import java.nio.ByteBuffer;

/**
 * The purpose of this class is to test
 * the methods of class Record.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-30
 */
public class RecordTest extends TestCase {

    private Record record;
    private byte[] data;

    @Override
    /**
     * setUp
     */
    protected void setUp() throws Exception {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(0, 0);
        bb.putDouble(8, 0);
        bb.rewind();
        this.data = bb.array();
        this.record = new Record(data);
    }

    /**
     * test getId
     */
    public void testGetId() {
        assertEquals(0, record.getId());
    }

    /**
     * test getKey
     */
    public void testGetKey() {
        assertEquals(0, Double.compare(0.0, record.getKey()));
    }

    /**
     * test getData
     */
    public void testGetData() {
        assertEquals(data, record.getData());
    }

    /**
     * test compareTo
     */
    public void testCompareTo() {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(0, 0);
        bb.putDouble(8, 1);
        bb.rewind();
        Record r = new Record(bb.array());
        assertTrue(r.compareTo(record) > 0);
    }

    /**
     * test toString
     */
    public void testToString() {
        assertEquals("0 0.0", record.toString());
    }
}