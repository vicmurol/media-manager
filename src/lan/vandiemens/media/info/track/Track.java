package lan.vandiemens.media.info.track;

import lan.vandiemens.util.lang.Language;

/**
 *
 * @author vmurcia
 */
public interface Track {

    public TrackType getType();

    public boolean isEnabled();

    public boolean isExternal();

    public void disable();

    public int getTrackId();

    public void setTrackId(int tid);

    public int getTrackNumber();

    public void setTrackNumber(int number);

    public String getTitle();

    public void setTitle(String title);

    public String getFormattedTitle();

    public Language getLanguage();

    public String getLanguageCode();

    public boolean hasLanguage(Language lang);

    public boolean isEnglish();

    public boolean isSpanish();

    public boolean isLatinSpanish();

    public void setLanguage(Language language);

    public void setLanguage(String language);

    public boolean isDefault();

    public void setAsDefault(boolean isDefault);

    public boolean isForced();

    public void setForced(boolean isForced);

    public String toXml();

    public String getMkvPropEditDescription();

    public String getAssociatedFileExtension();
}
