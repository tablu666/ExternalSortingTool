import student.TestCase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The purpose of this class is to test
 * the methods of class Operator.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-30
 */
public class OperatorTest extends TestCase {

    private Operator opr;
    private RandomAccessFile file;
    private int heapSize = 65536;
    private MinHeap<Record> minHeap;
    private List<RunInfo> runInfoList;

    @Override
    /**
     * setup
     */
    public void setUp() throws Exception {
        Files.deleteIfExists(Paths.get("opr.bin"));
        String[] input = {"opr.bin", "9"};
        random(input);
        this.file = new RandomAccessFile("opr.bin", "rw");
        this.opr = new Operator(file);

        int recordSize = 16;
        int numOfRecord = heapSize / recordSize;
        Record[] records =
                IOHelper.readRecords(file, 0, numOfRecord);
        this.minHeap = new MinHeap<>(records, numOfRecord, numOfRecord);
        this.runInfoList = new ArrayList<>();
    }

    @Override
    /**
     * setup
     */
    public void tearDown() throws Exception {
        this.file.close();
    }

    /**
     * test Replacement Selection
     */
    public void testReplacementSelection() {
        try {
            RandomAccessFile runFile =
                    opr.replacementSelection(
                            runInfoList,
                            minHeap,
                            heapSize);
            assertTrue(runFile.length() > 0);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * test MultiWayMerge
     */
    public void testMultiWayMerge() {
        try {
            RandomAccessFile runFile =
                    opr.replacementSelection(
                            runInfoList,
                            minHeap,
                            heapSize);
            opr.multiWayMerge(runFile, minHeap.getData(), runInfoList);
            assertTrue(file.getFilePointer() > 0);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a bin file with random values
     *
     * @param args
     *            string array of arguments
     * @throws IOException
     */
    public static void random(String[] args) throws IOException {
        long val;
        double val2;
        Random value = new Random();
        if (args.length != 2) {
            System.out.println("");
        }
        else {
            int filesize = Integer.parseInt(args[1]);
            DataOutputStream file = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(args[0])));
            for (int i = 0; i < filesize; i++) {
                for (int j = 0; j < 512; j++) {
                    val = (long)(value.nextLong());
                    file.writeLong(val);
                    val2 = (double)(value.nextDouble());
                    file.writeDouble(val2);
                }
            }
            file.flush();
            file.close();
        }
    }

}