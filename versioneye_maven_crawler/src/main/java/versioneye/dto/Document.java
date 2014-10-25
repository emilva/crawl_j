package versioneye.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:43 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {

    private String id;
    private String path;
    private String g;
    private String a;
    private String v;
    private String p;
    private Integer timestamp;
    private String name;
    private String type;
    private String lastModified;
    private String repositoryId;
    private Long fileSize;
    private String parentId;
    private String sha1;


    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getSha1() {
        return sha1;
    }
    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getLastModified() {
        return lastModified;
    }
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    public String getRepositoryId() {
        return repositoryId;
    }
    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

}
