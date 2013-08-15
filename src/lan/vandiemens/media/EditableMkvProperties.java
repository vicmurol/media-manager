package lan.vandiemens.media;

import lan.vandiemens.media.info.MediaInfo;
import lan.vandiemens.media.info.track.MediaTrack;
import lan.vandiemens.media.info.track.Track;
import lan.vandiemens.media.info.track.TrackType;
import lan.vandiemens.util.lang.Language;



/**
 *
 * @author vmurcia
 */
public class EditableMkvProperties {

    private String title = null;
    private EditableTrackInfo[] tracksProperties = null;

    public EditableMkvProperties(MediaInfo mediaInfo) {
        tracksProperties = new EditableTrackInfo[mediaInfo.getTotalTrackCount()];
        for (int i = 0; i < tracksProperties.length; i++) {
            tracksProperties[i] = getEditableInfo(mediaInfo.getTracks()[i]);
        }
    }

    private EditableTrackInfo getEditableInfo(Track track) {
        EditableTrackInfo info = new EditableTrackInfo(track.getType());
        if (track.getType() != TrackType.MENU) {
            MediaTrack mediaTrack = (MediaTrack) track;
            info.setTitle(mediaTrack.getTitle());
            info.setLanguage(mediaTrack.getLanguage());
            info.setDefault(mediaTrack.isDefault());
            info.setForced(mediaTrack.isForced());
        }
        return info;
    }

    public int getTrackCount() {
        return tracksProperties.length;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle(int trackIndex) {
        return tracksProperties[trackIndex].getTitle();
    }

    public void setTitle(int trackIndex, String title) {
        tracksProperties[trackIndex].setTitle(title);
    }

    public Language getLanguage(int trackIndex) {
        return tracksProperties[trackIndex].getLanguage();
    }

    public void setLanguage(int trackIndex, Language language) {
        tracksProperties[trackIndex].setLanguage(language);
    }

    public boolean isDefault(int trackIndex) {
        return tracksProperties[trackIndex].isDefault();
    }

    public void setDefault(int trackIndex, boolean isDefault) {
        tracksProperties[trackIndex].setDefault(isDefault);
    }

    public boolean isForced(int trackIndex) {
        return tracksProperties[trackIndex].isForced();
    }

    public void setForced(int trackIndex, boolean forced) {
        tracksProperties[trackIndex].setForced(forced);
    }

    public boolean hasMenuTrack() {
        for (EditableTrackInfo editableTrack : tracksProperties) {
            if (editableTrack.getType() == TrackType.MENU) {
                return true;
            }
        }
        return false;
    }
}

class EditableTrackInfo {

    private TrackType type = null;
    private String title = null;
    private boolean isDefault = false;
    private boolean isForced = false;
    private Language language = Language.UNDEFINED;

    public EditableTrackInfo(TrackType type) {
        this.type = type;
    }

    public TrackType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isForced() {
        return isForced;
    }

    public void setForced(boolean forced) {
        isForced = forced;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}