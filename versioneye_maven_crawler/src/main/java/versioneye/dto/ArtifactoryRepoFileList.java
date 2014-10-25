package versioneye.dto;


public class ArtifactoryRepoFileList {

    private String uri;
    private String created;
    private ArtifactoryFile[] files;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public ArtifactoryFile[] getFiles() {
        return files;
    }

    public void setFiles(ArtifactoryFile[] files) {
        this.files = files;
    }

}
