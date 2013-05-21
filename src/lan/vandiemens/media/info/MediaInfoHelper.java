package lan.vandiemens.media.info;

import java.io.File;
import lan.vandiemens.util.SystemUtils;

/**
 *
 * @author vmurcia
 */
public class MediaInfoHelper {

    private static File mediaInfoExecutable = null;


    public static File getMediaInfoExecutable() {
        if (mediaInfoExecutable == null) {
            mediaInfoExecutable = searchMediaInfoExecutable();
        }
        return mediaInfoExecutable;
    }

    private static File searchMediaInfoExecutable() {
        File mediaInfoExeFile;

        if (SystemUtils.isWindows()) {
            mediaInfoExeFile = new File("/Program Files/MediaInfo CLI/mediainfo.exe");
        } else if (SystemUtils.isMacOs()) {
            mediaInfoExeFile = new File("/usr/local/bin/mediainfo");
        } else {
            mediaInfoExeFile = null; // TODO Add Linux support
        }

        return mediaInfoExeFile;
    }
}
