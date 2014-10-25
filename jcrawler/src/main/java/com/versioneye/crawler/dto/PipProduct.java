package com.versioneye.crawler.dto;

import versioneye.domain.Versionarchive;
import versioneye.domain.Versionlink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/16/12
 * Time: 4:00 PM
 *
 */
public class PipProduct {

    private static String LANGUAGE = "Python";
    private static String UNKNOWN = "UNKNOWN";

    private PipProductInfo info;
    private PipProductUrls[] urls;
    private Map<String, ArrayList<PipRelease>> releases = new HashMap<String, ArrayList<PipRelease>>();

    public PipProductInfo getInfo() {
        return info;
    }

    public void setInfo(PipProductInfo info) {
        this.info = info;
    }

    public PipProductUrls[] getUrls() {
        return urls;
    }

    public void setUrls(PipProductUrls[] urls) {
        this.urls = urls;
    }

    public Map<String, ArrayList<PipRelease>> getReleases() {
        return releases;
    }

    public void setReleases(Map<String, ArrayList<PipRelease>> releases) {
        this.releases = releases;
    }

    public List<Versionlink> getLinks(){
        List<Versionlink> links = new ArrayList<Versionlink>();

        String prodKey = info.getName().toLowerCase();
        String docs = info.getDocs_url();
        if (docs != null && !docs.trim().equals("") && !docs.trim().equals( UNKNOWN )){
            links.add( new Versionlink(LANGUAGE, prodKey, "Documentation", info.getDocs_url(), info.getVersion() ) );
        }

        String bugtracker = info.getBugtrack_url();
        if (bugtracker != null && !bugtracker.trim().equals("") && !bugtracker.trim().equals( UNKNOWN )){
            links.add( new Versionlink(LANGUAGE, prodKey, "Bugtracker", info.getBugtrack_url(), info.getVersion() ) );
        }

        String homepage = info.getHome_page();
        if (homepage != null && !homepage.trim().equals("") && !homepage.trim().equals( UNKNOWN )){
            links.add( new Versionlink(LANGUAGE, prodKey, "Homepage", info.getHome_page(), info.getVersion() ) );
        }

        String packageUrl = info.getPackage_url();
        if (packageUrl != null && !packageUrl.trim().equals("") && !packageUrl.trim().equals( UNKNOWN )){
            links.add( new Versionlink(LANGUAGE, prodKey, "Package URL", packageUrl, info.getVersion() ) );
        }

        String releasUrl = info.getRelease_url();
        if (releasUrl != null && !releasUrl.trim().equals("") && !releasUrl.trim().equals( UNKNOWN )){
            links.add( new Versionlink(LANGUAGE, prodKey, "Release URL", releasUrl, info.getVersion() ) );
        }

        return links;
    }

    public List<Versionarchive> getArchives(){
        String prodKey = info.getName().toLowerCase();
        List<Versionarchive> links = new ArrayList<Versionarchive>();

        if (info.getDownload_url() != null && !info.getDownload_url().trim().equals("") && !info.getDownload_url().equals("UNKNOWN")){
            String[] urlParts = info.getDownload_url().split("/");
            String linkName = urlParts[urlParts.length - 1];
            Versionarchive link = new Versionarchive(LANGUAGE, prodKey, linkName, info.getDownload_url());
            link.setVersion_id(info.getVersion());
            links.add(link);
        }

        if (urls != null && urls.length > 0)
            for (PipProductUrls url: urls){
                Versionarchive link = new Versionarchive(LANGUAGE, prodKey, url.getFilename(), url.getUrl());
                link.setVersion_id(info.getVersion());
                links.add(link);
            }
        return links;
    }

    public String getReleaseDate(){
        String release = null;
        if (urls == null || urls.length == 0){
            return release;
        }
        for (PipProductUrls url: urls){
            if (url.getUpload_time() != null){
                release = url.getUpload_time();
                break;
            }
        }
        return release;
    }

}