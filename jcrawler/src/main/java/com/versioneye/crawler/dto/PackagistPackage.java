package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 7/24/12
 * Time: 9:29 AM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackagistPackage {

    private String name;
    private String description;
    private PackagistVersions versions;

    public PackagistVersions getVersions() {
        return versions;
    }

    public void setVersions(PackagistVersions versions) {
        this.versions = versions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
