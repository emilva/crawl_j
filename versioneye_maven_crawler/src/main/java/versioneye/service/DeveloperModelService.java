package versioneye.service;

import org.apache.maven.model.Model;
import com.versioneye.domain.Developer;
import com.versioneye.domain.Product;
import com.versioneye.persistence.IDeveloperDao;
import com.versioneye.utils.LogUtils;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 8/11/13
 * Time: 10:55 AM
 */
public class DeveloperModelService {

    private IDeveloperDao developerDao;
    private LogUtils logUtils;

    public void createDevelopersIfNotExist(Model model, Product product){
        try{
            for (org.apache.maven.model.Developer dev : model.getDevelopers()){
                String devId = dev.getId();
                String name = dev.getName();
                String email = dev.getEmail();
                String organization = dev.getOrganization();
                String organizationUrl = dev.getOrganizationUrl();
                String timezone = dev.getTimezone();
                String role = "";
                if (dev.getRoles() != null && !dev.getRoles().isEmpty()){
                    role = dev.getRoles().get(0);
                }

                Developer developer = new Developer(product.getLanguage(), product.getProd_key(), product.getVersion(), devId, name, email,
                        dev.getUrl(), role, organization, organizationUrl, timezone);

                if (!developerDao.doesExistAlready(product.getLanguage(), product.getProd_key(), product.getVersion(), name))
                    developerDao.create(developer);
            }
        } catch (Exception ex) {
            logUtils.addError("CrawlerUtils.createDevelopersIfNotExist ", ex.getMessage(), null);
        }
    }

    public void setDeveloperDao(IDeveloperDao developerDao) {
        this.developerDao = developerDao;
    }

    public void setLogUtils(LogUtils logUtils) {
        this.logUtils = logUtils;
    }

}
