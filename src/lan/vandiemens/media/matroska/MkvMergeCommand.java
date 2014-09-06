package lan.vandiemens.media.matroska;

import java.util.ArrayList;
import java.util.List;
import lan.vandiemens.media.AudioFile;
import lan.vandiemens.media.MediaFile;
import lan.vandiemens.media.SubtitleFile;
import lan.vandiemens.media.info.track.Track;
import lan.vandiemens.media.info.track.TrackType;
import lan.vandiemens.media.info.track.VideoTrack;

/**
 *
 * @author vmurcia
 */
public class MkvMergeCommand extends MkvToolnixCommand {

    private static final String MAIN_FILE_ID = "0";
    private static final String TRACK_ORDER_OPTION = "--track-order";
    private static final String LANGUAGE_OPTION = "--language";
    private static final String DEFAULT_TRACK_OPTION = "--default-track";
    private static final String FORCED_TRACK_OPTION = "--forced-track";
    private static final String TRACK_NAME_OPTION = "--track-name";
    private static final String OUTPUT_OPTION = "-o";
    private static final String TITLE_OPTION = "--title";
    private static final String AUDIO_TRACKS_OPTION = "-a";
    private static final String NO_AUDIO_OPTION = "-A";
    private static final String VIDEO_TRACKS_OPTION = "-d";
    private static final String DISPLAY_DIMENSIONS_OPTION = "--display-dimensions";
    private static final String NO_VIDEO_OPTION = "-D";
    private static final String SUBTITLE_TRACKS_OPTION = "-s";
    private static final String NO_SUBTITLES_OPTION = "-S";
    private static final String NO_TRACK_TAGS_OPTION = "-T";
    private static final String NO_GLOBAL_TAGS_OPTION = "--no-global-tags";
    private static final String NO_CHAPTERS_OPTION = "--no-chapters";
    private static final int DEFAULT_TRACK_DESCRIPTION_LENGTH = 200;
    private String[] inputDescriptions = null;
    private String trackOrder = null;

    public MkvMergeCommand(MediaFile mediaFile) {
        mediaFile.consolidate();
        title = mediaFile.getFormattedTitle();
        inputFile = mediaFile.getMainFile();
        outputFile = generateOutputMkvFile(mediaFile);
        inputDescriptions = getInputDescriptions(mediaFile);
        trackOrder = getTrackOrder(mediaFile);
    }

    @Override
    public String toString() {
        StringBuilder command = new StringBuilder(DEFAULT_COMMAND_LENGTH);
        command.append(DOUBLE_QUOTES).append(MkvToolNixHelper.getMkvMergeExecutable().getAbsolutePath()).append(DOUBLE_QUOTES);
        command.append(SPACE);
        command.append(OUTPUT_OPTION);
        command.append(SPACE);
        command.append(DOUBLE_QUOTES).append(outputFile.getAbsolutePath()).append(DOUBLE_QUOTES);
        command.append(SPACE);
        for (String description : inputDescriptions) {
            command.append(description);
            command.append(SPACE);
        }
        command.append(DOUBLE_QUOTES).append(TRACK_ORDER_OPTION).append(DOUBLE_QUOTES);
        command.append(SPACE);
        command.append(DOUBLE_QUOTES).append(trackOrder).append(DOUBLE_QUOTES);
        command.append(SPACE);
        command.append(DOUBLE_QUOTES).append(TITLE_OPTION).append(DOUBLE_QUOTES);
        command.append(SPACE);
        command.append(DOUBLE_QUOTES).append(title).append(DOUBLE_QUOTES);

        return command.toString();
    }

    @Override
    public List<String> toList() {
        // Given the way mkvmerge processes the command, it should be split in 4
        // parts: the real command, the output argument, the output folder, and
        // the remainder options
        List<String> result = new ArrayList<>(4);

        // Add command
        result.add(MkvToolNixHelper.getMkvMergeExePath());

        // Add output option
        result.add(OUTPUT_OPTION);

        // Add output folder
        result.add(DOUBLE_QUOTES + outputFile.getAbsolutePath() + DOUBLE_QUOTES);

        // Add the rest of the options
        StringBuilder argumentSequence = new StringBuilder(DEFAULT_COMMAND_LENGTH);
        for (String description : inputDescriptions) {
            argumentSequence.append(description);
            argumentSequence.append(SPACE);
        }
        argumentSequence.append(DOUBLE_QUOTES).append(TRACK_ORDER_OPTION).append(DOUBLE_QUOTES);
        argumentSequence.append(SPACE);
        argumentSequence.append(DOUBLE_QUOTES).append(trackOrder).append(DOUBLE_QUOTES);
        argumentSequence.append(SPACE);
        argumentSequence.append(DOUBLE_QUOTES).append(TITLE_OPTION).append(DOUBLE_QUOTES);
        argumentSequence.append(SPACE);
        argumentSequence.append(DOUBLE_QUOTES).append(title).append(DOUBLE_QUOTES);
        result.add(argumentSequence.toString());

        return result;
    }

