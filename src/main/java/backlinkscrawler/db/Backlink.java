package backlinkscrawler.db;

public class Backlink {
    private String sourceUrl;
    private String targetUrl;
    private String anchorText;
    private Boolean isDoFollow;

    public Backlink(String sourceUrl,String targetUrl,String anchorText,Boolean isDoFollow){
        this.anchorText = anchorText;
        this.sourceUrl = sourceUrl;
        this.targetUrl = targetUrl;
        this.isDoFollow =isDoFollow;
    }

    public Boolean getDoFollow() {
        return this.isDoFollow;
    }

    public String getAnchorText() {
        return this.anchorText;
    }

    public String getSourceUrl() {
        return this.sourceUrl;
    }

    public String getTargetUrl() {
        return this.targetUrl;
    }

    public void setAnchorText(String anchorText) {
        this.anchorText = anchorText;
    }

    public void setDoFollow(Boolean doFollow) {
        this.isDoFollow = doFollow;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
