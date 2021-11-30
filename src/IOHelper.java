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

    // copy file a to b
    public static void copyToFile(RandomAccessFile a, RandomAccessFile b) throws IOException {
        a.seek(0);
        b.seek(0);

        // write file a to b repeatedly - 1 block a time
        while (a.getFilePointer() < a.length()) {
            byte[] data = new byte[BLOCK_SIZE];
            a.read(data);
            b.write(data);
        }
    }

    // read multi blocks of records from multi runs [1, 8]
    public static int readMultiBlocks(RandomAccessFile runFile, List<RunInfo> runInfoList,
                                      Record[] heapRecords, int[] recordsEndIndices) throws IOException {
        // maintain index of run info list [1, 8]
        int i;
        for (i = 0; i < Math.min(runInfoList.size(), 8); i++) {
            readBlock(runFile, runInfoList, heapRecords, recordsEndIndices, i);
        }

        return i;
    }

    // read 1 block of records from current run
    public static void readBlock(RandomAccessFile runFile, List<RunInfo> runInfoList,
                                 Record[] heapRecords, int[] recordsEndIndices, int runInfoIdx) throws IOException {
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
            recordsEndIndices[runInfoIdx] = i;
        }
    }

    public static void sortAndOutput(RandomAccessFile file, MaxHeap<Record> maxHeap) throws IOException {
        // directly output from heap
        maxHeap.sort();
        Record[] records = maxHeap.getData();
        file.seek(0);

        for (Record record : records) {
            byte[] data = record.getData();
            file.write(data);
        }
    }

}
