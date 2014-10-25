package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 4/11/12
 * Time: 3:09 PM
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubTagVersion {

    private String zipball_url;
    private String tarball_url;
    private String name;

    public String getZipball_url() {
        return zipball_url;
    }

    public void setZipball_url(String zipball_url) {
        this.zipball_url = zipball_url;
    }

    public String getTarball_url() {
        return tarball_url;
    }

    public void setTarball_url(String tarball_url) {
        this.tarball_url = tarball_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}