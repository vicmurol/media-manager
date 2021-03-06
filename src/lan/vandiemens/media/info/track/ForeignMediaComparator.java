package lan.vandiemens.media.info.track;

import lan.vandiemens.util.lang.Language;

/**
 * Compares media tracks from media whose original language is neither English
 * nor Spanish.
 *
 * @author vmurcia
 */
public class ForeignMediaComparator extends AbstractTrackComparator {

    Language originalLanguage = null;

    public ForeignMediaComparator(Language language) {
        if (language == null) {
            throw new NullPointerException();
        }
        originalLanguage = language;
    }

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
        // Castilian Spanish comes first, original language comes second,
        // English comes third and the others don't matter
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
                            if (track2.hasLanguage(originalLanguage)) {
                                return 1;
                            } else {
                                return -1;
                            }
                    }
                } else { // Track 1: Castilian Spanish
                    switch (track2.getLanguage()) {
                        case SPANISH:
                            if (track2.isLatinSpanish()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        default: // Spanish comes first
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
                        if (track2.hasLanguage(originalLanguage)) {
                            return 1;
                        } else {
                            return -1;
                        }
                }
            default:
                if (track1.hasLanguage(originalLanguage)) {
                    switch (track2.getLanguage()) {
                        case SPANISH:
                            if (track2.isLatinSpanish()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        default:
                            if (track2.hasLanguage(originalLanguage)) {
                                return 0;
                            } else {
                                return -1;
                            }
                    }
                } else { // It doesn't matter, put it in the last position
                    return 1;
                }
        }
    }
}
