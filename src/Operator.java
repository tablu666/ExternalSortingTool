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
