/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lan.vandiemens.media.cataloguer;

import java.io.File;
import lan.vandiemens.util.file.DirectoryFilter;

/**
 * @author Victor
 */
public class MultiFolderCataloguer {

    private static void catalogFolder(File folder, boolean isMediaInfoOnlyEnabled, boolean isReverseMode) {
        if (folder.isDirectory()) {
            File[] subfolders = folder.listFiles(new DirectoryFilter());
            for (File subfolder : subfolders) {
                catalogFolder(subfolder, isMediaInfoOnlyEnabled, isReverseMode);
            }

            // Do the actual cataloguing process
            Cataloguer cataloguer = new Cataloguer(folder);
            if (isReverseMode) {
                cataloguer.setReverseModeEnabled(true);
            } else if (isMediaInfoOnlyEnabled) {
                cataloguer.setMediaInfoOnlyEnabled(true);
            }
            cataloguer.start();
        }
    }

    public static void main(String[] args) {
        // Check arguments entered by the user
        if (args.length == 0) {
            Cataloguer.showUsage();
            System.exit(0);
        }

        boolean isMediaInfoOnlyEnabled = false;
        boolean isReverseMode = false;

        // Parse argument list
        int argi = 0;
        OUTER:
        for (; argi < args.length; argi++) {
            switch (args[argi]) {
                case "-i":
                case "--info":
                    isMediaInfoOnlyEnabled = true;
                    break;
                case "-r":
                case "--reverse":
                    isReverseMode = true;
                    break;
                default:
                    break OUTER;
            }
        }
        if (isReverseMode && isMediaInfoOnlyEnabled) {
            System.out.println("Media info only and reverse mode can't be used together.");
            Cataloguer.showUsage();
            System.exit(0);
        }

        // Check if there is at least one directory
        if (argi == args.length) {
            Cataloguer.showUsage();
            System.exit(0);
        }

        // Recursively catalog each directory
        for (; argi < args.length; argi++) {
            catalogFolder(new File(args[argi]), isMediaInfoOnlyEnabled, isReverseMode);
        }
    }
}
