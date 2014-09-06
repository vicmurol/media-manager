package lan.vandiemens.media.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lan.vandiemens.media.info.track.*;
import lan.vandiemens.util.file.FileUtils;
import lan.vandiemens.util.lang.Language;
import nu.xom.*;

/**
 *
 * @author vmurcia
 */
public class MediaInfo {

    public static final Language DEFAULT_ORIGINAL_LANGUAGE = Language.ENGLISH;
    private final boolean UNDEFINED_AUDIO_AS_ENGLISH = true;
    private final String XML_OUTPUT_OPTION = "--Output=XML";
    private final Pattern pixelPattern = Pattern.compile("^(?<length>[\\d]{1,3}(?:[ ,]?[\\d]{3})*) pixels$");
    private String version = null;
    private String uniqueId = null;
    private String completeName = null;
    private String title = null;
    private Language originalLanguage = Language.ENGLISH;
    private String format = null;
    private String formatVersion = null;
    private String fileSize = null;
    private String duration = null;
    private String overallBitrate = null;
    private String encodedDate = null;
    private String writingApplication = null;
    private String writingLibrary = null;
    private Chapters chapters = null;
    private Track[] tracks = null;

    public MediaInfo(File file) throws MediaInfoException {
        checkInputFile(file);
        parse(file);
        makeCorrections();
        System.out.println("Media information has been generated\n");
    }

    public MediaInfo(Document xmlDocument, String language) throws IOException, MediaInfoException {
        this(xmlDocument, Language.parseLanguage(language));
    }

    public MediaInfo(Document xmlDocument, Language language) throws IOException, MediaInfoException {
        if (xmlDocument == null || language == null) {
            throw new IllegalArgumentException("Arguments can't be null");
        }
        originalLanguage = language;
        parse(xmlDocument);
    }

