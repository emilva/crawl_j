package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 7/24/12
 * Time: 9:28 AM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackagistDetail {

    @JsonProperty(value = "package")
    private PackagistPackage packageIsaReservedWordInJava;

    public PackagistPackage getPackageIsaReservedWordInJava() {
        return packageIsaReservedWordInJava;
    }

    public void setPackageIsaReservedWordInJava(PackagistPackage packageIsaReservedWordInJava) {
        this.packageIsaReservedWordInJava = packageIsaReservedWordInJava;
    }

}
