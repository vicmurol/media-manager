package lan.vandiemens.media.info.track;

import static lan.vandiemens.media.matroska.MkvPropEditCommand.*;
import lan.vandiemens.util.lang.Language;


/**
 *
 * @author vmurcia
 */
public abstract class MediaTrack implements Track {

    // mkvmerge and mkvextract use track IDs but mkvpropedit and MediaInfo use track numbers instead
    // Track numbers start at 1, but track IDs start at 0
    protected int trackId = 0;
    protected int streamId = 0;
    protected String format = null;
    protected String formatInfo = null;
    protected String codecId = null;
    protected String title = null;
    protected Language language = null;
    protected boolean isDefault = false;
    protected boolean isForced = false;
    protected boolean isDisabled = false;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCodecId() {
        return codecId;
    }

    public void setCodecId(String codecId) {
        this.codecId = codecId;
    }

    public String getFormatInfo() {
        return formatInfo;
    }

    public void setFormatInfo(String formatInfo) {
        this.formatInfo = formatInfo;
    }

    @Override
    public int getTrackId() {
        return trackId;
    }

    @Override
    public void setTrackId(int tid) {
        trackId = tid;
    }

    @Override
    public int getTrackNumber() {
        return trackId + 1;
    }

    @Override
    public void setTrackNumber(int number) {
        trackId = number - 1;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setAsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isForced() {
        return isForced;
    }

    public void setForced(boolean isForced) {
        this.isForced = isForced;
    }

    @Override
    public boolean isEnabled() {
        return !isDisabled;
    }

    @Override
    public void disable() {
        isDisabled = true;
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    public String getLanguageCode() {
        return (language == null || language == Language.UNDEFINED) ? "und" : language.getThreeLettersIsoCode();
    }

    @Override
    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public void setLanguage(String language) {
        this.language = language == null ? Language.UNDEFINED : Language.parseLanguage(language);
    }

    public boolean isLatinSpanish() {
        return (language == Language.SPANISH && title != null && (title.indexOf("Latin") != -1 || title.indexOf("LATIN") != -1 || title.indexOf("latin") != -1));
    }

    public boolean isSpanish() {
        return language == Language.SPANISH;
    }

    public boolean isEnglish() {
        return language == Language.ENGLISH;
    }

    public boolean hasLanguage(Language lang) {
        return language == lang;
    }

    public int getStreamId() {
        return streamId;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getMkvPropEditDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(EDIT_OPTION);
        builder.append(" ");
        // mkvpropedit uses track numbers instead of track IDs to reference tracks
        builder.append(TRACK_HEADER_OPTION).append(getTrackNumber());
        builder.append(" ");
        builder.append(SET_OPTION);
        builder.append(" ");
        builder.append(DEFAULT_TRACK_FLAG_PROPERTY).append("=").append(isDefault ? "1" : "0");
        builder.append(" ");
        builder.append(SET_OPTION);
        builder.append(" ");
        builder.append(FORCED_TRACK_FLAG_PROPERTY).append("=").append(isForced ? "1" : "0");
        builder.append(" ");
        builder.append(SET_OPTION);
        builder.append(" ");
        builder.append(TRACK_NAME_PROPERTY).append("=").append("\"").append(getFormattedTitle()).append("\"");
        builder.append(" ");
        builder.append(SET_OPTION);
        builder.append(" ");
        builder.append(LANGUAGE_PROPERTY).append("=").append("\"").append(language.getThreeLettersIsoCode()).append("\"");

        return builder.toString();
    }
}
