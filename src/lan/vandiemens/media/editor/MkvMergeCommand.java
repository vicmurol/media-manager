package lan.vandiemens.media.editor;

import java.util.ArrayList;
import java.util.List;
import lan.vandiemens.media.MediaFile;
import lan.vandiemens.media.SubtitleFile;
import lan.vandiemens.media.info.track.MediaTrack;
import lan.vandiemens.media.info.track.Track;
import lan.vandiemens.media.info.track.TrackType;
import lan.vandiemens.media.info.track.VideoTrack;

/**
 *
 * @author vmurcia
 */
public class MkvMergeCommand extends MkvToolnixCommand {

    private static String TRACK_ORDER_OPTION = "--track-order";
    private static String LANGUAGE_OPTION = "--language";
    private static String DEFAULT_TRACK_OPTION = "--default-track";
    private static String FORCED_TRACK_OPTION = "--forced-track";
    private static String TRACK_NAME_OPTION = "--track-name";
    private static String OUTPUT_OPTION = "-o";
    private static String TITLE_OPTION = "--title";
    private static String AUDIO_TRACKS_OPTION = "-a";
    private static String NO_AUDIO_OPTION = "-A";
    private static String VIDEO_TRACKS_OPTION = "-d";
    private static String DISPLAY_DIMENSIONS_OPTION = "--display-dimensions";
    private static String NO_VIDEO_OPTION = "-D";
    private static String SUBTITLE_TRACKS_OPTION = "-s";
    private static String NO_SUBTITLES_OPTION = "-S";
    private static String NO_TRACK_TAGS_OPTION = "-T";
    private static String NO_GLOBAL_TAGS_OPTION = "--no-global-tags";
    private static String NO_CHAPTERS_OPTION = "--no-chapters";
    private static int DEFAULT_TRACK_DESCRIPTION_LENGTH = 200;
    private String[] inputDescriptions = null;
    private String trackOrder = null;
    private boolean hasChapters = false;

    public MkvMergeCommand(MediaFile mediaFile) {
        mediaFile.consolidate();
        title = mediaFile.getFormattedTitle();
        inputFile = mediaFile.getMainFile();
        outputFile = generateOutputMkvFile(mediaFile);
        inputDescriptions = getInputDescriptions(mediaFile);
    }

