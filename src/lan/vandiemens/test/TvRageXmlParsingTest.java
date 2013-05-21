package lan.vandiemens.test;

import java.io.File;
import nu.xom.*;

/**
 *
 * @author vmurcia
 */
public class TvRageXmlParsingTest {

    public static void main(String[] args) {
        String urlName = "http://services.tvrage.com/feeds/episodeinfo.php?sid=2930&ep=2x04";
        try {
            Builder parser = new Builder();
            Document xmlDocument = parser.build(urlName);
            System.out.println(xmlDocument.toXML());
//            xmlDocument
//            Element root = xmlDocument.getRootElement(); // <Mediainfo>
//            System.out.println("Root element: " + root.getLocalName());
//            System.out.println("Mediainfo version: " + root.getAttributeValue("version"));
//            //System.out.println(root.getChildCount());
//            // Notice that:
//            // <mediainfo><whatever...></mediainfo>
//            // yields childcount = 1 but:
//            // <mediainfo>
//            //     <whatever...>
//            // </mediainfo>
//            // yields childcount = 3 (because of the line breaks)
//            Element fileElement = root.getFirstChildElement("File");
//            Elements trackElements = fileElement.getChildElements("track");
//            System.out.println("Number of tracks: " + trackElements.size());
//            Element generalTrack = trackElements.get(0);
//            System.out.println("First track: " + generalTrack.getAttributeValue("type"));
//
//            // Print General track information
//            Element element = generalTrack.getFirstChildElement("Unique_ID");
//            System.out.println(element.getLocalName() + ": " + ((element != null) ? element.getValue() : null));
//            element = generalTrack.getFirstChildElement("Complete_name");
//            System.out.println(element.getLocalName() + ": " + ((element != null) ? element.getValue() : null));
//            element = generalTrack.getFirstChildElement("Format");
//            System.out.println(element.getLocalName() + ": " + ((element != null) ? element.getValue() : null));
//            element = generalTrack.getFirstChildElement("Format_version");
//            System.out.println(element.getLocalName() + ": " + ((element != null) ? element.getValue() : null));
//            element = generalTrack.getFirstChildElement("File_size");
//            System.out.println(element.getLocalName() + ": " + ((element != null) ? element.getValue() : null));
//            element = generalTrack.getFirstChildElement("Duration");
//            System.out.println(element.getLocalName() + ": " + ((element != null) ? element.getValue() : null));
//            element = generalTrack.getFirstChildElement("Overall_bit_rate");
//            System.out.println(element.getLocalName() + ": " + ((element != null) ? element.getValue() : null));
//            element = generalTrack.getFirstChildElement("Encoded_date");
//            System.out.println(element.getLocalName() + ": " + ((element != null) ? element.getValue() : null));
//            element = generalTrack.getFirstChildElement("Writing_application");
//            System.out.println(element.getLocalName() + ": " + ((element != null) ? element.getValue() : null));
//            element = generalTrack.getFirstChildElement("Writing_library");
//            System.out.println(element.getLocalName() + ": " + ((element != null) ? element.getValue() : null));

        // Indicates a well-formedness error
        } catch (ParsingException ex) {
            System.err.println("MediaInfo XML output is malformed!");
            System.err.println(ex.getMessage());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}
