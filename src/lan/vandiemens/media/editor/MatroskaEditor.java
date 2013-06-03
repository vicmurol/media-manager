package lan.vandiemens.media.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.ResourceBundle;
import lan.vandiemens.media.MediaFile;
import lan.vandiemens.media.SubtitleFile;
import static lan.vandiemens.media.editor.MkvToolNixUtils.getMkvMergeExecutable;
import static lan.vandiemens.media.editor.MkvToolNixUtils.getMkvPropEditExecutable;
import static lan.vandiemens.media.editor.MkvToolNixUtils.getMkvPropEditPath;
import static lan.vandiemens.media.info.MediaInfoHelper.getMediaInfoExecutable;
import lan.vandiemens.util.file.FileExtensionFilter;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author vmurcia
 */
public class MatroskaEditor {

    public static final String[] supportedVideoFileFormats = {"mkv", "avi", "divx", "mp4", "ogm"};
    public static final String[] supportedSubtitleFileFormats = {"srt"};
    public static final String HELP_OPTION = "-h";
    public static final String LONG_HELP_OPTION = "--help";
    public static final String VERSION_OPTION = "-v";
    public static final String LONG_VERSION_OPTION = "--version";


    private MatroskaEditor() {
        // Do not instantiate
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Check arguments entered by the user
        if (args.length == 0) {
            showUsage();
            System.exit(0);
        }

        // Parse argument list looking for options
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case HELP_OPTION:
                case LONG_HELP_OPTION:
                    showUsage();
                    System.exit(0);
                    break;
                case VERSION_OPTION:
                case LONG_VERSION_OPTION:
                    showVersion();
                    System.exit(0);
                    break;
                default:
                    showUsage();
                    System.exit(0);
            }
        }

        File moviesDir = new File(args[args.length - 1]);
        MatroskaEditor.process(moviesDir);
    }

    /**
     * Edits supported videos located in the given directory.
     * <p>
     * NOTE: Edition consists of adding text subtitle tracks (if provided as
     * SubRip subtitle files in the same folder), disabling unwanted audio
     * and subtitle tracks, and renaming Matroska properties fields.
     */
    public static void process(File folder) {
        checkApplicationDependencies();
        checkIfValidDirectory(folder);

        MediaFile[] mediaFiles = getEditableMediaFiles(folder);
        if (mediaFiles.length == 0) {
            System.out.println("No valid media files to edit!");
            return;
        }
        SubtitleFile[] subtitleFiles = getSupportedSubtitleFiles(folder);
        fixWrongSubtitleFiles(subtitleFiles);
        map(mediaFiles, subtitleFiles);

        mediaFiles = discardIncomplete(mediaFiles);
        disableUnwantedTracks(mediaFiles);

        Command[] commands = getMkvToolNixCommands(mediaFiles);
        runWithMkvToolNix(commands);
    }

    private static void checkApplicationDependencies() {
        checkIfMediaInfoIsInstalled();
        checkIfMkvToolNixIsInstalled();
    }

    private static void checkIfValidDirectory(File folder) {
        if (folder == null) {
            throw new IllegalArgumentException(folder + " can't be null!");
        }
        if (!folder.exists()) {
            throw new IllegalArgumentException(folder + " doesn't exist!");
        }
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException(folder + " is not a directory!");
        }
        System.out.println("Media folder: " + folder.getAbsolutePath());
    }

    private static void checkIfMkvToolNixIsInstalled() {
        System.out.println("Checking if MKVToolNix is installed on this system...");
        System.out.print("Looking for " + getMkvMergeExecutable().getAbsolutePath() + "... ");
        System.out.println(getMkvMergeExecutable().exists() ? "Found" : "Not found");
        System.out.print("Looking for " + getMkvPropEditPath() + "... ");
        System.out.println(getMkvPropEditExecutable().exists() ? "Found" : "Not found");
        if (getMkvMergeExecutable().exists() && getMkvPropEditExecutable().exists()) {
            System.out.println("> MKVToolNix found\n");
        } else {
            throw new RuntimeException("MkvToolNix not found");
        }
    }

    private static void checkIfMediaInfoIsInstalled() {
        System.out.println("Checking if MediaInfo is installed on this system...");
        System.out.print("Looking for " + getMediaInfoExecutable().getAbsolutePath() + "... ");
        System.out.println(getMediaInfoExecutable().exists() ? "Found" : "Not found");
        if (getMediaInfoExecutable().exists()) {
            System.out.println("> MediaInfo found\n");
        } else {
            throw new RuntimeException("MediaInfo not found");
        }
    }

    private static void runWithMkvToolNix(Command[] commands) {
        for (Command command : commands) {
            System.out.println("Command: " + command);
            try {
                if (execute(command)) {
                    System.out.println(command.getOutputFile().getName() + " processed successfully");
                    performPostCommandTasks(command);
                } else {
                    System.out.println(command.getOutputFile().getName() + " processed with errors!");
                }
            } catch (IOException ex) {
                System.out.println(command.getOutputFile().getName() + " processed with errors!");
            }
        }
    }

    private static void performPostCommandTasks(Command command) {
        if (command instanceof MkvPropEditCommand) {
            File inputFile = command.getInputFile();
            File outputFile = command.getOutputFile();
            if (inputFile.renameTo(outputFile)) {
                System.out.println(inputFile.getName() + " successfully renamed to " + outputFile.getName());
            } else {
                System.out.println(inputFile.getName() + " couldn't be renamed to " + outputFile.getName());
            }
        }
    }

    private static void showUsage() {
        System.out.println("Usage:");
        System.out.println("    MatroskaEditor [-h | -v] <movies_dir>");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("    -h, --help     Shows this help information.");
        System.out.println("    -v, --version  Shows the version of this program.");
    }

    private static void showVersion() {
        String versionNumber = ResourceBundle.getBundle("version").getString("VERSION");
        String buildNumber = ResourceBundle.getBundle("version").getString("BUILD");
        System.out.println("MatroskaEditor v" + versionNumber + " Build " + buildNumber);
    }

    /**
     * Runs a platform-dependent command to edit Matroska videos.
     *
     * @param command the command to be executed
     * @return <code>true</code> if the command was successfully executed,
     *         <code>false</code> otherwise
     */
    private static boolean execute(Command command) throws IOException {
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

    @SuppressWarnings("UseSpecificCatch")
    private static MediaFile[] getEditableMediaFiles(File folder) {
        File[] containerFiles = folder.listFiles(new FileExtensionFilter(supportedVideoFileFormats));
        if (containerFiles.length == 0) {
            return new MediaFile[0];
        }
        ArrayList<MediaFile> mediaFiles = new ArrayList<>(containerFiles.length);
        System.out.println("Candidate media files:");
        System.out.println(ArrayUtils.toString(containerFiles));
        for (File file : containerFiles) {
            try {
                mediaFiles.add(new MediaFile(file));
                System.out.println("Video file: " + file.getName() + "... parsed");
            } catch (Exception ex) {
                System.out.println("Video file: " + file.getName() + "... skipped");
                System.out.println("Reason: " + ex.getMessage());
            }
        }
        System.out.println();

        return mediaFiles.toArray(new MediaFile[mediaFiles.size()]);
    }

    private static SubtitleFile[] getSupportedSubtitleFiles(File folder) {
        File[] textSubFiles = folder.listFiles(new FileExtensionFilter(supportedSubtitleFileFormats));
        if (textSubFiles.length == 0) {
            return new SubtitleFile[0];
        }
        ArrayList<SubtitleFile> subtitleFiles = new ArrayList<>(textSubFiles.length);
        System.out.println("Found text subtitle files:");
        for (File file : textSubFiles) {
            try {
                subtitleFiles.add(new SubtitleFile(file));
                System.out.println("Subtitle file: " + file.getName() + "... parsed");
            } catch (Exception ex) {
                System.out.println("Subtitle file: " + file.getName() + "... skipped");
                System.out.println("Reason: " + ex.getMessage());
            }
        }
        System.out.println();

        return subtitleFiles.toArray(new SubtitleFile[subtitleFiles.size()]);
    }

    private static void fixWrongSubtitleFiles(SubtitleFile[] subtitleFiles) {
        for (SubtitleFile subtitleFile : subtitleFiles) {
            if (subtitleFile.isTextBased()) {
                try {
                    subtitleFile.fixEncoding();
                } catch (IOException ex) {
                    System.out.println("Encoding could not be fixed for " + subtitleFile.getName());
                }
            }
        }
    }

    /**
     * Binds every subtitle file to its corresponding media file.
     *
     * @param mediaFiles the media files which may need additional subtitles
     * @param subtitleFiles the subtitle files to be mapped to media files
     */
    private static void map(MediaFile[] mediaFiles, SubtitleFile[] subtitleFiles) {
        System.out.println("Mapping subtitle files to Matroska container files...");
        for (MediaFile mediaFile : mediaFiles) {
            for (SubtitleFile subtitleFile : subtitleFiles) {
                if (subtitleFile.correspondsTo(mediaFile)) {
                    mediaFile.addSubtitle(subtitleFile);
                }
            }
        }
    }

    private static MediaFile[] discardIncomplete(MediaFile[] mediaFiles) {
        System.out.println("Discarding incomplete media files...");
        ArrayList<MediaFile> completeMediaFiles = new ArrayList<>();
        for (MediaFile mediaFile : mediaFiles) {
            if (mediaFile.meetsLanguageRequirements()) {
                completeMediaFiles.add(mediaFile);
            } else {
                System.out.println("Media file \"" + mediaFile.getMainFile().getName() + "\" has been discarded!");
            }
        }
        return completeMediaFiles.toArray(new MediaFile[completeMediaFiles.size()]);
    }

    private static void disableUnwantedTracks(MediaFile[] mediaFiles) {
        for (int i = 0; i < mediaFiles.length; i++) {
            mediaFiles[i].disableUnwantedTracks();
        }
    }

    private static Command[] getMkvToolNixCommands(MediaFile[] mediaFiles) {
        Command[] commands = new Command[mediaFiles.length];
        for (int i = 0; i < mediaFiles.length; i++) {
            if (mediaFiles[i].needsRemux()) {
                commands[i] = new MkvMergeCommand(mediaFiles[i]);
            } else {
                commands[i] = new MkvPropEditCommand(mediaFiles[i]);
            }
        }
        return commands;
    }
}
