package lan.vandiemens.media;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lan.vandiemens.media.cataloguer.ReleaseInfoParser;
import lan.vandiemens.media.info.release.ReleaseInfo;
import lan.vandiemens.media.info.track.SubtitleTrack;
import lan.vandiemens.media.info.track.SubtitleTrackType;
import lan.vandiemens.util.file.FileUtils;
import lan.vandiemens.util.lang.Language;

/**
 * A subtitle file is considered an external subtitle track for MKV merging
 * purposes.
 *
 * @author vmurcia
 */
public class SubtitleFile extends SubtitleTrack {

    public static final String SUBRIP_EXTENSION = "srt";
    public static final String SUBSTATION_ALPHA_EXTENSION = "ssa";
    public static final String ADVANCED_SUBSTATION_ALPHA_EXTENSION = "ass";
    public static final Pattern subtitlePattern = Pattern.compile("(?i)^(?<filename>.+?)(?:\\.(?<subtype>hi|fhi|complete|normal|forced|comment))?\\.(?<lang>en|eng|english|es|esp|spa|spanish|castellano|cat)$");
    private static final Language DEFAULT_LANGUAGE = Language.ENGLISH;
    private File file = null;
    private ReleaseInfo releaseInfo = null;

    public SubtitleFile(File file) throws IOException {
        checkIfSupportedSubtitle(file);
        parseName(file);
        this.file = file;
    }

    private void checkIfSupportedSubtitle(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.exists()) {
            throw new IllegalArgumentException(file + " doesn't exist!");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file + " is a directory!");
        }
        if (!isSupportedSubtitle(file)) {
            throw new IllegalArgumentException(file + " is not a supported subtitle!");
        }
    }

    private boolean isSupportedSubtitle(File file) throws IOException {
        boolean result = FileUtils.isEncodedInUtf8(file);
        System.out.println("Subtitle file: " + file.getName() + " is" + (result ? " " : " NOT ") + "encoded in UTF-8");
        // TODO: Check if it is a valid SubRip text subtitle
        return result;
    }

    private void parseName(File file) {
        System.out.println("Parsing \"" + file.getName() + "\" as subtitle...");
        String filename = FileUtils.getNameWithoutExtension(file);
        parseExtension(file);
        Matcher matcher = subtitlePattern.matcher(filename);
        if (matcher.matches()) {
            System.out.println("Subtitle info found in the filename");
            language = Language.parseLanguage(matcher.group("lang"));
            if (matcher.group("subtype") != null) {
                subtitleType = SubtitleTrackType.parse(matcher.group("subtype"));
            }
            filename = matcher.group("filename");
        } else {
            language = DEFAULT_LANGUAGE;
        }
        releaseInfo = ReleaseInfoParser.parse(filename);
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

    @Override
    /**
     * Checks if this subtitle file is a text file.
     * <p>
     * NOTE: The subtitle file is considered a text file only if it is encoded
     * in UTF-8 (which includes ANSI ASCII).
     */
    public boolean isTextBased() {
        return true; // TODO: Currently only UTF-8 encoded SubRip text subtitles supported, but check in the future
    }

    private void parseExtension(File file) {
        String extension = FileUtils.getExtension(file).toLowerCase();
        switch (extension) {
            case SUBRIP_EXTENSION:
            case SUBSTATION_ALPHA_EXTENSION:
            case ADVANCED_SUBSTATION_ALPHA_EXTENSION:
                codecId = "S_TEXT/UTF8";
                break;
            default:
                // TODO Add support for other text-based subtitle formats
        }
    }

    /**
     * Checks if this file contains the right subtitles for the given media.
     *
     * @param mediaFile the media file which these subtitles may refer to
     * @return <code>true</code> if this file has the corresponding subtitles
     *         for the given media file, <code>false</code> otherwise
     */
    public boolean correspondsTo(MediaFile mediaFile) {
        if (releaseInfo == null) {
            return false;
        }
        return releaseInfo.equalsBasicInfoIgnoreSceneGroup(mediaFile.getReleaseInfo());
    }

    public void fixEncoding() throws IOException {
        if (FileUtils.isEncodedInUtf8(file) && !FileUtils.hasUtf8Bom(file)) {
            FileUtils.prependUtf8Bom(file);
        }
    }
}
