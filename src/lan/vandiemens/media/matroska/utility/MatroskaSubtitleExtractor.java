package lan.vandiemens.media.matroska.utility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import lan.vandiemens.media.MediaFile;
import lan.vandiemens.media.analysis.MediaInfoException;
import lan.vandiemens.media.matroska.Command;
import lan.vandiemens.media.matroska.MkvExtractSubtitlesCommand;
import lan.vandiemens.media.matroska.MkvToolNixHelper;
import lan.vandiemens.util.file.DropboxUtils;
import lan.vandiemens.util.file.FileExtensionFilter;
import lan.vandiemens.util.file.FileUtils;
import lan.vandiemens.util.lang.Language;
import org.apache.commons.lang3.ArrayUtils;

/**
 * An application for extracting subtitle tracks from Matroska media files by
 * using the MkvToolNix project command line tools.
 * <p>
 * Specifically, it extracts English and Spanish subtitles from movies and TV
 * series episodes.
 *
 * @author vmurcia
 */
public class MatroskaSubtitleExtractor extends MatroskaUtility {

    public static final String HELP_OPTION = "-h";
    public static final String LONG_HELP_OPTION = "--help";
    public static final String VERSION_OPTION = "-v";
    public static final String LONG_VERSION_OPTION = "--version";
    public static final String[] SUBTITLE_FILE_EXTENSIONS = new String[] { "srt", "ass", "ssa", "idx", "sub", "sup" };
    public static final String[] TEXT_BASED_SUBTITLE_FILE_EXTENSIONS = new String[] { "srt", "ass", "ssa" };
    public static final String[] IMAGE_BASED_SUBTITLE_FILE_EXTENSIONS = new String[] { "idx", "sub", "sup" };
    private static final Language[] DESIRED_LANGUAGES = new Language[] { Language.SPANISH, Language.ENGLISH };


