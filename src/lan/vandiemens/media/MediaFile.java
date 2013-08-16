package lan.vandiemens.media;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import lan.vandiemens.media.cataloguer.ReleaseInfoParser;
import lan.vandiemens.media.info.MediaInfo;
import lan.vandiemens.media.info.MediaInfoException;
import lan.vandiemens.media.info.release.ReleaseInfo;
import lan.vandiemens.media.info.release.TvEpisodeReleaseInfo;
import lan.vandiemens.media.info.track.SubtitleTrackType;
import lan.vandiemens.util.lang.Language;

/**
 *
 * @author vmurcia
 */
public class MediaFile {

    private static final int BLURAY_SIZE_THRESHOLD_IN_GIBIS = 20; // Measured in GiB
    private File file = null;
    private MediaInfo mediaInfo = null;
    private ReleaseInfo releaseInfo = null;
    private ArrayList<SubtitleFile> addedSubtitles = new ArrayList<>(2);
    private boolean isConsolidated = false;

    public MediaFile(File file) throws IOException, MediaInfoException {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.exists()) {
            throw new IllegalArgumentException(file + " doesn't exist!");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file + " is a directory!");
        }

        // Check if the given file is truly a media file
        // Si no, sólo existen los campos XML: complete name y file size
        mediaInfo = new MediaInfo(file);
        System.out.println("Media information has been generated\n");
        releaseInfo = ReleaseInfoParser.parse(file);
        System.out.println("Release information has been generated\n");
        fixWrongInfo();

        this.file = file;
    }

    public File getMainFile() {
        return file;
    }

    public MediaInfo getMediaInfo() {
        return mediaInfo;
    }

    public ReleaseInfo getReleaseInfo() {
        return releaseInfo;
    }

    public String getFormattedTitle() {
        return releaseInfo.getFormattedTitle();
    }

    public void addSubtitle(SubtitleFile subtitleFile) {
        addedSubtitles.add(subtitleFile);
        System.out.println("Subtitle \"" + subtitleFile.getName() + "\" mapped to media container \"" + file.getName() + "\"");
    }

    public SubtitleFile[] getAddedSubtitles() {
        return addedSubtitles.toArray(new SubtitleFile[addedSubtitles.size()]);
    }

    /**
     * Returns main video file and external subtitle files (if any) in an array
     * @return the main video file and external subtitle files if any
     */
    public File[] getFiles() {
        File[] files = new File[1 + addedSubtitles.size()];
        files[0] = file; // The main video file
        for (int i = 1; i < files.length; i++) {
            files[i] = addedSubtitles.get(i-1).getFile();
        }
        return files;
    }

    /**
     * Returns number of files including external subtitles and the main media.
     * @return
     */
    public int getFileCount() {
        return addedSubtitles.size() + 1;
    }

    public boolean isSpanish() {
        return mediaInfo.getOriginalLanguage() == Language.SPANISH;
    }

    public boolean isEnglish() {
        return mediaInfo.getOriginalLanguage() == Language.ENGLISH;
    }

    public boolean hasEnglishAudioTrack() {
        return mediaInfo.hasEnglishAudioTrack();
    }

    public boolean hasSpanishAudioTrack() {
        return mediaInfo.hasSpanishAudioTrack();
    }

    public boolean hasChapters() {
        return mediaInfo.hasChapters();
    }

    public boolean hasEnglishCompleteTextSubtitles() {
        return (mediaInfo.hasEnglishCompleteTextSubtitles() || hasMappedEnglishCompleteTextSubtitles());
    }

    private boolean hasMappedEnglishCompleteTextSubtitles() {
        for (SubtitleFile subtitleFile : addedSubtitles) {
            if (subtitleFile.isEnglish() && subtitleFile.isCompleteOrForHearingImpaired()) {
                return true;
            }
        }
        return false;
    }

    public void disableUnwantedTracks() {
        mediaInfo.disableUnwantedTracks();
        disableDuplicatedMappedSubtitles();
    }

    private void disableDuplicatedMappedSubtitles() {
        System.out.println("Disabling duplicated external subtitles...");
        SubtitleFile subtitleFile;
        for (int i = 0; i < addedSubtitles.size(); i++) {
            subtitleFile = addedSubtitles.get(i);
            if (mediaInfo.hasSubtitle(subtitleFile.getLanguage(), subtitleFile.getSubType())) {
                addedSubtitles.remove(i);
                System.out.println("Previously added subtitle file " + subtitleFile.getName() + " disabled: the media container has equivalent subtitles");
            }
        }
    }

    public void consolidate() {
        if (!isConsolidated) {
            System.out.println("Consolidating media file " + file.getName() + "...");
            disableUnwantedTracks();
            mediaInfo.rearrange();
            mediaInfo.addExternalSubtitles(addedSubtitles);
            if (mediaInfo.getOriginalLanguage() == Language.ENGLISH && !mediaInfo.hasSpanishAudioTrack() && mediaInfo.hasSubtitle(Language.SPANISH, SubtitleTrackType.COMPLETE)) {
                mediaInfo.swapSubtitles(Language.ENGLISH, Language.SPANISH);
            }
            mediaInfo.setDefaults();
            isConsolidated = true;
        }
    }

    /**
     * NOTE: Must be called after <code>disableUnwantedTracks()</code>.
     * @return <code>true</code> if no track is left over and all of them are
     *         in order, <code>false</code> otherwise.
     */
    public boolean needsRemux() {
        if (addedSubtitles.size() > 0) {
            return true;
        } else {
            return mediaInfo.needsRemux();
        }
    }

    public boolean isTvSeries() {
        return releaseInfo instanceof TvEpisodeReleaseInfo;
    }

    public boolean meetsLanguageRequirements() {
        System.out.print("Checking language requirements for \"" + file.getName() + "\"... ");
        boolean result = false;
        switch (mediaInfo.getOriginalLanguage()) {
            case SPANISH:
                if (hasSpanishAudioTrack()) {
                    result = true;
                    System.out.println("Approved");
                } else {
                    System.out.println("Disapproved\nRequirement not met: Spanish media without Spanish audio track!");
                }
                break;
            case ENGLISH:
                if (hasEnglishAudioTrack()) {
                    if (hasEnglishCompleteTextSubtitles()) {
                        System.out.println("Approved");
                        result = true;
                    } else {
                        System.out.println("Disapproved\nRequirement not met: English media without complete English subtitle track!");
                    }
                } else {
                    System.out.println("Disapproved\nRequirement not met: English media without English audio track!");
                }
                break;
            default:
                if (hasSpanishAudioTrack()) {
                    System.out.println("Approved");
                    result = true;
                } else {
                    System.out.println("Disapproved\nRequirement not met: " + mediaInfo.getOriginalLanguage().getLanguageName() + " media without Spanish audio track!");
                }
        }
        return result;
    }

    public boolean meetsReleaseInfoRequirements() {
        System.out.print("Checking release information requirements for \"" + file.getName() + "\"... ");
        boolean result = false;
        if (releaseInfo == null) {
            System.out.println("Disapproved\nRequirement not met: " + file + " has not a supported file name pattern!");
        } else if (!(isTvSeries()) && !releaseInfo.isReleaseDateKnown()) {
            System.out.println("Disapproved\nRequirement not met: " + file + " is a movie without year information!");
        } else if (isTvSeries() && !((TvEpisodeReleaseInfo) releaseInfo).isEpisodeTitleKnown()) {
            System.out.println("Disapproved\nRequirement not met: " + file + " is a TV series without episode title known!");
        } else {
            System.out.println("Approved");
            result = true;
        }

        return result;
    }

    private void fixWrongInfo() {
        // Consider undefined audio tracks as Spanish for Spanish movies
        if (mediaInfo.getOriginalLanguage() == Language.SPANISH && mediaInfo.getAudioTrackCount() == 1 && !mediaInfo.hasSpanishAudioTrack()) {
            mediaInfo.setAudioAsSpanish();
        }
        if (releaseInfo == null) { // TODO: Release info shouldn't never be null
            return;
        }
        fixVideoTrackTitle();
        // Try to guess media source if unknown
        if (releaseInfo.getVideoSource() == null && releaseInfo.isVideoQualityKnown() && releaseInfo.getVideoQuality().startsWith("1080")) {
            releaseInfo.setVideoSource(mediaInfo.getFileSizeInGigas() < BLURAY_SIZE_THRESHOLD_IN_GIBIS ? "BDRip" : "BluRay");
        // Correct media source if incorrect
        } else if (releaseInfo.getVideoSource() != null && releaseInfo.getVideoSource().equalsIgnoreCase("bluray") && mediaInfo.getFileSizeInGigas() < BLURAY_SIZE_THRESHOLD_IN_GIBIS) {
            releaseInfo.setVideoSource("BDRip");
        }
    }

    private void fixVideoTrackTitle() {
        mediaInfo.setVideoTrackTitle(getFormattedTitle());
    }
}
