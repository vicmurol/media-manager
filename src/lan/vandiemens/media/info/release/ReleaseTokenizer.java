package lan.vandiemens.media.info.release;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lan.vandiemens.media.cataloguer.ReleaseInfoParser;
import lan.vandiemens.media.analysis.Codec;
import lan.vandiemens.media.analysis.VideoMode;
import lan.vandiemens.media.analysis.VideoSource;
import lan.vandiemens.util.lang.Language;
import org.apache.commons.validator.routines.DomainValidator;




/**
 *
 * @author vmurcia
 */
public class ReleaseTokenizer {

    private static final String delimiterRegex = "[ \\.\\-_\\(\\)\\[\\]]+";
    private static final String nonDelimiterRegex = "[^ \\.\\-_\\(\\)\\[\\]]+";
    private static final String movieYearRegex = "^(?:19|20)\\d\\d$";
    private static final String[] bluraySceneGroups = {"AMIABLE, BLOW"};
    private static final String[] hditunesSceneGroups = {"CtrlHD", "ECI"};
    private static final String[] hdtvSceneGroups = {"Orenji", "IMMERSE", "DIMENSION"};
    private static final String[] sdtvSceneGroups = {"ASAP", "FQM", "LOL", "P0W4", "mSD"};
    private static final String[] webSourceIds = {"TRCKHD", "PublicHD", "eztv"};
    private static final String[] extraInfoFields = {"Dual", "MKV", "Dubbed", "Subs", "Subtitled"};
    private static ArrayList<ReleaseToken> releaseTokens = null;
    private static String releaseName;
    private static int safeZoneStartIndex;

    public static ReleaseInfo process(String filename) {
        tokenize(filename);
        confirmSureTokens();
        parseRemainingCandidates();
        return null;
    }

    /**
     * NOTE: For JUnit testing purposes.
     *
     * @return the release tokens after the processing previously done
     */
    static ReleaseToken[] getReleaseTokens() {
        return releaseTokens.toArray(new ReleaseToken[releaseTokens.size()]);
    }

    static void tokenize(String filename) {
        releaseName = filename;
        releaseTokens = new ArrayList<> ();
        addCandidateTags();
        addCandidateDelimiters();
        safeZoneStartIndex = releaseTokens.size();
    }

    private static void addCandidateTags() {
        Matcher matcher = Pattern.compile(nonDelimiterRegex).matcher(releaseName);
        boolean needsOrdering = !releaseTokens.isEmpty();
        while (matcher.find()) {
            releaseTokens.add(new Tag(matcher.group(), matcher.start()));
        }
        if (needsOrdering) {
            Collections.sort(releaseTokens);
        }
    }

    private static void addCandidateDelimiters() {
        Matcher matcher = Pattern.compile(delimiterRegex).matcher(releaseName);
        boolean needsOrdering = !releaseTokens.isEmpty();
        while (matcher.find()) {
            releaseTokens.add(new Delimiter(matcher.group(), matcher.start()));
        }
        if (needsOrdering) {
            Collections.sort(releaseTokens);
        }
    }

    /**
     * Sets tokens which can be unmistakably identified as final.
     */
    static void confirmSureTokens() {
        confirmSureTags();
        confirmSureDelimiters();
    }

    /**
     * Sets delimiters surrounded by confirmed tags as final.
     * <p>
     * Pre-condition: tags and delimiters are interspersed.
     */
    private static void confirmSureDelimiters() {
        if (releaseTokens.size() < 3) {
            return;
        }

        // Special case: first token
        ReleaseToken first = releaseTokens.get(0);
        ReleaseToken second = releaseTokens.get(1);
        if (first.isCandidateDelimiter() && second.isFinalTag()) {
            first.confirm();
        }

        // Bulk of the tokens
        ReleaseToken token;
        ReleaseToken previous;
        ReleaseToken next;
        for (int i = 1; i < releaseTokens.size() - 1; i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateDelimiter()) {
                previous = releaseTokens.get(i - 1);
                next = releaseTokens.get(i + 1);
                if (previous.isFinalTag() && next.isFinalTag()) {
                    token.confirm();
                }
                // Jump the next token, as it is a tag
                i++;
            }
        }

