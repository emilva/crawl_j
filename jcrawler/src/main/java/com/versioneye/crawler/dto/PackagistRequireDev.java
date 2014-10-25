package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 7/24/12
 * Time: 2:02 PM
 */
public class PackagistRequireDev {

    private Map<String, String> all = new HashMap<String, String> ();

    @JsonAnySetter
    public void set(String name, String value) {
        all.put(name, value);
    }

    public Map<String, String> getAll() {
        return all;
    }

    public void setAll(Map<String, String> all) {
        this.all = all;
    }

}