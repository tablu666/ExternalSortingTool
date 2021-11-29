import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
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
    private RandomAccessFile mergeFile;

    public Operator(RandomAccessFile file) {
        this.file = file;
    }

    public RandomAccessFile replacementSelection(MinHeap<Record> minHeap, int start,
                                                 List<RunInfo> runInfoList) throws IOException {
        RandomAccessFile runFile = new RandomAccessFile(IOHelper.RUN_FILE, "rw");
        int blockSize = IOHelper.BLOCK_SIZE;
        int recordSize = IOHelper.RECORD_SIZE;
        int numOfRecord = blockSize / recordSize; // 512
        Record[] inputBuffer;
        Record[] outputBuffer = new Record[numOfRecord];

        Record[] heapData = minHeap.getData();
        int heapSize = heapData.length;
        long runStart = 0;
        long runLength = 0;
        int outputIdx = 0;

        // traverse file
        for (long filePos = start; filePos < file.length(); filePos += blockSize) {
            // read 1 block (512) record from file to input buffer
            inputBuffer = IOHelper.readRecords(file, filePos, numOfRecord);

            // read record from input buffer 1 by 1
            for (int inputIdx = 0; inputIdx < inputBuffer.length; ) {
                // check output buffer is full
                if (outputIdx == numOfRecord) {
                    IOHelper.write(runFile, numOfRecord, outputBuffer);
                    outputIdx = 0;
                    runLength += blockSize;
                }

                // check heap size is 0 (a run)
                if (heapSize == 0) {
                    // check remaining record in output buffer of current run
                    if (outputIdx > 0) {
                        IOHelper.write(runFile, outputIdx, outputBuffer);
                        runLength += outputIdx * recordSize;
                        outputIdx = 0;
                    }

                    minHeap.heapify();
                    heapData = minHeap.getData();
                    heapSize = heapData.length;
                    runInfoList.add(new RunInfo(runStart, runLength));
                    runStart += runLength;
                    runLength = 0;
                } else {
                    // 0-index is min record
                    minHeap.sift(0, heapSize);
                    outputBuffer[outputIdx++] = heapData[0];
                    Record prev = outputBuffer[outputIdx - 1];

                    Record curr = inputBuffer[inputIdx++];
                    heapData[0] = curr;
                    if (curr.compareTo(prev) < 0) {
                        // swap
                        Record temp = heapData[heapSize - 1];
                        heapData[heapSize - 1] = curr;
                        heapData[0] = temp;
                        heapSize--;
                    }
                }
            }
        }

        int remain = heapSize; // [0, 8 * 512]

        // heap has remaining records from last run
        while (heapSize > 0) {
            // output buffer is full
            if (outputIdx == numOfRecord) {
                IOHelper.write(runFile, numOfRecord, outputBuffer);
                outputIdx = 0;
                runLength += blockSize;
            } else {
                minHeap.sift(0, heapSize);
                outputBuffer[outputIdx++] = heapData[0];
                // swap
                if (heapSize > 1) {
                    Record temp = heapData[heapSize - 1];
                    heapData[heapSize - 1] = heapData[0];
                    heapData[0] = temp;
                }
                heapSize--;
            }
        }

        // update run info
        if (remain > 0) {
            if (outputIdx > 0) {
                IOHelper.write(runFile, outputIdx, outputBuffer);
                runLength += outputIdx * recordSize;
                outputIdx = 0;
            }
            runInfoList.add(new RunInfo(runStart, runLength));
            runStart += runLength;
            runLength = 0;
        }

        // heap has another run to do
        if (remain < 8 * numOfRecord) {
            // new run
            Record[] newRecords = new Record[8 * numOfRecord - remain];
            System.arraycopy(minHeap.getData(), remain, newRecords, 0, newRecords.length);
            minHeap = new MinHeap<>(newRecords);
            minHeap.sort();
            for (int k = 0; k < minHeap.getData().length; k++) {
                // output buffer is full
                if (outputIdx == numOfRecord) {
                    IOHelper.write(runFile, numOfRecord, outputBuffer);
                    outputIdx = 0;
                    runLength += blockSize;
                }
                outputBuffer[outputIdx++] = minHeap.getData()[k];
            }
            if (outputIdx > 0) {
                IOHelper.write(runFile, outputIdx, outputBuffer);
                runLength += outputIdx * recordSize;
            }
            runInfoList.add(new RunInfo(runStart, runLength));
        }

        return runFile;
    }


    public void multiWayMerge(RandomAccessFile runFile, Record[] blocks, List<RunInfo> runInfoList) throws IOException {
        mergeFile = new RandomAccessFile(IOHelper.MERGE_FILE, "rw");
        List<RunInfo> mergeInfoList = runInfoList;

        while (true) {
            if (mergeInfoList.size() == 1) {
                IOHelper.copyToFile(mergeFile, file);
                break;
            } else {
                System.out.println("begin merge");
                Arrays.fill(blocks, null);
                mergeInfoList = mergeToFile(runFile, blocks, mergeInfoList);
                // swap
                RandomAccessFile temp = runFile;
                runFile = mergeFile;
                mergeFile = temp;

                runFile.seek(0);
                mergeFile.seek(0);
            }
        }
    }

    private List<RunInfo> mergeToFile(RandomAccessFile runFile, Record[] heapRecords, List<RunInfo> runInfoList) throws IOException {
        List<RunInfo> mergeInfoList = new ArrayList<>();
        int blockSize = IOHelper.BLOCK_SIZE;
        int recordSize = IOHelper.RECORD_SIZE;
        int numOfRecord = blockSize / recordSize; // 512
        Record[] outputBuffer = new Record[numOfRecord];
        int runInfoIdx = 0;
        int outputIdx = 0;
        long runStart = 0;
        long runLength = 0;

        // traverse run list
        while (runInfoIdx < runInfoList.size()) {
            System.out.println("traverse run info list");
            // run num ([1, 8])
            int runNum = Math.min(8, runInfoList.size() - runInfoIdx);

            // init 8 or less indices for the 8-block-size run heap memory
            int[] recordIndices = new int[runNum];
            for (int i = 0; i < runNum; i++) {
                recordIndices[i] = numOfRecord * i;
                System.out.println("i=" + i + " recordIdx=" + recordIndices[i]);
            }

            // init 8 or less indices for run file
            long[] runFileIndices = new long[runNum];
            for (int i = 0; i < runNum; i++) {
                runFileIndices[i] = runInfoList.get(runInfoIdx + i).getStart();
            }

            // initial reading from each run
            for (int i = 0; i < runNum; i++) {
                RunInfo runInfo = runInfoList.get(runInfoIdx + i);
                int recordIdx = recordIndices[i];
                IOHelper.readBlockOfRecords(runFile, runInfo, recordIdx, i, heapRecords, runFileIndices);
            }

            // merge current 8 blocks respectively
            while (!groupMergeOver(runFileIndices, runNum, heapRecords, runInfoList, runInfoIdx)) {
//                System.out.println("merge 8 blocks");
                // compare each run and find minimum record
                Record min = null;
                int minRun = -1;
                for (int i = 0; i < runNum; i++) {
                    RunInfo runInfo = runInfoList.get(runInfoIdx + i);
                    long capacity = runInfo.getStart() + runInfo.getLength() - runFileIndices[i];
                    if (readDone(capacity, i, heapRecords)) continue;

                    // block is empty - reread from run file
                    if (recordIndices[i] == (i + 1) * numOfRecord) {
//                        System.out.println(recordIndices[i] + " i=" + i);
                        recordIndices[i] = i * numOfRecord;
                        IOHelper.readBlockOfRecords(runFile, runInfo, 0, i, heapRecords, runFileIndices);
                    }

                    Record curr = heapRecords[recordIndices[i]];
                    // the last data from current run - not enough filling 1 block
                    if (curr == null) {
                        recordIndices[i] = i * numOfRecord;
                        curr = heapRecords[recordIndices[i]];
                        if (curr == null) continue;
                    }
                    if (min == null || curr.compareTo(min) < 0) {
                        min = curr;
                        minRun = i;
                    }
                }

                // write to merge file
                if (outputIdx == numOfRecord) {
                    IOHelper.write(mergeFile, numOfRecord, outputBuffer);
                    outputIdx = 0;
                    runLength += blockSize;
                }

                // put the minimum record to output buffer
                if (min != null) {
                    outputBuffer[outputIdx++] = min;
                    heapRecords[recordIndices[minRun]] = null;
                    recordIndices[minRun]++;
                }
            }

            if (outputIdx > 0) {
                IOHelper.write(mergeFile, outputIdx, outputBuffer);
                runLength += outputIdx * recordSize;
                outputIdx = 0;
            }

            mergeInfoList.add(new RunInfo(runStart, runLength));
            runStart += runLength;
            runLength = 0;
            runInfoIdx += runNum;
        }

        return mergeInfoList;
    }

    // check current 8-blocks-group merge is over
    private boolean groupMergeOver(long[] runFileIndices, int runNum, Record[] heapRecords,
                                   List<RunInfo> runInfoList, int runInfoIdx) {

        for (int i = 0; i < runNum; i++) {
            RunInfo runInfo = runInfoList.get(runInfoIdx + i);
            long capacity = runInfo.getStart() + runInfo.getLength() - runFileIndices[i];

            if (!readDone(capacity, i, heapRecords)) {
                return false;
            }
        }

        System.out.println("true");
        return true;
    }

    private boolean readDone(long capacity, int i, Record[] heapRecords) {
        int numOfRecord = IOHelper.BLOCK_SIZE / IOHelper.RECORD_SIZE; // 512

        for (int j = i * numOfRecord; j < (i + 1) * numOfRecord; j++) {
            if (heapRecords[j] != null) {
//                System.out.println("false i=" + i + " j=" + j);
                return false;
            }
        }

        return capacity == 0;
    }
}
