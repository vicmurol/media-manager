package lan.vandiemens.media;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lan.vandiemens.media.info.release.ReleaseInfoParser;
import lan.vandiemens.media.info.release.ReleaseInfo;
import lan.vandiemens.media.info.track.AudioTrack;
import lan.vandiemens.util.file.FileUtils;
import lan.vandiemens.util.lang.Language;

/**
 * An audio file is considered an external audio track for MKV merging purposes.
 *
 * @author vmurcia
 */
public class AudioFile extends AudioTrack {

    public static final String MATROSKA_AUDIO_EXTENSION = "mka";
    public static final String DOLBY_DIGITAL_EXTENSION = "ac3";
    public static final String DTS_EXTENSION = "dts";
    public static final String AAC_EXTENSION = "aac";
    public static final String MP3_EXTENSION = "mp3";
    public static final String OGG_EXTENSION = "ogg";
    public static final Pattern audioPattern = Pattern.compile("(?i)^(?<filename>.+?)\\.(?<lang>en|eng|english|es|esp|spa|spanish|castellano|cat|catalan)$");
    private static final Language DEFAULT_LANGUAGE = Language.ENGLISH;
    private File file = null;
    private ReleaseInfo releaseInfo = null;

    public AudioFile(File file) throws IOException {
        checkIfSupportedAudio(file);
        parse(file);
        this.file = file;
        isExternal = true;
    }

    private void checkIfSupportedAudio(File file) throws IOException {
        if (!file.exists()) {
            throw new IllegalArgumentException(file + " doesn't exist!");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file + " is a directory!");
        }
        if (!isSupportedAudio(file)) {
            throw new IllegalArgumentException(file + " is not a supported audio file!");
        }
    }

    private boolean isSupportedAudio(File file) throws IOException {
        // TODO: Check if it is a valid audio file
        return true;
    }

    private void parse(File file) {
        System.out.println("Parsing \"" + file.getName() + "\" as audio...");
        parseFilename(file);
        parseExtension(file);
    }

    private void parseFilename(File file) {
        String filename = FileUtils.getNameWithoutExtension(file);
        Matcher matcher = audioPattern.matcher(filename);
        if (matcher.matches()) {
            System.out.println("Audio file follows basic naming pattern");
            language = Language.parseLanguage(matcher.group("lang"));
            filename = matcher.group("filename");
        } else {
            language = DEFAULT_LANGUAGE;
        }
        releaseInfo = ReleaseInfoParser.parse(filename);
    }

    private void parseExtension(File file) {
        String extension = FileUtils.getExtension(file).toLowerCase();
        switch (extension) {
            case DOLBY_DIGITAL_EXTENSION:
                codecId = "A_AC3";
                format="AC-3";
                break;
            case DTS_EXTENSION:
                codecId = "A_DTS";
                format="DTS";
                break;
            case AAC_EXTENSION:
                codecId = "A_AAC";
                format="AAC";
                break;
            case MP3_EXTENSION:
                codecId = "A_MPEG/L3";
                format="MPEG Audio";
                break;
            case OGG_EXTENSION:
                codecId = "A_VORBIS";
                format="Vorbis";
                break;
            default:
                // TODO Add support for other audio formats
        }
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return file.getName();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public ReleaseInfo getReleaseInfo() {
        return releaseInfo;
    }

    /**
     * Checks if this file contains the corresponding audio for the given media.
     *
     * @param mediaFile the media file which these subtitles may refer to
     * @return <code>true</code> if this file has the corresponding subtitles
     *         for the given media file, <code>false</code> otherwise
     */
    public boolean correspondsTo(MediaFile mediaFile) {
        return releaseInfo.equalsBasicInfoIgnoreSceneGroup(mediaFile.getReleaseInfo());
    }
}
