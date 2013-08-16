package lan.vandiemens.media.info.track;

/**
 * Chapters are a way to set predefined points to jump to in video or audio.
 *
 * @author vmurcia
 */
public class Chapters {

    private boolean enabled = true;

    public boolean areEnabled() {
        return enabled;
    }

    public void disable() {
        enabled = false;
    }

    @Override
    public String toString() {
        return "[Menu]";
    }

    public String toXml() {
        return "Menu";
    }

    public String getMkvPropEditDescription() {
        return "";
    }
}
