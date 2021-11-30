import java.lang.Comparable;

/**
 * This class implements the min heap structure
 * based on source code from Clifford A. Shaffer
 * <p>
 * Source code example for "A Practical Introduction to Data
 * Structures and Algorithm Analysis, 3rd Edition (Java)"
 * by Clifford A. Shaffer
 * Copyright 2008-2011 by Clifford A. Shaffer
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-14
 */
public class MinHeap<T extends Comparable<? super T>> {
    private T[] data;   // Pointer to the heap array
    private int size;   // Maximum size of the heap
    private int n;      // Number of things in heap

    /**
     * Constructor of MinHeap class.
     *
     * @param data the array of min heap
     * @param n    the item count in min heap
     * @param size the capacity of min heap
     */
    public MinHeap(T[] data, int n, int size) {
        this.data = data;
        this.n = n;
        this.size = size;
        buildHeap();
    }

    /**
     * Return current size of the heap
     */
    public int heapSize() {
        return n;
    }

    public void setHeapSize(int n) {
        this.n = n;
    }

    /**
     * Is pos a leaf position?
     */
    public boolean isLeaf(int pos) {
        return (pos >= n / 2) && (pos < n);
    }

    /**
     * Return position for left child of pos
     */
    public int leftChild(int pos) {
        assert pos < n / 2 : "Position has no left child";
        return 2 * pos + 1;
    }

    /**
     * Heapify contents of Heap
     */
    public void buildHeap() {
        for (int i = n / 2 - 1; i >= 0; i--) sift(i);
    }

    public T removeMin() {     // Remove minimum value
        if (n < 1) return null;

        // swap
        n--;
        T temp = data[n];
        data[n] = data[0];
        data[0] = temp;

        if (n != 0) sift(0);

        return data[n];
    }

    public void sift(int pos) {
        assert (pos >= 0) && (pos < n) : "Illegal heap position";
        while (!isLeaf(pos)) {
            int j = leftChild(pos);
            if ((j < (n - 1)) && (data[j].compareTo(data[j + 1]) > 0))
                j++; // j is now index of child with greater value
            if (data[pos].compareTo(data[j]) <= 0)
                return;
            T temp = data[pos];
            data[pos] = data[j];
            data[j] = temp;
            pos = j;  // Move down
        }
    }

    public boolean empty() {
        return n == 0;
    }

    public int capacity() {
        return this.size;
    }

    public T[] getData() {
        return data;
    }

}


// -------------------------------
    /*
public class MinHeap<T extends Comparable<? super T>> {

    private T[] data;

    public MinHeap(T[] data) {
        this.data = data;
        heapify();
    }

    public void heapify() {
        for (int i = data.length / 2 - 1; i >= 0; i--) {
            sift(i, data.length);
        }
    }

    public void sift(int i, int n) {
        while ((2 * i + 1) < n) {
            int j = 2 * i + 1;
            if (j + 1 < n && data[j + 1].compareTo(data[j]) < 0) {
                j = j + 1;
            }

            if (data[i].compareTo(data[j]) > 0) {
                T temp = data[i];
                data[i] = data[j];
                data[j] = temp;
                i = j;
            } else {
                break;
            }
        }
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

     */
