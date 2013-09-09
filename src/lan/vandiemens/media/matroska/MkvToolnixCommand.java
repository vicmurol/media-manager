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

    public static String DOUBLE_QUOTES = "\"";
    public static String SPACE = " ";
    public static String UNDERSCORE = "_";
    public static String COMMA = ",";
    public static String SEMICOLON = ";";
    public static String COLON = ":";
    public static String ASTERISK = "*";
    public static String QUESTION_MARK = "?";
    public static String LEFT_PARENTHESIS = "(";
    public static String RIGHT_PARENTHESIS = ")";
    public static String EQUALS_SIGN = "=";
    public static int DEFAULT_COMMAND_LENGTH = 1024;
    private static String MKV_EXTENSION = ".mkv";
    private static String UPLOADER_PLACEHOLDER = "unknown";
    private static String WEB_SOURCE_PLACEHOLDER = "unknown";
    private static String SCENE_GROUP_PLACEHOLDER = "unknown";
    private static String SOURCE_PLACEHOLDER = "unknown";
    private static String ORIGINAL_TITLE_PLACEHOLDER = "=";
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
        filenameBuilder.append(info.getSceneGroup() == null ? SCENE_GROUP_PLACEHOLDER : info.getSceneGroup());
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
        return candidateTitle.replace(COLON, SEMICOLON).replace(ASTERISK, "").replace(QUESTION_MARK, "");
    }
}
