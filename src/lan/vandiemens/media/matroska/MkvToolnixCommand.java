package lan.vandiemens.media.matroska;

import java.io.File;
import java.util.Date;
import lan.vandiemens.media.MediaFile;
import lan.vandiemens.media.info.release.ReleaseInfo;

/**
 *
 * @author vmurcia
 */
public abstract class MkvToolnixCommand implements Command {

    public static final String VOID_CHAR = "";
    public static final String DOUBLE_QUOTES = "\"";
    public static final String SPACE = " ";
    public static final String UNDERSCORE = "_";
    public static final String PERIOD = ".";
    public static final String COMMA = ",";
    public static final String SEMICOLON = ";";
    public static final String COLON = ":";
    public static final String ASTERISK = "*";
    public static final String QUESTION_MARK = "?";
    public static final String LEFT_PARENTHESIS = "(";
    public static final String RIGHT_PARENTHESIS = ")";
    public static final String EQUALS_SIGN = "=";
    public static final int DEFAULT_COMMAND_LENGTH = 1024;
    private static final String MKV_EXTENSION = ".mkv";
    private static final String UPLOADER_PLACEHOLDER = "unknown";
    private static final String WEB_SOURCE_PLACEHOLDER = "unknown";
    private static final String SCENE_GROUP_PLACEHOLDER = "unknown";
    private static final String SOURCE_PLACEHOLDER = "unknown";
    private static final String ORIGINAL_TITLE_PLACEHOLDER = "=";
    protected String title = null;
    protected File inputFile = null;
    protected File outputFile = null;

    @Override
    public File getInputFile() {
        return inputFile;
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    protected File generateOutputMkvFile(MediaFile mediaFile) {
        ReleaseInfo info = mediaFile.getReleaseInfo();
        StringBuilder filenameBuilder = new StringBuilder();
        filenameBuilder.append(getTitleWithoutTroublesomeCharacters(title));
        filenameBuilder.append(UNDERSCORE);
        filenameBuilder.append(info.getWebSource() == null ? WEB_SOURCE_PLACEHOLDER : info.getWebSource());
        filenameBuilder.append(UNDERSCORE);
        filenameBuilder.append(info.getUploader() == null ? UPLOADER_PLACEHOLDER : info.getUploader());
        filenameBuilder.append(UNDERSCORE);
        filenameBuilder.append(info.getOriginalTitle() == null ? ORIGINAL_TITLE_PLACEHOLDER : info.getOriginalTitle());
        filenameBuilder.append(UNDERSCORE);
        filenameBuilder.append(info.getVideoSource() == null ? SOURCE_PLACEHOLDER : info.getVideoSource());
        filenameBuilder.append(UNDERSCORE);
        filenameBuilder.append(info.getReleaseGroup() == null ? SCENE_GROUP_PLACEHOLDER : info.getReleaseGroup());
        File resultFile = new File(mediaFile.getMainFile().getParentFile(), filenameBuilder.toString() + MKV_EXTENSION);
        // Check if file already exists to generate a unique filename
        if (resultFile.exists()) {
            for (int i = 1; i < 10; i++) {
                resultFile = new File(mediaFile.getMainFile().getParentFile(), filenameBuilder.toString() + " (" + i + ")" + MKV_EXTENSION);
                if (!resultFile.exists()) {
                    break;
                }
            }
            resultFile = new File(mediaFile.getMainFile().getParentFile(), filenameBuilder.toString() + " (" + new Date() + ")" + MKV_EXTENSION);
        }
        return resultFile;
    }

    private String getTitleWithoutTroublesomeCharacters(String candidateTitle) {
        return candidateTitle.replace(COLON, SEMICOLON).replace(ASTERISK, VOID_CHAR).replace(QUESTION_MARK, VOID_CHAR);
    }
}
