import java.nio.ByteBuffer;

/**
 * This class is the record from input file.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class Record implements Comparable<Record> {

    private byte[] data;

    /**
     * Record
     * @param data data
     */
    public Record(byte[] data) {
        this.data = data;
    }

    /**
     * getId
     * @return long
     */
    public long getId() {
        return ByteBuffer.wrap(data).getLong();
    }

    /**
     * getKey
     * @return double
     */
    public double getKey() {
        return ByteBuffer.wrap(data).getDouble(8);
    }

    /**
     * getData
     * @return byte
     */
    public byte[] getData() {
        return data;
    }

    @Override
    /**
     * compareTo
     */
    public int compareTo(Record o) {
        return Double.compare(this.getKey(), o.getKey());
    }

    @Override
    /**
     * toString
     */
    public String toString() {
        ByteBuffer bb = ByteBuffer.wrap(data);
        long id = bb.getLong();
        double key = bb.getDouble();
        return id + " " + key;
    }
}
