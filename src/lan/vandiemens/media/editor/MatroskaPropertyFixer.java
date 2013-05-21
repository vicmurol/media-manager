package lan.vandiemens.media.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import static lan.vandiemens.media.editor.MkvPropEditCommand.*;
import static lan.vandiemens.media.editor.MkvToolnixCommand.*;
import lan.vandiemens.media.info.MediaInfo;
import lan.vandiemens.media.info.MediaInfoException;
import lan.vandiemens.util.file.FileExtensionFilter;

/**
 * Multipurpose class to fix Matroska files which have been messed up.
 *
 * @author vmurcia
 */
public class MatroskaPropertyFixer {

    public static final String[] supportedVideoFileFormats = {"mkv"};

    private MatroskaPropertyFixer() {
        // Do not instantiate
    }

    public static void main(String[] args) {
        // Check arguments entered by the user
        if (args.length == 0) {
            showUsage();
            System.exit(0);
        }

        // Parse argument list looking for options
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-h":
                case "--help":
                    showUsage();
                    System.exit(0);
                    break;
                case "-v":
                case "--version":
                    showVersion();
                    System.exit(0);
                    break;
                default:
                    showUsage();
                    System.exit(0);
                    break;
            }
        }

        File moviesDir = new File(args[args.length - 1]);
        MatroskaPropertyFixer.process(moviesDir);
    }

    /**
     * Begin to edit all supported videos located in the media directory.
     */
    public static void process(File folder) {
        System.out.println("Media folder: " + folder.getAbsolutePath());
        File[] mkvFiles = folder.listFiles(new FileExtensionFilter(supportedVideoFileFormats));
        String[] fixedTitles = new String[mkvFiles.length];
        String[] commands = new String[mkvFiles.length];
        MediaInfo mediaInfo;
        for (int i = 0; i < mkvFiles.length; i++) {
            try {
                mediaInfo = new MediaInfo(mkvFiles[i]);
                System.out.println("Media information has been generated\n");
                System.out.println("Title: " + mediaInfo.getTitle());
                fixedTitles[i] = mediaInfo.getTitle().replace("Spartacus", "Spartacus: War of the Damned");
                System.out.println("Fixed Title: " + fixedTitles[i]);
                commands[i] = getCommand(mkvFiles[i], fixedTitles[i]);
                System.out.println("Command: " + commands[i]);
                execute(commands[i]);
            } catch (IOException ex) {
                System.err.println("Error executing command");
            } catch (MediaInfoException ex) {
                System.err.println("Could not get MediaInfo");
            }
        }
//        runWithMkvToolNix(commands);
    }

    private static String getCommand(File file, String title) {
        StringBuilder command = new StringBuilder(1024);
        command.append(DOUBLE_QUOTES).append(MkvToolNixUtils.getMkvPropEditExecutable().getAbsolutePath()).append(DOUBLE_QUOTES);
        command.append(SPACE);
        command.append(DOUBLE_QUOTES).append(file.getAbsolutePath()).append(DOUBLE_QUOTES);
        command.append(SPACE);
        command.append(EDIT_OPTION);
        command.append(SPACE);
        command.append(SEGMENT_INFO);
        command.append(SPACE);
        command.append(SET_OPTION);
        command.append(SPACE);
        command.append(DOUBLE_QUOTES).append(TITLE_PROPERTY).append(EQUALS_SIGN).append(title).append(DOUBLE_QUOTES);
        command.append(SPACE);
        command.append(EDIT_OPTION);
        command.append(SPACE);
        command.append(TRACK_HEADER_OPTION).append("v1");
        command.append(SPACE);
        command.append(SET_OPTION);
        command.append(SPACE);
        command.append(TRACK_NAME_PROPERTY).append("=").append("\"").append(title).append("\"");

        return command.toString();
    }

    /**
     * Edits media containers.
     *
     * @param command the command to be executed
     * @return <code>true</code> if the command was successfully executed,
     *         <code>false</code> otherwise
     */
    private static boolean execute(String command) throws IOException {
        System.out.println("Processing...");

        Process process;
        BufferedReader reader = null;
        boolean result = false;
        try {
            process = Runtime.getRuntime().exec(command);
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
                    System.out.println("Exited with error code " + exitValue);
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
    
    private static void showUsage() {
        System.out.println("Usage:");
        System.out.println("    MatroskaPropertyFixer [-h | -v] <movies_dir>");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("    -h, --help     Shows this help information.");
        System.out.println("    -v, --version  Shows the version of this program.");
    }

    private static void showVersion() {
        String versionNumber = ResourceBundle.getBundle("version").getString("VERSION");
        String buildNumber = ResourceBundle.getBundle("version").getString("BUILD");
        System.out.println("MatroskaPropertyFixer v" + versionNumber + " Build " + buildNumber);
    }
}
