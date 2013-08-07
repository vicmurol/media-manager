package lan.vandiemens.media.info;

import java.io.BufferedReader;
import java.io.File;
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
import lan.vandiemens.media.SubtitleFile;
import lan.vandiemens.media.info.track.*;
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
    private Track[] tracks = null;
    private Pattern pixelPattern = Pattern.compile("^(?<length>[\\d]{1,3}(?:[ ,]?[\\d]{3})*) pixels$");

    public MediaInfo(File file) throws MediaInfoException {
        checkIfValid(file);
        parse(file);
        makeCorrections();
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

//    public void setOriginalLanguage(String language) {
//        originalLanguage = Language.parseLanguage(language);
//    }

    public void setOriginalLanguage(Language language) {
        if (language == null) {
            throw new IllegalArgumentException("Language can't be null");
        }
        originalLanguage = language;
    }

    public int getTotalTrackCount() {
        return tracks.length;
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

    public int getEnabledAudioTrackCount() {
        int count = 0;
        for (Track track : tracks) {
            if (track.getType() == TrackType.AUDIO && track.isEnabled()) {
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

    public Track[] getTracks() {
        return tracks;
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

    public boolean hasSpanishAudioTrack() {
        for (Track track : tracks) {
            if (track.getType() == TrackType.AUDIO && ((AudioTrack)track).getLanguage() == Language.SPANISH) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMenuTrack() {
        for (Track track : tracks) {
            if (track.getType() == TrackType.MENU && track.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEnglishCompleteTextSubtitles() {
        // TO-DO Check if it's a commentary subtitle
        for (Track track : tracks) {
            if (track.getType() == TrackType.SUBTITLE
                    && ((SubtitleTrack) track).getLanguage() == Language.ENGLISH
                    && !((SubtitleTrack) track).isForced()
                    && ((SubtitleTrack) track).getCodecId().startsWith("S_TEXT")) {
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

    /**
     * NOTE: Every track should have its corresponding language tag correct for
     * this method to work as wanted. Unknown language tracks will be disabled.
     */
    public void disableUnwantedTracks() {
        int videoTrackCount = 0;
        int menuTrackCount = 0;
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
                    switch (originalLanguage) {
                        case ENGLISH:
                            // Only allow the first English audio track, disable the rest
                            if (audioTrack.isEnglish()) {
                                if (++englishAudioTrackCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one English audio track is allowed");
                                }
                            // Disable latin audio for English movies
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
                                    System.out.println("Reason: Only one original language audio track is allowed");
                                }
                            // Disable latin audio for English movies
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
                    // Disable non-text subtitles
                    if (!subtitleTrack.isTextBased()) {
                        tracks[i].disable();
                        System.out.println("Track #" + i + " disabled: " + tracks[i]);
                        System.out.println("Reason:  Only text subtitles allowed");
                        continue;
                    }
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
                        default:
                            // Only Spanish subtitles needed
                            // TODO: If not available use English complete subtitles
                            if (subtitleTrack.isSpanish() && subtitleTrack.isComplete()) {
                                if (++spanishCompleteSubtitleCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one Spanish complete subtitle track is allowed for foreign movies");
                                }
                            // Only allow the first Spanish forced subtitle, disable the rest
                            } else if (subtitleTrack.isSpanish() && subtitleTrack.isForced()) {
                                if (++spanishForcedSubtitleCount > 1) {
                                    tracks[i].disable();
                                    System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                    System.out.println("Reason: Only one Spanish forced subtitle track is allowed for foreign movies");
                                }
                            // Disable the remaining subtitles for foreign movies
                            } else {
                                tracks[i].disable();
                                System.out.println("Track #" + i + " disabled: " + tracks[i]);
                                System.out.println("Reason: Only Spanish subtitles allowed for foreign movies");
                            }
                    }
                    break;
                // Only allow the first menu track, disable the rest
                case MENU:
                    if (++menuTrackCount > 1) {
                        tracks[i].disable();
                        System.out.println("Track #" + i + " disabled: " + tracks[i]);
                        System.out.println("Reason: Only one menu track is allowed");
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
        return hasDisabledTracks() || !hasOriginalTrackOrder();
    }

    private boolean hasDisabledTracks() {
        for (Track track : tracks) {
            if (!track.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOriginalTrackOrder() {
        int lastId = -1;
        int id;
        for (Track track : tracks) {
            if (track.getType() != TrackType.MENU) {
                id = ((MediaTrack) track).getId();
                if (id > lastId) {
                    lastId = id;
                } else {
                    return false;
                }
            }
        }
        return true;
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

    public void addExternalSubtitles(ArrayList<SubtitleFile> addedSubtitles) {
        if (!addedSubtitles.isEmpty()) {
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
            trackSet.addAll(addedSubtitles);
            tracks = trackSet.toArray(new Track[trackSet.size()]);
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
        for (int i = 0; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.AUDIO) {
                ((AudioTrack) tracks[i]).setLanguage(Language.SPANISH);
                return;
            }
        }
    }

    public void setDefaults() {
        System.out.println("Determining default tracks...");
        int i = 0;
        for (; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.VIDEO) {
                ((MediaTrack) tracks[i++]).setAsDefault(true);
                break;
            }
        }
        for (; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.AUDIO) {
                ((MediaTrack) tracks[i++]).setAsDefault(true);
                break;
            } else { // Remaining video tracks are not default
                ((MediaTrack) tracks[i]).setAsDefault(false);
            }
        }
        for (; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.SUBTITLE) {
                ((MediaTrack) tracks[i++]).setAsDefault(true);
                break;
            } else if (tracks[i].getType() == TrackType.MENU) { // No subs but menu track
                i++;
                break;
            } else { // Remaining audio tracks are not default
                ((MediaTrack) tracks[i]).setAsDefault(false);
            }
        }
        for (; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.MENU) {
                break;
            } else { // Remaining subtitle tracks are not default
                ((MediaTrack) tracks[i]).setAsDefault(false);
            }
        }
    }

    private void checkIfValid(File file) {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.exists()) {
            throw new IllegalArgumentException(file + " doesn't exist!");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file + " is a directory!");
        }
    }

    private void parse(File mediaFile) throws MediaInfoException {
        System.out.println("Parsing \"" + mediaFile.getName() + "\" media information...");
        parseOriginalLanguage(mediaFile);
        parse(getXmlMediaInfo(mediaFile));
    }

    private void parseOriginalLanguage(File mediaFile) {
        String dirName = mediaFile.getParentFile().getName();
        Language lang = Language.parseLanguage(dirName);
        originalLanguage = (lang == Language.UNDEFINED ? DEFAULT_ORIGINAL_LANGUAGE : lang);
        System.out.println("Original language: " + originalLanguage.getLanguageName());
    }

    private Document getXmlMediaInfo(File mediaFile) throws MediaInfoException {
        String[] commandArray = new String[]{MediaInfoHelper.getMediaInfoExecutable().getAbsolutePath(),
            XML_OUTPUT_OPTION,
            mediaFile.getAbsolutePath()};
        BufferedReader reader = null;
        Document xmlDocument = null;
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
        if (trackElements.size() < 3) { // General, Video and Audio at least
            System.err.println("Not enough media tracks!");
            throw new MediaInfoException(xmlDocument + " is not a valid MediaInfo XML document!");
        }

        parseGeneralInfo(trackElements);
        parseMediaTracks(trackElements);
    }

    private void parseGeneralInfo(Elements trackElements) throws MediaInfoException {
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

    private void parseMediaTracks(Elements trackElements) throws MediaInfoException {
        System.out.println("Parsing media tracks...");
        tracks = new Track[trackElements.size() - 1]; // Without General track
        for (int i = 1; i < trackElements.size(); i++) {
            tracks[i - 1] = parseTrack(trackElements.get(i));
            if (tracks[i - 1] == null) {
                throw new MediaInfoException("Track #" + (i - 1) + " could not be parsed");
            }
        }
        fixTidOffset();
    }

    private void fixTidOffset() {
        System.out.println("Fixing TID offset...");
        if (((MediaTrack) tracks[0]).getId() > 0) {
            for (int i = 0; i < tracks.length; i++) {
                if (tracks[i].getType() != TrackType.MENU) {
                    ((MediaTrack) tracks[i]).setTid(((MediaTrack) tracks[i]).getId() - 1);
                }
            }
        }
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
                track = getVideoTrack(trackElement);
                break;
            case "Audio":
                track = getAudioTrack(trackElement);
                break;
            case "Text":
                track = getSubtitleTrack(trackElement);
                break;
            case "Menu":
                track = getMenuTrack(trackElement);
                break;
            default:
                System.err.println("Invalid Mediainfo XML file: An unknown <track> element has been found");
        }

        return track;
    }

    private VideoTrack getVideoTrack(Element trackElement) {
        System.out.print("Parsing video track XML info...");
        VideoTrack track = new VideoTrack();
        track.setStreamId((trackElement.getAttribute("streamid") != null) ? Integer.parseInt(trackElement.getAttributeValue("streamid")) : 0);
        Element element = trackElement.getFirstChildElement("ID");
        track.setId(Integer.parseInt(element.getValue()));
        element = trackElement.getFirstChildElement("Format");
        track.setFormat(element.getValue());
        element = trackElement.getFirstChildElement("Format_Info");
        track.setFormatInfo(element.getValue());
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

    private AudioTrack getAudioTrack(Element trackElement) {
        System.out.print("Parsing audio track XML info...");
        AudioTrack track = new AudioTrack();
        track.setStreamId((trackElement.getAttribute("streamid") != null) ? Integer.parseInt(trackElement.getAttributeValue("streamid")) : 0);
        Element element = trackElement.getFirstChildElement("ID");
        track.setId(Integer.parseInt(element.getValue()));
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

    private SubtitleTrack getSubtitleTrack(Element trackElement) {
        System.out.print("Parsing subtitle track XML info...");
        SubtitleTrack track = new SubtitleTrack();
        track.setStreamId((trackElement.getAttribute("streamid") != null) ? Integer.parseInt(trackElement.getAttributeValue("streamid")) : 0);
        Element element = trackElement.getFirstChildElement("ID");
        track.setId(Integer.parseInt(element.getValue()));
        element = trackElement.getFirstChildElement("Format");
        track.setFormat(element.getValue());
        element = trackElement.getFirstChildElement("Codec_ID");
        track.setCodecId(element.getValue());
        element = trackElement.getFirstChildElement("Codec_ID_Info");
        track.setFormatInfo(element.getValue());
        element = trackElement.getFirstChildElement("Forced");
        track.setForced(element.getValue().equalsIgnoreCase("yes"));
        element = trackElement.getFirstChildElement("Title");
        track.setTitle((element != null) ? element.getValue() : null);
        if (element != null && element.getValue().equalsIgnoreCase("FHI")) {
            track.setSubType(SubtitleTrackType.FOR_HEARING_IMPAIRED);
        }
        if (element != null && containsForcedTrackHint(element.getValue())) {
            track.setForced(true);
        }
        element = trackElement.getFirstChildElement("Language");
        track.setLanguage(element == null ? null : element.getValue());
        element = trackElement.getFirstChildElement("Default");
        track.setAsDefault(element.getValue().equalsIgnoreCase("yes"));
        System.out.println(" done");

        return track;
    }

    private Track getMenuTrack(Element trackElement) {
        return new MenuTrack();
    }

    private void makeCorrections() {
        fixUndefinedAudioInEnglishMedia();
    }

    private void fixUndefinedAudioInEnglishMedia() {
        if (UNDEFINED_AUDIO_AS_ENGLISH && originalLanguage == Language.ENGLISH && getAudioTrackCount() == 1 && !hasEnglishAudioTrack()) {
            for (int i = 0; i < tracks.length; i++) {
                if (tracks[i].getType() == TrackType.AUDIO) {
                    ((AudioTrack) tracks[i]).setLanguage(Language.ENGLISH);
                    break;
                }
            }
        }
    }

    private boolean containsForcedTrackHint(String title) {
        String normalizedTitle = title.toLowerCase().trim();
        return normalizedTitle.contains("forced") || normalizedTitle.contains("forzados");
    }

    public void setVideoTrackTitle(String formattedTitle) {
        for (int i = 0; i < tracks.length; i++) {
            if (tracks[i].getType() == TrackType.VIDEO) {
                ((VideoTrack) tracks[i]).setTitle(formattedTitle);
            }
        }
    }
}
