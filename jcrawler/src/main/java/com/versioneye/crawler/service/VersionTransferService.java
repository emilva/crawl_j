package com.versioneye.crawler.service;

import com.versioneye.crawler.dto.PipProduct;
import com.versioneye.crawler.dto.RubyGemsVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.versioneye.domain.Version;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/26/13
 * Time: 6:04 PM
 */
public class VersionTransferService {

    static final Logger logger = LogManager.getLogger(VersionTransferService.class.getName());

    public void updateFromGem(String name, RubyGemsVersion gemsVersion, Version version){
        version.setVersion(gemsVersion.getNumber());
        version.setDownloads(gemsVersion.getDownloads_count());
        version.setAuthors(gemsVersion.getAuthors());
        version.setDescription(gemsVersion.getDescription());
        version.setSummary(gemsVersion.getSummary());
        version.setPrerelease(gemsVersion.isPrerelease());
        version.setReleased_string((gemsVersion.getBuilt_at()));
        try {
            String date_string = version.getReleased_string();
            if (date_string == null) {
                logger.info(" - built_at date for " + name + " : " + version.getVersion() + " is null. ");
            } else {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date_string = date_string.replaceAll("T", " ");
                date_string = date_string.replaceAll("Z", "");
                Date date = df.parse(date_string);
                version.setReleased_at(date);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateFromPip(PipProduct pip, Version version){
        version.setVersion(pip.getInfo().getVersion());
        version.setLink(pip.getInfo().getRelease_url());
        if (pip.getUrls() != null && pip.getUrls().length > 0)
            version.setDownloads(pip.getUrls()[0].getDownloads());
        version.setAuthors(pip.getInfo().getAuthor());
        version.setSummary(pip.getInfo().getSummary());
    }

}
