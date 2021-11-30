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
    private RandomAccessFile mergeFile;

    public Operator(RandomAccessFile file) {
        this.file = file;
    }

    public RandomAccessFile replacementSelection(List<RunInfo> runInfoList, MinHeap<Record> minHeap, int start) throws IOException {
        RandomAccessFile runFile = new RandomAccessFile(IOHelper.RUN_FILE, "rw");
        int blockSize = IOHelper.BLOCK_SIZE;
        int recordSize = IOHelper.RECORD_SIZE;
        int numOfRecord = blockSize / recordSize; // 512
        Record[] inputBuffer;
        Record[] outputBuffer = new Record[numOfRecord];

        long runStart = 0;
        long runLength = 0;
        int inputIdx = 0;
        int outputIdx = 0;
        int hiddenNum = 0;

//        file.seek(start);
        // read 1 block (512) record from file to input buffer
        inputBuffer = IOHelper.readRecords(file, start, numOfRecord);

        while (minHeap.heapSize() > 0) {
            // begin a new run
            System.out.println("begin a new run");

            while (minHeap.heapSize() > 0) {
                System.out.println("run info size = " + runInfoList.size());
//                if (heapSize == 1) {
//                    int a = 0;
//                }
                runLength += recordSize;

                // check output buffer is full
                if (outputIdx == numOfRecord) {
                    IOHelper.write(runFile, numOfRecord, outputBuffer);
                    outputIdx = 0;
                }

                // read 1 block (512) record from file to input buffer
                if (inputIdx == numOfRecord && file.getFilePointer() < file.length()) {
                    inputBuffer = IOHelper.readRecords(file, file.getFilePointer(), numOfRecord);
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
                        if (!minHeap.empty()) {
                            minHeap.setHeapSize(minHeap.heapSize() - 1);
                            // swap
                            Record temp = minHeap.getData()[minHeap.heapSize()];
                            minHeap.getData()[minHeap.heapSize()] = minHeap.getData()[0];
                            minHeap.getData()[0] = temp;

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

            // heapify the heap - hiddenNum [1, 512 * 8]
            if (hiddenNum > 0) {
                int total = minHeap.capacity();
                System.out.println("total=" + total + " hidden num=" + hiddenNum);
                // move to front
                for (int i = total - hiddenNum, j = 0; i < total; i++, j++) {
                    minHeap.getData()[j] = minHeap.getData()[i];
                }
                minHeap.setHeapSize(hiddenNum);
                minHeap.buildHeap();
            }

            runStart += runLength;
            runLength = 0;
            hiddenNum = 0;
        }

        return runFile;
    }

    public void multiWayMerge(RandomAccessFile runFile, Record[] blocks, List<RunInfo> runInfoList) throws IOException {
        mergeFile = new RandomAccessFile(IOHelper.MERGE_FILE, "rw");
        List<RunInfo> mergeInfoList = runInfoList;

        // only 1 run
        if (runInfoList.size() == 1) {
            mergeToFile(runFile, blocks, mergeInfoList);
            IOHelper.copyToFile(mergeFile, file);
            return;
        }

        // many runs
        while (true) {
            System.out.println("run number = " + mergeInfoList.size());
            if (mergeInfoList.size() == 1) {
                IOHelper.copyToFile(mergeFile, file);
//                IOHelper.standOutput(mergeFile);
                break;
            } else {
                System.out.println("begin merge");
                mergeInfoList = mergeToFile(runFile, blocks, mergeInfoList);
                // swap
                IOHelper.copyToFile(mergeFile, runFile);
//                RandomAccessFile temp = runFile;
//                runFile = mergeFile;
//                mergeFile = temp;

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
        int[] runEndIndices = new int[8];
        int outputIdx = 0;
        long runLength = 0;
        long runStart = 0;

        // traverse run list
        while (runInfoList.size() > 0) {
            System.out.println("traverse run info list");

            int runNum = IOHelper.readBlockOfRecords(runFile, runInfoList,
                    heapRecords, runEndIndices, 0);

            int runDoneNum = 0;
            int minRun = 0;


            // init 8 or less readDone flag
            boolean[] runDone = new boolean[runNum];

            int[] recordIndices = new int[runNum];

            // init 8 or less indices for the 8-block-size run heap memory
            for (int i = 0; i < runNum; i++) {
                recordIndices[i] = numOfRecord * i;
                System.out.println("i=" + i + " recordIdx=" + recordIndices[i]);
            }

            // merge current 8 blocks respectively
            while (runDoneNum < runNum) {
                // update runLength
                runLength += recordSize;

                // write to merge file
                if (outputIdx == numOfRecord) {
                    IOHelper.write(mergeFile, numOfRecord, outputBuffer);
                    outputIdx = 0;
                }

                // compare each run and find minimum record
                Record minRecord = getMaxRecord();
                for (int i = 0; i < recordIndices.length; i++) {
                    // skip done block
                    if (runDone[i]) continue;
                    Record curr = heapRecords[recordIndices[i]];
                    if (curr.compareTo(minRecord) < 0) {
                        minRun = i;
                        minRecord = curr;
                    }
                }

                outputBuffer[outputIdx++] = minRecord;

                // check if current block index reaching end
                if (recordIndices[minRun] == runEndIndices[minRun]) {
                    // empty block
                    if (runInfoList.get(minRun).getLength() == 0) {
                        runDone[minRun] = true;
                        runDoneNum++;
                    } else {
                        // continue to read from input
                        IOHelper.readOneBlockOfRecords(runFile, runInfoList, heapRecords, runEndIndices, minRun);
                        // back to start
                        recordIndices[minRun] = minRun * numOfRecord;
                    }
                } else {
                    recordIndices[minRun]++;
                }
            }

            if (outputIdx > 0) {
                IOHelper.write(mergeFile, outputIdx, outputBuffer);
                outputIdx = 0;
            }

            // remove prev 8 runs
            for (int i = 0; i < runNum; i++) {
                runInfoList.remove(0);
            }

            // update merge run info list
            mergeInfoList.add(new RunInfo(runStart, runLength));
            runStart += runLength;
            runLength = 0;
        }

        return mergeInfoList;
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
