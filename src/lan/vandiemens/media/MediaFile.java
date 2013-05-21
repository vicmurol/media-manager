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

    private static int BLURAY_SIZE_THRESHOLD = 20; // Measured in GiB
    private File file = null;
    private MediaInfo info = null;
    private ReleaseInfo release = null;
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
        info = new MediaInfo(file);
        System.out.println("Media information has been generated\n");

        // Process filename to get release info
        release = ReleaseInfoParser.parse(file);
        if (release == null) {
            throw new IllegalArgumentException(file + " has not a supported file name pattern!");
        } else if (!(release instanceof TvEpisodeReleaseInfo) && release.getYear() < 0) {
            throw new IllegalArgumentException(file + " is a movie without year information!");
        } else {
            fixWrongInfo();
            System.out.println("Release information has been generated\n");
        }

        this.file = file;
    }

    public File getMainFile() {
        return file;
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

    public MediaInfo getMediaInfo() {
        return info;
    }

    public ReleaseInfo getReleaseInfo() {
        return release;
    }

    public String getFormattedTitle() {
        return release.getFormattedTitle();
    }

    public void addSubtitle(SubtitleFile subtitleFile) {
        addedSubtitles.add(subtitleFile);
        System.out.println("Subtitle \"" + subtitleFile.getFile().getName() + "\" mapped to media container \"" + file.getName() + "\"");
    }

    public SubtitleFile[] getAddedSubtitles() {
        return addedSubtitles.toArray(new SubtitleFile[addedSubtitles.size()]);
    }

    public boolean isSpanish() {
        return info.getOriginalLanguage() == Language.SPANISH;
    }

    public boolean isEnglish() {
        return info.getOriginalLanguage() == Language.ENGLISH;
    }

    public boolean hasEnglishAudioTrack() {
        return info.hasEnglishAudioTrack();
    }

    public boolean hasSpanishAudioTrack() {
        return info.hasSpanishAudioTrack();
    }

    public boolean hasMenuTrack() {
        return info.hasMenuTrack();
    }

    public boolean hasEnglishCompleteTextSubtitles() {
        return (info.hasEnglishCompleteTextSubtitles() || hasMappedEnglishCompleteTextSubtitles());
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
        info.disableUnwantedTracks();
        disableDuplicatedMappedSubtitles();
    }

    private void disableDuplicatedMappedSubtitles() {
        System.out.println("Disabling duplicated external subtitles...");
        SubtitleFile subtitleFile;
        for (int i = 0; i < addedSubtitles.size(); i++) {
            subtitleFile = addedSubtitles.get(i);
            if (info.hasSubtitle(subtitleFile.getLanguage(), subtitleFile.getSubType())) {
                addedSubtitles.remove(i);
                System.out.println("Previously added subtitle file " + subtitleFile.getFile().getName() + " disabled: the media container has equivalent subtitles");
            }
        }
    }

    public void consolidate() {
        if (!isConsolidated) {
            System.out.println("Consolidating media file " + file.getName() + "...");
            disableUnwantedTracks();
            info.rearrange();
            info.addExternalSubtitles(addedSubtitles);
            if (info.getOriginalLanguage() == Language.ENGLISH && !info.hasSpanishAudioTrack() && info.hasSubtitle(Language.SPANISH, SubtitleTrackType.COMPLETE)) {
                info.swapSubtitles(Language.ENGLISH, Language.SPANISH);
            }
            info.setDefaults();
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
            return info.needsRemux();
        }
    }

    public boolean isTvSeries() {
        return release instanceof TvEpisodeReleaseInfo;
    }

    public int getFileCount() {
        return addedSubtitles.size() + 1;
    }

    public boolean meetsLanguageRequirements() {
        System.out.println("Checking language requirements for \"" + file.getName() + "\"...");
        boolean result = false;
        switch (info.getOriginalLanguage()) {
            case SPANISH:
                if (hasSpanishAudioTrack()) {
                    result = true;
                } else {
                    System.out.println("Requirement not met: Spanish media without Spanish audio track!");
                }
                break;
            case ENGLISH:
                if (hasEnglishAudioTrack()) {
                    if (hasEnglishCompleteTextSubtitles()) {
                        result = true;
                    } else {
                        System.out.println("Requirement not met: English media without complete English subtitle track!");
                    }
                } else {
                    System.out.println("Requirement not met: English media without English audio track!");
                }
                break;
            default:
                if (hasSpanishAudioTrack()) {
                    result = true;
                } else {
                    System.out.println("Requirement not met: " + info.getOriginalLanguage().getLanguageName() + " media without Spanish audio track!");
                }
        }
        return result;
    }

    private void fixWrongInfo() {
        // Consider undefined audio tracks as Spanish for Spanish movies
        if (info.getOriginalLanguage() == Language.SPANISH && info.getAudioTrackCount() == 1 && !info.hasSpanishAudioTrack()) {
            info.setAudioAsSpanish();
        }
        fixVideoTrackTitle();
        // Try to guess source if unknown
        if (release.getVideoSource() == null && release.getVideoQuality().startsWith("1080")) {
            release.setVideoSource(info.getFileSizeInGigas() < BLURAY_SIZE_THRESHOLD ? "BDRip" : "BluRay");
        // Correct source if incorrect
        } else if (release.getVideoSource() != null && release.getVideoSource().equalsIgnoreCase("bluray") && info.getFileSizeInGigas() < BLURAY_SIZE_THRESHOLD) {
            release.setVideoSource("BDRip");
        }
    }

    private void fixVideoTrackTitle() {
        info.setVideoTrackTitle(getFormattedTitle());
    }
}
