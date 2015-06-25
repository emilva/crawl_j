package com.versioneye.crawler.crawl;

import com.versioneye.crawler.dto.PipProduct;
import com.versioneye.crawler.dto.PipProductInfo;
import com.versioneye.crawler.service.ProductTransferService;
import com.versioneye.crawler.service.VersionTransferService;
import org.codehaus.jackson.map.ObjectMapper;
import org.htmlcleaner.TagNode;
import versioneye.domain.*;
import versioneye.persistence.IProductDao;
import versioneye.persistence.IVersionarchiveDao;
import versioneye.service.LicenseService;
import versioneye.service.ProductService;
import versioneye.service.VersionLinkService;
import versioneye.utils.HttpUtils;
import versioneye.utils.LogUtils;

import java.io.Reader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/16/12
 * Time: 3:19 PM
 */
public class CrawlerPythonPip extends SuperCrawler implements ICrawl {

    private String crawlerName = "PIP";
    private String crawlerVersion = "0.1";
    private List<Repository> repositories;
    private Repository repository;
    private LogUtils logUtils;
    private HttpUtils httpUtils;
    private ProductService productService;
    private VersionLinkService versionLinkService;
    private ProductTransferService productTransferService;
    private VersionTransferService versionTransferService;
    private LicenseService licenseService;
    private Crawle crawle;
    private String execGroup;
    private IVersionarchiveDao versionarchiveDao;
    private IProductDao productDao;
    private boolean threadable = false;

    public void run() {
        crawl();
    }

    public void crawl() {
        Date start = new Date();
        logUtils.logStart(start, crawlerName, getRepository().getSrc());

        Set<String> pipNames = getFirstLevelList();
        for (String pipName : pipNames){
            crawlePackage(pipName);
        }

        logUtils.logStop(start, crawlerName, getRepository().getSrc());
    }

    public Set<String> getFirstLevelList() {
        Set<String> pipNames = new HashSet<String>();
        try{
            String resource = "https://pypi.python.org/pypi?%3Aaction=index";
            String xpath = "//table[@class=\"list\"]/tbody/tr/td/a";
            Object[] objects = httpUtils.getObjectsFromPage(resource, xpath);
            for (Object obj : objects){
                TagNode node = (TagNode) obj;
                String href = node.getAttributeByName("href");
                String name = href.replaceFirst("/pypi/", "");
                name = name.replaceFirst("/.*", "");
                pipNames.add(name);
            }
        } catch (Exception ex) {
            logUtils.addError("ERROR in CrawlerPythonPip.getFirstLevelList()", ex.toString(), crawle);
        }
        return pipNames;
    }

    public void crawlePackage(String pipName) {
        try{
            String resource = "https://pypi.python.org/pypi/" + encodeURI(pipName) + "/json";
            Reader reader = httpUtils.getResultReader(resource);
            ObjectMapper mapper = new ObjectMapper();
            PipProduct pip = mapper.readValue(reader, PipProduct.class);
            reader.close();

            Set<String> versions = pip.getReleases().keySet();
            for (String version_string : versions){
                if (productDao.doesVersionExistAlready("Python", pipName.toLowerCase(), version_string))
                    continue;
                crawlePackageVersion(pipName, version_string);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logUtils.addError("ERROR in CrawlerPython.crawlePackage(" + pipName + ")", ex.toString(), crawle);
        }
    }

    private void crawlePackageVersion(String pipName, String version){
        try{
            String resource = "https://pypi.python.org/pypi/" + encodeURI(pipName) + "/"+ encodeURI(version) +"/json";
            Reader reader = httpUtils.getResultReader(resource);
            ObjectMapper mapper = new ObjectMapper();
            PipProduct pip = mapper.readValue(reader, PipProduct.class);
            reader.close();

            Product product = new Product();
            productTransferService.updateFromPip(pip, product);
            productService.createProductIfNotExist(product, getRepository());

            List<Versionlink> links = pip.getLinks();
            for (Versionlink link: links)
                versionLinkService.convertAndPersistIfNotExist(product.getProd_key(), link);

            List<Versionarchive> archives = pip.getArchives();
            for (Versionarchive archive: archives)
                if (!versionarchiveDao.doesLinkExistArleady(product.getLanguage(), archive.getProduct_key(), archive.getVersion_id(), archive.getLink()))
                    versionarchiveDao.create(archive);

            addVersionIfNotExist(product, pip);
            createLicenses( product, pip );
        } catch (Exception ex) {
            ex.printStackTrace();
            logUtils.addError("ERROR in CrawlerPython.crawlePackageVersion(" + pipName + ", "+ version +")", ex.toString(), crawle);
        }
    }

    private void addVersionIfNotExist(Product product, PipProduct pip){
        Version version = new Version();
        versionTransferService.updateFromPip(pip, version);
        product.setVersion(version.getVersion());
        version.setProduct_key(product.getProd_key());
        version.setType("PIP");
        version.setLanguage(product.getLanguage());
        version.setReleased_string(pip.getReleaseDate());
        boolean dateParsed = productService.parseDate(version);
        if (dateParsed == false){
            System.out.println("Not able to parse Date for " + pip.getInfo().getName() + " version " + version.getVersion());
        }
        boolean created = productService.createVersionIfNotExist(product, version, null);
        if (!created){
            productDao.updateVersionReleaseTime(version);
        }
    }

    public void createLicenses(Product product, PipProduct pip ){
        PipProductInfo info = pip.getInfo();
        licenseService.createLicenseIfNotExist(product, info.getLicense(), null, null, null);

        String[] classifiers = info.getClassifiers();
        if (classifiers == null || classifiers.length == 0){
            return ;
        }

        for (String classification : classifiers){
            if (!classification.startsWith("License")){
                continue;
            }
            String[] sps = classification.split("::");
            for (String lic : sps){
                String license = lic.trim();
                if (license.equalsIgnoreCase("License") || license.equalsIgnoreCase("OSI Approved")){
                    continue;
                }
                licenseService.createLicenseIfNotExist(product, license, null, null, null);
            }
        }
    }

    public Crawle getCrawle() {
        return crawle;
    }

    public String getName() {
        return crawlerName;
    }

    public String getCrawlerVersion() {
        return crawlerVersion;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        if (repository == null && repositories != null && !repositories.isEmpty()){
            repository = repositories.get(0);
        }
        this.repositories = repositories;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public String getExecGroup() {
        return execGroup;
    }

    public void setExecGroup(String execGroup) {
        this.execGroup = execGroup;
    }

    public void setVersionarchiveDao(IVersionarchiveDao versionarchiveDao) {
        this.versionarchiveDao = versionarchiveDao;
    }

    public void setStartPoint(String startPoint) {}
    public String getStartPoint(){return "";}

    public boolean isThreadable() {
        return threadable;
    }

    public void setThreadable(boolean threadable) {
        this.threadable = threadable;
    }

    public void setProductDao(IProductDao productDao) {
        this.productDao = productDao;
    }

    public void setLogUtils(LogUtils logUtils) {
        this.logUtils = logUtils;
    }

    public void setHttpUtils(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public void setVersionLinkService(VersionLinkService versionLinkService) {
        this.versionLinkService = versionLinkService;
    }

    public void setProductTransferService(ProductTransferService productTransferService) {
        this.productTransferService = productTransferService;
    }

    public void setVersionTransferService(VersionTransferService versionTransferService) {
        this.versionTransferService = versionTransferService;
    }

    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }
}