    private String[] getInputDescriptions(MediaFile mediaFile) {
        String[] descriptions = new String[mediaFile.getFileCount()];
        int fileId = 1; // Main video file is always FID=0, as its video track is always the first one in the output file
        int audioTrackCounter = 0;
        int audioTrackCount = mediaFile.getMediaInfo().getEnabledInternalAudioTrackCount();
        int subtitleCounter = 0;
        StringBuilder trackTypeDescription = new StringBuilder();
        StringBuilder mainDescription = new StringBuilder(200);

        Track[] tracks = mediaFile.getEnabledTracks();
        for (Track track : tracks) {
            switch (track.getType()) {
                case VIDEO: // First and only video track
                    mainDescription.append(getDescription(track));
                    mainDescription.append(SPACE);
                    trackTypeDescription.append(DOUBLE_QUOTES).append(VIDEO_TRACKS_OPTION).append(DOUBLE_QUOTES);
                    trackTypeDescription.append(SPACE);
                    trackTypeDescription.append(DOUBLE_QUOTES).append(track.getTrackId()).append(DOUBLE_QUOTES);
                    trackTypeDescription.append(SPACE);
                    break;
                case AUDIO:
                    if (track.isExternal()) {
                        descriptions[fileId++] = getDescription((AudioFile) track);
                    } else {
                        mainDescription.append(getDescription(track));
                        mainDescription.append(SPACE);
                        if (audioTrackCounter++ > 0) {
                            trackTypeDescription.append(COMMA);
                        } else {
                            trackTypeDescription.append(DOUBLE_QUOTES).append(AUDIO_TRACKS_OPTION).append(DOUBLE_QUOTES);
                            trackTypeDescription.append(SPACE);
                            trackTypeDescription.append(DOUBLE_QUOTES);
                        }
                        trackTypeDescription.append(track.getTrackId());
                        if (audioTrackCounter == audioTrackCount) { // Last audio track
                            trackTypeDescription.append(DOUBLE_QUOTES);
                            trackTypeDescription.append(SPACE);
                        }
                    }
                    break;
                case SUBTITLE:
                    if (track.isExternal()) {
                        descriptions[fileId++] = getDescription((SubtitleFile) track);
                    } else {
                        mainDescription.append(getDescription(track));
                        mainDescription.append(SPACE);
                        if (subtitleCounter++ > 0) {
                            trackTypeDescription.append(COMMA);
                        } else {
                            trackTypeDescription.append(DOUBLE_QUOTES).append(SUBTITLE_TRACKS_OPTION).append(DOUBLE_QUOTES);
                            trackTypeDescription.append(SPACE);
                            trackTypeDescription.append(DOUBLE_QUOTES);
                        }
                        trackTypeDescription.append(track.getTrackId());
                    }
                    break;
                default: // Can't happen
            }
        }
        if (subtitleCounter > 0) {
            trackTypeDescription.append(DOUBLE_QUOTES);
        } else {
            trackTypeDescription.append(DOUBLE_QUOTES).append(NO_SUBTITLES_OPTION).append(DOUBLE_QUOTES);
        }

        mainDescription.append(trackTypeDescription);
        mainDescription.append(SPACE);
        mainDescription.append(DOUBLE_QUOTES).append(NO_TRACK_TAGS_OPTION).append(DOUBLE_QUOTES);
        mainDescription.append(SPACE);
        mainDescription.append(DOUBLE_QUOTES).append(NO_GLOBAL_TAGS_OPTION).append(DOUBLE_QUOTES);
        mainDescription.append(SPACE);
        if (!mediaFile.hasChapters()) {
            mainDescription.append(DOUBLE_QUOTES).append(NO_CHAPTERS_OPTION).append(DOUBLE_QUOTES);
            mainDescription.append(SPACE);
        }
        mainDescription.append(DOUBLE_QUOTES).append(LEFT_PARENTHESIS).append(DOUBLE_QUOTES);
        mainDescription.append(SPACE);
        mainDescription.append(DOUBLE_QUOTES).append(mediaFile.getMainFile().getAbsolutePath()).append(DOUBLE_QUOTES);
        mainDescription.append(SPACE);
        mainDescription.append(DOUBLE_QUOTES).append(RIGHT_PARENTHESIS).append(DOUBLE_QUOTES);

        descriptions[0] = mainDescription.toString();
        return descriptions;
    }

