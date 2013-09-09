package lan.vandiemens.media.matroska;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lan.vandiemens.media.EditableMkvProperties;
import lan.vandiemens.media.MediaFile;
import lan.vandiemens.media.info.track.Track;

/**
 *
 * @author vmurcia
 */
public class MkvPropEditCommand extends MkvToolnixCommand {

    public static final String EDIT_OPTION = "--edit";
    public static final String ADD_OPTION = "--add";
    public static final String SET_OPTION = "--set";
    public static final String DELETE_OPTION = "--delete";
    public static final String TRACK_HEADER_OPTION = "track:";
    public static final String SEGMENT_INFO = "info";
    public static final String TITLE_PROPERTY = "title";
    public static final String DEFAULT_TRACK_FLAG_PROPERTY = "flag-default";
    public static final String FORCED_TRACK_FLAG_PROPERTY = "flag-forced";
    public static final String TRACK_NAME_PROPERTY = "name";
    public static final String LANGUAGE_PROPERTY = "language";
    private String[] trackEditDescriptions = null;


    public MkvPropEditCommand(MediaFile mediaFile) {
        title = mediaFile.getFormattedTitle();
        inputFile = mediaFile.getMainFile();
        outputFile = generateOutputMkvFile(mediaFile);
        trackEditDescriptions = getTrackEditDescriptions(mediaFile);
    }

    public MkvPropEditCommand(File mkvFile, EditableMkvProperties mkvProperties) {
        title = mkvProperties.getTitle();
        outputFile = inputFile = mkvFile;
        trackEditDescriptions = getTrackEditDescriptions(mkvProperties);
    }

    private String[] getTrackEditDescriptions(MediaFile mediaFile) {
        Track[] tracks = mediaFile.getMediaInfo().getTracks();
        String[] descriptions = new String[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            descriptions[i] = tracks[i].getMkvPropEditDescription();
        }
        return descriptions;
    }

    private String[] getTrackEditDescriptions(EditableMkvProperties mkvProperties) {
        String[] descriptions = new String[mkvProperties.getTrackCount()];
        for (int i = 0; i < mkvProperties.getTrackCount(); i++) {
            descriptions[i] = getMkvPropEditDescription(mkvProperties, i);
        }
        return descriptions;
    }

    private String getMkvPropEditDescription(EditableMkvProperties mkvProperties, int trackIndex) {
        StringBuilder builder = new StringBuilder();
        builder.append(EDIT_OPTION);
        builder.append(" ");
        // mkvpropedit uses track numbers instead of track IDs, unlike mkvmerge and mkvextrack
        // Track numbering starts at 1
        int trackNumber = trackIndex + 1;
        builder.append(TRACK_HEADER_OPTION).append(trackNumber);
        builder.append(" ");
        builder.append(SET_OPTION);
        builder.append(" ");
        builder.append(DEFAULT_TRACK_FLAG_PROPERTY).append("=").append(mkvProperties.isDefault(trackIndex) ? "1" : "0");
        builder.append(" ");
        builder.append(SET_OPTION);
        builder.append(" ");
        builder.append(FORCED_TRACK_FLAG_PROPERTY).append("=").append(mkvProperties.isForced(trackIndex) ? "1" : "0");
        builder.append(" ");
        builder.append(SET_OPTION);
        builder.append(" ");
        builder.append(TRACK_NAME_PROPERTY).append("=").append("\"").append(mkvProperties.getTitle(trackIndex)).append("\"");
        builder.append(" ");
        builder.append(SET_OPTION);
        builder.append(" ");
        builder.append(LANGUAGE_PROPERTY).append("=").append("\"").append(mkvProperties.getLanguage(trackIndex).getThreeLettersIsoCode()).append("\"");

        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder command = new StringBuilder(DEFAULT_COMMAND_LENGTH);
        command.append(DOUBLE_QUOTES).append(MkvToolNixHelper.getMkvPropEditExecutable().getAbsolutePath()).append(DOUBLE_QUOTES);
        command.append(SPACE);
        command.append(DOUBLE_QUOTES).append(inputFile.getAbsolutePath()).append(DOUBLE_QUOTES);
        command.append(SPACE);
        command.append(EDIT_OPTION);
        command.append(SPACE);
        command.append(SEGMENT_INFO);
        command.append(SPACE);
        command.append(SET_OPTION);
        command.append(SPACE);
        command.append(DOUBLE_QUOTES).append(TITLE_PROPERTY).append(EQUALS_SIGN).append(title).append(DOUBLE_QUOTES);
        for (String description : trackEditDescriptions) {
            command.append(SPACE);
            command.append(description);
        }

        return command.toString();
    }

    @Override
    public List<String> toList() {
        List<String> result = new ArrayList<>(2);

        // Add command
        result.add(MkvToolNixHelper.getMkvPropEditExecutable().getAbsolutePath());

        // Add arguments
        StringBuilder argumentSequence = new StringBuilder(DEFAULT_COMMAND_LENGTH);
        argumentSequence.append(DOUBLE_QUOTES).append(inputFile.getAbsolutePath()).append(DOUBLE_QUOTES);
        argumentSequence.append(SPACE);
        argumentSequence.append(EDIT_OPTION);
        argumentSequence.append(SPACE);
        argumentSequence.append(SEGMENT_INFO);
        argumentSequence.append(SPACE);
        argumentSequence.append(SET_OPTION);
        argumentSequence.append(SPACE);
        argumentSequence.append(DOUBLE_QUOTES).append(TITLE_PROPERTY).append(EQUALS_SIGN).append(title).append(DOUBLE_QUOTES);
        argumentSequence.append(SPACE);
        for (int i = 0; i < trackEditDescriptions.length - 1; i++) {
            argumentSequence.append(trackEditDescriptions[i]);
            argumentSequence.append(SPACE);
        }
        argumentSequence.append(trackEditDescriptions[trackEditDescriptions.length - 1]);
        result.add(argumentSequence.toString());

        return result;
    }
}
