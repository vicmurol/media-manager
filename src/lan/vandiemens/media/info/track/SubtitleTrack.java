package lan.vandiemens.media.info.track;

/**
 *
 * @author vmurcia
 */
public class SubtitleTrack extends MediaTrack {

    protected SubtitleTrackType subtitleType = SubtitleTrackType.NORMAL;
    protected CompressionAlgorithm compressionMode = CompressionAlgorithm.NONE;

    @Override
    public TrackType getType() {
        return TrackType.SUBTITLE;
    }

    public boolean isTextBased() {
        return codecId.startsWith("S_TEXT");
    }

    @Override
    public String getAssociatedFileExtension() {
        String extension;
        switch (codecId) {
            case "S_TEXT/UTF8":
                extension = "srt";
                break;
            case "S_TEXT/SSA":
                extension = "ssa";
                break;
            case "S_TEXT/ASS":
                extension = "ass";
                break;
            case "S_TEXT/USF":
                extension = "usf";
                break;
            case "S_VOBSUB":
                extension = "sub";
                break;
            case "S_HDMV/PGS":
                extension = "sup";
                break;
            default:
                extension = UNKNOWN_FILE_EXTENSION;
        }
        return extension;
    }

    public boolean isComplete() {
        return isNormal() || isForHearingImpaired();
    }

    public boolean isNormal() {
        return subtitleType == SubtitleTrackType.NORMAL;
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
        return "[ID#" + trackId + " - Subtitle - " + format + " - " + language.getLanguageName() + " " + subtitleType + "]";
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
        builder.append(getTrackNumber());
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

    public CompressionAlgorithm getCompressionMode() {
        return compressionMode;
    }
    
    public void setCompressionMode(String algorithm) {
        if (algorithm == null) {
            compressionMode = CompressionAlgorithm.NONE;
        } else if (algorithm.equalsIgnoreCase("zlib")) {
            compressionMode = CompressionAlgorithm.ZLIB;
        } else {
            compressionMode = CompressionAlgorithm.UNKNOWN;
        }
    }
}
