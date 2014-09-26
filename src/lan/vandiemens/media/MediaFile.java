package lan.vandiemens.media;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import lan.vandiemens.media.info.release.ReleaseInfoParser;
import lan.vandiemens.media.analysis.MediaInfo;
import lan.vandiemens.media.analysis.MediaInfoException;
import lan.vandiemens.media.info.release.MotionPictureGenre;
import lan.vandiemens.media.info.release.ReleaseInfo;
import lan.vandiemens.media.info.track.SubtitleTrackType;
import lan.vandiemens.media.info.track.Track;
import lan.vandiemens.util.lang.Language;
import static lan.vandiemens.util.lang.Language.*;

/**
 *
 * @author vmurcia
 */
public class MediaFile {

    private static final int BLURAY_SIZE_THRESHOLD_IN_GIBIS = 20;
    private File containerFile = null;
    private MediaInfo mediaInfo = null;
    private ReleaseInfo release = null;
    private final ArrayList<SubtitleFile> addedSubtitles = new ArrayList<>(2);
    private final ArrayList<AudioFile> addedAudioTracks = new ArrayList<>(2);
    private boolean isConsolidated = false;
    private boolean externalSubsPreferred = false;

    public MediaFile(File file) throws IOException, MediaInfoException {
        checkInputFile(file);
        mediaInfo = new MediaInfo(file);
        release = ReleaseInfoParser.parse(file);
        containerFile = file;
    }

