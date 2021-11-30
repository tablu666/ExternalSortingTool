import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * This class provides useful binary data
 * read and write methods.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class IOHelper {

    public static final int HEAP_SIZE = 65536;
    public static final int BLOCK_SIZE = 8192;
    public static final int RECORD_SIZE = 16;
    public static final String RUN_FILE = "RunFile.bin";
    public static final String MERGE_FILE = "MergeFile.bin";

    // Write num of records from block buffer
    public static void write(RandomAccessFile file, int numOfRecord, Record[] block) throws IOException {
        for (int i = 0; i < numOfRecord; i++) {
            file.write(block[i].getData());
        }
    }

    // Reads data from the file and fill the input records
    public static Record[] readRecords(RandomAccessFile file, long filePos, int numOfRecord) throws IOException {
        Record[] records = new Record[numOfRecord];
        for (int i = 0; i < numOfRecord; i++, filePos += RECORD_SIZE) {
            records[i] = readRecord(file, filePos);
        }
        return records;
    }

    public static Record readRecord(RandomAccessFile file, long filePos) throws IOException {
        byte[] data = new byte[RECORD_SIZE];
        file.seek(filePos);
        file.read(data);
        return new Record(data);
    }

    public static void standOutput(RandomAccessFile file) throws IOException {
        // standard output
        for (int filePos = 0, cnt = 1; filePos < file.length(); filePos += BLOCK_SIZE, cnt++) {
            Record record = readRecord(file, filePos);
            System.out.print(record);
            if (cnt % 5 == 0) {
                System.out.println();
            } else if (filePos < file.length() - BLOCK_SIZE) {
                System.out.print(" ");
            }
        }
    }

    public static void copyToFile(RandomAccessFile mergeFile, RandomAccessFile file) throws IOException {
        mergeFile.seek(0);
        file.seek(0);

        // write merge data to file repeatedly - 1 block a time
        while (mergeFile.getFilePointer() != mergeFile.length()) {
            byte[] data = new byte[BLOCK_SIZE];
            mergeFile.read(data);
            file.write(data);
        }
    }

    // read full block of records from current run - i
    public static int readBlockOfRecords(RandomAccessFile runFile, List<RunInfo> runInfoList,
                                         Record[] heapRecords, int[] runEndIndices, int runInfoIdx) throws IOException {
        while (runInfoIdx < runInfoList.size() && runInfoIdx < 8) {
            readOneBlockOfRecords(runFile, runInfoList, heapRecords, runEndIndices, runInfoIdx);
            runInfoIdx++;
        }

        return runInfoIdx;
    }

    public static void readOneBlockOfRecords(RandomAccessFile runFile, List<RunInfo> runInfoList,
                                             Record[] heapRecords, int[] runEndIndices, int runInfoIdx) throws IOException {
        RunInfo runInfo = runInfoList.get(runInfoIdx);
        int numOfRecord = BLOCK_SIZE / RECORD_SIZE;

        // load 512 records to heap memory
        for (int i = runInfoIdx * numOfRecord; i < (runInfoIdx + 1) * numOfRecord; i++) {
            // runInfo read done
            if (runInfo.getLength() == 0) break;

            long filePos = runInfo.getStart();
            heapRecords[i] = readRecord(runFile, filePos);

            // update runInfo
            runInfo.setStart(runInfo.getStart() + RECORD_SIZE);
            runInfo.setLength(runInfo.getLength() - RECORD_SIZE);

            // update the end index of current block within heap memory
            runEndIndices[runInfoIdx] = i;
        }
    }

    public static void sortAndOutput(RandomAccessFile file, MinHeap<Record> minHeap) throws IOException {
        // directly output from heap
        minHeap.sort();
        Record[] records = minHeap.getData();
        file.seek(0);

        for (Record record : records) {
//            System.out.println("out");
            byte[] data = record.getData();
            file.write(data);
        }
    }

}
