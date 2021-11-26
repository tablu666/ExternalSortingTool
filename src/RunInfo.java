/**
 * This class keeps the start position and the length of a run.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class RunInfo {

    private long start;
    private byte length;

    public RunInfo(long start, byte length) {
        this.start = start;
        this.length = length;
    }

    public long getStart() {
        return start;
    }

    public byte getLength() {
        return length;
    }
}
