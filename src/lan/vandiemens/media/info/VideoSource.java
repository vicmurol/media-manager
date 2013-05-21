package lan.vandiemens.media.info;

/**
 *
 * @author vmurcia
 */
public enum VideoSource {
    FULL_BLURAY,
    BD_REMUX,
    BD_RIP,
    HD_ITUNES,
    HDTV_RIP,
    TV_RIP,
    HD_DVD,
    FULL_DVD,
    DVD_REMUX,
    DVD_RIP,
    DVD_SCREENER,
    TS_SCREENER,
    UNKNOWN;

    public static VideoSource parse(String source) {
        switch (source.toLowerCase()) {
            case "webdl":
            case "web-dl":
            case "hditunes":
                return HD_ITUNES;
            case "bdr":
            case "brip":
            case "brrip":
            case "bdrip":
            case "bluray":
                return BD_RIP;
            case "bdremux":
                return BD_REMUX;
            case "hdtv":
            case "hdtvrip":
                return HDTV_RIP;
            case "screener":
            case "dvdscreener":
                return DVD_SCREENER;
            case "dvdrip":
                return DVD_RIP;
            case "dvdr":
                return DVD_REMUX;
            case "hddvd":
                return HD_DVD;
            default:
                return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case FULL_BLURAY:
                return "BluRay";
            case BD_REMUX:
                return "BDRemux";
            case BD_RIP:
                return "BDRip";
            case HD_ITUNES:
                return "HDiTunes";
            case HDTV_RIP:
                return "HDTVRip";
            case TV_RIP:
                return "TVRip";
            case HD_DVD:
                return "HDDVD";
            case FULL_DVD:
                return "DVD";
            case DVD_REMUX:
                return "DVDRemux";
            case DVD_RIP:
                return "DVDRip";
            case DVD_SCREENER:
                return "DVDScreener";
            case TS_SCREENER:
                return "TSScreener";
            default:
                return "Unknown";
        }
    }
}
