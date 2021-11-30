import student.TestCase;

public class RunInfoTest extends TestCase {

    private RunInfo runInfo;

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