package lan.vandiemens.media.editor;

import java.io.File;
import lan.vandiemens.util.SystemUtils;

/**
 *
 * @author vmurcia
 */
public class MkvToolNixUtils {

    public static File getMkvMergeExecutable() {
        File mkvMergeExeFile;

        if (SystemUtils.isWindows()) {
            if (SystemUtils.isWindows64bits()) {
                mkvMergeExeFile = new File("/Program Files (x86)/MKVToolNix/mkvmerge.exe");
            } else {
                mkvMergeExeFile = new File("/Program Files/MKVToolNix/mkvmerge.exe");
            }
        } else if (SystemUtils.isMacOs()) {
            mkvMergeExeFile = new File("/usr/local/bin/mkvmerge");
        } else {
            mkvMergeExeFile = null; // TODO Add Linux support
        }

        return mkvMergeExeFile;
    }

    public static File getMkvPropEditExecutable() {
        File mkvPropEditExeFile;

        if (SystemUtils.isWindows()) {
            if (SystemUtils.isWindows64bits()) {
                mkvPropEditExeFile = new File("/Program Files (x86)/MKVToolNix/mkvpropedit.exe");
            } else {
                mkvPropEditExeFile = new File("/Program Files/MKVToolNix/mkvpropedit.exe");
            }
        } else if (SystemUtils.isMacOs()) {
            mkvPropEditExeFile = new File("/usr/local/bin/mkvpropedit");
        } else {
            mkvPropEditExeFile = null; // TODO Add Linux support
        }

        return mkvPropEditExeFile;
    }
}
