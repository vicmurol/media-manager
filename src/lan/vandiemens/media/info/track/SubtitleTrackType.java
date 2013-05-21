package lan.vandiemens.media.info.track;

/**
 *
 * @author vmurcia
 */
public enum SubtitleTrackType {

    COMPLETE, FOR_HEARING_IMPAIRED, FORCED, COMMENTARY;

    public static SubtitleTrackType parse(String type) {
        switch (type.toLowerCase()) {
            case "normal":
            case "complete":
                return COMPLETE;
            case "fhi":
            case "hi":
                return FOR_HEARING_IMPAIRED;
            case "forced":
                return FORCED;
            case "comment":
                return COMMENTARY;
            default:
                return COMPLETE;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case COMPLETE:
                return "Complete";
            case FORCED:
                return "Forced";
            case FOR_HEARING_IMPAIRED:
                return "For Hearing-Impaired";
            case COMMENTARY:
                return "Commentary";
            default:
                throw new AssertionError();
        }
    }
}