    /**
     * Returns a text consisting of a comma-separated list of pairs IDs.
     * <p>
     * Each pair contains first the file ID (FID1) which is simply the number of
     * the file on the command line starting at 0.
     * The second is a track ID (TID1) from that file.
     *
     * @param mediaFile the media file containing the tracks whose order has to
     *                  be described
     * @return a track order description
     */
    private String getTrackOrder(MediaFile mediaFile) {
        int fileId = 1; // Main video file is always FID=0, as its video track is always the first one in the output file
        StringBuilder order = new StringBuilder();
        for (Track track : mediaFile.getEnabledTracks()) {
            switch (track.getType()) {
                case VIDEO: // First and only video track
                    order.append(MAIN_FILE_ID);
                    break;
                case AUDIO:
                case SUBTITLE:
                    if (track.isExternal()) {
                        order.append(COMMA);
                        order.append(fileId++);
                    } else {
                        order.append(COMMA);
                        order.append(MAIN_FILE_ID);
                    }
                    break;
                default: // Can't happen
                    continue;
            }
            order.append(COLON);
            order.append(track.getTrackId());
        }

        return order.toString();
    }

    private String getDescription(AudioFile audioFile) {
        StringBuilder description = new StringBuilder(DEFAULT_TRACK_DESCRIPTION_LENGTH);
        description.append(DOUBLE_QUOTES).append(LANGUAGE_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append("0:").append(audioFile.getLanguageCode()).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(TRACK_NAME_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append("0:").append(getTrackName(audioFile)).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(DEFAULT_TRACK_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append("0:").append(audioFile.isDefault() ? "yes" : "no").append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(FORCED_TRACK_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append("0:").append(audioFile.isForced() ? "yes" : "no").append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(AUDIO_TRACKS_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append("0").append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(NO_VIDEO_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(NO_SUBTITLES_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(NO_TRACK_TAGS_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(NO_GLOBAL_TAGS_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(NO_CHAPTERS_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(LEFT_PARENTHESIS).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(audioFile.getAbsolutePath()).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(RIGHT_PARENTHESIS).append(DOUBLE_QUOTES);

        return description.toString();
    }

    private String getDescription(SubtitleFile subtitleFile) {
        StringBuilder description = new StringBuilder(DEFAULT_TRACK_DESCRIPTION_LENGTH);
        description.append(DOUBLE_QUOTES).append(LANGUAGE_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append("0:").append(subtitleFile.getLanguageCode()).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(TRACK_NAME_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append("0:").append(getTrackName(subtitleFile)).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(DEFAULT_TRACK_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append("0:").append(subtitleFile.isDefault() ? "yes" : "no").append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(FORCED_TRACK_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append("0:").append(subtitleFile.isForced() ? "yes" : "no").append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(SUBTITLE_TRACKS_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append("0").append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(NO_VIDEO_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(NO_AUDIO_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(NO_TRACK_TAGS_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(NO_GLOBAL_TAGS_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(NO_CHAPTERS_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(LEFT_PARENTHESIS).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(subtitleFile.getAbsolutePath()).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(RIGHT_PARENTHESIS).append(DOUBLE_QUOTES);

        return description.toString();
    }

    private String getDescription(Track track) {
        StringBuilder description = new StringBuilder(DEFAULT_TRACK_DESCRIPTION_LENGTH);
        description.append(DOUBLE_QUOTES).append(LANGUAGE_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(track.getTrackId()).append(COLON).append(track.getLanguageCode()).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(TRACK_NAME_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(track.getTrackId()).append(COLON).append(getTrackName(track)).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(DEFAULT_TRACK_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(track.getTrackId()).append(COLON).append(track.isDefault() ? "yes" : "no").append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(FORCED_TRACK_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(track.getTrackId()).append(COLON).append(track.isForced() ? "yes" : "no").append(DOUBLE_QUOTES);

        if (track.getType() == TrackType.VIDEO) {
            description.append(SPACE);
            description.append(DOUBLE_QUOTES).append(DISPLAY_DIMENSIONS_OPTION).append(DOUBLE_QUOTES);
            description.append(SPACE);
            description.append(DOUBLE_QUOTES).append(track.getTrackId()).append(COLON).append(((VideoTrack) track).getResolution()).append(DOUBLE_QUOTES);
        }

        return description.toString();
    }

    private String getTrackName(Track track) {
        switch (track.getType()) {
            case VIDEO:
                return title;
            default:
                return track.getFormattedTitle();
        }
    }
}
