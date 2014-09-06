package lan.vandiemens.media.info.release;

import lan.vandiemens.util.lang.EnhancedWordUtils;

/**
 *
 * @author vmurcia
 */
public class MovieReleaseInfo extends ReleaseInfo {

    ReleaseType type = ReleaseType.COMMON;

    public MovieReleaseInfo(String title) {
        super(title);
        genre = MotionPictureGenre.MOVIE;
    }

    public MovieReleaseInfo(String title, int year) {
        this(title);
        this.year = year;
    }

    public boolean hasKnownReleaseDate() {
        return year > 0;
    }

    public ReleaseType getType() {
        return type;
    }

    public void setType(ReleaseType type) {
        this.type = type;
    }

    @Override
    public boolean hasCompleteBasicInfo() {
        return hasKnownReleaseDate();
    }

    @Override
    public String getFormattedTitle() {
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(isTitleCapitalized() ? title : EnhancedWordUtils.capitalizeAsHeadline(title));
        if (type.isSpecialVersion()) {
            titleBuilder.append(" [").append(type).append("]");
        }
        if (year > 0) {
            titleBuilder.append(" (").append(year).append(")");
        }

        return titleBuilder.toString();
    }
}
