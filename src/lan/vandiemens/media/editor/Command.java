package lan.vandiemens.media.editor;

import java.io.File;
import java.util.List;

/**
 *
 * @author vmurcia
 */
public interface Command {

    public File getInputFile();
    public File getOutputFile();
    /**
     * Useful for methods that need the command and its arguments split.
     *
     * @return a list consisting of the command and its options
     */
    public List<String> toList();
}