    private MatroskaSubtitleExtractor() {
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
                case VERSION_OPTION:
                case LONG_VERSION_OPTION:
                    showVersion();
                    System.exit(0);
                default:
                    showUsage();
                    System.exit(0);
            }
        }

        File mediaFolder = new File(args[args.length - 1]);
        MatroskaSubtitleExtractor.process(mediaFolder);
    }

    private static void showUsage() {
        System.out.println("Usage:");
        System.out.println("    MatroskaSubtitleExtractor [-h | -v] <media_dir>");
        System.out.println("");
        System.out.println("Options:");
        System.out.println("    -h, --help     Shows this help information.");
        System.out.println("    -v, --version  Shows the version of this program.");
    }


    private static void showVersion() {
        String versionNumber = ResourceBundle.getBundle("version").getString("VERSION");
        String buildNumber = ResourceBundle.getBundle("version").getString("BUILD");
        System.out.println("MatroskaSubtitleExtractor v" + versionNumber + " Build " + buildNumber);
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

        MediaFile[] mediaFiles = getMatroskaVideoFiles(folder);
        for (MediaFile mediaFile : mediaFiles) {
            process(mediaFile);
        }
    }

    private static MediaFile[] getMatroskaVideoFiles(File folder) {
        FileExtensionFilter mkvFilter = new FileExtensionFilter(MATROSKA_FILE_EXTENSION);
        File[] mkvFiles = folder.listFiles(mkvFilter);
        List<MediaFile> mediaFiles = new ArrayList<>(mkvFiles.length);
        for (File file : mkvFiles) {
            System.out.println("Candidate Matroska video file: " + file.getName());
            try {
                mediaFiles.add(new MediaFile(file));
                System.out.println("Matroska video file: " + file.getName() + "... parsed");
            } catch (IOException | MediaInfoException ex) {
                System.out.println("Matroska video file: " + file.getName() + "... skipped");
                System.out.println("Reason: " + ex.getMessage());
            }
            System.out.println("\n**************************************************\n");
        }
        System.out.println(); // Vertical separator

        return mediaFiles.toArray(new MediaFile[mediaFiles.size()]);
    }

    private static void process(MediaFile mediaFile) {
        if (mediaFile.hasSubtitles()) {
            extractSubtitles(mediaFile, DESIRED_LANGUAGES);
        } else {
            System.out.println("Skipped (No subtitles): " + mediaFile.getName());
        }
    }

    private static void extractSubtitles(MediaFile mediaFile, Language[] languages) {
        Command command = new MkvExtractSubtitlesCommand(mediaFile, languages);
        if (runWithMkvToolNix(command)) {
            performPostCommandTasks(mediaFile);
        }
    }

    private static boolean runWithMkvToolNix(Command command) {
        boolean result = false;
        System.out.println(); // Console output separator
        System.out.println("Command: " + command);
        try {
            if (MkvToolNixHelper.execute(command)) {
                result = true;
                System.out.println(command.getInputFile().getName() + " processed successfully");
            } else {
                System.out.println(command.getOutputFile().getName() + " processed with errors!");
            }
        } catch (IOException ex) {
            System.out.println(command.getOutputFile().getName() + " processed with errors!");
        }

        return result;
    }

    private static void performPostCommandTasks(MediaFile mediaFile) {
        if (DropboxUtils.isDropboxFolderAvailable()) {
            System.out.println("Dropbox folder found: " + DropboxUtils.DROPBOX_FOLDER);
            try {
                backupExtractedSubtitles(mediaFile);
            } catch (IOException ex) {
                System.out.println("I/O error: Subtitle files associated to " + mediaFile.getName() + " could not be backed up");
                System.out.println(ex.getMessage());
            }
        } else {
            System.out.println("Subtitle backup not done: Dropbox folder is not available at this moment!");
        }
    }

    private static void backupExtractedSubtitles(MediaFile mediaFile) throws IOException {
        File[] subtitleFiles = getExtractedSubtitleFiles(mediaFile);
        File destinationFile;
        for (File subtitleFile : subtitleFiles) {
            destinationFile = generateDestinationFile(subtitleFile, mediaFile);
            Files.move(subtitleFile.toPath(), destinationFile.toPath());
            System.out.println(subtitleFile.getName() + " has been backed up to Dropbox");
        }
    }

    private static File[] getExtractedSubtitleFiles(MediaFile mediaFile) {
        FileExtensionFilter subtitleFilter = new FileExtensionFilter(SUBTITLE_FILE_EXTENSIONS);
        String mkvFilename = mediaFile.getName();
        File folder = mediaFile.getParentFolder();
        File[] candidateFiles = folder.listFiles(subtitleFilter);
        List<File> subtitleFiles = new ArrayList<>(candidateFiles.length);
        for (File file : candidateFiles) {
            System.out.println("Candidate subtitle file: " + file.getName());
            String subtitleName = file.getName();
            if (subtitleName.startsWith(FileUtils.getNameWithoutExtension(mkvFilename))) {
                System.out.println("Found subtitle " + subtitleName + " corresponding to " + mkvFilename);
                subtitleFiles.add(file);
            }
        }
        System.out.println(); // Vertical separator

        return subtitleFiles.toArray(new File[subtitleFiles.size()]);
    }

    private static File generateDestinationFile(File subtitleFile, MediaFile mediaFile) {
        File destinationFolder;
        if (mediaFile.isMovie()) {
            if (hasTextBasedSubtitleExtension(subtitleFile)) {
                destinationFolder = DropboxUtils.UNCHECKED_MOVIE_SUBTITLES_FOLDER;
            } else {
                destinationFolder = DropboxUtils.IMAGE_BASED_MOVIE_SUBTITLES_FOLDER;
            }
        } else if (mediaFile.isTvSeries()) {
            if (hasTextBasedSubtitleExtension(subtitleFile)) {
                destinationFolder = DropboxUtils.UNCHECKED_TV_SERIES_SUBTITLES_FOLDER;
            } else {
                destinationFolder = DropboxUtils.IMAGE_BASED_TV_SERIES_SUBTITLES_FOLDER;
            }
        } else {
            destinationFolder = DropboxUtils.MULTIMEDIA_FOLDER;
        }

        return new File(destinationFolder, subtitleFile.getName());
    }

    private static boolean hasTextBasedSubtitleExtension(File subtitleFile) {
        String extension = FileUtils.getExtension(subtitleFile);
        return ArrayUtils.contains(TEXT_BASED_SUBTITLE_FILE_EXTENSIONS, extension);
    }
}
