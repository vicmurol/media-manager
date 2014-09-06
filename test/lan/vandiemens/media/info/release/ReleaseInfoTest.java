package lan.vandiemens.media.info.release;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vmurcia
 */
public class ReleaseInfoTest {

    ReleaseInfo sample;

    @Before
    public void setUp() {
        sample = new ReleaseInfo("halloween '13 in valencia");
        sample.setUploader("vmurcia");
        sample.setYear(2013);
    }

    @Test
    public void testGetFormattedTitle() {
        System.out.println("Testing get formatted title...");
        assertEquals("Halloween '13 In Valencia", sample.getFormattedTitle());
    }

    @Test
    public void testIsTitleCapitalized() {
        System.out.println("Testing if title is capitalized...");
        assertFalse(sample.isTitleCapitalized());
    }

    @Test
    public void testEqualsBasicInfo() {
        System.out.println("equalsBasicInfo");
        ReleaseInfo info2 = null;
        ReleaseInfo instance = null;
        boolean expResult = false;
        boolean result = instance.equalsBasicInfo(info2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testEqualsBasicInfoIgnoreSceneGroup() {
        System.out.println("equalsBasicInfoIgnoreSceneGroup");
        ReleaseInfo info2 = null;
        ReleaseInfo instance = null;
        boolean expResult = false;
        boolean result = instance.equalsBasicInfoIgnoreSceneGroup(info2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}