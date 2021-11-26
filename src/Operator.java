import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class is responsible for two phases operations.
 * These are replacement selection and eight-way merge.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class Operator {

    public static void replacementSelection(MinHeap minHeap, byte[] input,
                                            byte[] output, FileOutputStream fos) throws IOException {

        fos.write(output);
    }

    public static void replacementSelection(MinHeap minHeap, byte[] output, FileOutputStream fos) throws IOException {
        fos.write(output);
    }
}
