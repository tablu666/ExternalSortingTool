import student.TestCase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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
            String[] input = {"test.bin", "1"};
            GenFile.reversed(input);
            RandomAccessFile file = new RandomAccessFile("test.bin", "rw");
            Externalsort.main(input);
            assertTrue(systemOut().getHistory().contains("0.0"));
        } catch (IOException e) {
            assertTrue(e instanceof FileNotFoundException);
        }
    }
}