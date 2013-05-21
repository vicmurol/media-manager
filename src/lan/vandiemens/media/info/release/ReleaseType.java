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
    UNDEFINED;


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
                return UNDEFINED;
        }
    }
}
