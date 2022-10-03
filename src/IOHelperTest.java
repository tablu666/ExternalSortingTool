import student.TestCase;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    /**
     * file
     */
    private RandomAccessFile file;

    /**
     * record
     */
    private Record record;

    @Override
    /**
     * setup
     */
    protected void setUp() throws Exception {
        Files.deleteIfExists(Paths.get("io.bin"));
        this.file = new RandomAccessFile("io.bin", "rw");
        this.record = generateRecord();
    }

    @Override
    /**
     * tearDown
     */
    protected void tearDown() throws Exception {
        file.close();
    }

    /**
     * generateRecord
     * @return Record
     */
    private Record generateRecord() {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(0, 0);
        bb.putDouble(8, 0);
        bb.rewind();
        return new Record(bb.array());
    }

    /**
     * testWrite
     */
    public void testWrite() {
        try {
            Record[] records = {record};
            IOHelper.write(file, 1, records);
            Record r = IOHelper.readRecord(file, 0);
            assertEquals(record.toString(), r.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * testReadRecords
     */
    public void testReadRecords() {
        try {
            Record[] records = IOHelper.readRecords(
                    file, 0, 1);
            assertEquals(1, records.length);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * testReadRecord
     */
    public void testReadRecord() {
        try {
            Record[] records = {record};
            IOHelper.write(file, 1, records);
            Record r = IOHelper.readRecord(file, 0);
            assertEquals(record.toString(), r.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * testStandOutput
     */
    public void testStandOutput() {
        try {
            Record[] records = {record};
            IOHelper.write(file, 1, records);
            IOHelper.standOutput(file);
            assertTrue(systemOut().getHistory().contains("0.0"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * testCopyToFile
     */
    public void testCopyToFile() {
        try {
            Record[] records = {record};
            IOHelper.write(file, 1, records);
            RandomAccessFile newFile =
                    new RandomAccessFile(
                    "io1.bin", "rw");
            IOHelper.copyToFile(file, newFile);
            assertEquals(8192, newFile.length());
            newFile.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * testReadMultiBlocks
     */
    public void testReadMultiBlocks() {
        try {
            List<RunInfo> runInfoList = new ArrayList<>();
            Record[] heapRecords = new Record[1];
            int[] recordsEndIndices = new int[1];
            int i = IOHelper.readMultiBlocks(
                    file, runInfoList,
                    heapRecords,
                    recordsEndIndices);
            assertEquals(0, i);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * testReadBlock
     */
    public void testReadBlock() {
        try {
            List<RunInfo> runInfoList = new ArrayList<>();
            runInfoList.add(new RunInfo(0, 2));
            Record[] heapRecords = new Record[512 * 8];
            int[] recordsEndIndices = new int[8];
            int runInfoIdx = 0;
            IOHelper.readBlock(file,
                    runInfoList,
                    heapRecords,
                    recordsEndIndices,
                    runInfoIdx);
            assertEquals(511, recordsEndIndices[0]);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * testSortAndOutput
     */
    public void testSortAndOutput() {
        try {
            Record[] records = {record};
            MaxHeap<Record> maxHeap = new MaxHeap<>(records);
            IOHelper.sortAndOutput(file, maxHeap);
            assertEquals(16, file.getFilePointer());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}