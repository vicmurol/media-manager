package lan.vandiemens.media.info.release;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lan.vandiemens.media.analysis.VideoSource;
import lan.vandiemens.media.net.Addic7edWebScraper;
import lan.vandiemens.media.net.MediaInfoProviderException;
import lan.vandiemens.util.file.FileUtils;

/**
 * Simple factory of ReleaseInfo objects. The created objects exact class is
 * based on the media filename given.
 *
 * @author vmurcia
 */
public class ReleaseInfoParser {

    public static final String[] knownUploaders = {"rockobossman", "Nestai", "Straw", "funebrero", "Tallahassee"};
    public static final Pattern basicTvSeriesPattern = Pattern.compile("(?:[sS](?<season>\\d+)[eE](?<episode>\\d+))|(?:(?<season2>\\d+)x(?<episode2>\\d+))");
    public static final Pattern sceneTvSeriesPattern = Pattern.compile("(?i)^(?<title>(?:[^\\.]+\\.)*(?:[^\\.]+))\\.s(?<season>\\d\\d)e(?<episode>\\d\\d)\\.(?:(?<episodetitle>.+?)\\.)??(?:(?<quality>720p?|1080p?)\\.)?(?:(?<source>bluray|b[dr]rip|bdremux|hddvd|web-dl|hditunes|hdtv|dvdr(?:ip)?)\\.)?(?<codecs>(?:(?:mkv|avc|h\\.?264|x264|xvid|divx|dxva|dts|dts-hd\\.ma\\.5\\.1|dd5\\.1|ac3|aac|aac2\\.0)\\.)*(?:mkv|avc|h\\.?264|x264|xvid|divx|dxva|dts|dts-hd\\.ma\\.5\\.1|dd5\\.1|ac3|aac|aac2\\.0))-(?<scenegroup>\\w+)$");
    public static final Pattern sceneMoviePattern = Pattern.compile("(?i)^(?<title>.+?)\\.(?:(?<year>(?:19|20)\\d\\d)\\.)?(?:(?<extratag>cee|collectors(?:\\.edition)?|dc|ext(?:ended)?(?:\\.cut)?|internal|limited|mgvc|p2p|proper|remastered|repack|retail|revisited|unrated(?:\\.cut)?)\\.)*(?:(?<source>(?:(?:aus|fra|gbr|usa)\\.)?blu-?ray(?:\\.remux)?|(?:(?:aus|fra|gbr|usa)\\.)?hybrid|b[dr]rip|bdremux|hddvd|web-dl|hditunes|hdtv(?:rip)?|dvdr(?:ip)?)\\.)?(?<quality>720p?|1080p?)\\.(?:(?<source2>(?:(?:aus|gbr|usa)\\.)?blu-?ray(?:\\.remux)?|b[dr]rip|bdremux|hddvd|web-dl|hditunes|hdtv(?:rip)?|dvdr(?:ip)?)\\.)?(?<codecs>(?:(?:dual|remux|revisited|mkv|avc|vc-?1|h\\.?264|x264|xvid|divx|dxva|dts(?:hd-|-)?ma|dts(?:-es|-hd\\.?(?:ma|hra)?)?|[57]\\.1|dd\\.?5\\.1|flac|ac3|aac|aac2\\.0|truehd)\\.)*(?:dual|remux|revisited|mkv|avc|vc-?1|h\\.?264|x264|xvid|divx|dxva|dts(?:hd-|-)?ma|dts(?:-es|-hd\\.?(?:ma|hra)?)?|[57]\\.1|dd\\.?5\\.1|flac|ac3|aac|aac2\\.0|truehd))(?:-(?<scenegroup>.+)|\\.(?<scenegroup2>D-Z0N3|FTW-HD)|\\.multisubs)$");
    private static final String COLON_SUBSTITUTE = ";c";
    private static final String ASTERISK_SUBSTITUTE = ";a";
    private static final String QUESTION_MARK_SUBSTITUTE = ";q";
    private static final String DOT_SUBSTITUTE = ";d";
    private static final String LESS_THAN_SUBSTITUTE = ";l";
    private static final String GREATER_THAN_SUBSTITUTE = ";g";
    private static final char SPACE_SUBSTITUTE = '.';


    /**
     * Parses the release information specified in the given filename.
     * @param filename the filename to be parsed
     * @return the release information which has been parsed and organized
     */
    public static ReleaseInfo parse(String filename) {
        System.out.println("Parsing release information...");
        if (hasTvSeriesNamePattern(filename)) {
            return parseAsTvSeries(filename);
        } else {
            return parseAsMovie(filename);
        }
    }

    /**
     * Parses the release information specified in the given filename.
     * @param file the file whose name is to be parsed
     * @return the release information which has been parsed and organized
     */
    public static ReleaseInfo parse(File file) {
        String filename = FileUtils.getNameWithoutExtension(file);
        return parse(filename);
    }

    /**
     * Checks if the filename consists of a typical scene TV series pattern.
     * @param file the file whose name is to be checked
     * @return <code>true</code> if the filename is that of a TV series as used
     *         typically in the scene, <code>false</code> otherwise.
     */
    public static boolean hasTvSeriesNamePattern(File file) {
        return hasTvSeriesNamePattern(file.getName());
    }

    /**
     * Checks if the given file name corresponds to a TV series episode.
     * @param filename the file name to be parsed
     * @return <code>true</code> if the filename is that of a TV series as used
     *         typically in the scene, <code>false</code> otherwise.
     */
    public static boolean hasTvSeriesNamePattern(String filename) {
        String fields[] = getInfoFields(filename);
        Matcher tvSeriesMatcher;
        for (String field : fields) {
            tvSeriesMatcher = basicTvSeriesPattern.matcher(field);
            if (tvSeriesMatcher.matches()) {
                return true;
            }
        }
        return false;
    }

