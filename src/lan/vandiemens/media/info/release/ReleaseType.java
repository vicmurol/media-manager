package lan.vandiemens.media.info.release;

/**
 *
 * @author vmurcia
 */
public enum ReleaseType {
    PROPER,
    FIXED,
    LIMITED,
    REMASTERED,
    REMAKE,
    DIRECTORS_CUT,
    UNRATED,
    EXTENDED_EDITION,
    COMMON;


    public static ReleaseType parse(String type) {
        switch (type.toLowerCase()) {
            case "proper":
            case "fixed":
                return PROPER;
            case "remastered":
                return REMASTERED;
            case "remake":
                return REMAKE;
            case "director's cut":
                return DIRECTORS_CUT;
            case "unrated":
                return UNRATED;
            case "extended":
            case "extended edition":
                return EXTENDED_EDITION;
            case "limited":
                return LIMITED;
            default:
                return COMMON;
        }
    }

    public boolean isSpecialVersion() {
        return this == EXTENDED_EDITION || this == DIRECTORS_CUT || this == REMASTERED;
    }

    @Override
    public String toString() {
        switch (this) {
            case PROPER:
            case FIXED:
                return "Proper";
            case REMASTERED:
                return "Remastered";
            case REMAKE:
                return "Remake";
            case UNRATED:
                return "Unrated";
            case EXTENDED_EDITION:
                return "Extended Edition";
            case DIRECTORS_CUT:
                return "Director's Cut";
            case LIMITED:
                return "Limited";
            default:
                return "Common";
        }
    }
}
