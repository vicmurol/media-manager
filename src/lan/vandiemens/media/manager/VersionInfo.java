package lan.vandiemens.media.manager;

import java.util.ResourceBundle;

/**
 *
 * @author vmurcia
 */
public class VersionInfo {

    public static final String APPLICATION_RESOURCE_NAME = "lan.vandiemens.media.manager.version";
    public static final String NAME_KEY = "media.manager.title";
    public static final String VERSION_KEY = "media.manager.version";
    public static final String BUILD_KEY = "media.manager.build";

    public static String getApplicationFullName() {
        String appName = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(NAME_KEY);
        String version = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(VERSION_KEY);
        String buildNumber = ResourceBundle.getBundle(APPLICATION_RESOURCE_NAME).getString(BUILD_KEY);
        return appName + " v" + version + " Build " + buildNumber;
    }
}