    private static ReleaseInfo parseAsTvSeries(String filename) {
        System.out.println("Parsing \"" + filename + "\" as TV series...");

        // Check if the file name consists of a typical TV series scene pattern
        TvEpisodeReleaseInfo info;
        Matcher tvSeriesMatcher = sceneTvSeriesPattern.matcher(filename);
        if (tvSeriesMatcher.matches()) {
            System.out.println("Parsing using the typical scene naming pattern...");
            String title = replaceSubstitutesForSpecialTitleCharacters(tvSeriesMatcher.group("title"));
            int season = Integer.parseInt(tvSeriesMatcher.group("season"));
            int episode = Integer.parseInt(tvSeriesMatcher.group("episode"));
            info = new TvEpisodeReleaseInfo(title, season, episode);
            String episodeTitle = tvSeriesMatcher.group("episodetitle");
            if (episodeTitle != null) {
                info.setEpisodeTitle(replaceSubstitutesForSpecialTitleCharacters(episodeTitle));
            } else {
                System.out.println("Episode title field not present in the file name");
                System.out.println("Let's try to get this info from Addic7ed...");
                try {
                    episodeTitle = Addic7edWebScraper.getEpisodeTitle(title, season, episode);
                    info.setEpisodeTitle(episodeTitle);
                } catch (MediaInfoProviderException ex) {
                    System.out.println(ex.getMessage());
                }
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
            System.out.println("Parsing using the generic naming pattern...");
            info = parseBasicTvEpisodeInfo(filename);
        }
        System.out.println("Release information has been generated\n");
        return info;
    }

    private static TvEpisodeReleaseInfo parseBasicTvEpisodeInfo(String filename) {
        Matcher tvSeriesMatcher = basicTvSeriesPattern.matcher(filename);
        tvSeriesMatcher.find();
        String title = filename.substring(0, tvSeriesMatcher.start()); // This is guessing, so basic parsing here
        int season = Integer.parseInt(tvSeriesMatcher.group("season") == null ? tvSeriesMatcher.group("season2") : tvSeriesMatcher.group("season"));
        int episode = Integer.parseInt(tvSeriesMatcher.group("episode") == null ? tvSeriesMatcher.group("episode2") : tvSeriesMatcher.group("episode"));
        return new TvEpisodeReleaseInfo(title, season, episode);
    }

    private static ReleaseInfo parseAsMovie(String filename) {
        System.out.println("Parsing \"" + filename + "\" as movie...");

        // Check if the file name consists of a typical movie scene pattern
        MovieReleaseInfo info;
        Matcher movieMatcher = sceneMoviePattern.matcher(filename);
        if (movieMatcher.matches()) {
            System.out.println("Parsing using the typical scene naming pattern...");
            String title = replaceSubstitutesForSpecialTitleCharacters(movieMatcher.group("title"));
            info = new MovieReleaseInfo(title);
            String year = movieMatcher.group("year");
            if (year != null) {
                info.setYear(Integer.parseInt(year));
            } else {
                System.out.println("No year info");
            }
            String type = movieMatcher.group("extratag");
            if (type != null) {
                info.setType(ReleaseType.parse(type));
            }

            info.setVideoQuality(movieMatcher.group("quality"));
            String source = movieMatcher.group("source");
            if (source != null) {
                info.setVideoSource(getCommonEquivalentSourceName(source));
            } else {
                source = movieMatcher.group("source2");
                if (source != null) {
                    info.setVideoSource(getCommonEquivalentSourceName(source));
                }
            }
            info.setCodecDescription(movieMatcher.group("codecs"));

            String sceneGroup = movieMatcher.group("scenegroup");
            if (sceneGroup != null) {
                info.setSceneGroup(sceneGroup);
            } else {
                sceneGroup = movieMatcher.group("scenegroup2");
                if (sceneGroup != null) {
                    info.setSceneGroup(sceneGroup);
                }
            }
        } else {
            // TODO Add additional filename parsing patterns
            System.out.println("Parsing using the generic naming pattern...");
            info = new MovieReleaseInfo(filename);
        }
        System.out.println("Release information has been generated\n");
        return info;
    }

    private static String replaceSubstitutesForSpecialTitleCharacters(String title) {
        String fixedTitle = title;
        fixedTitle = fixedTitle.replace(SPACE_SUBSTITUTE, ' ');
        fixedTitle = fixedTitle.replace(COLON_SUBSTITUTE, ":");
        fixedTitle = fixedTitle.replace(DOT_SUBSTITUTE, ".");
        fixedTitle = fixedTitle.replace(ASTERISK_SUBSTITUTE, "*");
        fixedTitle = fixedTitle.replace(QUESTION_MARK_SUBSTITUTE, "?");
        fixedTitle = fixedTitle.replace(LESS_THAN_SUBSTITUTE, "<");
        fixedTitle = fixedTitle.replace(GREATER_THAN_SUBSTITUTE, ">");
        return fixedTitle;
    }

    private static String getCommonEquivalentSourceName(String name) {
        // TODO: Detect Blu-ray disc country in a cleaner way. Also, add better detection for hybrids
        if (name.toLowerCase().endsWith("blu-ray") || name.toLowerCase().endsWith("bluray") || name.toLowerCase().endsWith("hybrid")) {
            name = "Blu-ray";
        }
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
