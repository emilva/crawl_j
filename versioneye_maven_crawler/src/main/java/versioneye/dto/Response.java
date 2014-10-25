package versioneye.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:42 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    private Integer numFound;
    private Integer start;
    private Document[] docs;

    public Integer getNumFound() {
        return numFound;
    }
    public void setNumFound(Integer numFound) {
        this.numFound = numFound;
    }
    public Integer getStart() {
        return start;
    }
    public void setStart(Integer start) {
        this.start = start;
    }
    public Document[] getDocs() {
        return docs;
    }
    public void setDocs(Document[] docs) {
        this.docs = docs;
    }

}
