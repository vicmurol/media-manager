package lan.vandiemens.media.matroska;

import java.io.File;
import java.io.IOException;
import java.util.List;
import lan.vandiemens.media.MediaFile;
import lan.vandiemens.media.analysis.MediaInfoException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vmurcia
 */
public class MkvExtractSubtitlesCommandTest {

    private static MediaFile captainPhillipsSample;

    @BeforeClass
    public static void setUpClass() throws IOException, MediaInfoException {
        File captainPhillipsFile = new File("test/lan/vandiemens/media/matroska/Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD.xml");
        captainPhillipsSample = new MediaFile(captainPhillipsFile);
    }

    @Before
    public void setUp() {
        System.out.println(""); // Console output separator before each test case
    }

    /**
     * Test of toString method, of class MkvExtractSubtitlesCommand.
     */
    @Test
    public void testToString() {
        System.out.println("Testing mkvextract command generation...");
        MkvExtractSubtitlesCommand command = new MkvExtractSubtitlesCommand(captainPhillipsSample);
        String expectedCommand = "\"C:\\Program Files (x86)\\MKVToolNix\\mkvextract.exe\" tracks \"C:\\Users\\d3m0sth3n3s\\Development\\NetBeans\\Media Manager\\test\\lan\\vandiemens\\media\\matroska\\Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD.xml\" 3:test\\lan\\vandiemens\\media\\matroska\\Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD_track4_eng.srt 4:test\\lan\\vandiemens\\media\\matroska\\Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD_track5_eng.srt 5:test\\lan\\vandiemens\\media\\matroska\\Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD_track6_Forced_eng.sup 6:test\\lan\\vandiemens\\media\\matroska\\Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD_track7_fre.sup 7:test\\lan\\vandiemens\\media\\matroska\\Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD_track8_fre.sup 8:test\\lan\\vandiemens\\media\\matroska\\Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD_track9_Forced_fre.sup 9:test\\lan\\vandiemens\\media\\matroska\\Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD_track10_spa.sup 10:test\\lan\\vandiemens\\media\\matroska\\Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD_track11_Forced_spa.sup 11:test\\lan\\vandiemens\\media\\matroska\\Captain.Phillips.2013.1080p.BluRay.DTS-HD.MA.5.1.x264-PublicHD_track12_eng.sup";
        assertEquals(expectedCommand, command.toString());
    }
}
