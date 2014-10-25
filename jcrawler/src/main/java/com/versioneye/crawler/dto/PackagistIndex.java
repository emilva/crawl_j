package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 7/23/12
 * Time: 11:52 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackagistIndex {

    private String[] packageNames;

    public String[] getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(String[] packageNames) {
        this.packageNames = packageNames;
    }
}
