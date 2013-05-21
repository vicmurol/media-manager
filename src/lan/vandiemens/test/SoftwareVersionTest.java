package lan.vandiemens.test;

import java.util.ResourceBundle;

/**
 * @author Victor
 */
public class SoftwareVersionTest {

    public static void main(String[] args) {
        String versionNumber = ResourceBundle.getBundle("version").getString("VERSION");
        String buildNumber = ResourceBundle.getBundle("version").getString("BUILD");

        System.out.println("Version: " + versionNumber);
        System.out.println("Build: " + buildNumber);
        System.out.println("Created using Media Manager v" + versionNumber + " Build " + buildNumber);
    }
}
