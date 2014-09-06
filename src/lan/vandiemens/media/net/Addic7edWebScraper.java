package lan.vandiemens.media.net;

import java.io.IOException;
import java.net.SocketTimeoutException;
import lan.vandiemens.util.net.WebRequest;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author vmurcia
 */
public class Addic7edWebScraper {

    public static final String ADDIC7ED_BASE_URLNAME = "http://www.addic7ed.com/";
    public static final String ADDIC7ED_TV_SHOW_BASE_URLNAME = "http://www.addic7ed.com/show/";
    public static final String ADDIC7ED_TV_SHOW_LIST_URLNAME = "http://www.addic7ed.com/shows.php";
    public static final String ADDIC7ED_SEARCH_BASE_URLNAME = "http://www.addic7ed.com/search.php";
    public static final String FAKE_USER_AGENT = "FDM 3.x";
    public static final String HREF_ATTRIBUTE = "href";
    public static final String TABLE_DATA_CELL = "td";
    public static final String MOST_LIKELY_SHOW_CSS_QUERY = ".titulo > a";
    public static final String SEASON_SUBTITLES_QUERY_TEMPLATE = "ajax_loadShow.php?show=%1$d&season=%2$d&langs=%3$s";
    public static final int ENGLISH_LANGUAGE_VALUE = 1;
    public static final int SPANISH_LANGUAGE_VALUE = 5;
    public static final int SEASON_NUMBER_COLUMN_INDEX = 0;
    public static final int EPISODE_NUMBER_COLUMN_INDEX = 1;
    public static final int EPISODE_TITLE_COLUMN_INDEX = 2;
    public static final int LANGUAGE_COLUMN_INDEX = 3;
    public static final int VERSION_COLUMN_INDEX = 4;
    public static final int COMPLETION_STATE_COLUMN_INDEX = 5;
    public static final int HEARING_IMPAIRED_FLAG_COLUMN_INDEX = 6;
    public static final int CORRECTION_FLAG_COLUMN_INDEX = 7;
    public static final int HD_FLAG_COLUMN_INDEX = 8;
    public static final int DOWNLOAD_LINK_COLUMN_INDEX = 9;
    public static final String SESSION_ID_COOKIE = "PHPSESSID";

    public static int getTvShowId(String tvShow) throws MediaInfoProviderException, IOException {
        String encodedTvShowTitle = WebRequest.encodeForUrl(tvShow);
        String searchQuery = "?search=" + encodedTvShowTitle + "&Submit=Search";
        Document doc = connect(ADDIC7ED_SEARCH_BASE_URLNAME + searchQuery).get();
        // TODO: Check web document structure
        Element link = doc.select(MOST_LIKELY_SHOW_CSS_QUERY).first();
        String tvShowUrl = link.attr(HREF_ATTRIBUTE);
        int tvShowId = 0;
        if (tvShowUrl.startsWith("/show/")) {
            tvShowId = Integer.parseInt(tvShowUrl.substring(6));
        } else {
            throw new MediaInfoProviderException("Addic7ed has changed the HTML structure used in its search results webpage");
        }
        System.out.println("TV show absolute URL: " + link.absUrl(HREF_ATTRIBUTE));
        return tvShowId;
    }

    private static Connection connect(String urlName) {
        return Jsoup.connect(urlName).userAgent(FAKE_USER_AGENT);
    }

    public static String getEpisodeTitle(String tvShow, int season, int episode) throws MediaInfoProviderException {
        String result = null;
        try {
            String seasonSubtitlesWebLink = getTvShowSeasonWebLink(tvShow, season);
            Document doc = connect(seasonSubtitlesWebLink).get();
            Elements episodeReleaseLinks = doc.select(".epeven.completed");
            Elements episodeDataCells;
            Element link;

            for (int i = 0; i < episodeReleaseLinks.size(); i++) {
                link = episodeReleaseLinks.get(i);
                episodeDataCells = link.select(TABLE_DATA_CELL);
                int cellEpisode = Integer.parseInt(episodeDataCells.get(EPISODE_NUMBER_COLUMN_INDEX).text());
                if (cellEpisode == episode) {
                    result = episodeDataCells.get(EPISODE_TITLE_COLUMN_INDEX).text();
                    System.out.println("Episode title: " + result);
                    break;
                }
            }
        } catch (SocketTimeoutException ex) {
            throw new MediaInfoProviderException("Connection timeout to Addic7ed website");
        } catch (NumberFormatException ex) {
            throw new MediaInfoProviderException("Addic7ed has changed the HTML structure used in its subtitle releases table");
        } catch (IOException ex) {
            throw new MediaInfoProviderException("I/O error: " + ex.getMessage());
        } catch (Exception ex) {
            throw new MediaInfoProviderException("Unknown error: " + ex.getMessage());
        }

        if (result == null) {
            throw new MediaInfoProviderException("Episode title information not found");
        }

        return result;
    }

    private static String getTvShowSeasonWebLink(String tvShow, int season) throws MediaInfoProviderException, IOException {
        String languageSelector = "|" + ENGLISH_LANGUAGE_VALUE + "|";
        String seasonSubsQuery = String.format(SEASON_SUBTITLES_QUERY_TEMPLATE, getTvShowId(tvShow), season, languageSelector);
        return ADDIC7ED_BASE_URLNAME + seasonSubsQuery;
    }
}
