import java.io.IOException;
import java.io.RandomAccessFile;

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

    // Reading a block of data from the file
    public static void readBlock(RandomAccessFile file, long pos, byte[] block) throws IOException {
        file.seek(pos);
        file.read(block, 0, block.length);
    }

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
        file.read(data, 0, RECORD_SIZE);
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
    public static void readBlockOfRecords(RandomAccessFile runFile, RunInfo runInfo, int recordIdx, int i,
                                          Record[] heapRecords, long[] runFileIndices) throws IOException {
        long runEnd = runInfo.getStart() + runInfo.getLength();
        int numOfRecord = BLOCK_SIZE / RECORD_SIZE;

        // read a full-block records or the left
        while (runFileIndices[i] < runEnd && recordIdx < (i + 1) * numOfRecord) {
            heapRecords[recordIdx++] = readRecord(runFile, runFileIndices[i]);
            runFileIndices[i] += RECORD_SIZE;
        }

//        System.out.println(runFileIndices[i]);
    }

}
