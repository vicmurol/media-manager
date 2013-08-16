package lan.vandiemens.media.info.track;

/**
 * Compares media tracks from a media whose original language is Spanish.
 *
 * @author vmurcia
 */
public class SpanishMediaComparator extends AbstractTrackComparator {

    @Override
    public int compare(Track track1, Track track2) {
        // Disabled tracks come second
        if (track1.isEnabled() && !track2.isEnabled()) {
            return -1;
        } else if (!track1.isEnabled() && track2.isEnabled()) {
            return 1;
        } else {
            return compareByType(track1, track2);
        }
    }

    @Override
    protected int compareByLanguage(Track track1, Track track2) {
        // Spanish comes first
        if (track1.isSpanish()) {
            if (track2.isSpanish()) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (track2.isSpanish()) {
                return 1;
            } else {
                return -1; // If neither of them is Spanish no matter which one comes first
            }
        }
    }
}
