package lan.vandiemens.media.info;

/**
 *
 * @author vmurcia
 */
public enum VideoMode {
    // High-definition television
    HDTV_1080_PROGRESSIVE,
    HDTV_1080_INTERLACED,
    HDTV_720_PROGRESSIVE,

    // Enhanced-definition television
    EDTV_576_PROGRESSIVE,
    EDTV_480_PROGRESSIVE,

    // Standard-definition television
    SDTV_576_INTERLACED,
    SDTV_480_INTERLACED,

    UNKNOWN;

    public static VideoMode parse(String mode) {
        switch (mode.toLowerCase()) {
            case "1080":
            case "1080p":
                return HDTV_1080_PROGRESSIVE;
            case "720":
            case "720p":
                return HDTV_720_PROGRESSIVE;
            case "1080i":
                return HDTV_1080_INTERLACED;
            case "480":
            case "480p":
                return EDTV_480_PROGRESSIVE;
            case "576":
            case "576p":
                return EDTV_576_PROGRESSIVE;
            case "480i":
                return SDTV_480_INTERLACED;
            case "576i":
                return SDTV_576_INTERLACED;
            default:
                return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case HDTV_1080_PROGRESSIVE:
                return "1080p";
            case HDTV_720_PROGRESSIVE:
                return "720p";
            case HDTV_1080_INTERLACED:
                return "1080i";
            case EDTV_480_PROGRESSIVE:
                return "480p";
            case EDTV_576_PROGRESSIVE:
                return "576p";
            case SDTV_480_INTERLACED:
                return "480i";
            case SDTV_576_INTERLACED:
                return "576i";
            default:
                return "Unknown";
        }
    }
}
