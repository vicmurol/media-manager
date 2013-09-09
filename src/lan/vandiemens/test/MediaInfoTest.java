package lan.vandiemens.test;

import java.io.File;
import lan.vandiemens.media.analysis.MediaInfo;
import lan.vandiemens.media.info.track.Track;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

/**
 *
 * @author vmurcia
 */
public class MediaInfoTest {

    public static void main(String[] args) {
        File sampleFile = new File("src/lan/vandiemens/media/sample/immortals.info.xml");
        System.out.println("Analysing... " + sampleFile.getName());
        MediaInfo info;
        try {
            Builder parser = new Builder();
            Document xmlDocument = parser.build(sampleFile);
            System.out.println("Consider original language to be English...");
            info = new MediaInfo(xmlDocument, "English");
            Track[] tracks = info.getTracks();
            for (Track track : tracks) {
                System.out.println(track.toXml());
            }

            System.out.println();
            System.out.println();
            System.out.println();

            info.rearrange();
            tracks = info.getTracks();
            for (Track track : tracks) {
                System.out.println(track.toXml());
            }

            System.out.println();
            System.out.println();
            System.out.println();

            info.setOriginalOrder();
            tracks = info.getTracks();
            for (Track track : tracks) {
                System.out.println(track.toXml());
            }
        // Indicates a well-formedness error
        } catch (ParsingException ex) {
            System.err.println("MediaInfo XML output is malformed!");
            System.err.println(ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Some error found!");
            System.err.println(ex.getMessage());
        }
    }
}
