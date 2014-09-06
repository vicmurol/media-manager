package lan.vandiemens.media.matroska;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lan.vandiemens.media.MediaFile;
import lan.vandiemens.media.info.track.Track;
import lan.vandiemens.media.info.track.TrackType;
import lan.vandiemens.util.file.FileUtils;
import lan.vandiemens.util.lang.Language;

/**
 *
 * @author vmurcia
 */
public class MkvExtractSubtitlesCommand extends MkvToolnixCommand {

    private static final String TRACKS_MODE = "tracks";
    private final List<Track> chosenTracks;

    public MkvExtractSubtitlesCommand (MediaFile mediaFile) {
        inputFile = mediaFile.getMainFile();
        chosenTracks = new ArrayList<>(mediaFile.getSubtitleTrackCount());
        for (Track track : mediaFile.getTracks()) {
            if (track.getType() == TrackType.SUBTITLE) {
                chosenTracks.add(track);
            }
        }
    }

    public MkvExtractSubtitlesCommand (MediaFile mediaFile, Language[] languages) {
        inputFile = mediaFile.getMainFile();
        chosenTracks = new ArrayList<>(mediaFile.getSubtitleTrackCount());
        for (Track track : mediaFile.getTracks()) {
            if (track.getType() == TrackType.SUBTITLE) {
                for (Language language : languages) {
                    if (track.hasLanguage(language)) {
                        chosenTracks.add(track);
                        break;
                    }
                }
                // Consider a media file with only one subtitle track whose language
                // is undefined as if it was the default language
                if (mediaFile.getSubtitleTrackCount() == 1 && track.getLanguage() == Language.UNDEFINED) {
                    System.out.println("Undefined language for unique subtitle track has been considered as media original language: " + mediaFile.getMediaInfo().getOriginalLanguage());
                    track.setLanguage(mediaFile.getMediaInfo().getOriginalLanguage());
                    chosenTracks.add(track);
                }
            }
        }
    }

    @Override
    public String toString() {
        String command = DOUBLE_QUOTES + MkvToolNixHelper.getMkvExtractExePath() + DOUBLE_QUOTES + SPACE;
        command += TRACKS_MODE + SPACE;
        command += DOUBLE_QUOTES + inputFile.getAbsolutePath() + DOUBLE_QUOTES;
        for (Track track : chosenTracks) {
            command += SPACE + getExtractionDescription(track);
        }

        return command;
    }

    private String getExtractionDescription(Track track) {
        String description = track.getTrackId() + COLON;
        description += inputFile.getParent() + File.separator;
        description += FileUtils.getNameWithoutExtension(inputFile);
        description += UNDERSCORE + "track" + track.getTrackNumber();
        if (track.getTitle() != null) {
            description += UNDERSCORE + track.getTitle();
        }
        description += UNDERSCORE + track.getLanguageCode();
        description += PERIOD + track.getAssociatedFileExtension();
        return description;
    }

    @Override
    public List<String> toList() {
        List<String> list = new ArrayList<>();

        // Add command
        list.add(MkvToolNixHelper.getMkvExtractExePath());

        // Add mode
        list.add(TRACKS_MODE);

        // Add input file
        list.add(DOUBLE_QUOTES + inputFile.getAbsolutePath() + DOUBLE_QUOTES);

        // Add the rest of the options
        for (Track track : chosenTracks) {
            list.add(getExtractionDescription(track));
        }

        return list;
    }
}
