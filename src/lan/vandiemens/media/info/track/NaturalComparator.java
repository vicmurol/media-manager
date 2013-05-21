package lan.vandiemens.media.info.track;

import java.util.Comparator;

/**
 *
 * @author vmurcia
 */
public class NaturalComparator implements Comparator<Track > {

    @Override
    public int compare(Track track1, Track track2) {
        // Menu track always last
        if (track1.getType() == TrackType.MENU) {
            return 1;
        } else if (track2.getType() == TrackType.MENU) {
            return -1;
        } else {
            return compareById(track1, track2);
        }
    }

    private int compareById(Track track1, Track track2) {
        // If not menu tracks they must be media tracks
        MediaTrack mediaTrack1 = (MediaTrack) track1;
        MediaTrack mediaTrack2 = (MediaTrack) track2;

        if (mediaTrack1.getId() < mediaTrack2.getId()) {
            return -1; // Lower ID comes first
        } else if (mediaTrack1.getId() == mediaTrack2.getId()) {
            return 0; // Shouldn't happen, IDs are unique
        } else {
            return 1;
        }
    }
}
