package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 7/24/12
 * Time: 10:43 AM
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackagistVersion {

    private String name;
    private String version;
    private String homepage;
    private String version_normalized;
    private String[] license;
    private PackagistAuthor[] authors;
    private String time;
    private PackagistDist dist;
    private PackagistRequire require;
    @JsonProperty("require-dev")
    private PackagistRequireDev requireDev;
    private PackagistReplace replace;

    public PackagistRequireDev getRequireDev() {
        return requireDev;
    }

    public void setRequireDev(PackagistRequireDev requireDev) {
        this.requireDev = requireDev;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PackagistDist getDist() {
        return dist;
    }

    public void setDist(PackagistDist dist) {
        this.dist = dist;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public PackagistRequire getRequire() {
        return require;
    }

    public void setRequire(PackagistRequire require) {
        this.require = require;
    }

    public PackagistReplace getReplace() {
        return replace;
    }

    public void setReplace(PackagistReplace replace) {
        this.replace = replace;
    }

    public PackagistAuthor[] getAuthors() {
        return authors;
    }

    public void setAuthors(PackagistAuthor[] authors) {
        this.authors = authors;
    }

    public String[] getLicense() {
        return license;
    }

    public void setLicense(String[] license) {
        this.license = license;
    }

    public String getVersion_normalized() {
        return version_normalized;
    }

    public void setVersion_normalized(String version_normalized) {
        this.version_normalized = version_normalized;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

}
