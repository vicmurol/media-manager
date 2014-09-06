/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lan.vandiemens.media.cataloguer;

import lan.vandiemens.media.info.release.ReleaseInfoParser;
import lan.vandiemens.media.info.release.MovieReleaseInfo;
import lan.vandiemens.media.info.release.ReleaseInfo;
import lan.vandiemens.media.info.release.TvEpisodeReleaseInfo;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vmurcia
 */
public class ReleaseInfoParserTest {

    public ReleaseInfoParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of isTvSeries method, of class ReleaseInfoParser.
     */
    @Test
    public void testIsTvSeries() {
        System.out.println("Check if filename matches TV series pattern");

        String filename = "The.Walking.Dead.S02E11.Judge.Jury.Executioner.720p.WEB-DL.AAC2.0.H.264-CtrlHD";
        assertTrue(ReleaseInfoParser.hasTvSeriesNamePattern(filename));

        filename = "misfits.3x07.720p.hdtv.x264-tla";
        assertTrue(ReleaseInfoParser.hasTvSeriesNamePattern(filename));

        filename = "Mad.Men.s01e01.pilot";
        assertTrue(ReleaseInfoParser.hasTvSeriesNamePattern(filename));

        filename = "The.Hitchhiker's.Guide.to.the.Galaxy.2005.720p.BluRay.x264-REVEiLLE";
        assertFalse(ReleaseInfoParser.hasTvSeriesNamePattern(filename));
    }

    /**
     * Test of parse method, of class ReleaseInfoParser.
     */
    @Test
    public void testParseTvEpisodeWithTypicalSceneFilename() {
        System.out.println("Parse typical scene-named TV series");
        String filename = "The.Walking.Dead.S02E11.Judge.Jury.Executioner.720p.WEB-DL.AAC2.0.H.264-CtrlHD";
        ReleaseInfo info = ReleaseInfoParser.parse(filename);

        Class type = TvEpisodeReleaseInfo.class;
        assertTrue(type.isInstance(info));

        TvEpisodeReleaseInfo episodeInfo = (TvEpisodeReleaseInfo) info;

        assertEquals("The Walking Dead", episodeInfo.getTitle());
        assertEquals(2, episodeInfo.getSeason());
        assertEquals(11, episodeInfo.getEpisodeNumber());
        assertEquals("Judge Jury Executioner", episodeInfo.getEpisodeTitle());
        assertEquals("720p", episodeInfo.getVideoQuality());
        assertEquals("HDiTunes", episodeInfo.getVideoSource());
        assertEquals("CtrlHD", episodeInfo.getReleaseGroup());
        assertEquals(-1,episodeInfo.getYear());
        assertNull(episodeInfo.getRipper());
        assertNull(episodeInfo.getUploader());
        assertNull(episodeInfo.getWebSource());
    }

    /**
     * Test of parse method, of class ReleaseInfoParser.
     */
    @Test
    public void testParseMovieWithTypicalSceneFilename() {
        System.out.println("Parse typical scene-named movie");
        String filename = "The.Hitchhiker's.Guide.to.the.Galaxy.2005.720p.BluRay.x264-REVEiLLE";
        ReleaseInfo movieInfo = ReleaseInfoParser.parse(filename);

        assertEquals("The Hitchhiker's Guide to the Galaxy", movieInfo.getTitle());
        assertEquals("720p", movieInfo.getVideoQuality());
        assertEquals("BluRay", movieInfo.getVideoSource());
        assertEquals("REVEiLLE", movieInfo.getReleaseGroup());
        assertEquals(2005,movieInfo.getYear());
        assertNull(movieInfo.getRipper());
        assertNull(movieInfo.getUploader());
        assertNull(movieInfo.getWebSource());
    }
}
