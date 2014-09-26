package lan.vandiemens.media.matroska.utility;

import lan.vandiemens.media.matroska.MkvPropEditCommand;
import lan.vandiemens.media.matroska.Command;
import lan.vandiemens.media.matroska.MkvToolNixHelper;
import lan.vandiemens.media.matroska.MkvMergeCommand;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import lan.vandiemens.media.AudioFile;
import lan.vandiemens.media.MediaFile;
import lan.vandiemens.media.SubtitleFile;
import lan.vandiemens.media.analysis.MediaInfoException;
import lan.vandiemens.media.manager.VersionInfo;
import lan.vandiemens.util.file.DirectoryFilter;
import lan.vandiemens.util.file.FileExtensionFilter;

/**
 * An application for editing Matroska media files to my liking by using the
 * MkvToolNix project command line tools.
 * <p>
 * Specifically, it removes unwanted tracks from movies and TV series episodes
 * based on their type, language, and other available information. Also, it
 * rearranges the wanted tracks and generates their title following a specific
 * name pattern. In addition, default and forced track flags are applied as
 * appropriate.
 *
 * @author vmurcia
 */
public class MatroskaEditor extends MatroskaUtility {

    public static final String[] SUPPORTED_VIDEO_FILE_FORMATS = {"mkv", "avi", "divx", "mp4", "ogm"};
    public static final String[] SUPPORTED_AUDIO_FILE_FORMATS = {"aac", "ac3", "dts", "mp3", "ogg"};
    public static final String[] SUPPORTED_SUBTITLE_FORMATS = {"idx", "srt", "sup"};
    public static final String HELP_OPTION = "-h";
    public static final String LONG_HELP_OPTION = "--help";
    public static final String REPLACE_SUBTITLES_OPTION = "-r";
    public static final String LONG_REPLACE_SUBTITLES_OPTION = "--replace-subs";
    public static final String VERSION_OPTION = "-v";
    public static final String LONG_VERSION_OPTION = "--version";
    private static boolean subtitleSubstitutionEnabled = false;
    private static int videoFilesCount;
    private static int editedFilesCount;

    private static void enableSubtitleSubstitution(boolean enabled) {
        subtitleSubstitutionEnabled = enabled;
        System.out.println("Subtitle substitution has been " + (enabled ? "enabled" : "disabled"));
        printConsoleSeparator();
    }

