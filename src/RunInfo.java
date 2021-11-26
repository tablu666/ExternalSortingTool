/**
 * This class keeps the start position and the length of a run.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class RunInfo {

    private long start;
    private long length;

    public RunInfo(long start, long length) {
        this.start = start;
        this.length = length;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
