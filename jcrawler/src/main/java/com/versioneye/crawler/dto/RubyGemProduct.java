package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import versioneye.domain.Versionlink;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/15/12
 * Time: 2:48 PM
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RubyGemProduct {

    private static final String LANGUAGE = "Ruby";

    private String name;
    private Integer downloads;
    private String version;
    private String authors;
    private String info;

    private String[] licenses;
    private String license;

    // Links
    private String project_uri;
    private String gem_uri;
    private String homepage_uri;
    private String wiki_uri;
    private String documentation_uri;
    private String mailing_list_uri;
    private String source_code_uri;
    private String bug_tracker_uri;

    // Version
    private Integer version_downloads;

    private RubyGemDependencies dependencies;

    public RubyGemDependencies getDependencies() {
        return dependencies;
    }

    public void setDependencies(RubyGemDependencies dependencies) {
        this.dependencies = dependencies;
    }

    public List<Versionlink> getLinks(){
        List<Versionlink> links = new ArrayList<Versionlink>();

        if (project_uri != null && !project_uri.trim().equals("")){
            links.add( new Versionlink(LANGUAGE, name, "Project", project_uri) );
        }
        if (homepage_uri != null && !homepage_uri.trim().equals("")){
            links.add( new Versionlink(LANGUAGE, name, "Homepage", homepage_uri ) );
        }
        if (wiki_uri != null && !wiki_uri.trim().equals("")){
            links.add( new Versionlink(LANGUAGE, name, "Wiki", wiki_uri ) );
        }
        if (documentation_uri != null && !documentation_uri.trim().equals("")){
            links.add( new Versionlink(LANGUAGE, name, "Documentation", documentation_uri ) );
        }
        if (mailing_list_uri != null && !mailing_list_uri.trim().equals("")){
            links.add( new Versionlink(LANGUAGE, name, "Mailing List", mailing_list_uri ) );
        }
        if (source_code_uri != null && !source_code_uri.trim().equals("")){
            links.add( new Versionlink(LANGUAGE, name, "Source Code", source_code_uri ) );
        }
        if (bug_tracker_uri != null && !bug_tracker_uri.trim().equals("")){
            links.add( new Versionlink(LANGUAGE, name, "Bug Tracker", bug_tracker_uri ) );
        }
        return links;
    }

    public RubyGemsVersion getGemVersion(){
        RubyGemsVersion version = new RubyGemsVersion();
        version.setNumber(this.version);
        version.setDescription(this.info);
        return version;
    }

    public String getName() {
        return name;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setLicenses(String[] licenses) {
        this.licenses = licenses;
    }

    public String[] getLicenses() {
        return licenses;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDownloads() {
        return downloads;
    }

    public void setDownloads(Integer downloads) {
        this.downloads = downloads;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getVersion_downloads() {
        return version_downloads;
    }

    public void setVersion_downloads(Integer version_downloads) {
        this.version_downloads = version_downloads;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getProject_uri() {
        return project_uri;
    }

    public void setProject_uri(String project_uri) {
        this.project_uri = project_uri;
    }

    public String getGem_uri() {
        return gem_uri;
    }

    public void setGem_uri(String gem_uri) {
        this.gem_uri = gem_uri;
    }

    public String getHomepage_uri() {
        return homepage_uri;
    }

    public void setHomepage_uri(String homepage_uri) {
        this.homepage_uri = homepage_uri;
    }

    public String getWiki_uri() {
        return wiki_uri;
    }

    public void setWiki_uri(String wiki_uri) {
        this.wiki_uri = wiki_uri;
    }

    public String getDocumentation_uri() {
        return documentation_uri;
    }

    public void setDocumentation_uri(String documentation_uri) {
        this.documentation_uri = documentation_uri;
    }

    public String getMailing_list_uri() {
        return mailing_list_uri;
    }

    public void setMailing_list_uri(String mailing_list_uri) {
        this.mailing_list_uri = mailing_list_uri;
    }

    public String getSource_code_uri() {
        return source_code_uri;
    }

    public void setSource_code_uri(String source_code_uri) {
        this.source_code_uri = source_code_uri;
    }

    public String getBug_tracker_uri() {
        return bug_tracker_uri;
    }

    public void setBug_tracker_uri(String bug_tracker_uri) {
        this.bug_tracker_uri = bug_tracker_uri;
    }

}
