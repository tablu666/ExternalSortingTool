import student.TestCase;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
    private String runFileName;

    @Override
    public void setUp() throws Exception {
        String[] input = {"opr.bin", "9"};
        reversed(input);
        this.file = new RandomAccessFile("opr.bin", "rw");
        this.opr = new Operator(file);
        this.runFileName = IOHelper.RUN_FILE;
    }

    @Override
    public void tearDown() throws Exception {
        this.file.close();
    }

    public void testReplacementSelection() {
        try {
            List<RunInfo> runInfoList = opr.replacementSelection();
            assertTrue(runInfoList.size() > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testMultiWayMerge() {
        try {
            List<RunInfo> runInfoList = opr.replacementSelection();
            RandomAccessFile runFile = new RandomAccessFile(runFileName, "rw");
            opr.multiWayMerge(runFile, runInfoList);
            assertTrue(file.getFilePointer() > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reversed(String[] args) throws IOException {
        long val;
        double val2;
        int filesize = Integer.parseInt(args[1]); // Size of file in blocks
        DataOutputStream file = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(args[0])));
        // For sorted.

        for (int i = filesize - 1; i >= 0; i--) {
            for (int j = 512 - 1; j >= 0; j--) {
                val = (long)(i * 512 + j);
                file.writeLong(val);
                val2 = (double)(i * 512 + j);
                file.writeDouble(val2);
            }
        }
        file.flush();
        file.close();
    }
}