    private void checkInputFile(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException(file + " doesn't exist!");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file + " is a directory!");
        }
    }

    public String getVersion() {
        return version;
    }

    public String getFormat() {
        return format;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getCompleteName() {
        return completeName;
    }

    public String getTitle() {
        return title;
    }

    public String getFormatVersion() {
        return formatVersion;
    }

    public String getFileSize() {
        return fileSize;
    }

    public int getFileSizeInGigas() {
        String[] words = fileSize.split(" ");
        switch (words[1]) {
            case "GiB":
                return (int) Float.parseFloat(words[0]);
            default:
                return 0;
        }
    }

    public String getDuration() {
        return duration;
    }

    public String getOverallBitrate() {
        return overallBitrate;
    }

    public String getEncodedDate() {
        return encodedDate;
    }

    public String getWritingApplication() {
        return writingApplication;
    }

    public String getWritingLibrary() {
        return writingLibrary;
    }

    public Language getOriginalLanguage() {
        return originalLanguage;
    }

    public String getOriginalLanguageName() {
        return originalLanguage.getLanguageName();
    }

    public void setOriginalLanguage(Language language) {
        if (language == null) {
            throw new IllegalArgumentException("Language can't be null");
        }
        originalLanguage = language;
    }

    public int getTotalTrackCount() {
        return tracks.length;
    }

    public int getVideoTrackCount() {
        int count = 0;
        for (Track track : tracks) {
            if (track.getType() == TrackType.VIDEO) {
                count++;
            }
        }
        return count;
    }

    public int getAudioTrackCount() {
        int count = 0;
        for (Track track : tracks) {
            if (track.getType() == TrackType.AUDIO) {
                count++;
            }
        }
        return count;
    }

    public int getEnabledInternalAudioTrackCount() {
        int count = 0;
        for (Track track : tracks) {
            if (track.getType() == TrackType.AUDIO && !track.isExternal() && track.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    public int getSubtitleTrackCount() {
        int count = 0;
        for (Track track : tracks) {
            if (track.getType() == TrackType.SUBTITLE) {
                count++;
            }
        }
        return count;
    }

    public Track getTrack(int index) {
        return tracks[index];
    }

    public Track[] getTracks() {
        return tracks;
    }

    public boolean isTrackDisabled(int index) {
        return !tracks[index].isEnabled();
    }

    public Track[] getEnabledTracks() {
        int length = 0;
        for (Track track : tracks) {
            if (track.isEnabled()) {
                length++;
            }
        }

        Track[] enabledTracks = new Track[length];
        int i = 0;
        for (Track track : tracks) {
            if (track.isEnabled()) {
                enabledTracks[i++] = track;
            }
        }

        return enabledTracks;
    }

    /**
     * Returns the first video track described in this MediaInfo.
     * @return the first video track found
     */
    public VideoTrack getVideoTrack() {
        for (Track track : tracks) {
            if (track.getType() == TrackType.VIDEO) {
                return (VideoTrack) track;
            }
        }

        return null;
    }

    public boolean hasEnglishAudioTrack() {
        for (Track track : tracks) {
            if (track.getType() == TrackType.AUDIO && ((AudioTrack)track).getLanguage() == Language.ENGLISH) {
                return true;
            }
        }
        return false;
    }

    public boolean hasWdTvLiveCompatibleEnglishAudioTrack() {
        // TODO: Improve compatibility detection by checking if the audio codec exists in a list of compatible
        // WD TV Live audio codecs
        for (Track track : tracks) {
            if (track.getType() == TrackType.AUDIO && ((AudioTrack)track).getLanguage() == Language.ENGLISH
                    && !((AudioTrack)track).getCodecId().equals("A_TRUEHD")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEnabledEnglishAudioTrack() {
        for (Track track : tracks) {
            if (track.getType() == TrackType.AUDIO && track.isEnabled() && ((AudioTrack)track).getLanguage() == Language.ENGLISH) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSpanishAudioTrack() {
        for (Track track : tracks) {
            if (track.getType() == TrackType.AUDIO && ((AudioTrack) track).getLanguage() == Language.SPANISH) {
                return true;
            }
        }
        return false;
    }

    public boolean hasWdTvLiveCompatibleSpanishAudioTrack() {
        // TODO: Improve compatibility detection by checking if the audio codec exists in a list of compatible
        // WD TV Live audio codecs
        for (Track track : tracks) {
            if (track.getType() == TrackType.AUDIO && ((AudioTrack)track).getLanguage() == Language.SPANISH
                    && !((AudioTrack)track).getCodecId().equals("A_TRUEHD")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEnglishCompleteSubtitles() {
        // TO-DO Check if it's a commentary subtitle
        for (Track track : tracks) {
            if (track.getType() == TrackType.SUBTITLE
                    && ((SubtitleTrack) track).getLanguage() == Language.ENGLISH
                    && !((SubtitleTrack) track).isForced()
                    && !((SubtitleTrack) track).isCommentary()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSubtitle(Language language, SubtitleTrackType type) {
        SubtitleTrack subtitleTrack;
        for (Track track : tracks) {
            if (track.getType() == TrackType.SUBTITLE) {
                subtitleTrack = (SubtitleTrack) track;
                if (subtitleTrack.isEnabled() && subtitleTrack.hasLanguage(language) && subtitleTrack.getSubType() == type) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasChapters() {
        return chapters != null;
    }

    /**
     * NOTE: Every track should have its corresponding language tag correct for
     * this method to work as wanted. Unknown language tracks will be disabled.
     */
    public void disableUnwantedTracks() {
        int videoTrackCount = 0;
        int englishAudioTrackCount = 0;
        int spanishAudioTrackCount = 0;
        int originalLanguageAudioTrackCount = 0;
        int englishCompleteSubtitleCount = 0;
        int spanishCompleteSubtitleCount = 0;
        int spanishForcedSubtitleCount = 0;

        System.out.println("Disabling unwanted tracks...");
        for (int i = 0; i < tracks.length; i++) {
            if (!tracks[i].isEnabled()) {
                continue;
            }
            switch (tracks[i].getType()) {
                // Only allow the first video track, disable the rest
                case VIDEO:
                    if (++videoTrackCount > 1) {
                        tracks[i].disable();
                        System.out.println("Track #" + i + " disabled: only one video track is allowed");
                    }
                    break;
                case AUDIO:
                    AudioTrack audioTrack = (AudioTrack) tracks[i];
                    if (audioTrack.getCodecId().equals("A_TRUEHD")) {
                        tracks[i].disable();
                        System.out.println("Track #" + i + " disabled: " + tracks[i]);
                        System.out.println("Reason: Dolby Digital TrueHD audio format is not supported by WD TV Live");
                    }
                    switch (originalLanguage) {
                        case ENGLISH:
                            // Only allow the first English audio track, disable the rest
                            if (audioTrack.isEnglish()) {
                                if (++englishAudioTrackCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one English audio track is allowed");
                                }
                            // Disable Latin Spanish audio for English movies
                            } else if (audioTrack.isLatinSpanish()) {
                                tracks[i].disable();
                                System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                System.out.println("Reason: Latin Spanish audio track for English movie not needed");
                            // Only allow the first Spanish audio track, disable the rest
                            } else if (audioTrack.isSpanish()) {
                                if (++spanishAudioTrackCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one Spanish audio track is allowed");
                                }
                            // Disable all non-English and non-Spanish audio tracks for English movies
                            } else {
                                tracks[i].disable();
                                System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                System.out.println("Reason: only Spanish and English audio tracks allowed for English movies");
                            }
                            break;
                        case SPANISH:
                            // Disable all Spanish audio tracks except first one
                            if (audioTrack.isSpanish()) {
                                if (++spanishAudioTrackCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one Spanish audio track is allowed");
                                }
                            // Disable all non-Spanish audio tracks for Spanish movies
                            } else {
                                tracks[i].disable();
                                System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                System.out.println("Reason: Only Spanish audio tracks allowed for Spanish movies");
                            }
                            break;
                        default:
                            // Only allow the first original language audio track, disable the rest
                            if (audioTrack.hasLanguage(originalLanguage)) {
                                if (++originalLanguageAudioTrackCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one original language audio track is allowed for foreign movies");
                                }
                            // Disable latin audio for foreign movies
                            } else if (audioTrack.isLatinSpanish()) {
                                tracks[i].disable();
                                System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                System.out.println("Reason: Latin Spanish audio track for foreign movie not needed");
                            // Only allow the first Spanish audio track, disable the rest
                            } else if (audioTrack.isSpanish()) {
                                if (++spanishAudioTrackCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one Spanish audio track is allowed");
                                }
                            // Disable all non-original language and non-Spanish audio tracks for foreign movies
                            } else {
                                tracks[i].disable();
                                System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                System.out.println("Reason: Only Spanish and original language audio tracks allowed for foreign movies");
                            }
                    }
                    break;
                case SUBTITLE:
                    SubtitleTrack subtitleTrack = (SubtitleTrack) tracks[i];
                    // Disable non-text subtitles for WD TV Live compatibility (deprecated)
//                    if (!subtitleTrack.isTextBased()) {
//                        tracks[i].disable();
//                        System.out.println("Track #" + i + " disabled: " + tracks[i]);
//                        System.out.println("Reason:  Only text subtitles allowed");
//                        continue;
//                    }
                    switch (originalLanguage) {
                        case ENGLISH:
                            // Only allow the first English complete subtitle, disable the rest
                            if (subtitleTrack.isEnglish() && subtitleTrack.isComplete()) {
                                if (++englishCompleteSubtitleCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one English complete subtitle track is allowed for English movies");
                                }
                            // Only allow the first Spanish complete subtitle, disable the rest
                            } else if (subtitleTrack.isSpanish() && subtitleTrack.isComplete()) {
                                if (++spanishCompleteSubtitleCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one Spanish complete subtitle track is allowed for English movies");
                                }
                            // Only allow the first Spanish forced subtitle, disable the rest
                            } else if (subtitleTrack.isSpanish() && subtitleTrack.isForced()) {
                                if (++spanishForcedSubtitleCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one Spanish forced subtitle track is allowed for English movies");
                                }
                            // Disable all non-English and non-Spanish subtitles for English movies
                            } else {
                                tracks[i].disable();
                                System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                System.out.println("Reason: Only Spanish and complete English subtitles allowed for English movies");
                            }
                            break;
                        case SPANISH:
                            // Only first Spanish forced subtitles needed
                            if (subtitleTrack.isSpanish() && subtitleTrack.isForced()) {
                                if (++spanishForcedSubtitleCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one Spanish forced subtitle track is allowed for Spanish movies");
                                }
                            // Disable the remaining subtitles for Spanish movies
                            } else {
                                tracks[i].disable();
                                System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                System.out.println("Reason: Only Spanish forced subtitles allowed for Spanish movies");
                            }
                            break;
                        default: // Non-English foreign language
                            if (subtitleTrack.isSpanish() && subtitleTrack.isComplete()) {
                                if (++spanishCompleteSubtitleCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one Spanish complete subtitle track is allowed for non-English foreign movies");
                                }
                            // Only allow the first Spanish forced subtitle, disable the rest
                            } else if (subtitleTrack.isSpanish() && subtitleTrack.isForced()) {
                                if (++spanishForcedSubtitleCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one Spanish forced subtitle track is allowed for non-English foreign movies");
                                }
                            // Only allow the first English complete subtitle, disable the rest
                            } else if (subtitleTrack.isEnglish() && subtitleTrack.isComplete()) {
                                if (++englishCompleteSubtitleCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one English complete subtitle track is allowed for non-English foreign movies");
                                }
                            } else { // Disable the remaining subtitles for non-English foreign movies
                                tracks[i].disable();
                                System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                System.out.println("Reason: Only Spanish and English subtitles allowed for non-english foreign movies");
                            }
                    }
                    break;
                // Disable unknown tracks
                default:
                    tracks[i].disable();
                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                    System.out.println("Reason: Unknown track type (" + tracks[i].getType() + ")");
            }
        }
    }

    /**
     * NOTE: Must be called after <code>disableUnwantedTracks()</code>.
     * @return <code>true</code> if no track is left over and all of them are
     *         in order, <code>false</code> otherwise.
     */
    public boolean needsRemux() {
        return hasDisabledTracks() || !hasOriginalTrackOrder() || hasUncompressedPgsSubtitleTracks();
    }

    private boolean hasDisabledTracks() {
        for (Track track : tracks) {
            if (!track.isEnabled()) {
                System.out.println("Track disabled: TID=" + track.getTrackId());
                return true;
            }
        }
        return false;
    }

    private boolean hasOriginalTrackOrder() {
        int lastTid = -1;
        int tid;
        for (Track track : tracks) {
            tid = track.getTrackId();
            if (tid > lastTid) {
                lastTid = tid;
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean hasUncompressedPgsSubtitleTracks() {
        for (Track track : tracks) {
            if (track.isEnabled() && track.getType() == TrackType.SUBTITLE
                    && ((SubtitleTrack) track).getCodecId().equals("S_HDMV/PGS")
                    && ((SubtitleTrack) track).getCompressionMode() == CompressionAlgorithm.NONE) {
                System.out.println("Uncompressed PGS subtitle track: TID=" + track.getTrackId());
                return true;
            }
        }
        return false;
    }

    /**
     * Sorts out the tracks according to its type and language.
     * <p>
     * NOTE: The order depends on the original language of the media content.
     */
    public void rearrange() {
        System.out.println("Rearranging tracks...");
        Comparator<Track> trackComparator;
        switch (originalLanguage) {
            case SPANISH:
                trackComparator = new SpanishMediaComparator();
                break;
            case ENGLISH:
                trackComparator = new EnglishMediaComparator();
                break;
            default:
                trackComparator = new ForeignMediaComparator(originalLanguage);
        }
        TreeSet<Track> trackSet = new TreeSet<>(trackComparator);
        trackSet.addAll(Arrays.asList(tracks));
        tracks = trackSet.toArray(new Track[trackSet.size()]);
        System.out.println("> Rearrangement has finished"); // TO-DO Show if there has been a rearrangement or not
    }

    /**
     * Sorts out the tracks according to its original order.
     */
    public void setOriginalOrder() {
        TreeSet<Track> trackSet = new TreeSet<>(new NaturalComparator());
        trackSet.addAll(Arrays.asList(tracks));
        tracks = trackSet.toArray(new Track[trackSet.size()]);
    }

    public void addExternalTracks(ArrayList<? extends MediaTrack> externalTracks) {
        System.out.println("Adding external tracks to MediaInfo...");
        if (!externalTracks.isEmpty()) {
            Comparator<Track> comparator;
            switch (originalLanguage) {
                case SPANISH:
                    comparator = new SpanishMediaComparator();
                    break;
                case ENGLISH:
                    comparator = new EnglishMediaComparator();
                    break;
                default:
                    comparator = new ForeignMediaComparator(originalLanguage);
            }
            TreeSet<Track> trackSet = new TreeSet<>(comparator);
            trackSet.addAll(Arrays.asList(tracks));
            trackSet.addAll(externalTracks);
            tracks = trackSet.toArray(new Track[trackSet.size()]);
        }
    }

    public void disableSubtitles(Language language, SubtitleTrackType subType) {
        System.out.println("Disabling subtitles in " + language.getLanguageName() + " whose type is " + subType);
        for (int i = 0; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.SUBTITLE) {
                SubtitleTrack subtitle = (SubtitleTrack) tracks[i];
                if (subtitle.getLanguage() == language && subtitle.getSubType() == subType) {
                    tracks[i].disable();
                    System.out.println("Subtitle track #" + i + " has been disabled");
                }
            }
        }
    }

    public void swapSubtitles(Language language, Language language2) {
        System.out.println("Swapping subtitles: " + language.getLanguageName() + " & " + language2.getLanguageName() + "...");
        if (language == language2) {
            throw new IllegalArgumentException("Both languages are the same language!");
        }

        int subtitleIndex = -1;
        int subtitleIndex2 = -1;
        for (int i = 0; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.SUBTITLE) {
                SubtitleTrack subtitle = (SubtitleTrack) tracks[i];
                if (subtitleIndex < 0 && subtitle.getLanguage() == language) {
                    subtitleIndex = i;
                    continue;
                }
                if (subtitleIndex2 < 0 && subtitle.getLanguage() == language2) {
                    subtitleIndex2 = i;
                }
            }
        }
        if (subtitleIndex >= 0 && subtitleIndex2 >= 0) {
            SubtitleTrack auxSubtitle = (SubtitleTrack) tracks[subtitleIndex];
            tracks[subtitleIndex] = tracks[subtitleIndex2];
            tracks[subtitleIndex2] = auxSubtitle;
            System.out.println("Subtitle tracks #" + subtitleIndex + " and #" + subtitleIndex2 + " have been swapped");
        }
    }

    public void setAudioAsSpanish() {
        System.out.println("Setting Spanish as audio track language...");
        for (Track track : tracks) {
            if (track.getType() == TrackType.AUDIO) {
                ((AudioTrack) track).setLanguage(Language.SPANISH);
                return;
            }
        }
    }

    /**
     * Sets the first track of each type as default.
     * <p>
     * NOTE: The remaining tracks of each type are made non default.
     */
    public void setDefaultTracks() {
        System.out.println("Determining default tracks...");
        int i = 0;
        // TODO: Allow set defaults to work when tracks are not ordered by track type
        for (; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.VIDEO) {
                tracks[i++].setAsDefault(true);
                break;
            }
        }
        for (; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.AUDIO) {
                tracks[i++].setAsDefault(true);
                break;
            } else { // Remaining video tracks are not default
                tracks[i].setAsDefault(false);
            }
        }
        for (; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.SUBTITLE) {
                tracks[i++].setAsDefault(true);
                break;
            } else { // Remaining audio tracks are not default
                tracks[i].setAsDefault(false);
            }
        }
        for (; i < tracks.length; i++) {
            // Remaining subtitle tracks are not default
            tracks[i].setAsDefault(false);
        }
    }

    private void parse(File file) throws MediaInfoException {
        System.out.println("Parsing \"" + file.getName() + "\" media information...");
        parseOriginalLanguage(file);
        parse(getMediaInfoAsXmlDocument(file));
    }

    /**
     * Gets the original language from the parent folder name of the media file.
     * <p>
     * NOTE: The language is specified by the user by naming the directory
     * containing the media file. The folder name must be a language ISO code
     * (two or three letters), the name of a language in English or the name of
     * a language in Spanish.
     * @param mediaFile the media file to be parsed
     */
    private void parseOriginalLanguage(File mediaFile) {
        String dirName = mediaFile.getParentFile().getName();
        Language lang = Language.parseLanguage(dirName);
        if (lang == Language.UNDEFINED) {
            originalLanguage = DEFAULT_ORIGINAL_LANGUAGE;
            System.out.println("Original language not specified, so default one applied...");
        } else {
            originalLanguage = lang;
            System.out.println("Original language parsed from parent folder '" + dirName + "'...");
        }
        System.out.println("Original language: " + originalLanguage.getLanguageName());
    }

    private Document getMediaInfoAsXmlDocument(File file) throws MediaInfoException {
        Document xmlDocument;
        if (hasMediaInfoFileExtension(file)) {
            xmlDocument = readMediaInfoFile(file);
        } else {
            xmlDocument = processWithMediaInfoUtility(file);
        }

        return xmlDocument;
    }

    private boolean hasMediaInfoFileExtension(File file) {
        String extension = FileUtils.getExtension(file);
        return extension.equalsIgnoreCase("xml");
    }

    private Document readMediaInfoFile(File file) throws MediaInfoException {
        Document xmlDocument = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            Builder parser = new Builder();
            xmlDocument = parser.build(reader);
        } catch (ParsingException ex) {
            System.out.println(ex.getMessage());
            throw new MediaInfoException("The XML document generated by MediaInfo is malformed");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            throw new MediaInfoException("I/O error when reading " + file);
        } finally {
            close(reader);
        }

        return xmlDocument;
    }

    private Document processWithMediaInfoUtility(File file) throws MediaInfoException {
        Document xmlDocument = null;
        String[] commandArray = new String[]{MediaInfoHelper.getMediaInfoCliExecutablePath(),
            XML_OUTPUT_OPTION,
            file.getAbsolutePath()};
        BufferedReader reader = null;
        try {
            Process process = Runtime.getRuntime().exec(commandArray);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Builder parser = new Builder();
            xmlDocument = parser.build(reader);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            throw new MediaInfoException("The MediaInfo application could not be executed");
        } catch (ParsingException ex) {
            System.out.println(ex.getMessage());
            throw new MediaInfoException("The XML document generated by MediaInfo is malformed");
        } finally {
            close(reader);
        }

        return xmlDocument;
    }

    private void close(BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(MediaInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void parse(Document xmlDocument) throws MediaInfoException {
        // TO-DO Check returned values
        Element root = xmlDocument.getRootElement();  // <Mediainfo>
        version = root.getAttributeValue("version");
        Element fileElement = root.getFirstChildElement("File");
        if (fileElement == null) {
            throw new MediaInfoException("Invalid MediaInfo XML document: No <File> element found");
        }
        Elements trackElements = fileElement.getChildElements("track");
        // MediaInfo provides only the General element with two fields when the file
        // analyzed is not a media file, especifically the file name and size info fields
        if (trackElements.size() < 3) { // General, Video and Audio at least
            System.err.println("Not enough media tracks!");
            throw new MediaInfoException(xmlDocument + " is not a valid MediaInfo XML document!");
        }

        parseGeneralInfo(trackElements);
        parseChapters(trackElements);
        parseMediaTracks(trackElements);
    }

    private void parseGeneralInfo(Elements trackElements) throws MediaInfoException {
        // MediaInfo's general track, which has index 0, is not really a track.
        // Instead, it corresponds to Matroska segment info.
        System.out.println("Parsing general information...");
        Element generalTrack = trackElements.get(0);
        String type = generalTrack.getAttributeValue("type");
        if (type == null || !type.equalsIgnoreCase("General")) {
            throw new MediaInfoException("Invalid MediaInfo XML file: First <track> element is not \"General\"");
        }
        Element element = generalTrack.getFirstChildElement("Unique_ID");
        uniqueId = (element != null) ? element.getValue() : null;
        element = generalTrack.getFirstChildElement("Complete_name");
        completeName = (element != null) ? element.getValue() : null;
        element = generalTrack.getFirstChildElement("Format");
        format = (element != null) ? element.getValue() : null;
        element = generalTrack.getFirstChildElement("Format_version");
        formatVersion = (element != null) ? element.getValue() : null;
        element = generalTrack.getFirstChildElement("File_size");
        fileSize = (element != null) ? element.getValue() : null;
        element = generalTrack.getFirstChildElement("Duration");
        duration = (element != null) ? element.getValue() : null;
        element = generalTrack.getFirstChildElement("Overall_bit_rate");
        overallBitrate = (element != null) ? element.getValue() : null;
        element = generalTrack.getFirstChildElement("Movie_name");
        title = (element != null) ? element.getValue() : null;
        element = generalTrack.getFirstChildElement("Encoded_date");
        encodedDate = (element != null) ? element.getValue() : null;
        element = generalTrack.getFirstChildElement("Writing_application");
        writingApplication = (element != null) ? element.getValue() : null;
        element = generalTrack.getFirstChildElement("Writing_library");
        writingLibrary = (element != null) ? element.getValue() : null;
    }

    private void parseChapters(Elements trackElements) {
        System.out.println("Parsing menu chapters...");
        Element element;
        // Skip general track, which has index 0
        for (int i = 1; i < trackElements.size(); i++) {
            element = trackElements.get(i);
            if (element.getAttributeValue("type").equalsIgnoreCase("Menu")) {
                System.out.println("Menu chapters found");
                chapters = getChapters(element);
                return;
            }
        }
    }

    private Chapters getChapters(Element element) {
        return new Chapters();
    }

    private void parseMediaTracks(Elements trackElements) throws MediaInfoException {
        System.out.println("Parsing media tracks...");
        int mediaTrackCount = getMediaTrackCount(trackElements); // General track and menu track doesn't count
        tracks = new Track[mediaTrackCount];
        // Skip general track (first) and menu tracks (at the end), if any
        for (int i = 1; i <= mediaTrackCount; i++) {
            tracks[i - 1] = parseTrack(trackElements.get(i));
            if (tracks[i - 1] == null) {
                throw new MediaInfoException("Track #" + (i - 1) + " could not be parsed");
            }
        }
    }

    private int getMediaTrackCount(Elements trackElements) {
        int count = 0;
        String type;
        for (int i = 0; i < trackElements.size(); i++) {
            type = trackElements.get(i).getAttributeValue("type");
            if (type.equals("Video") || type.equals("Audio") || type.equals("Text")) {
                count++;
            }
        }
        return count;
    }

    private Track parseTrack(Element trackElement) {
        String type = trackElement.getAttributeValue("type");
        if (type == null) {
            System.err.println("Invalid Mediainfo XML file: Found a <track> element with no type attribute");
            return null;
        }

        Track track = null;
        switch (type) {
            case "Video":
                track = parseVideoTrack(trackElement);
                break;
            case "Audio":
                track = parseAudioTrack(trackElement);
                break;
            case "Text":
                track = parseSubtitleTrack(trackElement);
                break;
            case "Menu":
                break;
            default:
                System.err.println("Invalid Mediainfo XML file: An unknown <track> element has been found");
        }

        return track;
    }

    private VideoTrack parseVideoTrack(Element trackElement) {
        System.out.print("Parsing video track XML info...");
        VideoTrack track = new VideoTrack();
        track.setStreamId((trackElement.getAttribute("streamid") != null) ? Integer.parseInt(trackElement.getAttributeValue("streamid")) : 0);
        Element element = trackElement.getFirstChildElement("ID");
        track.setTrackNumber(Integer.parseInt(element.getValue()));
        element = trackElement.getFirstChildElement("Format");
        track.setFormat(element.getValue());
        element = trackElement.getFirstChildElement("Format_Info");
        track.setFormatInfo((element != null) ? element.getValue() : null);
        element = trackElement.getFirstChildElement("Codec_ID");
        track.setCodecId(element.getValue());
        Matcher resolutionMatcher;
        element = trackElement.getFirstChildElement("Width");
        resolutionMatcher = pixelPattern.matcher(element.getValue());
        if (resolutionMatcher.matches()) {
            track.setWidth(Integer.parseInt(resolutionMatcher.group("length").replaceAll(" ", "")));
        }
        element = trackElement.getFirstChildElement("Height");
        resolutionMatcher = pixelPattern.matcher(element.getValue());
        if (resolutionMatcher.matches()) {
            track.setHeight(Integer.parseInt(resolutionMatcher.group("length").replaceAll(" ", "")));
        }
        element = trackElement.getFirstChildElement("Title");
        track.setTitle((element != null) ? element.getValue() : null);
        element = trackElement.getFirstChildElement("Language");
        track.setLanguage(element == null ? null : element.getValue());
        if (track.getLanguage() != originalLanguage) {
            System.out.println("Video track language changed from " + track.getLanguage().getLanguageName() + " to " + originalLanguage.getLanguageName());
            track.setLanguage(originalLanguage);
        }
        element = trackElement.getFirstChildElement("Default");
        track.setAsDefault(element.getValue().equalsIgnoreCase("yes"));
        element = trackElement.getFirstChildElement("Forced");
        track.setForced(element.getValue().equalsIgnoreCase("yes"));
        System.out.println(" done");

        return track;
    }

    private AudioTrack parseAudioTrack(Element trackElement) {
        System.out.print("Parsing audio track XML info...");
        AudioTrack track = new AudioTrack();
        track.setStreamId((trackElement.getAttribute("streamid") != null) ? Integer.parseInt(trackElement.getAttributeValue("streamid")) : 0);
        Element element = trackElement.getFirstChildElement("ID");
        track.setTrackNumber(Integer.parseInt(element.getValue()));
        element = trackElement.getFirstChildElement("Format");
        track.setFormat(element.getValue());
        element = trackElement.getFirstChildElement("Format_Info");
        track.setFormatInfo(element == null ? null : element.getValue());
        element = trackElement.getFirstChildElement("Codec_ID");
        track.setCodecId(element.getValue());
        element = trackElement.getFirstChildElement("Channel_s_");
        Matcher channelsMatcher = AudioTrack.channelsPattern.matcher(element.getValue());
        if (channelsMatcher.matches()) {
            track.setChannelCount(Integer.parseInt(channelsMatcher.group("channels")));
        }
        element = trackElement.getFirstChildElement("Title");
        track.setTitle((element != null) ? element.getValue() : null);
        element = trackElement.getFirstChildElement("Language");
        track.setLanguage(element == null ? null : element.getValue());
        element = trackElement.getFirstChildElement("Default");
        track.setAsDefault(element.getValue().equalsIgnoreCase("yes"));
        element = trackElement.getFirstChildElement("Forced");
        track.setForced(element.getValue().equalsIgnoreCase("yes"));
        System.out.println(" done");

        return track;
    }

    private SubtitleTrack parseSubtitleTrack(Element trackElement) {
        System.out.print("Parsing subtitle track XML info...");
        SubtitleTrack track = new SubtitleTrack();
        track.setStreamId((trackElement.getAttribute("streamid") != null) ? Integer.parseInt(trackElement.getAttributeValue("streamid")) : 0);
        Element element = trackElement.getFirstChildElement("ID");
        track.setTrackNumber(Integer.parseInt(element.getValue()));
        element = trackElement.getFirstChildElement("Format");
        track.setFormat(element.getValue());
        element = trackElement.getFirstChildElement("Muxing_mode");
        track.setCompressionMode((element != null) ? element.getValue() : null);
        element = trackElement.getFirstChildElement("Codec_ID");
        track.setCodecId(element.getValue());
        element = trackElement.getFirstChildElement("Codec_ID_Info");
        track.setFormatInfo(element.getValue());
        element = trackElement.getFirstChildElement("Title");
        String trackTitle = (element != null) ? element.getValue() : null;
        track.setTitle(trackTitle);
        if (trackTitle != null && containsForHearingImpairedHint(trackTitle)) {
            track.setSubType(SubtitleTrackType.FOR_HEARING_IMPAIRED);
        }
        element = trackElement.getFirstChildElement("Language");
        track.setLanguage(element == null ? null : element.getValue());
        element = trackElement.getFirstChildElement("Default");
        track.setAsDefault(element.getValue().equalsIgnoreCase("yes"));
        element = trackElement.getFirstChildElement("Forced");
        track.setForced(element.getValue().equalsIgnoreCase("yes"));
        if (trackTitle != null && containsForcedTrackHint(trackTitle)) {
            track.setForced(true);
        }
        System.out.println(" done");

        return track;
    }

    private void makeCorrections() {
        fixUndefinedAudioInEnglishMedia();
    }

    private void fixUndefinedAudioInEnglishMedia() {
        if (UNDEFINED_AUDIO_AS_ENGLISH && originalLanguage == Language.ENGLISH && getAudioTrackCount() == 1 && !hasEnglishAudioTrack()) {
            for (Track track : tracks) {
                if (track.getType() == TrackType.AUDIO) {
                    ((AudioTrack) track).setLanguage(Language.ENGLISH);
                    break;
                }
            }
        }
    }

    private boolean containsForHearingImpairedHint(String title) {
        String normalizedTitle = title.toUpperCase().trim();
        return normalizedTitle.equals("FHI") || normalizedTitle.equals("SDH");
    }

    private boolean containsForcedTrackHint(String title) {
        String normalizedTitle = title.toLowerCase().trim();
        return normalizedTitle.contains("forced") || normalizedTitle.contains("forzados")
                || normalizedTitle.contains("non english") || normalizedTitle.contains("non-english")
                || normalizedTitle.contains("foreign only");
    }

    public void setVideoTrackTitle(String title) {
        for (Track track : tracks) {
            if (track.getType() == TrackType.VIDEO) {
                ((VideoTrack) track).setTitle(title);
                break;
            }
        }
    }
}
