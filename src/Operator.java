import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for two phases operations.
 * These are replacement selection and eight-way merge.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class Operator {

    private RandomAccessFile file;
    private long filePos;
    private final int heapSize; // 65536
    private final int blockSize; // 8192
    private final int recordSize; // 16

    public Operator(RandomAccessFile file) {
        this.file = file;
        this.heapSize = IOHelper.HEAP_SIZE;
        this.blockSize = IOHelper.BLOCK_SIZE;
        this.recordSize = IOHelper.RECORD_SIZE;
        this.filePos = 0;
    }

    public List<RunInfo> replacementSelection() throws IOException {
        RandomAccessFile runFile = new RandomAccessFile(IOHelper.RUN_FILE, "rw");
        List<RunInfo> runInfoList = new ArrayList<>();

        int inputIdx = 0;
        int outputIdx = 0;
        // hidden record which is smaller than prev output record
        int hiddenNum = 0;
        long runStart = 0;
        long runLength = 0;

        int numOfRecord = blockSize / recordSize; // 512

        Record[] inputBuffer;
        Record[] outputBuffer = new Record[numOfRecord];

        // read 8 blocks of records into heap
        MinHeap<Record> minHeap = buildMinHeap();

        // read 1 block of records into input buffer
        inputBuffer = new Record[blockSize / recordSize];
        filePos = IOHelper.readMultiRecords(file, filePos, inputBuffer);

        while (minHeap.heapSize() > 0) {

            // begin a new run
            while (minHeap.heapSize() > 0) {
                // output 1 record per selection
                runLength += recordSize;

                // output buffer is full
                if (outputIdx == numOfRecord) {
                    IOHelper.write(runFile, numOfRecord, outputBuffer);
                    outputIdx = 0;
                }

                // input buffer is empty
                // read 1 block (512) records from file to input buffer
                if (inputIdx == numOfRecord && file.getFilePointer() < file.length()) {
                    filePos = IOHelper.readMultiRecords(file, filePos, inputBuffer);
                    inputIdx = 0;
                }

                // input buffer has record
                if (inputIdx < numOfRecord) {
                    Record record = minHeap.getData()[0];
                    outputBuffer[outputIdx++] = record;

                    if (inputBuffer[inputIdx].compareTo(record) > 0) {
                        minHeap.getData()[0] = inputBuffer[inputIdx++];
                        minHeap.sift(0);
                    } else {
                        // reduce 1 size and swap with the last
                        if (minHeap.heapSize() > 0) {
                            minHeap.setHeapSize(minHeap.heapSize() - 1);
                            // swap with last index
                            Record temp = minHeap.getData()[minHeap.heapSize()];
                            minHeap.getData()[minHeap.heapSize()] = minHeap.getData()[0];
                            minHeap.getData()[0] = temp;
                            // place to end as hidden one
                            minHeap.getData()[minHeap.heapSize()] = inputBuffer[inputIdx++];
                            minHeap.sift(0);
                        }
                        hiddenNum++;
                    }
                } else {
                    // output from heap for the current run
                    outputBuffer[outputIdx++] = minHeap.removeMin();
                }
            }

            // check remaining record in output buffer of current run
            if (outputIdx > 0) {
                IOHelper.write(runFile, outputIdx, outputBuffer);
                outputIdx = 0;
            }

            // update run info
            runInfoList.add(new RunInfo(runStart, runLength));

            // rebuild the heap with hiddenNum [1, 512 * 8]
            if (hiddenNum > 0) {
                int total = minHeap.capacity();
                // move to front
                for (int i = total - hiddenNum, j = 0; i < total; i++, j++) {
                    minHeap.getData()[j] = minHeap.getData()[i];
                }
                // heapify records of min heap
                minHeap.setHeapSize(hiddenNum);
                minHeap.buildHeap();
            }

            runStart += runLength;
            runLength = 0;
            hiddenNum = 0;
        }

        runFile.close();

        return runInfoList;
    }

    private MinHeap<Record> buildMinHeap() throws IOException {
        int numOfRecord = heapSize / recordSize; // 8 * 512
        Record[] heapData = new Record[numOfRecord];
        for (int i = 0; i < numOfRecord; i++) {
            heapData[i] = IOHelper.readRecord(file, filePos);
            filePos += recordSize;
        }
        return new MinHeap<>(heapData, numOfRecord, numOfRecord);
    }

    public void multiWayMerge(RandomAccessFile runFile, List<RunInfo> runInfoList) throws IOException {
        RandomAccessFile mergeFile = new RandomAccessFile(IOHelper.MERGE_FILE, "rw");
        List<RunInfo> mergeInfoList = new ArrayList<>();
        Record[] heapRecords = new Record[heapSize / recordSize];

        // create an output buffer
        int numOfRecord = blockSize / recordSize; // 512
        Record[] outputBuffer = new Record[numOfRecord];

        // keep 8 block end index
        int[] recordsEndIndices = new int[8];

        int outputIdx = 0;
        long runLength = 0;
        long runStart = 0;

        // traverse run info list
        while (runInfoList.size() > 0) {
            // begin read
            runFile.seek(0);
            // get run number [1, 8]
            int runNum = IOHelper.readMultiBlocks(runFile, runInfoList,
                    heapRecords, recordsEndIndices);
            // index of minimum run
            int minRunIdx = 0;
            // maintain read done run number
            int runDoneNum = 0;
            // maintain 8 or less read done flags
            boolean[] runDone = new boolean[runNum];
            // init 8 or less indices for the 8-block-size run heap memory
            int[] recordIndices = new int[runNum];
            for (int i = 0; i < runNum; i++) {
                recordIndices[i] = numOfRecord * i;
            }

            // merge current 8 or less blocks respectively
            while (runDoneNum < runNum) {
                // update runLength
                runLength += recordSize;

                // write to merge file
                if (outputIdx == numOfRecord) {
                    IOHelper.write(mergeFile, numOfRecord, outputBuffer);
                    outputIdx = 0;
                }

                // compare each run and find the minimum record
                Record minRecord = getMaxRecord();
                for (int i = 0; i < recordIndices.length; i++) {
                    // skip run read done block
                    if (runDone[i]) continue;

                    Record curr = heapRecords[recordIndices[i]];
                    // update min record
                    if (curr.compareTo(minRecord) < 0) {
                        minRunIdx = i;
                        minRecord = curr;
                    }
                }

                // output the min record
                outputBuffer[outputIdx++] = minRecord;

                // current block index reaching end
                if (recordIndices[minRunIdx] == recordsEndIndices[minRunIdx]) {
                    RunInfo minRunInfo = runInfoList.get(minRunIdx);
                    if (minRunInfo.getLength() > 0) {
                        // continue to read from input
                        IOHelper.readBlock(runFile, runInfoList, heapRecords, recordsEndIndices, minRunIdx);
                        // back to start index
                        recordIndices[minRunIdx] = minRunIdx * numOfRecord; // [0,7] * 512
                    } else {
                        // read done
                        runDone[minRunIdx] = true;
                        runDoneNum++;
                    }
                } else {
                    recordIndices[minRunIdx]++;
                }
            }

            // remaining records from output buffer
            if (outputIdx > 0) {
                IOHelper.write(mergeFile, outputIdx, outputBuffer);
                outputIdx = 0;
            }

            // remove prev 8 or less runs
            for (int i = 0; i < runNum; i++) {
                runInfoList.remove(0);
            }

            // update merge run info list
            mergeInfoList.add(new RunInfo(runStart, runLength));
            runStart += runLength;
            runLength = 0;

            // only 1 run after merge
            if (runInfoList.size() == 0 && mergeInfoList.size() == 1) {
                break;
            }

            // many runs after merge
            if (runInfoList.size() == 0) {
                // swap files and run infos and merge
                List<RunInfo> temp = runInfoList;
                runInfoList = mergeInfoList;
                mergeInfoList = temp;
                IOHelper.copyToFile(mergeFile, runFile);
                // back to start
                mergeFile.seek(0);
            }
        }
    }

    // set the key of current record to max
    private Record getMaxRecord() {
        int recordSize = IOHelper.RECORD_SIZE;
        ByteBuffer bb = ByteBuffer.allocate(recordSize);

        // id
        bb.putLong(0, 0);

        // key
        int keyPos = recordSize / 2;
        bb.putDouble(keyPos, Double.POSITIVE_INFINITY);

        bb.rewind();

        return new Record(bb.array());
    }
}
