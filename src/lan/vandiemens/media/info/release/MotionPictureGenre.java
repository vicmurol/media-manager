package lan.vandiemens.media.info.release;

/**
 *
 * @author vmurcia
 */
public enum MotionPictureGenre {
    MOVIE,
    TV_SERIES,
    TV_PROGRAM,
    DOCUMENTARY,
    DOCUMENTARY_SERIES,
    SPORTS_BROADCAST,
    GENERIC_VIDEO;

    @Override
    public String toString() {
        switch (this) {
            case MOVIE:
                return "movie";
            case TV_SERIES:
                return "TV series";
            case TV_PROGRAM:
                return "TV program";
            case DOCUMENTARY:
            case DOCUMENTARY_SERIES:
                return "documentary";
            case SPORTS_BROADCAST:
                return "sports broadcast";
            default:
                return "video";
        }
    }
}
