package lan.vandiemens.media.info.track;

/**
 * Compares media tracks from a media whose original language is English.
 *
 * @author vmurcia
 */
public class EnglishMediaComparator extends AbstractTrackComparator {

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
        // Castilian Spanish comes first, English comes second,
        // Latin Spanish comes third and the others don't matter
        switch (track1.getLanguage()) {
            case SPANISH:
                if (track1.isLatinSpanish()) {
                    switch (track2.getLanguage()) {
                        case SPANISH:
                            if (track2.isLatinSpanish()) {
                                return 0;
                            } else {
                                return 1;
                            }
                        case ENGLISH:
                            return 1;
                        default:
                            return -1;
                    }
                } else { // Track 1: Castilian Spanish
                    switch (track2.getLanguage()) {
                        case SPANISH:
                            if (track2.isLatinSpanish()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        default:
                            return -1;
                    }
                }
            case ENGLISH:
                switch (track2.getLanguage()) {
                    case SPANISH:
                        if (track2.isLatinSpanish()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    case ENGLISH:
                        return 0;
                    default:
                        return -1;
                }
            default: // It doesn't matter
                return 1;
        }
    }
}
