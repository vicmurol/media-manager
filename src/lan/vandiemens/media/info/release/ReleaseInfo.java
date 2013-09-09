package lan.vandiemens.media.info.release;

import org.apache.commons.lang3.text.WordUtils;

/**
 * The release information of a media container as parsed from a media filename.
 *
 * @author vmurcia
 */
public class ReleaseInfo {

    protected ReleaseGenre genre = ReleaseGenre.GENERIC_VIDEO;
    protected String title = null;
    protected String originalTitle = null;
    protected String ripper = null;
    protected String sceneGroup = null;
    protected String uploader = null;
    protected String videoSource = null;
    protected String videoQuality = null;
    protected String codecDescription = null;
    protected String webSource = null;
    protected int year = -1;

    public ReleaseInfo(String title) {
        this.title = title;
    }

    public ReleaseGenre getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getVideoSource() {
        return videoSource;
    }

    public void setVideoSource(String source) {
        videoSource = source;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getWebSource() {
        return webSource;
    }

    public void setWebSource(String source) {
        webSource = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        title = name;
    }

    public String getFormattedTitle() {
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(isTitleCapitalized() ? title : WordUtils.capitalize(title));
        if (year > 0) {
            titleBuilder.append(" (").append(year).append(")");
        }

        return titleBuilder.toString();
    }

    public boolean isTitleCapitalized() {
        return Character.isUpperCase(title.toCharArray()[0]);
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String name) {
        originalTitle = name;
    }

    public String getVideoQuality() {
        return videoQuality;
    }

    public boolean isVideoQualityKnown() {
        return videoQuality != null;
    }

    public void setVideoQuality(String quality) {
        videoQuality = quality;
    }

    public String getRipper() {
        return ripper;
    }

    public void setRipper(String ripper) {
        this.ripper = ripper;
    }

    public String getCodecDescription() {
        return codecDescription;
    }

    public void setCodecDescription(String desc) {
        codecDescription = desc;
    }

    public String getSceneGroup() {
        return sceneGroup;
    }

    public void setSceneGroup(String sceneGroup) {
        this.sceneGroup = sceneGroup;
    }

    public boolean equalsBasicInfo(ReleaseInfo info2) {
        return (equalsBasicInfoIgnoreSceneGroup(info2)
                && info2.getSceneGroup().equalsIgnoreCase(sceneGroup));
    }

    public boolean equalsBasicInfoIgnoreSceneGroup(ReleaseInfo info2) {
        if (info2 == null) {
            return false;
        }
        return (info2.getTitle().equalsIgnoreCase(title) && info2.getYear() == year);
    }

    public boolean hasCompleteBasicInfo() {
        return true; // Title suffices
    }
}
