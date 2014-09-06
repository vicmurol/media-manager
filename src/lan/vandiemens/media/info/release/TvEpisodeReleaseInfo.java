package lan.vandiemens.media.info.release;

import lan.vandiemens.util.lang.EnhancedWordUtils;

/**
 *
 * @author vmurcia
 */
public class TvEpisodeReleaseInfo extends ReleaseInfo {

    private String episodeTitle = null;
    private int season = -1;
    private int episodeNumber = -1;


    public TvEpisodeReleaseInfo(String showTitle, int season, int episode) {
        super(showTitle);
        this.season = season;
        episodeNumber = episode;
        genre = MotionPictureGenre.TV_SERIES;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public boolean hasEpisodeTitle() {
        return episodeTitle != null;
    }

    public void setEpisodeTitle(String title) {
        episodeTitle = title;
    }

    public int getSeason() {
        return season;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    @Override
    public String getFormattedTitle() {
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(isTitleCapitalized() ? title : EnhancedWordUtils.capitalizeAsHeadline(title));
        titleBuilder.append(" - [");
        titleBuilder.append(season);
        int episode = episodeNumber;
        titleBuilder.append(episode > 9 ? "x" : "x0");
        titleBuilder.append(episode);
        titleBuilder.append("]");
        if (hasEpisodeTitle()) {
            titleBuilder.append(" ").append(isTitleCapitalized() ?
                    episodeTitle : EnhancedWordUtils.capitalizeAsHeadline(episodeTitle));
        }

        return titleBuilder.toString();
    }

    @Override
    public boolean equalsBasicInfo(ReleaseInfo info2) {
        return (equalsBasicInfoIgnoreSceneGroup(info2)
                && info2.getReleaseGroup().equalsIgnoreCase(sceneGroup));
    }

    @Override
    public boolean equalsBasicInfoIgnoreSceneGroup(ReleaseInfo info2) {
        if (info2 == null) {
            return false;
        }
        if (!(info2 instanceof TvEpisodeReleaseInfo)) {
            return false;
        }
        TvEpisodeReleaseInfo tvSeriesInfo2 = (TvEpisodeReleaseInfo) info2;
        return (tvSeriesInfo2.getTitle().equalsIgnoreCase(title)
                && tvSeriesInfo2.getSeason() == season
                && tvSeriesInfo2.getEpisodeNumber() == episodeNumber);
    }

    @Override
    public boolean hasCompleteBasicInfo() {
        return hasEpisodeTitle();
    }
}
