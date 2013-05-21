package lan.vandiemens.test;

import java.util.regex.Matcher;
import lan.vandiemens.media.cataloguer.ReleaseInfoParser;

/**
 *
 * @author vmurcia
 */
public class RegexTest {

    public static void main(String[] args) {
        String[] filenames = {"Six.Feet.Under.S04E03.Parallel.Play.720p.WEB-DL.DD5.1.AAC2.0.AVC-iON",
            "Six.Feet.Under.S03E10.DVDRip.XviD-FFNDVD"};

        Matcher tvSeriesMatcher;
        for (String filename : filenames) {
            tvSeriesMatcher = ReleaseInfoParser.sceneTvSeriesPattern.matcher(filename);
            if (tvSeriesMatcher.matches()) {
                System.out.println(filename + " is a TV Series episode...");
                System.out.println("Title: " + tvSeriesMatcher.group("title").replace('.', ' '));
                System.out.println("Season: " + Integer.parseInt(tvSeriesMatcher.group("season")));
                System.out.println("Episode: " + Integer.parseInt(tvSeriesMatcher.group("episode")));
                String episodeTitle = tvSeriesMatcher.group("episodetitle");
                System.out.println("Episode title: " + (episodeTitle == null ? "<Unknown>" : episodeTitle.replace('.', ' ')));
                System.out.println("Quality: " + tvSeriesMatcher.group("quality"));
                String source = tvSeriesMatcher.group("source");
                if (source != null) {
                    System.out.println("Source: " + source);
                }
                System.out.println("Codecs: " + tvSeriesMatcher.group("codecs"));
                System.out.println("Ripper: " + tvSeriesMatcher.group("ripper"));
                System.out.println("");
            }
        }
    }
}
