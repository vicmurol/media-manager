package lan.vandiemens.media.matroska;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import lan.vandiemens.util.SystemUtils;

/**
 *
 * @author vmurcia
 */
public class MkvToolNixHelper {

    public static final String MKVTOOLNIX_WINDOWS_X86_INSTALL_FOLDER = "/Program Files/MKVToolNix/";
    public static final String MKVTOOLNIX_WINDOWS_X64_INSTALL_FOLDER = "/Program Files (x86)/MKVToolNix/";
    public static final String MKVTOOLNIX_OSX_INSTALL_FOLDER = "/usr/local/bin/";
    public static final String MKVTOOLNIX_LINUX_INSTALL_FOLDER = "./"; // TODO: Find out MkvToolNix install folder on Linux systems


    private MkvToolNixHelper() {
        // Do not instantiate
    }

    public static boolean isMkvToolNixInstalled() {
        File mkvMergeExe = getMkvMergeExecutable();
        System.out.print("Looking for " + mkvMergeExe.getAbsolutePath() + "... ");
        System.out.println(mkvMergeExe.exists() ? "Found" : "Not found");
        File mkvPropEditExe = getMkvPropEditExecutable();
        System.out.print("Looking for " + mkvPropEditExe.getAbsolutePath() + "... ");
        System.out.println(mkvPropEditExe.exists() ? "Found" : "Not found");
        File mkvExtractExe = getMkvExtractExecutable();
        System.out.print("Looking for " + mkvExtractExe.getAbsolutePath() + "... ");
        System.out.println(mkvExtractExe.exists() ? "Found" : "Not found");
        return mkvMergeExe.exists() && mkvPropEditExe.exists() && mkvExtractExe.exists();
    }

    public static String getMkvToolNixInstallFolder() {
        String installPath;

        if (SystemUtils.isWindows()) {
            if (SystemUtils.isWindows64bits()) {
                installPath = MKVTOOLNIX_WINDOWS_X64_INSTALL_FOLDER;
            } else {
                installPath = MKVTOOLNIX_WINDOWS_X86_INSTALL_FOLDER;
            }
        } else if (SystemUtils.isMacOs()) {
            installPath = MKVTOOLNIX_OSX_INSTALL_FOLDER;
        } else {
            installPath = MKVTOOLNIX_LINUX_INSTALL_FOLDER; // TODO: Add Linux support
        }

        File folder = new File(installPath);
        return folder.getAbsolutePath() + File.separator;
    }

    public static File getMkvMergeExecutable() {
        File mkvMergeExeFile;

        if (SystemUtils.isWindows()) {
            if (SystemUtils.isWindows64bits()) {
                mkvMergeExeFile = new File(MKVTOOLNIX_WINDOWS_X64_INSTALL_FOLDER, "mkvmerge.exe");
            } else {
                mkvMergeExeFile = new File(MKVTOOLNIX_WINDOWS_X86_INSTALL_FOLDER, "mkvmerge.exe");
            }
        } else if (SystemUtils.isMacOs()) {
            mkvMergeExeFile = new File(MKVTOOLNIX_OSX_INSTALL_FOLDER, "mkvmerge");
        } else {
            mkvMergeExeFile = new File(MKVTOOLNIX_LINUX_INSTALL_FOLDER, "mkvmerge"); // TODO: Add Linux support
        }

        return mkvMergeExeFile;
    }

    public static String getMkvMergeExePath() {
        return getMkvMergeExecutable().getAbsolutePath();
    }

    public static File getMkvPropEditExecutable() {
        File mkvPropEditExeFile;

        if (SystemUtils.isWindows()) {
            if (SystemUtils.isWindows64bits()) {
                mkvPropEditExeFile = new File(MKVTOOLNIX_WINDOWS_X64_INSTALL_FOLDER, "mkvpropedit.exe");
            } else {
                mkvPropEditExeFile = new File(MKVTOOLNIX_WINDOWS_X86_INSTALL_FOLDER, "mkvpropedit.exe");
            }
        } else if (SystemUtils.isMacOs()) {
            mkvPropEditExeFile = new File(MKVTOOLNIX_OSX_INSTALL_FOLDER, "mkvpropedit");
        } else {
            mkvPropEditExeFile = new File(MKVTOOLNIX_LINUX_INSTALL_FOLDER, "mkvpropedit"); // TODO: Add Linux support
        }

        return mkvPropEditExeFile;
    }

    public static String getMkvPropEditExePath() {
        return getMkvPropEditExecutable().getAbsolutePath();
    }

    public static File getMkvExtractExecutable() {
        File mkvExtractExeFile;

        if (SystemUtils.isWindows()) {
            if (SystemUtils.isWindows64bits()) {
                mkvExtractExeFile = new File(MKVTOOLNIX_WINDOWS_X64_INSTALL_FOLDER, "mkvextract.exe");
            } else {
                mkvExtractExeFile = new File(MKVTOOLNIX_WINDOWS_X86_INSTALL_FOLDER, "mkvextract.exe");
            }
        } else if (SystemUtils.isMacOs()) {
            mkvExtractExeFile = new File(MKVTOOLNIX_OSX_INSTALL_FOLDER, "mkvextract");
        } else {
            mkvExtractExeFile = new File(MKVTOOLNIX_LINUX_INSTALL_FOLDER, "mkvextract"); // TODO: Add Linux support
        }

        return mkvExtractExeFile;
    }

    public static String getMkvExtractExePath() {
        return getMkvExtractExecutable().getAbsolutePath();
    }

    /**
     * Runs a platform-dependent command to edit Matroska videos.
     *
     * @param command the command to be executed
     * @return <code>true</code> if the command was successfully executed,
     *         <code>false</code> otherwise
     * @throws java.io.IOException
     */
    public static boolean execute(Command command) throws IOException {
        System.out.println("Processing...");

        Process process;
        BufferedReader reader = null;
        boolean result = false;
        try {
            process = new ProcessBuilder(command.toList()).start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitValue = process.waitFor();
            switch (exitValue) {
                case 0:
                    System.out.println("Muxing/Edition has completed successfully");
                    result = true;
                    break;
                case 1:
                    System.out.println("Some issues occurred during the muxing/edition process!");
                    System.out.println("Check both the warnings and the resulting file");
                    result = true;
                    break;
                case 2:
                    System.out.println("ERROR: Muxing/Edition operation has been aborted!");
                    break;
                default: // Couldn't happen
                    System.out.println("Exited with error code: " + exitValue);
            }
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.getClass());
            System.out.println(ex.getMessage());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return result;
    }
}
