package lan.vandiemens.media.matroska;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**
 *
 * @author vmurcia
 */
public class MkvChapters {

    private final String EDITION_ENTRY_TAG = "EditionEntry";
    private final String CHAPTER_ATOM_TAG = "ChapterAtom";
    private int count;

    public MkvChapters(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Number of chapters must be positive");
        }
        this.count = count;
    }

    public MkvChapters(File file) throws MatroskaException {
        checkIfValid(file);
        parse(file);
    }

    private void checkIfValid(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException(file + " doesn't exist!");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file + " is a directory!");
        }
    }

    private void parse(File file) throws MatroskaException {
        System.out.println("Parsing \"" + file.getName() + "\" for Matroska chapters...");
        parse(getMatroskaChaptersAsXmlDocument(file));
    }

    private Document getMatroskaChaptersAsXmlDocument(File file) throws MatroskaException {
        BufferedReader reader = null;
        Document xmlDocument = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            boolean validationEnabled = true;
            Builder parser = new Builder(validationEnabled);
            xmlDocument = parser.build(reader);
        } catch (ValidityException ex) {
            System.out.println(ex.getMessage());
            throw new MatroskaException("The XML document is not valid according to matroskachapters.dtd");
        } catch (ParsingException ex) {
            System.out.println(ex.getMessage());
            throw new MatroskaException("The XML document is malformed");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            throw new MatroskaException("The given file could not be read");
        } finally {
            close(reader);
        }

        return xmlDocument;
    }

    private void close(BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private void parse(Document xmlDocument) throws MatroskaException {
        Element root = xmlDocument.getRootElement();  // <Chapters>
        Element editionEntry = root.getFirstChildElement(EDITION_ENTRY_TAG);
        Elements chapterAtoms = editionEntry.getChildElements(CHAPTER_ATOM_TAG);
        count = chapterAtoms.size();
    }

    public int getCount() {
        return count;
    }
}