    private MatroskaEditor() {
        // Do not instantiate
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        showVersion();

        // Check arguments entered by the user
        if (args.length == 0) {
            showUsage();
            System.exit(0);
        }

        // Parse argument list looking for options
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case REPLACE_SUBTITLES_OPTION:
                case LONG_REPLACE_SUBTITLES_OPTION:
                    MatroskaEditor.enableSubtitleSubstitution(true);
                    break;
                case VERSION_OPTION:
                case LONG_VERSION_OPTION:
                    System.exit(0);
                case HELP_OPTION:
                case LONG_HELP_OPTION:
                default:
                    showUsage();
                    System.exit(0);
            }
        }

        File mediaFolder = new File(args[args.length - 1]);
        MatroskaEditor.process(mediaFolder);
    }

    /**
     * Edits supported videos located in the given directory.
     * <p>
     * NOTE: Edition consists of adding text subtitle tracks (if provided as
     * SubRip subtitle files in the same folder), disabling unwanted audio
     * and subtitle tracks, and renaming Matroska properties fields.
     *
     * @param folder the directory containing the videos to be processed
     */
    public static void process(File folder) {
        checkApplicationDependencies();
        checkIfValidDirectory(folder);

        MediaFile[] mediaFiles = getEditableMediaFiles(folder);
        AudioFile[] audioFiles = getSupportedAudioFiles(folder);
        SubtitleFile[] subtitleFiles = getSupportedSubtitleFiles(folder);
        fixCommonSubtitleEncodingErrors(subtitleFiles);
        map(mediaFiles, audioFiles, subtitleFiles);

        for (MediaFile mediaFile : mediaFiles) {
            process(mediaFile);
        }

        printResultSummary();
    }

    private static void process(MediaFile mediaFile) {
        videoFilesCount++;
        mediaFile.fixCommonMetadataErrors();
        if (!mediaFile.meetsRequirements()) {
            System.out.println("Media file \"" + mediaFile.getName() + "\" has been discarded!");
            return;
        }
        printConsoleSeparator();

        if (subtitleSubstitutionEnabled) {
            mediaFile.preferExternalSubtitles();
        }
        mediaFile.disableUnwantedTracks();
        Command command = getMkvToolNixCommand(mediaFile);
        runWithMkvToolNix(command);
    }

    private static Command getMkvToolNixCommand(MediaFile mediaFile) {
        return mediaFile.needsRemux() ? new MkvMergeCommand(mediaFile) : new MkvPropEditCommand(mediaFile);
    }

    private static void runWithMkvToolNix(Command command) {
        printConsoleSeparator();
        System.out.println("Command: " + command);
        String filename = command.getOutputFile().getName();
        try {
            if (MkvToolNixHelper.execute(command)) {
                System.out.println(filename + " processed successfully");
                performPostCommandTasks(command);
                editedFilesCount++;
            } else {
                System.out.println(filename + " processed with errors!");
            }
        } catch (IOException ex) {
            System.out.println(filename + " processed with errors!");
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
        System.out.println("    MatroskaEditor [-h | -v] <media_dir>");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("    -h, --help     Shows this help information.");
        System.out.println("    -v, --version  Shows the version of this program.");
    }

    private static void showVersion() {
        System.out.println(VersionInfo.getMatroskaEditorFullName());
    }

    private static MediaFile[] getEditableMediaFiles(File folder) {
        File[] subfolders = folder.listFiles(new DirectoryFilter());
        File[] videoFiles = folder.listFiles(new FileExtensionFilter(SUPPORTED_VIDEO_FILE_FORMATS));
        List<MediaFile> mediaFiles = new ArrayList<>(videoFiles.length);
        for (File subfolder : subfolders) {
            mediaFiles.addAll(Arrays.asList(getEditableMediaFiles(subfolder)));
        }
        for (File file : videoFiles) {
            System.out.println("Candidate video file: " + file.getName());
            try {
                mediaFiles.add(new MediaFile(file));
                System.out.println("Video file: " + file.getName() + "... parsed");
            } catch (IOException | MediaInfoException ex) {
                System.out.println("Video file: " + file.getName() + "... skipped");
                System.out.println("Reason: " + ex.getMessage());
            }
        }
        printConsoleSeparator();

        return mediaFiles.toArray(new MediaFile[mediaFiles.size()]);
    }

    private static AudioFile[] getSupportedAudioFiles(File folder) {
        File[] subfolders = folder.listFiles(new DirectoryFilter());
        File[] supportedFiles = folder.listFiles(new FileExtensionFilter(SUPPORTED_AUDIO_FILE_FORMATS));
        ArrayList<AudioFile> audioFiles = new ArrayList<>(supportedFiles.length);
        for (File subfolder : subfolders) {
            audioFiles.addAll(Arrays.asList(getSupportedAudioFiles(subfolder)));
        }
        for (File file : supportedFiles) {
            System.out.println("Audio file: " + file.getName() + "... ");
            try {
                audioFiles.add(new AudioFile(file));
                System.out.println("parsed");
            } catch (IOException ex) {
                System.out.println("skipped");
                System.out.println("Reason: " + ex.getMessage());
            }
        }
        if (audioFiles.size() > 0)
            printConsoleSeparator();

        return audioFiles.toArray(new AudioFile[audioFiles.size()]);
    }

    private static SubtitleFile[] getSupportedSubtitleFiles(File folder) {
        File[] subfolders = folder.listFiles(new DirectoryFilter());
        File[] textSubFiles = folder.listFiles(new FileExtensionFilter(SUPPORTED_SUBTITLE_FORMATS));
        ArrayList<SubtitleFile> subtitleFiles = new ArrayList<>(textSubFiles.length);
        for (File subfolder : subfolders) {
            subtitleFiles.addAll(Arrays.asList(getSupportedSubtitleFiles(subfolder)));
        }
        for (File file : textSubFiles) {
            System.out.println("Subtitle file: " + file.getName() + "... ");
            try {
                subtitleFiles.add(new SubtitleFile(file));
                System.out.println("parsed");
            } catch (IOException ex) {
                System.out.println("skipped");
                System.out.println("Reason: " + ex.getMessage());
            }
        }
        if (subtitleFiles.size() > 0)
            printConsoleSeparator();

        return subtitleFiles.toArray(new SubtitleFile[subtitleFiles.size()]);
    }

    private static void fixCommonSubtitleEncodingErrors(SubtitleFile[] subtitleFiles) {
        for (SubtitleFile subtitleFile : subtitleFiles) {
            if (subtitleFile.isTextBased()) {
                try {
                    subtitleFile.fixEncoding();
                } catch (IOException ex) {
                    System.out.println("Encoding could not be fixed for " + subtitleFile.getName());
                    System.out.println("Reason: " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Binds every subtitle and audio file to its corresponding media file.
     *
     * @param mediaFiles the media files which may need additional subtitles
     * @param audioFiles the audio files to be mapped to media files
     * @param subtitleFiles the subtitle files to be mapped to media files
     */
    private static void map(MediaFile[] mediaFiles, AudioFile[] audioFiles, SubtitleFile[] subtitleFiles) {
        System.out.println("Mapping audio and subtitle files to Matroska container files...");
        for (MediaFile mediaFile : mediaFiles) {
            for (AudioFile audioFile : audioFiles) {
                if (audioFile.correspondsTo(mediaFile)) {
                    mediaFile.addAudioTrack(audioFile);
                }
            }
            for (SubtitleFile subtitleFile : subtitleFiles) {
                if (subtitleFile.correspondsTo(mediaFile)) {
                    mediaFile.addSubtitle(subtitleFile);
                }
            }
        }
        printConsoleSeparator();
    }

    private static void printResultSummary() {
        System.out.println("");
        if (videoFilesCount == 0) {
            System.out.println("No valid media files to edit!");
        } else {
            System.out.println("> Results:");
            System.out.print("Found " + videoFilesCount + " media files");
            System.out.print(editedFilesCount == 0 ? "" : ", " + editedFilesCount + " edited successfully");
            int troublesomeFilesCount = videoFilesCount - editedFilesCount;
            System.out.print(troublesomeFilesCount == 0 ? "" : ", " + troublesomeFilesCount + " couldn't be edited");
            System.out.println("");
        }
    }

    private static void printConsoleSeparator() {
        System.out.println("");
    }
}
