package lan.vandiemens.media.analysis;

/**
 *
 * @author vmurcia
 */
public enum VideoSource {
    FULL_BLURAY,
    BD_REMUX,
    BD_RIP,
    WEB_DOWNLOAD,
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
                return WEB_DOWNLOAD;
            case "bdr":
            case "brip":
            case "brrip":
            case "bdrip":
            case "bluray":
            case "blu-ray":
                return BD_RIP;
            case "blu-ray.remux":
            case "bluray.remux":
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
                return "Blu-ray";
            case BD_REMUX:
                return "BDRemux";
            case BD_RIP:
                return "BDRip";
            case WEB_DOWNLOAD:
                return "Web-DL";
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
