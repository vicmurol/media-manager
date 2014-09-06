package lan.vandiemens.media.info.release;

import lan.vandiemens.util.lang.EnhancedWordUtils;

/**
 * The release information of a media container.
 *
 * @author vmurcia
 */
public class ReleaseInfo {

    private static final int YEAR_OF_EARLIEST_SURVIVING_MOTION_PICTURE = 1888;

    protected MotionPictureGenre genre = MotionPictureGenre.GENERIC_VIDEO;
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
        if (title == null) {
        throw new IllegalArgumentException("A valid title must be provided");
        }
        this.title = title;
    }

    public MotionPictureGenre getGenre() {
        return genre;
    }

    public boolean hasYearInfo() {
        return year > 0;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        if (year < YEAR_OF_EARLIEST_SURVIVING_MOTION_PICTURE) {
            throw new IllegalArgumentException("Invalid year, as earliest surviving motion picture dates from "
                    + YEAR_OF_EARLIEST_SURVIVING_MOTION_PICTURE);
        }
        this.year = year;
    }

    public boolean hasVideoSourceInfo() {
        return videoSource != null;
    }

    public String getVideoSource() {
        return videoSource;
    }

    public void setVideoSource(String source) {
        videoSource = source;
    }

    public boolean hasUploaderInfo() {
        return uploader != null;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public boolean hasWebSourceInfo() {
        return webSource != null;
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
        return isTitleCapitalized() ? title : EnhancedWordUtils.capitalizeAsHeadline(title);
    }

    public boolean isTitleCapitalized() {
        return Character.isUpperCase(title.toCharArray()[0]);
    }

    public boolean hasOriginalTitleInfo() {
        return originalTitle != null;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String name) {
        originalTitle = name;
    }

    public boolean hasVideoQualityInfo() {
        return videoQuality != null;
    }

    public String getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(String quality) {
        videoQuality = quality;
    }

    public boolean hasRipperInfo() {
        return ripper != null;
    }

    public String getRipper() {
        return ripper;
    }

    public void setRipper(String ripper) {
        this.ripper = ripper;
    }

    public boolean hasCodecDescription() {
        return codecDescription != null;
    }

    public String getCodecDescription() {
        return codecDescription;
    }

    public void setCodecDescription(String desc) {
        codecDescription = desc;
    }

    public String getReleaseGroup() {
        return sceneGroup;
    }

    public void setSceneGroup(String sceneGroup) {
        this.sceneGroup = sceneGroup;
    }

    public boolean equalsBasicInfo(ReleaseInfo info2) {
        return (equalsBasicInfoIgnoreSceneGroup(info2)
                && info2.getReleaseGroup().equalsIgnoreCase(sceneGroup));
    }

    public boolean equalsBasicInfoIgnoreSceneGroup(ReleaseInfo info2) {
        if (info2 == null) {
            return false;
        }
        return (info2.getTitle().equalsIgnoreCase(title) && info2.getYear() == year);
    }

    public boolean hasCompleteBasicInfo() {
        return true; // Title suffices for generic video
    }
}
