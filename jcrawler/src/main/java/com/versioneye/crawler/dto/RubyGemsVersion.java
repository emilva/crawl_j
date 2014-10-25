package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/15/12
 * Time: 3:40 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RubyGemsVersion {

    private String authors;
    private String built_at;
    private String description;
    private Integer downloads_count;
    private String number;
    private String summary;
    private String platform;
    private boolean prerelease;

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getBuilt_at() {
        return built_at;
    }

    public void setBuilt_at(String built_at) {
        this.built_at = built_at;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDownloads_count() {
        return downloads_count;
    }

    public void setDownloads_count(Integer downloads_count) {
        this.downloads_count = downloads_count;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isPrerelease() {
        return prerelease;
    }

    public void setPrerelease(boolean prerelease) {
        this.prerelease = prerelease;
    }
}
