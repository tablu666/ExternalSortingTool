import student.TestCase;

/**
 * The purpose of this class is to test
 * the methods of class MinHeap.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-30
 */
public class MinHeapTest extends TestCase {

    /**
     * minHeap
     */
    private MinHeap<Integer> minHeap;

    @Override
    /**
     * setUp
     */
    protected void setUp() throws Exception {
        Integer[] data = new Integer[5];
        for (int i = 0; i < 5; i++) {
            data[i] = 5 - i;
        }
        this.minHeap = new MinHeap<>(data, 5, 5);
    }

    /**
     * testHeapSize
     */
    public void testHeapSize() {
        assertEquals(5, minHeap.heapSize());
    }

    /**
     * testSetHeapSize
     */
    public void testSetHeapSize() {
        minHeap.setHeapSize(4);
        assertEquals(4, minHeap.heapSize());
    }

    /**
     * testIsLeaf
     */
    public void testIsLeaf() {
        assertFalse(minHeap.isLeaf(0));
    }

    /**
     * testLeftChild
     */
    public void testLeftChild() {
        assertEquals(1, minHeap.leftChild(0));
    }

    /**
     * testBuildHeap
     */
    public void testBuildHeap() {
        minHeap.buildHeap();
        int i = minHeap.getData()[0];
        assertEquals(1, i);
    }

    /**
     * testRemoveMin
     */
    public void testRemoveMin() {
        minHeap.removeMin();
        int i = minHeap.getData()[0];
        assertEquals(2, i);
    }

    /**
     * testSift
     */
    public void testSift() {
        minHeap.sift(0);
        int i = minHeap.getData()[0];
        assertEquals(1, i);
    }

    /**
     * testEmpty
     */
    public void testEmpty() {
        assertFalse(minHeap.empty());
    }

    /**
     * testCapacity
     */
    public void testCapacity() {
        assertEquals(5, minHeap.capacity());
    }

    /**
     * testGetData
     */
    public void testGetData() {
        assertEquals(1, (int) minHeap.getData()[0]);
    }
}