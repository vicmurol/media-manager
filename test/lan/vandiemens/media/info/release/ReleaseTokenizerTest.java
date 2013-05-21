package lan.vandiemens.media.info.release;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author vmurcia
 */
public class ReleaseTokenizerTest {

    private String habemusPapam;
    private String headhunters;
    private String theInsider;
    private String dosMulasyUnaMujer;
    private ReleaseToken[] habemusTokens;
    private ReleaseToken[] habemusConfirmedTokens;

    @Before
    public void setUp() throws Exception {
        habemusPapam = "Habemus.Papam.1080p.DTS.AC3.Dual.Bluray.2011.www.trackerhd.com";
        headhunters = "Headhunters.2011.1080p.MKV.x264.AC3.DTS.Eng.Nor.NL.Subs";
        theInsider = "The Insider 1999 720p HDTVRip H264 AC3-GreatMagician (Kingdom-Release)";
        dosMulasyUnaMujer = "[TRCKHD]Dos.Mulas.y.una.Mujer.m720p.AC3.Dual.Bluray.1970";

        habemusTokens = new ReleaseToken[] {
            new Tag("Habemus", 0),
            new Delimiter(".", 7),
            new Tag("Papam", 8),
            new Delimiter(".", 13),
            new Tag("1080p", 14),
            new Delimiter(".", 19),
            new Tag("DTS", 20),
            new Delimiter(".", 23),
            new Tag("AC3", 24),
            new Delimiter(".", 27),
            new Tag("Dual", 28),
            new Delimiter(".", 32),
            new Tag("Bluray", 33),
            new Delimiter(".", 39),
            new Tag("2011", 40),
            new Delimiter(".", 44),
            new Tag("www", 45),
            new Delimiter(".", 48),
            new Tag("trackerhd", 49),
            new Delimiter(".", 58),
            new Tag("com", 59)};

        habemusConfirmedTokens = new ReleaseToken[] {
            new Tag("Habemus", 0),
            new Delimiter(".", 7),
            new Tag("Papam", 8),
            new Delimiter(".", 13),
            new Tag(TagType.VIDEO_MODE, "1080p", 14),
            new Delimiter(".", 19, true),
            new Tag(TagType.CODEC, "DTS", 20),
            new Delimiter(".", 23, true),
            new Tag(TagType.CODEC, "AC3", 24),
            new Delimiter(".", 27, true),
            new Tag(TagType.EXTRA_TAG, "Dual", 28),
            new Delimiter(".", 32, true),
            new Tag(TagType.VIDEO_SOURCE, "Bluray", 33),
            new Delimiter(".", 39, true),
            new Tag(TagType.YEAR, "2011", 40),
            new Delimiter(".", 44, true),
            new Tag(TagType.WEB_SOURCE, "www.trackerhd.com", 45)};
    }

    /**
     * Test of tokenize method, of class ReleaseTokenizer.
     */
    @Test
    public void testTokenize() {
        System.out.println("Tokenize");
        ReleaseTokenizer.tokenize(habemusPapam);
        assertArrayEquals(habemusTokens, ReleaseTokenizer.getReleaseTokens());
    }

    /**
     * Test of confirmSureTags method, of class ReleaseTokenizer.
     */
    @Test
    public void testConfirmSureTags() {
        System.out.println("Confirm sure tags");
        ReleaseTokenizer.tokenize(habemusPapam);
        ReleaseTokenizer.confirmSureTokens();
        assertArrayEquals(habemusConfirmedTokens, ReleaseTokenizer.getReleaseTokens());
    }
}
