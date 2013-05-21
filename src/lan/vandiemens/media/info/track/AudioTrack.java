package lan.vandiemens.media.info.track;

import java.util.regex.Pattern;

/**
 *
 * @author vmurcia
 */
public class AudioTrack extends MediaTrack {

    public static final Pattern channelsPattern = Pattern.compile("(?<channels>\\d+) channels?");
    private String modeExtension = null;
    private String duration = null;
    private int channelCount = 0;
    private int bitrate = 0;
    private AudioTrackType audioType = AudioTrackType.NORMAL;

    @Override
    public TrackType getType() {
        return TrackType.AUDIO;
    }

    /**
     * Returns the type of audio track: normal or commentary.
     * @return the audio type
     */
    public AudioTrackType getAudioType() {
        return audioType;
    }

    public void setAudioType(AudioTrackType type) {
        audioType = type;
    }

    public boolean isCommentary() {
        return audioType == AudioTrackType.COMMENTARY;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(int count) {
        channelCount = count;
    }

    /**
     * Returns the bitrate in bits per second (bps).
     * @return
     */
    public int getBitrate() {
        return bitrate;
    }

    /**
     * Sets the bitrate in bits per second (bps).
     * @param rate the bitrate in bps
     */
    public void setBitrate(int rate) {
        bitrate = rate;
    }

    @Override
    public String toString() {
        return "[Audio - " + format + " - " + language.getLanguageName() + (isCommentary() ? " Commentary]" : "]");
    }

    @Override
    public String toXml() {
        StringBuilder builder = new StringBuilder();
        builder.append("<track type=\"Audio\"");
        if (streamId > 0) {
            builder.append(" streamid=\"");
            builder.append(streamId);
        }
        builder.append("\">\n");
        builder.append("<ID>");
        builder.append(id);
        builder.append("</ID>\n");
        builder.append("<Format>");
        builder.append(format);
        builder.append("</Format>\n");
        if (formatInfo != null) {
            builder.append("<Format_Info>");
            builder.append(formatInfo);
            builder.append("</Format_Info>\n");
        }

//        builder.append("<Mode_extension>");
//        builder.append(modeExtension);
//        builder.append("</Mode_extension>\n");
        builder.append("<Codec_ID>");
        builder.append(codecId);
        builder.append("</Codec_ID>\n");
//        builder.append("<Bit_rate>");
//        builder.append(getBitrateString());
//        builder.append("</Bit_rate>\n");
//        builder.append("<Duration>");
//        builder.append(duration);
//        builder.append("</Duration>\n");
        if (title != null) {
            builder.append("<Title>");
            builder.append(title);
            builder.append("</Title>\n");
        }
        builder.append("<Language>");
        builder.append(language.getLanguageName());
        builder.append("</Language>\n");
        builder.append("<Default>");
        builder.append(isDefault ? "Yes" : "No");
        builder.append("</Default>\n");
        builder.append("<Forced>");
        builder.append(isForced ? "Yes" : "No");
        builder.append("</Forced>\n");
        builder.append("</track>\n");

        return builder.toString();
    }

    private String getBitrateString() {
        return bitrate + "bps"; // TODO: Calculate units better
    }

    @Override
    public String getFormattedTitle() {
        StringBuilder name = new StringBuilder();
        switch (format) {
            case "AC-3":
                name.append("AC3");
                break;
            default:
                name.append(format);
        }
        switch (language) {
            case ENGLISH:
                name.append(" English");
                if (isCommentary()) {
                    name.append(" Commentary");
                }
                break;
            case SPANISH:
                name.append(" Castellano");
                if (isCommentary()) {
                    name.append(" Comentarios");
                }
                break;
            default:
                name.append(" ").append(language.getLanguageName());
        }

        return name.toString();
    }
}