    @Override
    public String toString() {
        StringBuilder command = new StringBuilder(DEFAULT_COMMAND_LENGTH);
        command.append(DOUBLE_QUOTES).append(MkvToolNixUtils.getMkvMergeExecutable().getAbsolutePath()).append(DOUBLE_QUOTES);
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
        result.add(MkvToolNixUtils.getMkvMergeExecutable().getAbsolutePath());

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
        int subtitleIndex = 1;
        int audioCounter = 0;
        int audioTrackCount = mediaFile.getMediaInfo().getEnabledAudioTrackCount();
        int subtitleCounter = 0;
        StringBuilder order = new StringBuilder();
        StringBuilder trackTypeDescription = new StringBuilder();
        StringBuilder mainDescription = new StringBuilder(200);

        Track[] tracks = mediaFile.getMediaInfo().getEnabledTracks();
        for (int i = 0; i < tracks.length; i++) {
//            System.out.println(tracks[i].toXml());
            switch (tracks[i].getType()) {
                case VIDEO: // First track, as well as only video track
                    mainDescription.append(getDescription((MediaTrack) tracks[i]));
                    mainDescription.append(SPACE);
                    order.append("0");
                    trackTypeDescription.append(DOUBLE_QUOTES).append(VIDEO_TRACKS_OPTION).append(DOUBLE_QUOTES);
                    trackTypeDescription.append(SPACE);
                    trackTypeDescription.append(DOUBLE_QUOTES).append(((MediaTrack) tracks[i]).getTid()).append(DOUBLE_QUOTES);
                    trackTypeDescription.append(SPACE);
                    break;
                case AUDIO:
                    mainDescription.append(getDescription((MediaTrack) tracks[i]));
                    mainDescription.append(SPACE);
                    order.append(COMMA);
                    order.append("0");
                    if (audioCounter++ > 0) {
                        trackTypeDescription.append(COMMA);
                    } else {
                        trackTypeDescription.append(DOUBLE_QUOTES).append(AUDIO_TRACKS_OPTION).append(DOUBLE_QUOTES);
                        trackTypeDescription.append(SPACE);
                        trackTypeDescription.append(DOUBLE_QUOTES);
                    }
                    trackTypeDescription.append(((MediaTrack) tracks[i]).getTid());
                    if (audioCounter == audioTrackCount) { // Last audio track
                        trackTypeDescription.append(DOUBLE_QUOTES);
                        trackTypeDescription.append(SPACE);
                    }
                    break;
                case SUBTITLE:
                    if (tracks[i] instanceof SubtitleFile) {
                        descriptions[subtitleIndex] = getDescription((SubtitleFile) tracks[i]);
                        order.append(COMMA);
                        order.append(subtitleIndex++);
                    } else {
                        mainDescription.append(getDescription((MediaTrack) tracks[i]));
                        mainDescription.append(SPACE);
                        order.append(COMMA);
                        order.append("0");
                        if (subtitleCounter++ > 0) {
                            trackTypeDescription.append(COMMA);
                        } else {
                            trackTypeDescription.append(DOUBLE_QUOTES).append(SUBTITLE_TRACKS_OPTION).append(DOUBLE_QUOTES);
                            trackTypeDescription.append(SPACE);
                            trackTypeDescription.append(DOUBLE_QUOTES);
                        }
                        trackTypeDescription.append(((MediaTrack) tracks[i]).getTid());
                    }
                    break;
                default: // MENU
                    hasChapters = true;
                    continue;
            }
            order.append(":");
            order.append(((MediaTrack)tracks[i]).getTid());
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
        if (!hasChapters) {
            mainDescription.append(DOUBLE_QUOTES).append(NO_CHAPTERS_OPTION).append(DOUBLE_QUOTES);
            mainDescription.append(SPACE);
        }
        mainDescription.append(DOUBLE_QUOTES).append(LEFT_PARENTHESIS).append(DOUBLE_QUOTES);
        mainDescription.append(SPACE);
        mainDescription.append(DOUBLE_QUOTES).append(mediaFile.getMainFile().getAbsolutePath()).append(DOUBLE_QUOTES);
        mainDescription.append(SPACE);
        mainDescription.append(DOUBLE_QUOTES).append(RIGHT_PARENTHESIS).append(DOUBLE_QUOTES);

        descriptions[0] = mainDescription.toString();
        trackOrder = order.toString();
        return descriptions;
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
        description.append(DOUBLE_QUOTES).append(subtitleFile.getFile().getAbsolutePath()).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(RIGHT_PARENTHESIS).append(DOUBLE_QUOTES);

        return description.toString();
    }

    private String getDescription(MediaTrack mediaTrack) {
        StringBuilder description = new StringBuilder(DEFAULT_TRACK_DESCRIPTION_LENGTH);
        description.append(DOUBLE_QUOTES).append(LANGUAGE_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(mediaTrack.getTid()).append(COLON).append(mediaTrack.getLanguageCode()).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(TRACK_NAME_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(mediaTrack.getTid()).append(COLON).append(getTrackName(mediaTrack)).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(DEFAULT_TRACK_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(mediaTrack.getTid()).append(COLON).append(mediaTrack.isDefault() ? "yes" : "no").append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(FORCED_TRACK_OPTION).append(DOUBLE_QUOTES);
        description.append(SPACE);
        description.append(DOUBLE_QUOTES).append(mediaTrack.getTid()).append(COLON).append(mediaTrack.isForced() ? "yes" : "no").append(DOUBLE_QUOTES);

        if (mediaTrack.getType() == TrackType.VIDEO) {
            description.append(SPACE);
            description.append(DOUBLE_QUOTES).append(DISPLAY_DIMENSIONS_OPTION).append(DOUBLE_QUOTES);
            description.append(SPACE);
            description.append(DOUBLE_QUOTES).append(mediaTrack.getTid()).append(COLON).append(((VideoTrack) mediaTrack).getResolution()).append(DOUBLE_QUOTES);
        }

        return description.toString();
    }

    private String getTrackName(MediaTrack mediaTrack) {
        switch (mediaTrack.getType()) {
            case VIDEO:
                return title;
            default:
                return mediaTrack.getFormattedTitle();
        }
    }
}
