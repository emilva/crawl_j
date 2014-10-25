package com.versioneye.crawler.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/16/12
 * Time: 4:01 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipProductInfo {

    private String maintainer;
    private String docs_url;
    private String requires_python;
    private String maintainer_email;
    private String cheesecake_code_kwalitee_id;
    private String keywords;
    private String package_url;
    private String author;
    private String author_email;
    private String download_url;
    private String platform;
    private String version;
    private String cheesecake_documentation_id;
    private Boolean _pypi_hidden;
    private String description;
    private String release_url;
    private String _pypi_ordering;
    private String bugtrack_url;
    private String name;
    private String license;
    private String summary;
    private String home_page;
    private String stable_version;
    private String cheesecake_installability_id;
    private String[] classifiers;


    public String[] getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(String[] classifiers) {
        this.classifiers = classifiers;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public String getDocs_url() {
        return docs_url;
    }

    public void setDocs_url(String docs_url) {
        this.docs_url = docs_url;
    }

    public String getRequires_python() {
        return requires_python;
    }

    public void setRequires_python(String requires_python) {
        this.requires_python = requires_python;
    }

    public String getMaintainer_email() {
        return maintainer_email;
    }

    public void setMaintainer_email(String maintainer_email) {
        this.maintainer_email = maintainer_email;
    }

    public String getCheesecake_code_kwalitee_id() {
        return cheesecake_code_kwalitee_id;
    }

    public void setCheesecake_code_kwalitee_id(String cheesecake_code_kwalitee_id) {
        this.cheesecake_code_kwalitee_id = cheesecake_code_kwalitee_id;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getPackage_url() {
        return package_url;
    }

    public void setPackage_url(String package_url) {
        this.package_url = package_url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor_email() {
        return author_email;
    }

    public void setAuthor_email(String author_email) {
        this.author_email = author_email;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCheesecake_documentation_id() {
        return cheesecake_documentation_id;
    }

    public void setCheesecake_documentation_id(String cheesecake_documentation_id) {
        this.cheesecake_documentation_id = cheesecake_documentation_id;
    }

    public Boolean get_pypi_hidden() {
        return _pypi_hidden;
    }

    public void set_pypi_hidden(Boolean _pypi_hidden) {
        this._pypi_hidden = _pypi_hidden;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRelease_url() {
        return release_url;
    }

    public void setRelease_url(String release_url) {
        this.release_url = release_url;
    }

    public String get_pypi_ordering() {
        return _pypi_ordering;
    }

    public void set_pypi_ordering(String _pypi_ordering) {
        this._pypi_ordering = _pypi_ordering;
    }

    public String getBugtrack_url() {
        return bugtrack_url;
    }

    public void setBugtrack_url(String bugtrack_url) {
        this.bugtrack_url = bugtrack_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getHome_page() {
        return home_page;
    }

    public void setHome_page(String home_page) {
        this.home_page = home_page;
    }

    public String getStable_version() {
        return stable_version;
    }

    public void setStable_version(String stable_version) {
        this.stable_version = stable_version;
    }

    public String getCheesecake_installability_id() {
        return cheesecake_installability_id;
    }

    public void setCheesecake_installability_id(String cheesecake_installability_id) {
        this.cheesecake_installability_id = cheesecake_installability_id;
    }
}
