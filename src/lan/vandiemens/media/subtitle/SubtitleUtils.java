package lan.vandiemens.media.subtitle;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lan.vandiemens.util.file.FileUtils;

/**
 *
 * @author vmurcia
 */
public class SubtitleUtils {
    private static final boolean FAST_FHI_IDENTIFICATION = false;
    private static final int FHI_ENTRY_THRESHOLD = 5;

    public static boolean isSubtitleForHearingImpaired(Path file) throws IOException {
        System.out.println("Analysing " + file.getFileName() + " for FHI entries...");
        checkIfValid(file);
        int fhiEntryCount = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isEntryForHearingImpaired(line)) {
                    fhiEntryCount++;
                    if (FAST_FHI_IDENTIFICATION && fhiEntryCount > FHI_ENTRY_THRESHOLD) {
                        return true;
                    }
                }
            }
        }
        System.out.println(file.getFileName() + " has " + fhiEntryCount + " FHI entries");
        return fhiEntryCount > FHI_ENTRY_THRESHOLD;
    }

    private static void checkIfValid(Path path) throws IllegalArgumentException, IOException {
        if (Files.notExists(path)) {
            throw new IllegalArgumentException(path.getFileName() + " does not exist!");
        } else if (Files.isDirectory(path)) {
            throw new IllegalArgumentException("The given file cannot be a directory");
        } else if (!FileUtils.isEncodedInUtf8(path.toFile())) {
            throw new IllegalArgumentException("The given file must be encoded in UTF-8");
        }
    }

    private static boolean isEntryForHearingImpaired(String line) {
        String typicalFhiWords = "indistinct, laughs, laughter, groans, moans, hums, caws,"
                + " grunts, sighs, sobs, scoffs, cries, sniffs, sniffles, chuckels,"
                + " gasps, hiss, exhales, vibrates, shatters, stammers, screams, chimes,"
                + " cocks, honks, creaks, thuds, buzz, screech, music, chatter, door,"
                + " drawer, cellphone, gun, shot, whistle, footsteps, barks, thunder,"
                + " engine, knock, "; // TODO: Use these keywords to improve the FHI detection pattern
        String fhiRegex = "^[\\[\\(](.+)[\\]\\)]$";
        Pattern fhiPattern = Pattern.compile(fhiRegex);
        Matcher matcher = fhiPattern.matcher(line);
        return matcher.matches();
    }
}
