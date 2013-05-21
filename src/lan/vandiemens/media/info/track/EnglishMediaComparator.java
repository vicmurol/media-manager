package lan.vandiemens.media.info.track;

import java.util.Comparator;

/**
 *
 * @author vmurcia
 */
public class EnglishMediaComparator implements Comparator<Track > {

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

    private int compareByType(Track track1, Track track2) {
        switch (track1.getType()) {
            case VIDEO:
                switch (track2.getType()) {
                    case VIDEO:
                        return compareVideoTracks(track1, track2);
                    default:
                        return -1;
                }
            case AUDIO:
                switch (track2.getType()) {
                    case VIDEO:
                        return 1;
                    case AUDIO:
                        return compareAudioTracks(track1, track2);
                    default:
                        return -1;
                }
            case SUBTITLE:
                switch (track2.getType()) {
                    case VIDEO:
                    case AUDIO:
                        return 1;
                    case SUBTITLE:
                        return compareSubtitleTracks(track1, track2);
                    default:
                        return -1;
                }
            case MENU:
                switch (track2.getType()) {
                    case MENU:
                        return compareMenuTracks(track1, track2);
                    default:
                        return 1;
                }
            default: // Not possible
                return 1;
        }
    }

    private int compareByLanguage(MediaTrack track1, MediaTrack track2) {
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

    private int compareVideoTracks(Track track1, Track track2) {
        return -1; // TODO: Compare by resolution and video codec quality
    }

    private int compareAudioTracks(Track track1, Track track2) {
        AudioTrack audioTrack1 = (AudioTrack) track1;
        AudioTrack audioTrack2 = (AudioTrack) track2;

        int result = compareByLanguage(audioTrack1, audioTrack2);
        if (result == 0) {
            if (!audioTrack1.isCommentary() && audioTrack2.isCommentary()) {
                return -1;
            } else if (audioTrack1.isCommentary() && !audioTrack2.isCommentary()) {
                return 1;
            } else {
                return compareByAudioCodec(audioTrack1, audioTrack2);
            }
        } else {
            return result;
        }
    }

    private int compareByAudioCodec(AudioTrack track1, AudioTrack track2) {
        String codec1 = track1.getCodecId();
        String codec2 = track2.getCodecId();

        switch (codec1) {
            case "A_DTS":
                switch (codec2) {
                    case "A_DTS":
                        return compareByBitrate(track1, track2);
                    default:
                        return -1;
                }
            case "A_AC3":
                switch (codec2) {
                    case "A_DTS":
                        return 1;
                    case "A_AC3":
                        return compareByBitrate(track1, track2);
                    default:
                        return -1;
                }
            case "A_AAC":
                switch (codec2) {
                    case "A_DTS":
                    case "A_AC3":
                        return 1;
                    case "A_AAC":
                        return compareByBitrate(track1, track2);
                    default:
                        return -1;
                }
            case "A_MP3":
                switch (codec2) {
                    case "A_DTS":
                    case "A_AC3":
                    case "A_AAC":
                        return 1;
                    case "A_MP3":
                        return compareByBitrate(track1, track2);
                    default:
                        return -1;
                }
            default:
                return 1;
        }
    }

    private int compareByBitrate(AudioTrack track1, AudioTrack track2) {
        return (track1.getBitrate() >= track2.getBitrate() ? -1 : 1); // No matter if equal bitrate
    }

    private int compareSubtitleTracks(Track track1, Track track2) {
        SubtitleTrack subtitleTrack1 = (SubtitleTrack) track1;
        SubtitleTrack subtitleTrack2 = (SubtitleTrack) track2;

        int result = compareByLanguage(subtitleTrack1, subtitleTrack2);
        if (result == 0) {
            result = compareBySubtitleType(subtitleTrack1, subtitleTrack2);
            if (result == 0) {
                result = compareByFormat(subtitleTrack1, subtitleTrack2);
                if (result == 0) {
                    result = compareById(subtitleTrack1, subtitleTrack2);
                }
            }
        }

        return result;
    }

    /**
     * Compares subtitles of the same language by its type.
     * @param track1 the first subtitle track to be compared
     * @param track2 the second subtitle track to be compared
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     */
    private int compareBySubtitleType(SubtitleTrack track1, SubtitleTrack track2) {
        switch (track1.getSubType()) {
            case FORCED:
                switch (track2.getSubType()) {
                    case FORCED:
                        return 0;
                    default:
                        return -1;
                }
            case COMPLETE:
                switch (track2.getSubType()) {
                    case FORCED:
                        return 1;
                    case COMPLETE:
                        return 0;
                    default:
                        return -1;
                }
            case FOR_HEARING_IMPAIRED:
                switch (track2.getSubType()) {
                    case FORCED:
                    case COMPLETE:
                        return 1;
                    case FOR_HEARING_IMPAIRED:
                        return 0;
                    default:
                        return -1;
                }
            case COMMENTARY:
                switch (track2.getSubType()) {
                    case FORCED:
                    case COMPLETE:
                    case FOR_HEARING_IMPAIRED:
                        return 1;
                    default:
                        return 0;
                }
            default: // Couldn't happen
                return -1;
        }
    }

    /**
     * Compares subtitle tracks of the same language and type by its format.
     * @param track1 the first subtitle track to be compared
     * @param track2 the second subtitle track to be compared
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     */
    private int compareByFormat(SubtitleTrack track1, SubtitleTrack track2) {
        switch (track1.getCodecId()) {
            case "S_TEXT/UTF8":
                switch (track2.getCodecId()) {
                    case "S_TEXT/UTF8":
                        return 0;
                    default:
                        return -1;
                }
            case "S_HDMV/PGS":
                switch (track2.getCodecId()) {
                    case "S_HDMV/PGS":
                        return 0;
                    default:
                        return 1;
                }
            default: // Text-based subtitles come first if unknown format
                if (track1.isTextBased()) {
                    if (track2.isTextBased()) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else {
                    if (track2.isTextBased()) {
                        return 1;
                    } else { // No one is text-based so it doesn't matter
                        return 0;
                    }
                }
        }
    }

    private int compareMenuTracks(Track track1, Track track2) {
        return -1; // TODO: Verify if it's possible that more than one menu coexist
    }
}
