package lan.vandiemens.media.matroska.utility;

import java.io.File;
import static lan.vandiemens.media.analysis.MediaInfoHelper.getMediaInfoCliExecutablePath;
import static lan.vandiemens.media.analysis.MediaInfoHelper.isMediaInfoInstalled;
import static lan.vandiemens.media.matroska.MkvToolNixHelper.getMkvToolNixInstallFolder;
import static lan.vandiemens.media.matroska.MkvToolNixHelper.isMkvToolNixInstalled;

/**
 * Basic functionality for a Matroska utility.
 *
 * @author vmurcia
 */
public class MatroskaUtility {

    public static final String MATROSKA_FILE_EXTENSION = "mkv";


    protected static void checkApplicationDependencies() {
        checkIfMediaInfoIsInstalled();
        checkIfMkvToolNixIsInstalled();
    }

    protected static void checkIfMediaInfoIsInstalled() {
        System.out.println("Checking if MediaInfo is installed on this system...");
        if (isMediaInfoInstalled()) {
            System.out.println("> MediaInfo found: " + getMediaInfoCliExecutablePath());
            System.out.println(""); // Vertical separation
        } else {
            throw new RuntimeException("MediaInfo not found: " + getMediaInfoCliExecutablePath());
        }
    }

    protected static void checkIfMkvToolNixIsInstalled() {
        System.out.println("Checking if MKVToolNix is installed on this system...");
        if (isMkvToolNixInstalled()) {
            System.out.println("> MKVToolNix found: " + getMkvToolNixInstallFolder());
            System.out.println(""); // Vertical separation
        } else {
            throw new RuntimeException("MkvToolNix not found: " + getMkvToolNixInstallFolder());
        }
    }

    protected static void checkIfValidDirectory(File candidate) {
        if (!candidate.isDirectory()) {
            throw new IllegalArgumentException(candidate + " is not a existing directory!");
        }
        System.out.println("Media folder: " + candidate.getAbsolutePath());
    }
}
