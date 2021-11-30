import student.TestCase;

/**
 * The purpose of this class is to test
 * the methods of class RunInfo.
 *
 * @author Tianbo Lu & Yuechen Feng
 * @version 2021-11-30
 */
public class RunInfoTest extends TestCase {

    private RunInfo runInfo;

    @Override
    public void setUp() throws Exception {
        this.runInfo = new RunInfo(0, 0);
    }

    public void testGetStart() {
        assertEquals(0, runInfo.getStart());
    }

    public void testSetStart() {
        runInfo.setStart(1);
        assertEquals(1, runInfo.getStart());
    }

    public void testGetLength() {
        assertEquals(0, runInfo.getLength());
    }

    public void testSetLength() {
        runInfo.setLength(1);
        assertEquals(1, runInfo.getLength());
    }
}