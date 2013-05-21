package lan.vandiemens.media.info.release;

import org.apache.commons.lang3.text.WordUtils;

/**
 *
 * @author vmurcia
 */
public class TvEpisodeReleaseInfo extends ReleaseInfo {

    private String episodeTitle = null;
    private int season = -1;
    private int episodeNumber = -1;


    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String name) {
        episodeTitle = name;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episode) {
        this.episodeNumber = episode;
    }

    @Override
    public String getFormattedTitle() {
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(isTitleCapitalized() ? title : WordUtils.capitalize(title));
        titleBuilder.append(" - [");
        titleBuilder.append(season);
        int episode = episodeNumber;
        titleBuilder.append(episode > 9 ? "x" : "x0");
        titleBuilder.append(episode);
        titleBuilder.append("] ");
        titleBuilder.append(isTitleCapitalized() ? episodeTitle : WordUtils.capitalize(episodeTitle)); // TODO: Check if episode title is known
        return titleBuilder.toString();
    }

    @Override
    public boolean equalsBasicInfo(ReleaseInfo info2) {
        return (equalsBasicInfoIgnoreSceneGroup(info2)
                && info2.getSceneGroup().equalsIgnoreCase(sceneGroup));
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
}
