package lan.vandiemens.media.subtitle;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author vmurcia
 */
public class SubtitleUtilsTest {

    private static Path fhiSubtitleFile;
    private static Path fhiSubtitleFile2;
    private static Path normalSubtitleFile;
    private static Path nonUtf8SubtitleFile;
    private static Path nonExistentSubtitleFile;
    private static Path directory;

    @Before
    public void setUp() {
        System.out.println(""); // Console output separator before each test case
    }

    @BeforeClass
    public static void setUpClass() {
        fhiSubtitleFile = Paths.get("test/lan/vandiemens/media/subtitle/fhi_subtitle_sample.srt");
        fhiSubtitleFile2 = Paths.get("test/lan/vandiemens/media/subtitle/fhi_subtitle_sample2.srt");
        normalSubtitleFile = Paths.get("test/lan/vandiemens/media/subtitle/normal_subtitle_sample.srt");
        nonUtf8SubtitleFile = Paths.get("test/lan/vandiemens/media/subtitle/non_UTF8_subtitle_sample.srt");
        nonExistentSubtitleFile = Paths.get("test/lan/vandiemens/media/subtitle/non_existent_subtitle_sample.srt");
        directory = Paths.get("test/lan/vandiemens/media/subtitle/");
    }

    /**
     * Test of isSubtitleForHearingImpaired method, of class SubtitleUtils.
     * @throws java.io.IOException
     */
    @Test
    public void testIsSubtitleForHearingImpaired() throws IOException {
        System.out.println("Testing FHI analysis of valid subtitle files...");
        assertTrue(SubtitleUtils.isSubtitleForHearingImpaired(fhiSubtitleFile));
        assertTrue(SubtitleUtils.isSubtitleForHearingImpaired(fhiSubtitleFile2));
        assertFalse(SubtitleUtils.isSubtitleForHearingImpaired(normalSubtitleFile));
    }

    /**
     * Test of isSubtitleForHearingImpaired method, of class SubtitleUtils.
     * @throws java.io.IOException
     */
    @Test (expected = IllegalArgumentException.class)
    public void testSubtitleFileNotFound() throws IOException {
        System.out.println("Testing FHI analysis of non existent subtitle files...");
        SubtitleUtils.isSubtitleForHearingImpaired(nonExistentSubtitleFile);
    }

    /**
     * Test of isSubtitleForHearingImpaired method, of class SubtitleUtils.
     * @throws java.io.IOException
     */
    @Test (expected = IllegalArgumentException.class)
    public void testDirectoryInsteadOfSubtitleFile() throws IOException {
        System.out.println("Testing directory not accepted for FHI analysis...");
        SubtitleUtils.isSubtitleForHearingImpaired(directory);
    }

    /**
     * Test of isSubtitleForHearingImpaired method, of class SubtitleUtils.
     * @throws java.io.IOException
     */
    @Test (expected = IllegalArgumentException.class)
    public void testNonUtf8SubtitleAnalysis() throws IOException {
        System.out.println("Testing FHI analysis of non UTF-8 subtitle files...");
        SubtitleUtils.isSubtitleForHearingImpaired(nonUtf8SubtitleFile);
    }
}
