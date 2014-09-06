package lan.vandiemens.media;

import java.io.File;
import java.io.IOException;
import lan.vandiemens.media.analysis.MediaInfoException;
import org.junit.Test;
import static lan.vandiemens.util.lang.Language.*;
import static org.junit.Assert.*;

/**
 *
 * @author vmurcia
 */
public class MediaFileTest {

    private final File dualMovieFile = new File("src/lan/vandiemens/media/sample/mediainfo/Carrie.1976.BDRip.1080p.DTS.x264-Taito.xml");

    /**
     * Tests the processing of a dual audio movie with graphics subtitles.
     *
     * @throws java.io.IOException
     * @throws lan.vandiemens.media.analysis.MediaInfoException
     */
    @Test
    public void testProcessingDualLanguageMovieWithGraphicsSubtitles() throws IOException, MediaInfoException {
        System.out.println("Testing a dual movie with forced and complete dual subtitles...");
        MediaFile mediaFile = new MediaFile(dualMovieFile);
        assertTrue(mediaFile.isMovie());
        assertFalse(mediaFile.isTvSeries());
        assertEquals(dualMovieFile, mediaFile.getMainFile());
        assertEquals("Parent folder is wrong", dualMovieFile.getParentFile(), mediaFile.getParentFolder());
        assertArrayEquals(new File[] {dualMovieFile}, mediaFile.getFiles());
        assertEquals("Media filename is wrong", dualMovieFile.getName(), mediaFile.getName());
        assertEquals("Formatted title is wrong", "Carrie (1976)", mediaFile.getFormattedTitle());
        assertEquals("Release group name is wrong", "Taito", mediaFile.getReleaseGroup());
        assertEquals("File count is wrong", 1, mediaFile.getFileCount());
        assertEquals("Video track count is wrong", 1, mediaFile.getVideoTrackCount());
        assertEquals("Audio track count is wrong", 2, mediaFile.getAudioTrackCount());
        assertEquals("Subtitle track count is wrong", 3, mediaFile.getSubtitleTrackCount());
        assertTrue(mediaFile.wasShotIn(ENGLISH));
        assertFalse(mediaFile.wasShotIn(SPANISH));
        assertTrue(mediaFile.hasWdTvCompatibleEnglishAudioTrack());
        assertTrue(mediaFile.hasWdTvCompatibleSpanishAudioTrack());
        assertTrue(mediaFile.hasSubtitles());
        assertTrue(mediaFile.hasEnglishCompleteSubtitles());
        assertArrayEquals("Added subtitles don't match", new SubtitleFile[0], mediaFile.getAddedSubtitles());
        assertFalse(mediaFile.hasChapters());
        assertTrue(mediaFile.meetsReleaseInfoRequirements());
        assertFalse(mediaFile.meetsLanguageRequirements());
        assertEquals("[ID#0 - Video - AVC - 1920x1040]", mediaFile.getTrack(0).toString());
        assertEquals("[ID#1 - Audio - DTS - Spanish]", mediaFile.getTrack(1).toString());
        assertEquals("[ID#2 - Audio - AC-3 - English]", mediaFile.getTrack(2).toString());
        assertEquals("[ID#3 - Subtitle - PGS - Spanish Forced]", mediaFile.getTrack(3).toString());
        assertEquals("[ID#4 - Subtitle - PGS - Spanish Complete]", mediaFile.getTrack(4).toString());
        assertEquals("[ID#5 - Subtitle - PGS - English Complete]", mediaFile.getTrack(5).toString());
        mediaFile.disableUnwantedTracks();
        assertFalse(mediaFile.isTrackDisabled(0));
        assertFalse(mediaFile.isTrackDisabled(1));
        assertFalse(mediaFile.isTrackDisabled(2));
        assertTrue(mediaFile.isTrackDisabled(3));
        assertTrue(mediaFile.isTrackDisabled(4));
        assertTrue(mediaFile.isTrackDisabled(5));
        assertTrue(mediaFile.needsRemux());
    }
}
