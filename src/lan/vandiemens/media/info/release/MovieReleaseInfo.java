package lan.vandiemens.media.info.release;

/**
 *
 * @author vmurcia
 */
public class MovieReleaseInfo extends ReleaseInfo {

    public MovieReleaseInfo(String title) {
        super(title);
        genre = ReleaseGenre.MOVIE;
    }

    public MovieReleaseInfo(String title, int year) {
        this(title);
        this.year = year;
    }

    public boolean hasKnownReleaseDate() {
        return year > 0;
    }

    @Override
    public boolean hasCompleteBasicInfo() {
        return hasKnownReleaseDate();
    }
}
