/**
 * This class keeps the start position and the length of a run.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class RunInfo {

    private long start;
    private long length;

    /**
     * RunInfo
     * @param start long
     * @param length long
     */
    public RunInfo(long start, long length) {
        this.start = start;
        this.length = length;
    }

    /**
     * getStart
     * @return long
     */
    public long getStart() {
        return start;
    }

    /**
     * setStart
     * @param start long
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     * getLength
     * @return long
     */
    public long getLength() {
        return length;
    }

    /**
     * setLength
     * @param length long
     */
    public void setLength(long length) {
        this.length = length;
    }
}
