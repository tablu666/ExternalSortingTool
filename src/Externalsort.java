import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * The class containing the main method, the entry point of the application. It
 * will take a binary file argument which include the data to be read
 * and creates the appropriate data structures to sort the data
 * and outputs the correct results to the console as specified in the file.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */

// On my honor:

//

// - I have not used source code obtained from another student,

// or any other unauthorized source, either modified or

// unmodified.

//

// - All source code and documentation used in my program is

// either my original work, or was derived by me from the

// source code published in the textbook for this course.

//

// - I have not discussed coding details about this project with

// anyone other than my partner (in the case of a joint

// submission), instructor, ACM/UPE tutors or the TAs assigned

// to this course. I understand that I may discuss the concepts

// of this program with other students, and that another student

// may help me debug my program so long as neither of us writes

// anything during the discussion or modifies any computer file

// during the discussion. I have violated neither the spirit nor

// letter of this restriction.
public class Externalsort {

    public static final int BLOCK_SIZE = 8192;
    public static final int RECORD_SIZE = 16;

    public static void main(String[] args) {
        try {
            RandomAccessFile file = new RandomAccessFile(args[0], "rw");

            // read 8 blocks to build min heap
            byte[] heapData = new byte[8 * BLOCK_SIZE];
            int numOfBytes = FileHelper.readBlock(file, 0, heapData);
            Record[] records = new Record[numOfBytes / RECORD_SIZE];
            for (int k = 0, i = 0; i < numOfBytes; k++, i += RECORD_SIZE) {
                byte[] temp = new byte[RECORD_SIZE];
                System.arraycopy(heapData, i, temp, 0, RECORD_SIZE);
                records[k] = new Record(temp);
            }
            MinHeap<Record> minHeap = new MinHeap<>(records);

            // replacement selection
            byte[] inputBuffer = new byte[BLOCK_SIZE];
            byte[] outputBuffer = new byte[BLOCK_SIZE];
            FileOutputStream fos = new FileOutputStream("RunFile.bin");
            List<RunInfo> runInfoList = new ArrayList<>();
            int outPos = Operator.replacementSelection(file, minHeap, inputBuffer, outputBuffer,
                    runInfoList, fos, numOfBytes);
            Operator.replacementSelection(minHeap, outputBuffer, runInfoList, fos, outPos);
            fos.close();
            file.close();

            // eight way merge
            String runFileName = Operator.multiWayMerge(runInfoList, outputBuffer);

            // copy to file
            FileChannel src = new FileInputStream(runFileName).getChannel();
            FileChannel dest = new FileOutputStream(args[0]).getChannel();
            dest.transferFrom(src, 0, src.size());
            src.close();
            dest.close();

            // standard output
            file = new RandomAccessFile(args[0], "rw");
            for (int i = 0, cnt = 0; i < file.length(); i += BLOCK_SIZE, cnt++) {
                FileHelper.readBlock(file, i, inputBuffer);
                byte[] temp = new byte[RECORD_SIZE];
                System.arraycopy(inputBuffer, 0, temp, 0, RECORD_SIZE);
                if (cnt % 5 == 0) System.out.println();
                System.out.print(new Record(temp));
            }
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
