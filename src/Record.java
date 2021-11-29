import java.nio.ByteBuffer;

/**
 * This class is the record from input file.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class Record implements Comparable<Record> {

    private byte[] data;

    public Record(byte[] data) {
        this.data = data;
    }

    public long getId() {
        return ByteBuffer.wrap(data).getLong();
    }

    public double getKey() {
        return ByteBuffer.wrap(data).getDouble(8);
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int compareTo(Record o) {
        return Double.compare(this.getKey(), o.getKey());
    }

    @Override
    public String toString() {
        return getId() + " " + getKey();
    }
}
