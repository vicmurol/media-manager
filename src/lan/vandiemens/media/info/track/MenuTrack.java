package lan.vandiemens.media.info.track;

/**
 *
 * @author vmurcia
 */
public class MenuTrack implements Track {

    private boolean isEnabled = true;

    @Override
    public TrackType getType() {
        return TrackType.MENU;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void disable() {
        isEnabled = false;
    }

    @Override
    public String toString() {
        return "[Menu]";
    }

    @Override
    public String toXml() {
        return "Menu";
    }

    @Override
    public String getMkvPropEditDescription() {
        return "";
    }
}
