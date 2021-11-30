/**
 * This class implements the max heap structure.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class MaxHeap<T extends Comparable<? super T>> {

    private T[] data;

    public MaxHeap(T[] data) {
        this.data = data;
    }

    public void sort() {
        for (int i = data.length / 2 - 1; i >= 0; i--) {
            siftMax(i, data.length);
        }

        for (int i = data.length - 1; i > 0; i--) {
            T temp = data[0];
            data[0] = data[i];
            data[i] = temp;
            siftMax(0, i);
        }
    }

    private void siftMax(int i, int n) {
        while ((2 * i + 1) < n) {
            int j = 2 * i + 1;
            if (j + 1 < n && data[j + 1].compareTo(data[j]) > 0) {
                j = j + 1;
            }

            if (data[i].compareTo(data[j]) < 0) {
                T temp = data[i];
                data[i] = data[j];
                data[j] = temp;
                i = j;
            } else {
                break;
            }
        }
    }

    public T[] getData() {
        return data;
    }
}
