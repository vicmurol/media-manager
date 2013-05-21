package lan.vandiemens.media.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import lan.vandiemens.util.net.WebRequest;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;

/**
 *
 * @author vmurcia
 */
public class TvRageClient {
    private final String TV_RAGE_HOST = "services.tvrage.com";
    private final String HTTP_PROTOCOL = "http";
    private final String FULL_SEARCH_QUERY = "/feeds/full_search.php?show=";
    private final String EPISODE_INFO_QUERY = "/feeds/episodeinfo.php?sid=";


    public String getEpisodeTitle(String showName, int season, int episode) throws MediaInfoProviderException {
        int showId = getTvSeriesId(showName);
        System.out.println(showName + " has the following show ID: " + showId);
        URL url = getEpisodeInfoUrl(showId, season, episode);
        Document xmlDocument = getXmlDocument(url);
        return parseEpisodeTitle(xmlDocument);
    }

    private int getTvSeriesId(String name) throws MediaInfoProviderException {
        URL searchUrl = getSearchUrl(name);
        Document xmlDocument = getXmlDocument(searchUrl);
        return parseShowId(xmlDocument);
    }

    private URL getSearchUrl(String name) {
        URL url = null;
        try {
            url = new URL(HTTP_PROTOCOL, TV_RAGE_HOST, FULL_SEARCH_QUERY + WebRequest.encodeUrl(name));
        } catch (MalformedURLException ex) {
            Logger.getLogger(TvRageClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return url;
    }

    private int parseShowId(Document xmlDocument) throws MediaInfoProviderException {
        Element resultsElement = xmlDocument.getRootElement();
        Element showElement = resultsElement.getFirstChildElement("show");
        if (showElement == null) {
            throw new MediaInfoProviderException("Invalid TV Rage full search XML document: No <show> element found");
        }
        Element showIdElement = showElement.getFirstChildElement("showid");
        if (showIdElement == null) {
            throw new MediaInfoProviderException("Invalid TV Rage full search XML document: No <showid> element found");
        }
        int showId = 0;
        try {
            showId = Integer.parseInt(showIdElement.getValue());
        } catch(NumberFormatException ex) {
            throw new MediaInfoProviderException("Invalid TV Rage full search XML document: wrong <showid> value: " + showIdElement.getValue());
        }
        return showId;
    }

    private Document getXmlDocument(URL url) throws MediaInfoProviderException {
        try {
            Builder parser = new Builder();
            Document xmlDocument = parser.build(url.toString());
            return xmlDocument;
        } catch (ParsingException | IOException ex) {
            throw new MediaInfoProviderException("Error when retrieving XML document from TV Rage server");
        }
    }

    private URL getEpisodeInfoUrl(int showId, int season, int episode) {
        URL url = null;
        try {
            url = new URL(HTTP_PROTOCOL, TV_RAGE_HOST, EPISODE_INFO_QUERY + showId + "&ep=" + season + "x" + episode);
        } catch (MalformedURLException ex) {
            Logger.getLogger(TvRageClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return url;
    }

    private String parseEpisodeTitle(Document xmlDocument) throws MediaInfoProviderException {
        Element showElement = xmlDocument.getRootElement();
        Element episodeElement = showElement.getFirstChildElement("episode");
        if (episodeElement == null) {
            throw new MediaInfoProviderException("Invalid episode XML document: No <episode> element found");
        }
        Element titleElement = episodeElement.getFirstChildElement("title");
        if (titleElement == null) {
            throw new MediaInfoProviderException("Invalid episode XML document: No <title> element found");
        }
        return titleElement.getValue();
    }
}
