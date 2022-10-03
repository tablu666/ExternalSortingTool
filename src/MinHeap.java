//import java.lang.Comparable;

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
 * @param <T> data type
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
     * heapSize
     * @return the heapSize
     */
    public int heapSize() {
        return n;
    }

    /**
     * set the heap size
     * @param num int
     */
    public void setHeapSize(int num) {
        this.n = num;
    }

    /**
     *
     * @param pos position
     * @return boolean
     */
    public boolean isLeaf(int pos) {
        return (pos >= n / 2) && (pos < n);
    }

    /**
     * leftChild
     * @param pos the position
     * @return the left child
     */
    public int leftChild(int pos) {
        assert pos < n / 2 : "Position has no left child";
        return 2 * pos + 1;
    }

    /**
     * Heapify the content
     */
    public void buildHeap() {
        for (int i = n / 2 - 1; i >= 0; i--) {
            sift(i);
        }
    }

    /**
     * removeMin
     * @return return type
     */
    public T removeMin() {     // Remove minimum value
        if (n < 1) {
            return null;
        }

        // swap
        n--;
        T temp = data[n];
        data[n] = data[0];
        data[0] = temp;

        if (n != 0) {
            sift(0);
        }

        return data[n];
    }

    /**
     * sift
     * @param pos the position
     */
    public void sift(int pos) {
        if (pos >= 0 && pos < n) { // legal heap position
            while (!isLeaf(pos)) {
                int j = leftChild(pos);
                if ((j < (n - 1)) && (data[j].compareTo(data[j + 1]) > 0)) {
                    j++; // j is now index of child with greater value
                }
                if (data[pos].compareTo(data[j]) <= 0) {
                    return;
                }
                T temp = data[pos];
                data[pos] = data[j];
                data[j] = temp;
                pos = j; // Move down
            }
        }
    }

    /**
     * empty
     * @return return boolean
     */
    public boolean empty() {
        return n == 0;
    }

    /**
     * capacity
     * @return the size
     */
    public int capacity() {
        return this.size;
    }

    /**
     * getData
     * @return the data
     */
    public T[] getData() {
        return data;
    }

}