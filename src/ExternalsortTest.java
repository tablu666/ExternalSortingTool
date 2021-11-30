import student.TestCase;

import java.io.*;

/**
 * The purpose of this class is to test
 * the methods of class Externalsort.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-30
 */
public class ExternalsortTest extends TestCase {

    public void testMain() {
        try {
            String[] input = {"extest.bin", "1"};
            reversed(input);
            RandomAccessFile file = new RandomAccessFile("extest.bin", "rw");
            Externalsort.main(input);
            assertTrue(systemOut().getHistory().contains("0.0"));
            file.close();
        } catch (IOException e) {
            assertTrue(e instanceof FileNotFoundException);
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