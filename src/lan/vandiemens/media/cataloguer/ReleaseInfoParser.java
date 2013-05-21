package lan.vandiemens.media.cataloguer;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lan.vandiemens.media.info.VideoSource;
import lan.vandiemens.media.info.release.ReleaseInfo;
import lan.vandiemens.media.info.release.TvEpisodeReleaseInfo;
import lan.vandiemens.util.file.FileUtils;

/**
 * @author Victor
 */
public class ReleaseInfoParser {

    public static final String[] knownUploaders = {"rockobossman", "Nestai", "Straw",
        "funebrero", "Tallahassee"};
    public static final Pattern tvSeriesPattern = Pattern.compile("(?:[sS](\\d+)[eE](\\d+))|(?:(\\d+)x(\\d+))");
    public static final Pattern sceneTvSeriesPattern = Pattern.compile("(?i)^(?<title>(?:[^\\.]+\\.)*(?:[^\\.]+))\\.s(?<season>\\d\\d)e(?<episode>\\d\\d)\\.(?:(?<episodetitle>.+?)\\.)??(?:(?<quality>720p?|1080p?)\\.)?(?:(?<source>bluray|b[dr]rip|bdremux|hddvd|web-dl|hditunes|hdtv|dvdr(?:ip)?)\\.)?(?<codecs>(?:(?:mkv|avc|h\\.?264|x264|xvid|divx|dxva|dts|dd5\\.1|ac3|aac|aac2\\.0)\\.)*(?:mkv|avc|h\\.?264|x264|xvid|divx|dxva|dts|dd5\\.1|ac3|aac|aac2\\.0))-(?<scenegroup>\\w+)$");
    public static final Pattern sceneMoviePattern = Pattern.compile("(?i)^(?<title>.+?)\\.(?:(?<year>(?:19|20)\\d\\d)\\.)?(?:(?<extratag>limited|remastered|proper)\\.)?(?:(?<source>bluray|b[dr]rip|bdremux|hddvd|web-dl|hditunes|hdtv(?:rip)?|dvdr(?:ip)?)\\.)?(?<quality>720p?|1080p?)\\.(?:(?<source2>bluray|b[dr]rip|bdremux|hddvd|web-dl|hditunes|hdtv(?:rip)?|dvdr(?:ip)?)\\.)?(?<codecs>(?:(?:mkv|avc|h\\.?264|x264|xvid|divx|dxva|dts|dd5\\.1|ac3|aac|aac2\\.0)\\.)*(?:mkv|avc|h\\.?264|x264|xvid|divx|dxva|dts|dd5\\.1|ac3|aac|aac2\\.0))(?:-(?<scenegroup>\\w+)|\\.multisubs)$");


    public static ReleaseInfo parse(File file) {
        System.out.println("Parsing release information...");
        if (isTvSeries(file)) {
            return parseAsTvSeries(file);
        } else {
            return parseAsMovie(file);
        }
    }

    public static ReleaseInfo parse(String filename) {
        System.out.println("Parsing release information...");
        if (isTvSeries(filename)) {
            return parseAsTvSeries(filename);
        } else {
            return parseAsMovie(filename);
        }
    }

    /**
     * Checks if the filename consists of a typical scene TV series pattern.
     * @param file the file whose name is to be checked
     * @return <code>true</code> if the filename is that of a TV series as used
     *         typically in the scene, <code>false</code> otherwise.
     */
    public static boolean isTvSeries(File file) {
        return isTvSeries(file.getName());
    }

    public static boolean isTvSeries(String filename) {
        // Divide the information contained in the filename itself
        String fields[] = getInfoFields(filename);

        // Check if the file name contains a TV series pattern
        Matcher tvSeriesMatcher;
        for (int i = 0; i < fields.length; i++) {
            tvSeriesMatcher = tvSeriesPattern.matcher(fields[i]);
            if (tvSeriesMatcher.matches()) {
                return true;
            }
        }

        return false;
    }

    private static ReleaseInfo parseAsTvSeries(File file) {
        String filename = FileUtils.getNameWithoutExtension(file);
        return parseAsTvSeries(filename);
    }

    private static ReleaseInfo parseAsTvSeries(String filename) {
        System.out.println("Parsing \"" + filename + "\" as TV series...");

        // Check if the file name consists of a typical TV series scene pattern
        TvEpisodeReleaseInfo info;
        Matcher tvSeriesMatcher = sceneTvSeriesPattern.matcher(filename);
        if (tvSeriesMatcher.matches()) {
            // Get info fields
            info = new TvEpisodeReleaseInfo();
            info.setTitle(fixTitleSpecialCharacters(tvSeriesMatcher.group("title")));
            info.setSeason(Integer.parseInt(tvSeriesMatcher.group("season")));
            info.setEpisodeNumber(Integer.parseInt(tvSeriesMatcher.group("episode")));
            String episodeTitle = tvSeriesMatcher.group("episodetitle");
            if (episodeTitle != null) {
                info.setEpisodeTitle(fixTitleSpecialCharacters(episodeTitle));
            } else {
                System.out.println("Episode title field not present in the file name");
            }
            info.setVideoQuality(tvSeriesMatcher.group("quality"));
            String source = tvSeriesMatcher.group("source");
            if (source != null) {
                info.setVideoSource(getCommonEquivalentSourceName(source));
            }
            info.setCodecDescription(tvSeriesMatcher.group("codecs"));
            info.setSceneGroup(tvSeriesMatcher.group("scenegroup"));
        } else {
            // TODO Add additional filename parsing patterns
            return null;
        }

        return info;
    }

    private static ReleaseInfo parseAsMovie(File file) {
        String filename = FileUtils.getNameWithoutExtension(file);
        return parseAsMovie(filename);
    }

    private static ReleaseInfo parseAsMovie(String filename) {
        System.out.println("Parsing \"" + filename + "\" as movie...");

        // Check if the file name consists of a typical movie scene pattern
        ReleaseInfo info;
        Matcher movieMatcher = sceneMoviePattern.matcher(filename);
        if (movieMatcher.matches()) {
            info = new ReleaseInfo();
            info.setTitle(fixTitleSpecialCharacters(movieMatcher.group("title")));
            String year = movieMatcher.group("year");
            if (year != null) {
                info.setYear(Integer.parseInt(year));
            } else {
                System.out.println("No year info");
            }
            info.setVideoQuality(movieMatcher.group("quality"));
            String source = movieMatcher.group("source");
            if (source != null) {
                info.setVideoSource(getCommonEquivalentSourceName(source));
            } else {
                source = movieMatcher.group("source2");
                if (source != null) {
                    info.setVideoSource(source);
                }
            }
            info.setCodecDescription(movieMatcher.group("codecs"));
            info.setSceneGroup(movieMatcher.group("scenegroup"));
        } else {
            // TODO Add additional filename parsing patterns
            return null;
        }

        return info;
    }

    private static String fixTitleSpecialCharacters(String oldTitle) {
        return oldTitle.replace('.', ' ').replace(";c", ":").replace(";d", ".").replace(";a", "*").replace(";q", "?").replace(";l", "<").replace(";g", ">");
    }

    private static String getCommonEquivalentSourceName(String name) {
        VideoSource source = VideoSource.parse(name);
        return source == VideoSource.UNKNOWN ? name : source.toString();
    }

    private static String[] getInfoFields(String filename) {
        // Check whether info fields are separated by dots
        String[] fields = filename.split("\\.");
        if (fields.length > 3) {
            System.out.println("It seems to be a dot-separated filename");
        } else {
            System.out.println("It seems that the filename is not separated by dots");
            fields = filename.split(" ");
        }

        return fields;
    }
}
