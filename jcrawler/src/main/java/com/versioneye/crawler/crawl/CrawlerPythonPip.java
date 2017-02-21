package com.versioneye.crawler.crawl;

import com.versioneye.crawler.dto.PipProduct;
import com.versioneye.crawler.dto.PipProductInfo;
import com.versioneye.crawler.dto.PipRelease;
import com.versioneye.crawler.service.ProductTransferService;
import com.versioneye.crawler.service.VersionTransferService;
import com.versioneye.persistence.IArtefactDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.htmlcleaner.TagNode;
import com.versioneye.domain.*;
import com.versioneye.persistence.IProductDao;
import com.versioneye.persistence.IVersionarchiveDao;
import com.versioneye.service.LicenseService;
import com.versioneye.service.ProductService;
import com.versioneye.service.VersionLinkService;
import com.versioneye.utils.HttpUtils;



import java.io.Reader;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/16/12
 * Time: 3:19 PM
 */
public class CrawlerPythonPip extends SuperCrawler implements ICrawl {

    static final Logger logger = LogManager.getLogger(CrawlerPythonPip.class.getName());

    private String crawlerName = "PIP";
    private String crawlerVersion = "0.1";
    private List<Repository> repositories;
    private Repository repository;
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
    private IArtefactDao artefactDao;
    private boolean threadable = false;
    private static String LANGUAGE = "Python";

    public void run() {
        crawl();
    }

    public void crawl() {
        Set<String> pipNames = getFirstLevelList();
        for (String pipName : pipNames){
            crawlePackage(pipName);
        }
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
            logger.error("ERROR in CrawlerPythonPip.getFirstLevelList()", ex.toString());
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
                handleArtefacts(pip, pipName, version_string);
                if (productDao.doesVersionExistAlready("Python", pipName.toLowerCase(), version_string))
                    continue;
                crawlePackageVersion(pipName, version_string);
            }
        } catch (Exception ex) {
            logger.error("ERROR in CrawlerPython.crawlePackage(" + pipName + ")", ex.toString());
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
            createLicenses( product, pip);
        } catch (Exception ex) {
            logger.error("ERROR in CrawlerPython.crawlePackageVersion(" + pipName + ", "+ version +")", ex.toString());
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
        if (dateParsed == false) {
            logger.info("Not able to parse Date for " + pip.getInfo().getName() + " version " + version.getVersion());
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

    private void handleArtefacts(PipProduct pip, String pipName, String version_string){
        ArrayList<PipRelease> artefacts = pip.getReleases().get(version_string);
        for( PipRelease artefact : artefacts){
            createArchive(artefact, pipName, version_string);
            createArtefacts(artefact, pipName, version_string);
        }
    }

    private void createArchive(PipRelease artefact, String pipName, String version_string){
        try{
            Versionarchive archive = new Versionarchive(LANGUAGE, pipName.toLowerCase(), artefact.getFilename(), artefact.getUrl());
            archive.setVersion_id(version_string);
            if (!versionarchiveDao.doesLinkExistArleady(LANGUAGE, archive.getProduct_key(), archive.getVersion_id(), archive.getLink())){
                versionarchiveDao.create(archive);
            }
        } catch (Exception ex) {
            logger.error("ERROR in CrawlerPython.createArchive " + pipName + ", "+ version_string +")", ex.toString());
        }

    }

    private void createArtefacts(PipRelease artefact, String pipName, String version_string){
        try{
            if (artefactDao.getBySha(artefact.getMd5_digest()) != null){
                return ;
            }
            Artefact art = new Artefact();
            art.setLanguage(LANGUAGE);
            art.setProd_key(pipName.toLowerCase());
            art.setVersion(version_string);
            art.setProd_type("PIP");
            art.setSha_value(artefact.getMd5_digest());
            art.setSha_method("md5");
            art.setFile(artefact.getFilename());
            artefactDao.create(art);
        } catch (Exception ex){
            logger.error("ERROR in CrawlerPython.createArtefacts " + pipName + ", "+ version_string +")", ex.toString());
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

    public IArtefactDao getArtefactDao() {
        return artefactDao;
    }

    public void setArtefactDao(IArtefactDao artefactDao) {
        this.artefactDao = artefactDao;
    }
}
