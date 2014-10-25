package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 7/24/12
 * Time: 10:35 AM
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackagistVersions {

    private Map<String, PackagistVersion> all = new HashMap<String, PackagistVersion>();

    @JsonAnySetter
    public void set(String name, PackagistVersion value) {
        all.put(name, value);
    }

    public Map<String, PackagistVersion> getAll() {
        return all;
    }

    public void setAll(Map<String, PackagistVersion> all) {
        this.all = all;
    }

}
