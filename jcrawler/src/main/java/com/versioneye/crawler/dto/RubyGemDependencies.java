package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/15/12
 * Time: 2:56 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RubyGemDependencies {

    private RubyGemDependency[] development;
    private RubyGemDependency[] runtime;

    public RubyGemDependency[] getDevelopment() {
        return development;
    }

    public void setDevelopment(RubyGemDependency[] development) {
        this.development = development;
    }

    public RubyGemDependency[] getRuntime() {
        return runtime;
    }

    public void setRuntime(RubyGemDependency[] runtime) {
        this.runtime = runtime;
    }
}