import student.TestCase;

import java.util.Arrays;

/**
 * The purpose of this class is to test
 * the methods of class MaxHeap.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-30
 */
public class MaxHeapTest extends TestCase {

    /**
     * Initialize
     */
    private MaxHeap<Integer> maxHeap;

    @Override
    /**
     * setup
     */
    protected void setUp() throws Exception {
        Integer[] data = new Integer[5];
        for (int i = 0; i < 5; i++) {
            data[i] = 5 - i;
        }
        this.maxHeap = new MaxHeap<>(data);
    }

    /**
     * testSort
     */
    public void testSort() {
        maxHeap.sort();
        assertEquals(java.util.Optional.ofNullable(
                maxHeap.getData()[0]),
                java.util.Optional.ofNullable(1));
    }

    /**
     * test get data
     */
    public void testGetData() {
        assertEquals(
                "[5, 4, 3, 2, 1]",
                Arrays.toString(maxHeap.getData()));
    }
}