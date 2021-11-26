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

    public static int replacementSelection(RandomAccessFile file, MinHeap<Record> minHeap,
                                            byte[] input, byte[] output, List<RunInfo> runInfoList,
                                            FileOutputStream fos, int start) throws IOException {
        Record[] records = minHeap.getData();
        int size = records.length;
        int recordSize = Externalsort.RECORD_SIZE;
        long runStart = 0;
        long runLength = 0;
        int outPos = 0;
        int numOfBytes;
        for (long i = start; i < file.length(); i += numOfBytes) {
            numOfBytes = FileHelper.readBlock(file, i, input);
            int inPos = 0;
            while (inPos < numOfBytes) {
                if (outPos == output.length) {
                    outPos = 0;
                    fos.write(output);
                    runLength += output.length;
                }
                if (size == 0) {
                    minHeap.heapify();
                    records = minHeap.getData();
                    size = records.length;
                    runInfoList.add(new RunInfo(runStart, runLength));
                    runStart += runLength;
                    runLength = 0;
                } else {
                    byte[] data = records[0].getData();
                    Record prev = records[0];
                    System.arraycopy(data, 0, output, outPos, recordSize);
                    outPos += recordSize;

                    data = new byte[recordSize];
                    System.arraycopy(input, inPos, data, 0, recordSize);
                    inPos += recordSize;
                    Record curr = new Record(data);
                    if (curr.compareTo(prev) >= 0) {
                        records[0] = curr;
                    } else {
                        Record temp = records[records.length - 1];
                        records[records.length - 1] = curr;
                        records[0] = temp;
                        minHeap.sift(0);
                        size--;
                    }
                }
            }
        }

        return outPos;
    }

    public static void replacementSelection(MinHeap<Record> minHeap, byte[] output, List<RunInfo> runInfoList,
                                            FileOutputStream fos, int outPos) throws IOException {
        minHeap.sort();
        Record[] records = minHeap.getData();
        int recordSize = Externalsort.RECORD_SIZE;
        int runLength = 0;
        for (int k = 0, i = outPos; k < records.length; k++, i += recordSize) {
            if (i == output.length) {
                i = 0;
                fos.write(output);
                RunInfo runInfo = runInfoList.get(runInfoList.size() - 1);
                runInfo.setLength(runInfo.getLength() + runLength);
            }
            byte[] data = records[k].getData();
            System.arraycopy(data, 0, output, i, recordSize);
            runLength += recordSize;
        }
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
                    FileHelper.readBlock(runFile, runFilePos[i], blocks.get(i));
                    runFilePos[i] += blockSize;
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
                            FileHelper.readBlock(runFile, runFilePos[i], blocks.get(i));
                            runFilePos[i] += blockSize;
                        }
                        byte[] temp = new byte[recordSize];
                        System.arraycopy(blocks.get(i), blocksPos[i], temp, 0, recordSize);
                        Record currRecord = new Record(temp);
                        if (minIdx == -1 || currRecord.compareTo(minRecord) < 0) {
                            minIdx = i;
                            minRecord = currRecord;
                        }
                    }
                    if (minRecord != null) {
                        System.arraycopy(minRecord.getData(), 0, output, outPos, recordSize);
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
}