    /**
     * Checks if the input file is really a file and exists.
     * @param file  the input file
     * @throws IllegalArgumentException if the input file is a directory or
     *         does not exist
     */
    private void checkInputFile(File file) throws IllegalArgumentException {
        if (!file.exists()) {
            throw new IllegalArgumentException(file + " doesn't exist!");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file + " is a directory!");
        }
    }

    public File getMainFile() {
        return containerFile;
    }

    public File getParentFolder() {
        return containerFile.getParentFile();
    }

    public String getName() {
        return containerFile.getName();
    }

    public MediaInfo getMediaInfo() {
        return mediaInfo;
    }

    public ReleaseInfo getReleaseInfo() {
        return release;
    }

    public String getReleaseGroup() {
        return release.getReleaseGroup();
    }

    public String getFormattedTitle() {
        return release.getFormattedTitle();
    }

    public void addSubtitle(SubtitleFile subtitleFile) {
        addedSubtitles.add(subtitleFile);
        System.out.println("Subtitle \"" + subtitleFile.getName() + "\" mapped to media container \"" + containerFile.getName() + "\"");
    }

    public void addAudioTrack(AudioFile audioFile) {
        addedAudioTracks.add(audioFile);
        System.out.println("Audio track \"" + audioFile.getName() + "\" mapped to media container \"" + containerFile.getName() + "\"");
    }

    public SubtitleFile[] getAddedSubtitles() {
        return addedSubtitles.toArray(new SubtitleFile[addedSubtitles.size()]);
    }

    public AudioFile[] getAddedAudioTracks() {
        return addedAudioTracks.toArray(new AudioFile[addedAudioTracks.size()]);
    }

    public Track getTrack(int index) {
        return mediaInfo.getTrack(index);
    }

    public Track[] getTracks() {
        return mediaInfo.getTracks();
    }

    public Track[] getEnabledTracks() {
        return mediaInfo.getEnabledTracks();
    }

    /**
     * Returns main video file and external files (if any) in an array.
     * <p>
     * External files can be subtitle and audio track files.
     * @return the media container file and external files if any
     */
    public File[] getFiles() {
        File[] files = new File[1 + addedAudioTracks.size() + addedSubtitles.size()];
        files[0] = containerFile; // The main video file
        int addedAudioTrackCount = addedAudioTracks.size();
        for (int i = 0; i < addedAudioTrackCount; i++) {
            files[i + 1] = addedAudioTracks.get(i).getFile();
        }
        for (int i = 0; i < addedSubtitles.size(); i++) {
            files[i + 1 + addedAudioTrackCount] = addedSubtitles.get(i).getFile();
        }
        return files;
    }

    /**
     * Returns number of files including external files and the main media.
     * @return
     */
    public int getFileCount() {
        return 1 + addedAudioTracks.size() + addedSubtitles.size();
    }

    /**
     * Returns the number of video tracks in the media container.
     *
     * @return the number of video tracks
     */
    public int getVideoTrackCount() {
        return mediaInfo.getVideoTrackCount();
    }

    /**
     * Returns the number of audio tracks including external ones added.
     *
     * @return the number of audio tracks, including external ones to be merged
     */
    public int getAudioTrackCount() {
        return mediaInfo.getAudioTrackCount();
    }

    /**
     * Returns the number of subtitle tracks including external subtitles added.
     *
     * @return the number of subtitle tracks
     */
    public int getSubtitleTrackCount() {
        return mediaInfo.getSubtitleTrackCount();
    }

    public boolean wasShotIn(Language language) {
        return mediaInfo.getOriginalLanguage() == language;
    }

    public boolean hasWdTvCompatibleEnglishAudioTrack() {
        return mediaInfo.hasWdTvLiveCompatibleEnglishAudioTrack() || hasMappedEnglishAudioTrack();
    }

    public boolean hasWdTvCompatibleSpanishAudioTrack() {
        return mediaInfo.hasWdTvLiveCompatibleSpanishAudioTrack() || hasMappedSpanishAudioTrack();
    }

    public boolean hasChapters() {
        return mediaInfo.hasChapters();
    }

    public boolean hasSubtitles() {
        return mediaInfo.getSubtitleTrackCount() > 0;
    }

    public boolean hasEnglishCompleteSubtitles() {
        return mediaInfo.hasEnglishCompleteSubtitles() || hasMappedEnglishCompleteSubtitles();
    }

    private boolean hasMappedEnglishAudioTrack() {
        for (AudioFile audioFile : addedAudioTracks) {
            if (audioFile.isEnglish()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMappedSpanishAudioTrack() {
        for (AudioFile audioFile : addedAudioTracks) {
            if (audioFile.isSpanish()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMappedEnglishCompleteSubtitles() {
        for (SubtitleFile subtitleFile : addedSubtitles) {
            if (subtitleFile.isEnglish() && subtitleFile.isComplete()) {
                return true;
            }
        }
        return false;
    }

    public void preferExternalSubtitles() {
        externalSubsPreferred = true;
    }

    public boolean isTrackDisabled(int index) {
        return mediaInfo.isTrackDisabled(index);
    }

    public void disableUnwantedTracks() {
        mediaInfo.disableUnwantedTracks();
        disableDuplicatedMappedSubtitles();
    }

    private void disableDuplicatedMappedSubtitles() {
        System.out.println("Disabling duplicated " + (externalSubsPreferred ? "internal" : "external") + " subtitles...");
        SubtitleFile subtitleFile;
        for (int i = 0; i < addedSubtitles.size(); i++) {
            subtitleFile = addedSubtitles.get(i);
            if (mediaInfo.hasSubtitle(subtitleFile.getLanguage(), subtitleFile.getSubType())) {
                if (externalSubsPreferred) {
                    mediaInfo.disableSubtitles(subtitleFile.getLanguage(), subtitleFile.getSubType());
                    System.out.println("MKV-muxed subtitles equivalent to " + subtitleFile.getName() + " disabled");
                } else {
                    addedSubtitles.remove(i);
                    System.out.println("Previously added subtitle file " + subtitleFile.getName() + " disabled: the media container has equivalent subtitles");
                }
            }
        }
    }

    public void consolidate() {
        if (!isConsolidated) {
            System.out.println("Consolidating media file " + containerFile.getName() + "...");
            disableUnwantedTracks();
            mediaInfo.rearrange();
            mediaInfo.addExternalTracks(addedAudioTracks);
            mediaInfo.addExternalTracks(addedSubtitles);
            if (mediaInfo.getOriginalLanguage() == ENGLISH && !mediaInfo.hasSpanishAudioTrack() && mediaInfo.hasSubtitle(SPANISH, SubtitleTrackType.NORMAL)) {
                mediaInfo.swapSubtitles(ENGLISH, SPANISH);
            }
            mediaInfo.setDefaultTracks();
            isConsolidated = true;
        }
    }

    public void fixCommonMetadataErrors() {
        tryGuessingUnknownAudioTrackLanguages();
        fixVideoTrackTitle();
        fixUnknownVideoSource();
    }

    /**
     * NOTE: Must be called after <code>disableUnwantedTracks()</code>.
     * @return <code>true</code> if no track is left over and all of them are
     *         in order, <code>false</code> otherwise.
     */
    public boolean needsRemux() {
        if (addedAudioTracks.size() > 0) {
            System.out.println("MKV edition needs remux as there are external audio tracks to be merged");
            return true;
        } else if (addedSubtitles.size() > 0) {
            System.out.println("MKV edition needs remux as there are external subtitles to be merged");
            return true;
        } else {
            return mediaInfo.needsRemux();
        }
    }

    public boolean isMovie() {
        return release.getGenre() == MotionPictureGenre.MOVIE;
    }

    public boolean isTvSeries() {
        return release.getGenre() == MotionPictureGenre.TV_SERIES;
    }

    public boolean meetsRequirements() {
        return meetsLanguageRequirements() && meetsReleaseInfoRequirements();
    }

    public boolean meetsLanguageRequirements() {
        System.out.print("Checking language requirements for \"" + containerFile.getName() + "\"... ");
        switch (mediaInfo.getOriginalLanguage()) {
            case SPANISH: // Native language
                return meetsSpanishMediaRequirements();
            case ENGLISH: // English as second language
                return meetsEnglishMediaRequirements();
            default: // Foreign language not spoken by me
                return meetsForeignMediaRequirements();
        }
    }

    private boolean meetsSpanishMediaRequirements() {
        boolean result = false;
        if (hasWdTvCompatibleSpanishAudioTrack()) {
            result = true;
            printApprovedMessage("Spanish media with WD TV Live compatible Spanish audio track");
        } else {
            printDisapprovedMessage("Spanish media without compatible Spanish audio track!");
        }
        return result;
    }

    private boolean meetsEnglishMediaRequirements() {
        boolean result = false;
        if (hasWdTvCompatibleEnglishAudioTrack()) {
            if (hasEnglishCompleteSubtitles()) {
                printApprovedMessage("English media with WD TV Live compatible English audio and subtitle tracks");
                result = true;
            } else {
                printDisapprovedMessage("English media without complete English subtitle track!");
            }
        } else {
            printDisapprovedMessage("English media without compatible English audio track!");
        }
        return result;
    }

    private boolean meetsForeignMediaRequirements() {
        boolean result = false;
        String language = mediaInfo.getOriginalLanguageName();
        if (hasWdTvCompatibleSpanishAudioTrack()) {
            printApprovedMessage(language + " media with WD TV Live compatible Spanish audio track");
            result = true;
        } else if (hasWdTvCompatibleEnglishAudioTrack() && hasEnglishCompleteSubtitles()) {
            printApprovedMessage(language + " media with WD TV Live compatible English audio and subtitle tracks");
            result = true;
        } else {
            printDisapprovedMessage(language + " media without compatible Spanish audio track nor English audio/subtitle track pair!");
        }
        return result;
    }

    private void printApprovedMessage(String message) {
        System.out.println("Approved\n  > " + message);
    }

    private void printDisapprovedMessage(String message) {
        System.out.println("Disapproved\n  > " + message);
    }

    public boolean meetsReleaseInfoRequirements() {
        System.out.print("Checking release information requirements for \"" + containerFile.getName() + "\"... ");
        boolean result = false;
        if (release.hasCompleteBasicInfo()) {
            System.out.println("Approved");
            result = true;
        } else {
            System.out.println("Disapproved");
            System.out.println(composeMissingRequirementsMessage());
        }
        return result;
    }

    private String composeMissingRequirementsMessage() {
        String message = "Requirement not met";
        if (isMovie()) {
            message += ": " + containerFile + " is a movie without year information!";
        } else if (isTvSeries()) {
            message += ": " + containerFile + " is a TV series episode without episode title information!";
        } else {
            message += " for " + release.getGenre() + ": " + containerFile;
        }
        return message;
    }

    private void tryGuessingUnknownAudioTrackLanguages() {
        // Consider undefined audio tracks as Spanish for Spanish movies
        if (mediaInfo.getOriginalLanguage() == SPANISH && mediaInfo.getAudioTrackCount() == 1 && !mediaInfo.hasSpanishAudioTrack()) {
            mediaInfo.setAudioAsSpanish();
        }
    }

    private void fixVideoTrackTitle() {
        mediaInfo.setVideoTrackTitle(getFormattedTitle());
    }

    private void fixUnknownVideoSource() {
        // Try to guess media source if unknown
        if (release.getVideoSource() == null && release.hasVideoQualityInfo() && release.getVideoQuality().startsWith("1080")) {
            release.setVideoSource(mediaInfo.getFileSizeInGigas() < BLURAY_SIZE_THRESHOLD_IN_GIBIS ? "BDRip" : "Blu-ray");
        // Correct media source if incorrect
        } else if (release.getVideoSource() != null && release.getVideoSource().equalsIgnoreCase("bluray") && mediaInfo.getFileSizeInGigas() < BLURAY_SIZE_THRESHOLD_IN_GIBIS) {
            release.setVideoSource("BDRip");
        }
    }
}
