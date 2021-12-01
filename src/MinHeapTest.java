import student.TestCase;

/**
 * The purpose of this class is to test
 * the methods of class MinHeap.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-30
 */
public class MinHeapTest extends TestCase {

    private MinHeap<Integer> minHeap;

    @Override
    protected void setUp() throws Exception {
        Integer[] data = new Integer[5];
        for (int i = 0; i < 5; i++) {
            data[i] = 5 - i;
        }
        this.minHeap = new MinHeap<>(data, 5, 5);
    }

    public void testHeapSize() {
        assertEquals(5, minHeap.heapSize());
    }

    public void testSetHeapSize() {
        minHeap.setHeapSize(4);
        assertEquals(4, minHeap.heapSize());
    }

    public void testIsLeaf() {
        assertFalse(minHeap.isLeaf(0));
    }

    public void testLeftChild() {
        assertEquals(1, minHeap.leftChild(0));
    }

    public void testBuildHeap() {
        minHeap.buildHeap();
        int i = minHeap.getData()[0];
        assertEquals(1, i);
    }

    public void testRemoveMin() {
        minHeap.removeMin();
        int i = minHeap.getData()[0];
        assertEquals(2, i);
    }

    public void testSift() {
        minHeap.sift(0);
        int i = minHeap.getData()[0];
        assertEquals(1, i);
    }

    public void testCapacity() {
        assertEquals(5, minHeap.capacity());
    }

    public void testGetData() {
        assertEquals(1, (int) minHeap.getData()[0]);
    }
}