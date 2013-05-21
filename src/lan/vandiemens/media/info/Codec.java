package lan.vandiemens.media.info;

/**
 *
 * @author vmurcia
 */
public enum Codec {
    AAC,
    AC3,
    DTS,
    MP3,
    DIVX,
    XVID,
    H_264,
    UNDEFINED;


    public static Codec parse(String codec) {
        switch (codec.toLowerCase()) {
            case "aac":
                return AAC;
            case "ac3":
                return AC3;
            case "dts":
                return DTS;
            case "mp3":
                return MP3;
            case "avc":
            case "h264":
            case "x264":
            case "h.264":
                return H_264;
            case "xvid":
                return XVID;
            case "divx":
            case "div4":
            case "div5":
                return DIVX;
            default:
                return UNDEFINED;
        }
    }

    public static boolean isPartialName(String value) {
        switch (value.toLowerCase()) {
            case "aac2":
            case "dd5":
            case "h":
                return true;
            default:
                return false;
        }
    }
}
