import student.TestCase;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The purpose of this class is to test
 * the methods of class IOHelper.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-30
 */
public class IOHelperTest extends TestCase {

    private RandomAccessFile file;
    private Record record;

    @Override
    protected void setUp() throws Exception {
        this.file = new RandomAccessFile("io.bin", "rw");
        this.record = generateRecord();
    }

    @Override
    protected void tearDown() throws Exception {
        file.close();
    }

    private Record generateRecord() {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(0, 0);
        bb.putDouble(8, 0);
        bb.rewind();
        return new Record(bb.array());
    }

    public void testWrite() {
        try {
            Record[] records = {record};
            IOHelper.write(file, 1, records);
            Record r = IOHelper.readRecord(file, 0);
            assertEquals(record.toString(), r.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testReadRecords() {
        try {
            Record[] records = IOHelper.readRecords(file, 0, 1);
            assertTrue(records.length == 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testReadRecord() {
        try {
            Record[] records = {record};
            IOHelper.write(file, 1, records);
            Record r = IOHelper.readRecord(file, 0);
            assertEquals(record.toString(), r.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testStandOutput() {
        try {
            Record[] records = {record};
            IOHelper.write(file, 1, records);
            IOHelper.standOutput(file);
            assertTrue(systemOut().getHistory().contains("0.0"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testCopyToFile() {
        try {
            Record[] records = {record};
            IOHelper.write(file, 1, records);
            RandomAccessFile newFile = new RandomAccessFile("io1.bin", "rw");
            IOHelper.copyToFile(file, newFile);
            assertTrue(newFile.length() == 8192);
            newFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testReadMultiBlocks() {
        try {
            List<RunInfo> runInfoList = new ArrayList<>();
            Record[] heapRecords = new Record[1];
            int[] recordsEndIndices = new int[1];
            int i = IOHelper.readMultiBlocks(file, runInfoList, heapRecords, recordsEndIndices);
            assertTrue(i == 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testReadBlock() {
        try {
            List<RunInfo> runInfoList = new ArrayList<>();
            runInfoList.add(new RunInfo(0, 2));
            Record[] heapRecords = new Record[512 * 8];
            int[] recordsEndIndices = new int[8];
            int runInfoIdx = 0;
            IOHelper.readBlock(file, runInfoList, heapRecords, recordsEndIndices, runInfoIdx);
            assertTrue(recordsEndIndices[0] == 511);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testSortAndOutput() {
        try {
            Record[] records = {record};
            MaxHeap<Record> maxHeap = new MaxHeap<>(records);
            IOHelper.sortAndOutput(file, maxHeap);
            assertTrue(file.getFilePointer() == 16);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}