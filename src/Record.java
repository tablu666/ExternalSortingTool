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
        ByteBuffer bb = ByteBuffer.wrap(data);
        return bb.getLong();
    }

    public double getKey() {
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.getLong();
        return bb.getDouble();
    }

    @Override
    public int compareTo(Record o) {
        return Double.compare(this.getKey(), o.getKey());
    }
}
