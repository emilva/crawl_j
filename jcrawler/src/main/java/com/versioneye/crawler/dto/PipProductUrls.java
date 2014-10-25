package com.versioneye.crawler.dto;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/16/12
 * Time: 4:01 PM
 */
public class PipProductUrls {

    private Boolean has_sig;
    private String upload_time;
    private String comment_text;
    private String python_version;
    private String url;
    private String md5_digest;
    private Integer downloads;
    private String filename;
    private String packagetype;
    private Integer size;

    public Boolean getHas_sig() {
        return has_sig;
    }

    public void setHas_sig(Boolean has_sig) {
        this.has_sig = has_sig;
    }

    public String getUpload_time() {
        return upload_time;
    }

    public void setUpload_time(String upload_time) {
        this.upload_time = upload_time;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public String getPython_version() {
        return python_version;
    }

    public void setPython_version(String python_version) {
        this.python_version = python_version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMd5_digest() {
        return md5_digest;
    }

    public void setMd5_digest(String md5_digest) {
        this.md5_digest = md5_digest;
    }

    public Integer getDownloads() {
        return downloads;
    }

    public void setDownloads(Integer downloads) {
        this.downloads = downloads;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPackagetype() {
        return packagetype;
    }

    public void setPackagetype(String packagetype) {
        this.packagetype = packagetype;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
