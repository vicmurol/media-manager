package lan.vandiemens.media.info.track;

/**
 * Compares media tracks taking only into consideration their track IDs.
 *
 * @author vmurcia
 */
public class NaturalComparator extends AbstractTrackComparator {

    @Override
    public int compare(Track track1, Track track2) {
        return compareByTrackId(track1, track2);
    }
}
