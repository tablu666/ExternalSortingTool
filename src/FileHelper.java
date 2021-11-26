import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class provides useful binary data
 * read and write methods.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class FileHelper {

    //Reading a block of data from the file
    public static int readBlock(RandomAccessFile file, long pos, byte[] block) throws IOException {
        file.seek(pos);
        int numBytes = file.read(block, 0, block.length);
        System.out.println("Number of bytes read:" + numBytes);
        return numBytes;
    }
}
