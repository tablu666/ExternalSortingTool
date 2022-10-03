import student.TestCase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The purpose of this class is to test
 * the methods of class Externalsort.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-30
 */
public class ExternalsortTest extends TestCase {

    /**
     * the function to test main()
     */
    public void testMain() {
        try {
            Files.deleteIfExists(Paths.get("extest.bin"));
            Files.deleteIfExists(Paths.get("extest1.bin"));
            String[] input = {"extest.bin", "8"};
            String[] input2 = {"extest1.bin", "24"};
            reversed(input);
            reversed(input2);
            RandomAccessFile file = new RandomAccessFile("extest.bin", "rw");
            RandomAccessFile file1 = new RandomAccessFile("extest1.bin", "rw");
            Externalsort.main(input);
            assertTrue(systemOut().getHistory().contains("0.0"));
            Externalsort.main(input2);
            assertTrue(file1.length() > 0);
            file.close();
            file1.close();
        }
        catch (IOException e) {
            assertTrue(e instanceof FileNotFoundException);
        }
    }

    /**
     *
     * @param args string
     * @throws IOException
     */
    private void reversed(String[] args) throws IOException {
        long val;
        double val2;
        int filesize = Integer.parseInt(args[1]); // Size of file in blocks
        DataOutputStream file = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(args[0])));
        // For sorted.

        for (int i = filesize - 1; i >= 0; i--) {
            for (int j = 512 - 1; j >= 0; j--) {
                val = (long) (i * 512 + j);
                file.writeLong(val);
                val2 = (double) (i * 512 + j);
                file.writeDouble(val2);
            }
        }
        file.flush();
        file.close();
    }
}