        // Special case: last token
        ReleaseToken last = releaseTokens.get(releaseTokens.size() - 1);
        previous = releaseTokens.get(releaseTokens.size() - 2);
        if (last.isCandidateDelimiter() && previous.isFinalTag()) {
            last.confirm();
        }
    }

    /**
     * Sets candidate tags as definitive if they are clearly identifiable tags.
     */
    private static void confirmSureTags() {
        // The confirmation order is important!
        confirmVideoModeTags();
        confirmVideoSourceTags();
        confirmCodecTags();
        confirmSceneGroupTags();
        confirmWebSourceTags();
        confirmTrackerIdTags(); // Must go after web source confirmation to avoid overlapping
        confirmReleaseTypeTags();
        confirmExtraTags();
        confirmUploaderTags();
        confirmYearTags();
        confirmLanguageTags();
        confirmTvSeriesTags();
    }

    /**
     * Sets identifiable video mode tags as final.
     * <p>
     * Typical values: 1080p, 720p, 480i, etc.
     * <p>
     * Post-condition: Safe zone for tag identification is updated.
     */
    private static void confirmVideoModeTags() {
        ReleaseToken token;
        for (int i = 0; i < releaseTokens.size(); i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && VideoMode.parse(token.getValue()) != VideoMode.UNKNOWN) {
                Tag tag = (Tag)token;
                tag.setTagType(TagType.VIDEO_MODE);
                safeZoneStartIndex = Math.min(safeZoneStartIndex, i);
            }
        }
    }

    /**
     * Sets identifiable video source tags as final.
     * <p>
     * Typical values: BluRay, Web-DL, DVDRip, HDTVRip, etc.
     * <p>
     * Post-condition: Safe zone for tag identification is updated.
     */
    private static void confirmVideoSourceTags() {
        ReleaseToken token;
        for (int i = 0; i < releaseTokens.size(); i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && VideoSource.parse(token.getValue()) != VideoSource.UNKNOWN) {
                Tag tag = (Tag)token;
                tag.setTagType(TagType.VIDEO_SOURCE);
                safeZoneStartIndex = Math.min(safeZoneStartIndex, i);
            }
        }
    }

    /**
     * Sets identifiable codec tags as final.
     * <p>
     * Typical values: AC3, DTS, XviD, H.264, etc.
     * <p>
     * Post-condition: Safe zone for tag identification is updated.
     */
    private static void confirmCodecTags() {
        confirmSimpleCodecTags();
        confirmCompoundCodecTags();
    }

    /**
     * Sets identifiable single-token codec tags as final.
     * <p>
     * Typical values: AC3, DTS, XviD, x264, etc.
     * <p>
     * Post-condition: Safe zone for tag identification is updated.
     */
    private static void confirmSimpleCodecTags() {
        ReleaseToken token;
        for (int i = 0; i < releaseTokens.size(); i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && isKnownCodec(token.getValue())) {
                Tag tag = (Tag)token;
                tag.setTagType(TagType.CODEC);
                safeZoneStartIndex = Math.min(safeZoneStartIndex, i);
            }
        }
    }

    /**
     * Sets identifiable multi-token codec tags as final.
     * <p>
     * Typical values: AAC2.0, DD5.1, H.264, etc.
     * <p>
     * Post-condition: Safe zone for tag identification is updated.
     */
    private static void confirmCompoundCodecTags() {
        ReleaseToken token;
        Delimiter nextDelimiter;
        Tag nextTag;
        for (int i = 0; i < releaseTokens.size() - 2; i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && isPartialCodecName(token.getValue())) {
                nextDelimiter = (Delimiter) releaseTokens.get(i + 1);
                if (nextDelimiter.isCandidate() && nextDelimiter.hasValue(".")) {
                    nextTag = (Tag) releaseTokens.get(i + 2);
                    String potentialCodec = token.getValue() + nextDelimiter.getValue() + nextTag.getValue();
                    if (nextTag.isCandidate() && isKnownCodec(potentialCodec)) {
                        replaceWithComposition(i, i + 2, TagType.CODEC);
                        safeZoneStartIndex = Math.min(safeZoneStartIndex, i);
                    }
                }
            }
        }
    }

    /**
     * Sets identifiable scene group tags as final.
     * <p>
     * Typical values: CtrlHD, Orenji, IMMERSE, AMIABLE, etc.
     */
    private static void confirmSceneGroupTags() {
        confirmKnownSceneGroupTags();
        confirmSpecialSceneGroupTags();
        confirmGenericSceneGroupTags();
    }

    /**
     * Sets known scene group tags as final.
     * <p>
     * Typical values: CtrlHD, Orenji, IMMERSION, AMIABLE, etc.
     */
    private static void confirmKnownSceneGroupTags() {
        ReleaseToken token;
        for (int i = safeZoneStartIndex; i < releaseTokens.size(); i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && isKnownSceneGroup(token.getValue())) {
                Tag tag = (Tag) token;
                tag.setTagType(TagType.SCENE_GROUP);
            }
        }
    }

    /**
     * Sets specific scene group tags whose uncommon position is known as final.
     * <p>
     * Special scene groups: aaF.
     */
    private static void confirmSpecialSceneGroupTags() {
        ReleaseToken first = releaseTokens.get(0);
        ReleaseToken second = releaseTokens.get(1);
        if (first.hasValue("aaF") && second.hasValue("-")) {
            Tag tag = (Tag) first;
            tag.setTagType(TagType.SCENE_GROUP);
            second.confirm();
        }
    }

    /**
     * Sets generic scene group tags as final.
     * <p>
     * NOTE: Generic scene groups are identified as tags following the pattern
     * <i>last_codec-scene_group</i> in the release token sequence.
     * <br>Ex. x264-BLOW
     */
    private static void confirmGenericSceneGroupTags() {
        ReleaseToken token;
        Delimiter nextDelimiter;
        Tag nextTag;
        for (int i = safeZoneStartIndex; i < releaseTokens.size() - 2; i++) {
            token = releaseTokens.get(i);
            if (token.isFinalTag() && ((Tag)token).getTagType() == TagType.CODEC) {
                nextDelimiter = (Delimiter) releaseTokens.get(i + 1);
                if (nextDelimiter.hasValue("-")) {
                    nextTag = (Tag) releaseTokens.get(i + 2);
                    if (nextTag.isCandidate()) {
                        nextTag.setTagType(TagType.SCENE_GROUP);
                    }
                }
            }
        }
    }

    /**
     * Sets identifiable uploader tags as final.
     * <p>
     * Post-condition: the number of tokens is reduced as several tags and
     * delimiters are merged in a resulting uploader tag.
     * <p>
     * Ex: by.Nestai, by_SemperFi, etc.
     */
    private static void confirmUploaderTags() {
        ReleaseToken token;
        Delimiter nextDelimiter;
        Tag nextTag;
        for (int i = safeZoneStartIndex; i < releaseTokens.size() - 2; i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && token.hasValue("by")) {
                nextDelimiter = (Delimiter) releaseTokens.get(i + 1);
                if (nextDelimiter.hasValue(".") || nextDelimiter.hasValue("_") || nextDelimiter.hasValue(" ")) {
                    nextTag = (Tag) releaseTokens.get(i + 2);
                    if (nextTag.isCandidate()) {
                        replaceWithComposition(i, i + 2, TagType.UPLOADER);
                    }
                }
            }
        }
    }

    /**
     * Sets identifiable web domain tags as final.
     * <p>
     * Post-condition: the number of tokens is reduced as several tags and
     * delimiters are merged in a resulting web source tag.
     * <p>
     * Ex: www.trackerhd.com
     */
    private static void confirmWebSourceTags() {
        boolean isCaptureEnabled = false;
        int startCaptureIndex = 0;
        for (int i = 0; i < releaseTokens.size(); i++) {
            ReleaseToken token = releaseTokens.get(i);
            if (isCaptureEnabled) {
                if (token.isCandidate()) {
                    if (containsWebDomain(startCaptureIndex, i)) {
                        replaceWithComposition(startCaptureIndex, i, TagType.WEB_SOURCE);
                        isCaptureEnabled = false;
                    }
                } else { // Captured tokens don't make a valid web source
                    isCaptureEnabled = false;
                }
            } else { // Start web source capture
                if (token.isCandidateTag() && token.getValue().equalsIgnoreCase("www")) {
                    startCaptureIndex = i;
                    isCaptureEnabled = true;
                }
            }
        }
    }

    private static boolean containsWebDomain(int startIndex, int endIndex) {
        String candidateDomain = "";
        for (int i = startIndex; i <= endIndex; i++) {
            candidateDomain += releaseTokens.get(i).getValue();
        }
        DomainValidator validator = DomainValidator.getInstance();
        return validator.isValid(candidateDomain);
    }

    /**
     * Sets identifiable tracker ID tags as final.
     * <p>
     * NOTE: Tracker IDs represent web source domains.
     * <p>
     * Ex. PublicHD, eztv, etc.
     */
    private static void confirmTrackerIdTags() {
        for (ReleaseToken token : releaseTokens) {
            if (token.isCandidateTag() && isTrackerId(token.getValue())) {
                Tag tag = (Tag)token;
                tag.setTagType(TagType.WEB_SOURCE);
            }
        }
    }

    /**
     * Sets identifiable release type tags as final.
     * <p>
     * NOTE: the tags must be inside the safe zone in order to be unmistakably
     * identified.
     * <p>
     * Typical values: proper, unrated, director's cut, extended edition, etc.
     */
    private static void confirmReleaseTypeTags() {
        ReleaseToken token;
        for (int i = safeZoneStartIndex; i < releaseTokens.size(); i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && isReleaseType(token.getValue())) {
                Tag tag = (Tag)token;
                tag.setTagType(TagType.RELEASE_TYPE);
            }
        }
    }

    /**
     * Sets identifiable extra tags as final.
     * <p>
     * NOTE: the tags must be inside the safe zone in order to be unmistakably
     * identified.
     * <p>
     * Typical values: dual, subs, etc.
     */
    private static void confirmExtraTags() {
        ReleaseToken token;
        for (int i = safeZoneStartIndex; i < releaseTokens.size(); i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && isExtraTag(token.getValue())) {
                Tag tag = (Tag)token;
                tag.setTagType(TagType.EXTRA_TAG);
            }
        }
    }

    /**
     * Sets identifiable year tags as final.
     * <p>
     * NOTE: the tags must be inside the safe zone in order to be unmistakably
     * identified. Also, only years from 1900 are valid.
     * <p>
     * Typical values: 1987, 1995, 2011, etc.
     */
    private static void confirmYearTags() {
        ReleaseToken token;
        for (int i = safeZoneStartIndex; i < releaseTokens.size(); i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && isValidMovieYear(token.getValue())) {
                Tag tag = (Tag)token;
                tag.setTagType(TagType.YEAR);
            }
        }
    }

    /**
     * Sets identifiable language tags as final.
     * <p>
     * NOTE: the tags must be inside the safe zone in order to be unmistakably
     * identified.
     * <p>
     * Typical values: English, es, de, nor, Italian, etc.
     */
    private static void confirmLanguageTags() {
        ReleaseToken token;
        for (int i = safeZoneStartIndex; i < releaseTokens.size(); i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && Language.parseLanguage(token.getValue()) != Language.UNDEFINED) {
                Tag tag = (Tag)token;
                tag.setTagType(TagType.LANGUAGE);
            }
        }
    }

    /**
     * Sets identifiable TV Series related tags as final.
     * <p>
     * NOTE: Looks for season and episode number tag.
     * <p>
     * Typical values: S02E12, 5x08, etc.
     */
    private static void confirmTvSeriesTags() {
        ReleaseToken token;
        for (int i = 0; i < releaseTokens.size(); i++) {
            token = releaseTokens.get(i);
            if (token.isCandidateTag() && isFullTvEpisodeNumber(token.getValue())) {
                Tag tag = (Tag)token;
                tag.setTagType(TagType.EPISODE_NUMBER);
                if (safeZoneStartIndex == i + 2) {
                    safeZoneStartIndex = i;
                }
            }
        }
    }

    private static boolean isFullTvEpisodeNumber(String value) {
        Matcher matcher = ReleaseInfoParser.basicTvSeriesPattern.matcher(value);
        return matcher.find();
    }

    private static boolean isValidMovieYear(String value) {
        Matcher matcher = Pattern.compile(movieYearRegex).matcher(value);
        return matcher.find();
    }

    private static boolean isKnownSceneGroup(String value) {
        for (String group : bluraySceneGroups) {
            if (group.equalsIgnoreCase(value)) {
                return true;
            }
        }
        for (String group : hditunesSceneGroups) {
            if (group.equalsIgnoreCase(value)) {
                return true;
            }
        }
        for (String group : hdtvSceneGroups) {
            if (group.equalsIgnoreCase(value)) {
                return true;
            }
        }
        for (String group : sdtvSceneGroups) {
            if (group.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isReleaseType(String value) {
        return ReleaseType.parse(value) != ReleaseType.UNDEFINED;
    }

    private static boolean isPartialCodecName(String value) {
        return Codec.isPartialName(value);
    }

    private static boolean isKnownCodec(String value) {
        return Codec.parse(value) != Codec.UNDEFINED;
    }

    private static boolean isTrackerId(String value) {
        for (String id : webSourceIds) {
            if (id.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isExtraTag(String value) {
        for (String extra : extraInfoFields) {
            if (extra.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Post-condition: release tokens are in order after replacement.
     *
     * @param startIndex the index of the first tag to be merged and replaced
     * @param endIndex the index of the last tag to be merged and replaced
     * @param tagType the tag type of the resulting tag
     */
    private static void replaceWithComposition(int startIndex, int endIndex, TagType tagType) {
        int startPosition = releaseTokens.get(startIndex).getStartPosition();
        String webSource = "";
        for (int i = startIndex; i <= endIndex; i++) {
            webSource += releaseTokens.get(i).getValue();
        }
        // Backwards removal for better performance
        for (int i = endIndex; i >= startIndex; i--) {
            releaseTokens.remove(i);
        }
        releaseTokens.add(new Tag(tagType, webSource, startPosition));
        Collections.sort(releaseTokens);
    }

    private static void parseRemainingCandidates() {

    }

    /**
     * Checks if the delimiter used to separate release tokens is unique.
     *
     * @return <code>true</code> if the delimiter used as token separator is
     *         unique, <code>false</code> if there are different delimiters or
     *         there is not delimiter at all.
     */
    private static boolean hasUniqueDelimiter() {
        String delimiter = null;
        for (ReleaseToken token : releaseTokens) {
            if (token.isDelimiter() && delimiter != null) {
                if (!delimiter.equals(token.getValue())) {
                    return false;
                }
            } else if (delimiter == null) {
                delimiter = token.getValue();
            }
        }

        return delimiter == null ? false : true;
    }

    /**
     * Checks if every tag and delimiter inside the safe zone is confirmed.
     *
     * @return <code>true</code> if all the tags and delimiters inside the safe
     * zone are final, <code>false</> if there is still some candidate token or
     * there is no safe zone.
     */
    private static boolean isSafeZoneFullyIdentified() {
        for (int i = safeZoneStartIndex; i < releaseTokens.size(); i++) {
            if (releaseTokens.get(i).isCandidate()) {
                return false;
            }
        }
        // No safe zone
        if (safeZoneStartIndex >= releaseTokens.size()) {
            return false;
        }
        return true;
    }
}

class ReleaseToken implements Comparable<ReleaseToken> {

    private final TokenType type;
    private final int startPosition;
    private String value = null;
    private boolean isCandidate = true;

    public ReleaseToken(TokenType type, String value, int startPosition) {
        this.type = type;
        this.value = value;
        this.startPosition = startPosition;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean hasValue(String value) {
        return value != null && this.value.equals(value);
    }

    public int getStartPosition() {
        return startPosition;
    }

    public boolean isCandidate() {
        return isCandidate;
    }

    public boolean isTag() {
        return type == TokenType.TAG;
    }

    public boolean isFinalTag() {
        return type == TokenType.TAG && !isCandidate;
    }

    public boolean isCandidateTag() {
        return type == TokenType.TAG && isCandidate;
    }

    public boolean isDelimiter() {
        return type == TokenType.DELIMITER;
    }

    public boolean isFinalDelimiter() {
        return type == TokenType.DELIMITER && !isCandidate;
    }

    public boolean isCandidateDelimiter() {
        return type == TokenType.DELIMITER && isCandidate;
    }

    public void confirm() {
        isCandidate = false;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ReleaseToken &&
                ((ReleaseToken) object).getStartPosition() == startPosition &&
                ((ReleaseToken) object).getType() == type &&
                ((ReleaseToken) object).isCandidate() == isCandidate;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 17 * hash + this.startPosition;
        hash = 17 * hash + Objects.hashCode(this.value);
        hash = 17 * hash + (this.isCandidate ? 1 : 0);
        return hash;
    }

    @Override
    public int compareTo(ReleaseToken token) {
        if (startPosition > token.getStartPosition()) {
            return 1;
        } else if (startPosition < token.getStartPosition()){
            return -1;
        } else {
            if (token.getType() == type && token.isCandidate() == isCandidate) {
                return 0;
            } else {
                return -1; // TODO Don't allow position overlapping
            }
        }
    }
}

enum TokenType {
    TAG, DELIMITER;
}

class Tag extends ReleaseToken {

    private TagType tagType;

    public Tag(TagType type, String value, int startPosition) {
        super(TokenType.TAG, value, startPosition);
        tagType = type;
        if (tagType != TagType.UNKNOWN) {
            confirm();
        }
    }

    public Tag(String value, int startPosition) {
        this(TagType.UNKNOWN, value, startPosition);
    }

    public TagType getTagType() {
        return tagType;
    }

    /**
     * Sets the type of this tag and makes it final.
     *
     * @param type the type to be set
     */
    public void setTagType(TagType type) {
        tagType = type;
        confirm();
    }

    @Override
    public String toString() {
        return getType() + " [" + tagType + "]=" + getValue();
    }
}

enum TagType {
    TITLE, SEASON, EPISODE_TITLE, EPISODE_NUMBER, YEAR, CODEC, VIDEO_SOURCE,
    VIDEO_MODE, RELEASE_TYPE, EXTRA_TAG, SCENE_GROUP, RIPPER, UPLOADER,
    WEB_SOURCE, LANGUAGE, UNKNOWN;
}

class Delimiter extends ReleaseToken {

    public Delimiter(String value, int startPosition) {
        super(TokenType.DELIMITER, value, startPosition);
    }

    public Delimiter(String value, int startPosition, boolean definitive) {
        this(value, startPosition);
        if (definitive) {
            confirm();
        }
    }

    @Override
    public String toString() {
        return getType() + (isCandidate() ? "?=" : "=") + getValue();
    }
}
