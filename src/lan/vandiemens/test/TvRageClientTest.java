package lan.vandiemens.test;

import lan.vandiemens.media.net.MediaInfoProviderException;
import lan.vandiemens.media.net.TvRageClient;

/**
 *
 * @author vmurcia
 */
public class TvRageClientTest {

    public static void main(String[] args) {
        TvRageClient client = new TvRageClient();
        String title;
        try {
            title = client.getEpisodeTitle("Breaking Bad", 1, 2);
            System.out.println("Title: " + title);
            title = client.getEpisodeTitle("Breaking Bad", 2, 8);
            System.out.println("Title: " + title);
        } catch (MediaInfoProviderException ex) {
            System.err.println("Error when retrieving episode title: " + ex.getMessage());
        }
    }
}
