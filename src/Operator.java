import java.io.FileOutputStream;
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

    public Operator(RandomAccessFile file) {
        this.file = file;
    }

    public static String multiWayMerge(List<RunInfo> runInfoList, byte[] output) throws IOException {
        List<RunInfo> mergeInfoList = new ArrayList<>();

        int recordSize = Externalsort.RECORD_SIZE;
        int blockSize = Externalsort.BLOCK_SIZE;

        List<byte[]> blocks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            byte[] block = new byte[blockSize];
            blocks.add(block);
        }

        String runFileName = "RunFile.bin";
        String mergeFileName = "MergeFile.bin";

        while (runInfoList.size() > 1) {
            System.out.println("runInfoSize=" + runInfoList.size());
            RandomAccessFile runFile = new RandomAccessFile(runFileName, "rw");
            FileOutputStream fos = new FileOutputStream(mergeFileName);

            long[] runFilePos = new long[8];
            int[] blocksPos = new int[8];
            int runInfoPos = 0;
            int outPos = 0;
            long runStart = 0;
            long runLength = 0;

            while (runInfoPos < runInfoList.size()) {
                if (outPos == output.length) {
                    outPos = 0;
                    fos.write(output);
                    runLength += output.length;
                }

                Arrays.fill(runFilePos, -1L);
                for (int i = 0; i < 8; i++) {
                    if (runInfoPos + i >= runInfoList.size()) break;
                    runFilePos[i] = runInfoList.get(runInfoPos + i).getStart();
                }
                for (int i = 0; i < 8; i++) {
                    if (runFilePos[i] == -1L) break;
                    IOHelper.readBlock(runFile, runFilePos[i], blocks.get(i));
                    runFilePos[i] += blockSize;
                    System.out.println(Arrays.toString(blocks.get(i)));
                    System.out.println("run file pos=" + runFilePos[i]);
                }

                boolean mergeOver = false;
                while (!mergeOver) {
                    int minIdx = -1;
                    Record minRecord = null;
                    for (int i = 0; i < 8; i++) {
                        if (runFilePos[i] == -1L
                                || runFilePos[i] >= runInfoList.get(runInfoPos + i).getStart()
                                + runInfoList.get(runInfoPos + i).getLength())
                            continue;
                        if (blocksPos[i] == blockSize) {
                            IOHelper.readBlock(runFile, runFilePos[i], blocks.get(i));
                            runFilePos[i] += blockSize;
                        }
                        byte[] temp = new byte[recordSize];
                        for (int j = 0; j < recordSize && blocksPos[i] + j < blockSize; j++) {
                            temp[j] = blocks.get(i)[blocksPos[i] + j];
                        }
//                        System.arraycopy(blocks.get(i), blocksPos[i], temp, 0, recordSize);
                        Record currRecord = new Record(temp);
                        if (minIdx == -1 || currRecord.compareTo(minRecord) < 0) {
                            minIdx = i;
                            minRecord = currRecord;
                        }
                    }
                    if (minRecord != null) {
                        for (int i = 0; i < recordSize; i++) {
                            output[outPos + i] = minRecord.getData()[i];
                        }
//                        System.arraycopy(minRecord.getData(), 0, output, outPos, recordSize);
                        outPos += recordSize;
                        blocksPos[minIdx] += recordSize;
                    } else {
                        mergeInfoList.add(new RunInfo(runStart, runLength));
                        runStart += runLength;
                        runLength = 0;
                        runInfoPos += 8;
                        mergeOver = true;
                    }
                }
            }

            if (runInfoPos == runInfoList.size()) {
                List<RunInfo> temp = runInfoList;
                runInfoList = mergeInfoList;
                mergeInfoList = temp;
                mergeInfoList.clear();

                fos.close();
                runFile.close();

                String tempName = mergeFileName;
                runFileName = mergeFileName;
                mergeFileName = tempName;
            }
        }

        return runFileName;
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

}
