package lan.vandiemens.media.manager;

import java.util.ResourceBundle;

/**
 *
 * @author vmurcia
 */
public class VersionInfo {

    public static final String APPLICATION_RESOURCE_NAME = "lan.vandiemens.media.manager.version";
    public static final String SUITE_NAME_KEY = "media.manager.title";
    public static final String EDITOR_NAME_KEY = "media.editor.title";
    public static final String SUBS_EXTRACTOR_NAME_KEY = "media.subtitle.extractor.title";
    public static final String VERSION_KEY = "media.manager.version";
    public static final String BUILD_KEY = "media.manager.build";

    public static String getApplicationSuiteFullName() {
        String appSuiteName = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(SUITE_NAME_KEY);
        String version = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(VERSION_KEY);
        String buildNumber = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(BUILD_KEY);
        return appSuiteName + " v" + version + " Build " + buildNumber;
    }

    public static String getMatroskaEditorFullName() {
        String appName = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(EDITOR_NAME_KEY);
        String version = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(VERSION_KEY);
        String buildNumber = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(BUILD_KEY);
        return appName + " v" + version + " Build " + buildNumber;
    }

    public static String getMatroskaSubtitleExtractorFullName() {
        String appName = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(SUBS_EXTRACTOR_NAME_KEY);
        String version = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(VERSION_KEY);
        String buildNumber = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(BUILD_KEY);
        return appName + " v" + version + " Build " + buildNumber;
    }
}
