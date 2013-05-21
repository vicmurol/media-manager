package lan.vandiemens.media.info.track;

/**
 *
 * @author vmurcia
 */
public class VideoTrack extends MediaTrack {

    private int width = 0;
    private int height = 0;

    @Override
    public TrackType getType() {
        return TrackType.VIDEO;
    }

    @Override
    public String toString() {
        return "[Video - " + format + " - " + getResolution() + "]";
    }

    @Override
    public String toXml() {
        StringBuilder builder = new StringBuilder();
        builder.append("<track type=\"Video\"");
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
        builder.append("<Format_Info>");
        builder.append(formatInfo);
        builder.append("</Format_Info>\n");
//        builder.append("<Mode_extension>");
//        builder.append(modeExtension);
//        builder.append("</Mode_extension>\n");
        builder.append("<Codec_ID>");
        builder.append(codecId);
        builder.append("</Codec_ID>\n");
//        builder.append("<Duration>");
//        builder.append(duration);
//        builder.append("</Duration>\n");
        builder.append("<Width>");
        builder.append(width).append(" pixels");
        builder.append("</Width>\n");
        builder.append("<Height>");
        builder.append(height).append(" pixels");
        builder.append("</Height>\n");
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

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getResolution() {
        return width + "x" + height;
    }

    @Override
    public String getFormattedTitle() {
        return title;
    }
}
