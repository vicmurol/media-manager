package lan.vandiemens.media.cataloguer;

/**
 * @author Victor
 */
public class MediaTag {

    private String referenceName = null;

    private String[] alternativeNames = null;

    public MediaTag(String name, String[] alternatives) {
        referenceName = name;
        alternativeNames = alternatives;
    }

    public String[] getAlternativeNames() {
        return alternativeNames;
    }

    public String getReferenceName() {
        return referenceName;
    }
}
