import java.io.IOException;
import java.io.RandomAccessFile;
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

    public static void main(String[] args) {
        try {
            RandomAccessFile file = new RandomAccessFile(args[0], "rw");
//            String[] input = {"test.bin", "500"};
//            GenFile.reversed(input);
//            RandomAccessFile file = new RandomAccessFile("test.bin", "rw");
//            System.out.println("original file length = " + file.length());

            int heapSize = IOHelper.HEAP_SIZE;
            int recordSize = IOHelper.RECORD_SIZE;
            if (file.length() <= heapSize) {
                heapSize = Math.min(heapSize, (int) file.length());
                int numOfRecord = heapSize / recordSize;
                Record[] records = IOHelper.readRecords(file, 0, numOfRecord);
                MaxHeap<Record> maxHeap = new MaxHeap<>(records);

                // directly output from heap
                IOHelper.sortAndWrite(file, maxHeap);
            } else {
                Operator opr = new Operator(file);

                // replacement selection
                List<RunInfo> runInfoList = opr.replacementSelection();
                // get run file
                RandomAccessFile runFile = new RandomAccessFile(IOHelper.RUN_FILE, "rw");
                // 8-way merge
                opr.multiWayMerge(runFile, runInfoList);
                // get merge file
                RandomAccessFile mergeFile = new RandomAccessFile(IOHelper.MERGE_FILE, "rw");
                // copy to input file
                IOHelper.copyToFile(mergeFile, file);

                // close stream
                runFile.close();
                mergeFile.close();
            }

            // standard output
            IOHelper.standOutput(file);
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
