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

    public int capacity() {
        return this.size;
    }

    public T[] getData() {
        return data;
    }

}