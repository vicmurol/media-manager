package lan.vandiemens.media.info.track;

/**
 *
 * @author vmurcia
 */
public interface Track {

    public TrackType getType();

    public void disable();

    public boolean isEnabled();

    public String toXml();

    public String getMkvPropEditDescription();
}
