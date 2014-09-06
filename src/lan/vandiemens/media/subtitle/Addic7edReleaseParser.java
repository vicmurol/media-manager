package lan.vandiemens.media.subtitle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lan.vandiemens.media.info.release.ReleaseInfo;

/**
 *
 * @author vmurcia
 */
public class Addic7edReleaseParser {

    public static final Pattern ADDIC7ED_SUBTITLE_PATTERN = Pattern.compile("(?i)^(?<title>.+) - (?<season>\\d\\d)x(?<episode>\\d\\d) - (?<episodetitle>[^\\.]+)\\.(?<version>.+)\\.(?<language>English|Spanish)\\.(?:(?<FHI>HI)\\.)?(?:(?<correction>C)\\.)?(?<update>orig|updated)\\.Addic7ed\\.com$");

    public static boolean canParse(String filename) {
        Matcher matcher = ADDIC7ED_SUBTITLE_PATTERN.matcher(filename);
        return matcher.matches();
    }

    public static ReleaseInfo parse(String filename) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
