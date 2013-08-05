package lan.vandiemens.media.info.track;

/**
 *
 * @author vmurcia
 */
public class SubtitleTrack extends MediaTrack {

    protected SubtitleTrackType subtitleType = SubtitleTrackType.COMPLETE;

    @Override
    public TrackType getType() {
        return TrackType.SUBTITLE;
    }

    public boolean isTextBased() {
        return codecId.startsWith("S_TEXT");
    }

    public boolean isCompleteOrForHearingImpaired() {
        return isComplete() || isForHearingImpaired();
    }

    public boolean isComplete() {
        return subtitleType == SubtitleTrackType.COMPLETE;
    }

    public boolean isForHearingImpaired() {
        return subtitleType == SubtitleTrackType.FOR_HEARING_IMPAIRED;
    }

    public boolean isCommentary() {
        return subtitleType == SubtitleTrackType.COMMENTARY;
    }

    public SubtitleTrackType getSubType() {
        return subtitleType;
    }

    @Override
    public void setForced(boolean isForced) {
        this.isForced = isForced;
        if (isForced) {
            subtitleType = SubtitleTrackType.FORCED;
        }
    }

    public void setSubType(SubtitleTrackType type) {
        if (type == null) {
            throw new NullPointerException();
        }
        subtitleType = type;
    }

    @Override
    public String toString() {
        return "[ID#" + id + " - Subtitle - " + format + " - " + language.getLanguageName() + " " + subtitleType + "]";
    }

    @Override
    public String toXml() {
        StringBuilder builder = new StringBuilder();
        builder.append("<track type=\"Text\"");
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
        builder.append("<Codec_ID>");
        builder.append(codecId);
        builder.append("</Codec_ID>\n");
        builder.append("<Codec_ID_Info>");
        builder.append(formatInfo);
        builder.append("</Codec_ID_Info>\n");
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

    @Override
    public String getFormattedTitle() {
        StringBuilder name = new StringBuilder();
        switch (language) {
            case ENGLISH:
                name.append("English");
                if (isForced()) {
                    name.append(" Forced");
                } else if (isForHearingImpaired()) {
                    name.append(" FHI");
                } else if (isCommentary()) {
                    name.append(" Commentary");
                }
                break;
            case SPANISH:
                name.append("Castellano");
                if (isForced()) {
                    name.append(" Forzados");
                } else if (isForHearingImpaired()) {
                    name.append(" para sordos");
                } else if (isCommentary()) {
                    name.append(" (Comentario)");
                }
                break;
            default:
                name.append(language.getLanguageName());
        }

        return name.toString();
    }
}
