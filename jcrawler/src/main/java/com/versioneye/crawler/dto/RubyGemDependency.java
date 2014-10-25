package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/15/12
 * Time: 2:57 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RubyGemDependency {

    private String name;
    private String requirements;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
}
