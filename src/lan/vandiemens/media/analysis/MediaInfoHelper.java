package lan.vandiemens.media.analysis;

import java.io.File;
import lan.vandiemens.util.SystemUtils;

/**
 *
 * @author vmurcia
 */
public class MediaInfoHelper {

    private MediaInfoHelper() {
        // Do not instantiate
    }

    public static boolean isMediaInfoInstalled() {
        File mediaInfoExe = getMediaInfoCliExecutable();
        return mediaInfoExe.exists();
    }

    public static File getMediaInfoCliExecutable() {
        File exeFile;
        if (SystemUtils.isWindows()) {
            exeFile = new File("/Program Files/MediaInfo CLI/mediainfo.exe");
        } else if (SystemUtils.isMacOs()) {
            exeFile = new File("/usr/local/bin/mediainfo");
        } else {
            exeFile = new File("./mediainfo"); // TODO Add Linux support
        }
        return exeFile;
    }

    public static String getMediaInfoCliExecutablePath() {
        return getMediaInfoCliExecutable().getAbsolutePath();
    }
}
