import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    /**
     * file
     */
    private RandomAccessFile file;

    /**
     * mergeFile
     */
    private RandomAccessFile mergeFile;

    /**
     * Operator
     * @param file the file
     */
    public Operator(RandomAccessFile file) {
        this.file = file;
    }

    /**
     * replacementSelection
     * @param runInfoList List
     * @param minHeap minHeap
     * @param start start
     * @return randomAccessFile
     * @throws IOException exception
     */
    public RandomAccessFile replacementSelection(
            List<RunInfo> runInfoList,
            MinHeap<Record> minHeap,
            int start) throws IOException {
        // delete file first
        Files.deleteIfExists(Paths.get(IOHelper.RUN_FILE));

        RandomAccessFile runFile =
                new RandomAccessFile(
                        IOHelper.RUN_FILE, "rw");

        int blockSize = IOHelper.BLOCK_SIZE;
        int recordSize = IOHelper.RECORD_SIZE;
        int numOfRecord = blockSize / recordSize; // 512

        Record[] inputBuffer;
        Record[] outputBuffer = new Record[numOfRecord];

        int inputIdx = 0;
        int outputIdx = 0;
        // hidden record which is smaller than prev output record
        int hiddenNum = 0;
        long runStart = 0;
        long runLength = 0;

        // read 1 block (512) records from file to input buffer
        inputBuffer = IOHelper.readRecords(file, start, numOfRecord);

        while (minHeap.heapSize() > 0) {

            // begin a new run
            while (minHeap.heapSize() > 0) {
                // output 1 record per selection
                runLength += recordSize;

                // output buffer is full
                if (outputIdx == numOfRecord) {
                    IOHelper.write(runFile,
                            numOfRecord,
                            outputBuffer);
                    outputIdx = 0;
                }

                // input buffer is empty
                // read 1 block (512) records
                // from file to input buffer
                if (inputIdx == numOfRecord &&
                        file.getFilePointer() <
                                file.length()) {
                    inputBuffer = IOHelper.readRecords(
                            file,
                            file.getFilePointer(),
                            numOfRecord);
                    inputIdx = 0;
                }

                // input buffer has record
                if (inputIdx < numOfRecord) {
                    Record record = minHeap.getData()[0];
                    outputBuffer[outputIdx++] = record;

                    if (inputBuffer[inputIdx].compareTo(
                            record) > 0) {
                        minHeap.getData()[0] =
                                inputBuffer[inputIdx++];
                        minHeap.sift(0);
                    }
                    else {
                        // reduce 1 size and swap with end
                        if (!minHeap.empty()) {
                            int currSize = minHeap.heapSize();
                            currSize--;
                            minHeap.setHeapSize(currSize);
                            // swap
                            Record temp = minHeap.getData()[currSize];
                            minHeap.getData()[currSize] =
                                    minHeap.getData()[0];
                            minHeap.getData()[0] = temp;
                            // place to end as hidden one
                            minHeap.getData()[currSize] =
                                    inputBuffer[inputIdx++];
                            minHeap.sift(0);
                        }
                        hiddenNum++;
                    }
                }
                else {
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
                for (int i = total - hiddenNum,
                     j = 0; i < total; i++, j++) {
                    minHeap.getData()[j] =
                            minHeap.getData()[i];
                }
                // heapify records of min heap
                minHeap.setHeapSize(hiddenNum);
                minHeap.buildHeap();
            }

            runStart += runLength;
            runLength = 0;
            hiddenNum = 0;
        }

        return runFile;
    }

    /**
     * multiWayMerge
     * @param runFile RandomAccessFile
     * @param blocks Record
     * @param runInfoList List
     * @throws IOException exception
     */
    public void multiWayMerge(RandomAccessFile runFile,
                              Record[] blocks,
                              List<RunInfo> runInfoList)
            throws IOException {
        // delete file first
        Files.deleteIfExists(Paths.get(IOHelper.MERGE_FILE));

        mergeFile = new RandomAccessFile(IOHelper.MERGE_FILE, "rw");
        List<RunInfo> mergeInfoList = runInfoList;

        // only 1 run
        if (runInfoList.size() == 1) {
            mergeToFile(runFile, blocks, mergeInfoList);
            IOHelper.copyToFile(mergeFile, file);

            runFile.close();
            mergeFile.close();

            return;
        }

        // many runs - swap files and run infos and merge
        while (true) {
            // only 1 run after merge
            if (mergeInfoList.size() == 1) {
                IOHelper.copyToFile(mergeFile, file);

                runFile.close();
                mergeFile.close();

                Files.deleteIfExists(Paths.get(IOHelper.RUN_FILE));
                Files.deleteIfExists(Paths.get(IOHelper.MERGE_FILE));

                break;
            }
            else {
                // swap run info with merge info
                mergeInfoList = mergeToFile(
                        runFile, blocks, mergeInfoList);
                IOHelper.copyToFile(mergeFile, runFile);

                runFile.seek(0);
                mergeFile.seek(0);
            }
        }
    }

    /**
     * mergeToFile
     * @param runFile RandomAccessFile
     * @param heapRecords Record
     * @param runInfoList List
     * @return List
     * @throws IOException exception
     */
    private List<RunInfo> mergeToFile(RandomAccessFile runFile,
                                      Record[] heapRecords,
                                      List<RunInfo> runInfoList)
            throws IOException {
        List<RunInfo> mergeInfoList = new ArrayList<>();

        int blockSize = IOHelper.BLOCK_SIZE;
        int recordSize = IOHelper.RECORD_SIZE;
        int numOfRecord = blockSize / recordSize; // 512

        // keep 8 block end index
        int[] recordsEndIndices = new int[8];

        Record[] outputBuffer = new Record[numOfRecord];
        int outputIdx = 0;
        long runLength = 0;
        long runStart = 0;

        // traverse run info list
        while (runInfoList.size() > 0) {
            // get run number [1, 8]
            int runNum = IOHelper.readMultiBlocks(
                    runFile, runInfoList,
                    heapRecords, recordsEndIndices);
            // maintain read done run number
            int runDoneNum = 0;
            // index of minimum run
            int minRunIdx = 0;

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
                    IOHelper.write(
                            mergeFile, numOfRecord, outputBuffer);
                    outputIdx = 0;
                }

                // compare each run and find the minimum record
                Record minRecord = getMaxRecord();
                for (int i = 0; i < recordIndices.length; i++) {
                    // skip run read done block
                    if (runDone[i]) {
                        continue;
                    }

                    Record curr = heapRecords[recordIndices[i]];
                    if (curr.compareTo(minRecord) < 0) {
                        minRunIdx = i;
                        minRecord = curr;
                    }
                }

                outputBuffer[outputIdx++] = minRecord;

                // current block index reaching end
                if (recordIndices[minRunIdx] ==
                        recordsEndIndices[minRunIdx]) {
                    RunInfo minRunInfo = runInfoList.get(minRunIdx);
                    if (minRunInfo.getLength() > 0) {
                        // continue to read from input
                        IOHelper.readBlock(runFile,
                                runInfoList,
                                heapRecords,
                                recordsEndIndices,
                                minRunIdx);
                        // back to start index
                        recordIndices[minRunIdx] =
                                minRunIdx * numOfRecord; // [0,7] * 512
                    }
                    else {
                        // read done
                        runDone[minRunIdx] = true;
                        runDoneNum++;
                    }
                }
                else {
                    recordIndices[minRunIdx]++;
                }
            }

            // remaining records from output buffer
            if (outputIdx > 0) {
                IOHelper.write(mergeFile,
                        outputIdx,
                        outputBuffer);
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
        }

        return mergeInfoList;
    }


    /**
     * set the key of current record to max
     * @return Record
     */
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
