import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
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

    public static void main(String[] args) {
        try {
//            RandomAccessFile file = new RandomAccessFile(args[0], "rw");
//            for (int i = 0; i < file.length(); i += 16) {
//                byte[] record = new byte[16];
//                file.seek(i);
//                file.read(record);
//                ByteBuffer bb = ByteBuffer.wrap(record);
//                Long id = bb.getLong();
//                Double key = bb.getDouble();
//                System.out.println(id + " " + key);
//            }

            String[] input = {"test1.bin", "1000"};
            long l = System.currentTimeMillis();
            GenFile.reversed(input);
            RandomAccessFile file = new RandomAccessFile("test1.bin", "rw");
            System.out.println("original file length = " + file.length());
//            for (int i = 0; i < file.length(); i += 16) {
//                byte[] record = new byte[16];
//                file.seek(i);
//                file.read(record);
//                ByteBuffer bb = ByteBuffer.wrap(record);
//                Long id = bb.getLong();
//                Double key = bb.getDouble();
//                System.out.println(id + " " + key);
//            }

            int heapSize = IOHelper.HEAP_SIZE;
            int recordSize = IOHelper.RECORD_SIZE;
            if (file.length() <= heapSize) {
                heapSize = Math.min(heapSize, (int) file.length());
                int numOfRecord = heapSize / recordSize;
                Record[] records = IOHelper.readRecords(file, 0, numOfRecord);
                MinHeap<Record> minHeap = new MinHeap<>(records);

                // directly output from heap
                IOHelper.sortAndOutput(file, minHeap);
                System.out.println("curr file length = " + file.length());
            } else {
                Operator opr = new Operator(file);

                // replacement selection
                int numOfRecord = heapSize / recordSize;
                MinHeap<Record> minHeap = new MinHeap<>(IOHelper.readRecords(file, 0, numOfRecord));
                List<RunInfo> runInfoList = new ArrayList<>();
                RandomAccessFile runFile = opr.replacementSelection(minHeap, heapSize, runInfoList);
                System.out.println("run file len = " + runFile.length());


//                IOHelper.standOutput(runFile);
//                for (int i = 0; i < runFile.length(); i += 16) {
//                    byte[] record = new byte[16];
//                    runFile.seek(i);
//                    runFile.read(record);
//                    ByteBuffer bb = ByteBuffer.wrap(record);
//                    Long id = bb.getLong();
//                    Double key = bb.getDouble();
//                    System.out.println(id + " " + key);
//                }



                // only 1 run - sort and output
                if (runInfoList.size() == 1) {
                    // directly output from heap
                    IOHelper.sortAndOutput(file, minHeap);
                } else {
                    // 8-way merge
                    opr.multiWayMerge(runFile, minHeap.getData(), runInfoList);
                }
            }

            // standard output
            IOHelper.standOutput(file);
            file.close();
            long l1 = System.currentTimeMillis();
            System.out.println("time = " + (l1 - l) / 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
