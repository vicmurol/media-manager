package lan.vandiemens.test;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import lan.vandiemens.media.matroska.MatroskaException;
import lan.vandiemens.media.matroska.MkvChapters;

/**
 *
 * @author vmurcia
 */
public class MkvChaptersTest {

    public static void main(String[] args) {
        File chaptersFile = new File("src/lan/vandiemens/media/sample",
                "Disconnect.2013.1080p.BluRay.DTS.x264-PublicHD_chapters.xml");
        MkvChapters chapters;
        try {
            chapters = new MkvChapters(chaptersFile);
            System.out.println("Chapter count = " + chapters.getCount());
        } catch (MatroskaException ex) {
            Logger.getLogger(MkvChaptersTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
