package lan.vandiemens.media.matroska;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vmurcia
 */
public class MkvChaptersTest {

    File chaptersFile;
    MkvChapters chapters;

    public MkvChaptersTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        chaptersFile = new File("test/lan/vandiemens/media/matroska", "Disconnect.2013.1080p.BluRay.DTS.x264-PublicHD_chapters.xml");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testReadingChaptersFromFile() {
        try {
            chapters = new MkvChapters(chaptersFile);
        } catch (MatroskaException ex) {
            Logger.getLogger(MkvChaptersTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals("Incorrect chapter count", 16, chapters.getCount());
    }

    @Test
    public void testCount() {
        int count = 10;
        chapters = new MkvChapters(count);
        assertEquals("Incorrect chapter count", count, chapters.getCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCount() {
        int count = -5;
        chapters = new MkvChapters(count);
        assertEquals("Incorrect chapter count", count, chapters.getCount());
    }
}