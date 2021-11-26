import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
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
}
