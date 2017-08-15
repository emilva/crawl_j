package com.versioneye.crawler.crawl;

import com.versioneye.crawler.dto.RubyGemDependency;
import com.versioneye.crawler.dto.RubyGemProduct;
import com.versioneye.crawler.dto.RubyGemsVersion;
import com.versioneye.crawler.service.ProductTransferService;
import com.versioneye.crawler.service.VersionTransferService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.htmlcleaner.TagNode;
import com.versioneye.domain.*;
import com.versioneye.persistence.IDeveloperDao;
import com.versioneye.persistence.IProductDao;
import com.versioneye.persistence.IVersionlinkDao;
import com.versioneye.service.*;
import com.versioneye.utils.HttpUtils;
import com.versioneye.utils.LicenseChecker;
import com.versioneye.utils.LogUtils;

import java.io.Reader;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/15/12
 * Time: 10:51 AM
 *
 */
public class CrawlerRubyGems extends SuperCrawler implements ICrawl {

    static final Logger logger = LogManager.getLogger(CrawlerRubyGems.class.getName());

    private String name = "RubyGems";
    private String crawlerVersion = "0.1";
    private List<Repository> repositories;
    private Repository repository;
    private HttpUtils httpUtils;
    private ProductService productService;
    private ProductTransferService productTransferService;
    private VersionTransferService versionTransferService;
    private VersionLinkService versionLinkService;
    private LicenseService licenseService;
    private ArchiveService archiveService;
    private DependencyService dependencyService;
    private IDeveloperDao developerDao;
    private LicenseChecker licenseChecker;
    private Crawle crawle;
    private String execGroup;
    private String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private boolean threadable = false;

    private IVersionlinkDao versionlinkDao;
    private IProductDao productDao;


    public void run() {
        crawl();
    }

    public void crawl() {
        Set<String> gemNames = getFirstLevelList();
        for (String gemName: gemNames){
            crawlePackage(gemName);
        }
    }

    public Set<String> getFirstLevelList(){
        Set<String> gemNames = new HashSet<String>();
        try{
            for (String letter: alphabet){
                Integer count = getPageCountForLetter(letter);
                for (int z = 1; z <= count; z++){
                    logger.info(gemNames.size() + " names. Collect names from page: " + z + " for letter " + letter);
                    getGemNamesFromPage(letter, String.valueOf(z), gemNames);
                }
            }
        } catch (Exception exception) {
            logger.error("ERROR in getGemNames()", exception.toString());
            exception.printStackTrace();
        }
        return gemNames;
    }

    public void crawlePackage(String name) {
        logger.info("crawle rubygem : " + name);
        try{
            String resource = "https://rubygems.org/api/v1/gems/" + encodeURI(name) + ".json";
            Reader reader = httpUtils.getResultReader(resource);
            ObjectMapper mapper = new ObjectMapper();
            RubyGemProduct gem = mapper.readValue(reader, RubyGemProduct.class);
            Product product = new Product();
            productTransferService.updateFromRubyGem(gem, product);
            reader.close();
            productService.createProductIfNotExist(product, getRepository());

            List<Versionlink> links = gem.getLinks();
            for (Versionlink link: links){
                link.setLanguage("Ruby");
                versionLinkService.convertAndPersistIfNotExist(product.getProd_key(), link);
            }
            createProductLinkIfNotExist(product);

            String resourceVersions = "https://rubygems.org/api/v1/versions/" + encodeURI(name) + ".json";
            Reader readerVersions = httpUtils.getResultReader(resourceVersions);
            ObjectMapper mapperVersions = new ObjectMapper();
            List<RubyGemsVersion> versions = mapperVersions.readValue(readerVersions, new TypeReference<List<RubyGemsVersion>>() {} ) ;
            readerVersions.close();
            versions.add(gem.getGemVersion()); // This is for the case that versions is empty.
            for (RubyGemsVersion ver: versions){
                if (productDao.doesVersionExistAlready(product.getLanguage(), product.getProd_key(), ver.getNumber())){
                    checkLicense(gem, product);
                    continue;
                }
                createVersionIfNotExist(product, gem, ver);
                createDependencies(product, gem);
                createArchiveIfNotExist(product);
                createAuthorsIfNotExist(product, ver);
                checkLicense(gem, product);
            }
        } catch (Exception ex) {
            logger.info("ERROR in CrawlerRubyGems.crawlePackage(" + name + ")", ex.toString());
            ex.printStackTrace();
        }
    }

    private Integer getPageCountForLetter(String letter) throws Exception{
        logger.info("get packages for letter: " + letter);
        String resource = "https://rubygems.org/gems?letter=" + letter;
        TagNode page = httpUtils.getPageForResource(resource);
        Object[] objects = page.evaluateXPath("//div[@class=\"pagination\"]/a");
        Object obj = objects[objects.length - 2];
        TagNode node = (TagNode) obj;
        String countString = node.getText().toString();
        return Integer.parseInt(countString);
    }

    private void getGemNamesFromPage(String letter, String pageCount, Set<String> gemNames) throws Exception {
        String resource = "https://rubygems.org/gems?letter="+letter+"&page=" + pageCount;
        TagNode page = httpUtils.getPageForResource(resource);
        Object[] objects = page.evaluateXPath("//a[@class=\"gems__gem\"]");
        for (Object obj : objects){
            TagNode node = (TagNode) obj;
            String href = node.getAttributeByName("href");
            String gemName = href.replaceFirst("/gems/", "");
            if (gemName.equals("Rack")){ // skip Rack because rack is more important.
                continue;
            }
            gemNames.add( gemName );
        }
    }

    private void createProductLinkIfNotExist(Product product){
        if (product == null || product.getLink() == null)
            return ;
        Versionlink link = new Versionlink(product.getLanguage(), product.getProd_key(), "RubyGem Page", product.getLink());
        if (!versionlinkDao.doesLinkExistArleady( product.getLanguage(), link.getProduct_key(), link.getLink()))
            versionlinkDao.create(link);
    }

    private void createVersionIfNotExist(Product product, RubyGemProduct gem, RubyGemsVersion ver) {
        Version version = new Version();
        versionTransferService.updateFromGem(name, ver, version);
        version.setProduct_key(product.getProd_key());
        version.setLink(product.getLink());
        version.setType("RubyGem");
        productService.createVersionIfNotExist(product, version, null);
        product.setVersion(ver.getNumber());
    }

    private void createDependencies(Product product, RubyGemProduct gem){
        RubyGemDependency[] runtime = gem.getDependencies().getRuntime();
        createDependenciesIfNotExist(runtime, product, Dependency.SCOPE_RUNTIME);

        RubyGemDependency[] development = gem.getDependencies().getDevelopment();
        createDependenciesIfNotExist(development, product, Dependency.SCOPE_DEVELOPMENT);
    }

    private void createArchiveIfNotExist(Product product){
        String archiveName = product.getName() + "-" + product.getVersion() + ".gem";
        Versionarchive archive = new Versionarchive(product.getLanguage(), product.getProd_key(),
                archiveName, "https://rubygems.org/gems/" + archiveName);
        archive.setVersion_id(product.getVersion());
        archiveService.createArchiveIfNotExist(product, archive);
    }

    private void createAuthorsIfNotExist(Product product, RubyGemsVersion version){
        if (version == null || version.getAuthors() == null || version.getAuthors().isEmpty()){
            return ;
        }
        String[] authors = version.getAuthors().split(",");

        for (String authorName : authors ){
            String devName = authorName.trim();
            Developer developer = new Developer(product.getLanguage(), product.getProd_key(), product.getVersion(),
                    devName, devName, "", "", "", null, null, null);
            if (!developerDao.doesExistAlready(product.getLanguage(), product.getProd_key(), product.getVersion(), devName))
                developerDao.create(developer);
        }
    }

    private void createDependenciesIfNotExist(RubyGemDependency[] dependencies, Product product, String scope){
        if (dependencies != null && dependencies.length > 0){
            for (RubyGemDependency dep: dependencies){
                Dependency dependency = new Dependency(product.getLanguage(), product.getProd_key(),
                        product.getVersion(), dep.getName(), dep.getRequirements(), dep.getName() );
                dependency.setScope(scope);
                dependency.setProdType("RubyGem");
                dependencyService.createDependencyIfNotExist(dependency);
            }
        }
    }

    private void checkLicense(RubyGemProduct gem, Product product) {
        if (gem.getLicense() != null && !gem.getLicense().isEmpty()) {
            licenseService.createLicenseIfNotExist(product, gem.getLicense(), null, null, null);
        }
        if (gem.getLicenses() != null && gem.getLicenses().length > 0){
            for (String license : gem.getLicenses()){
                licenseService.createLicenseIfNotExist(product, license, null, null, null);
            }
        }
    }

    private void checkLicenseTheHardWay(RubyGemProduct gem, Product product){
        logger.info("check license the hard way for " + product.getProd_key());
        String license = licenseChecker.checkLicenseOnGitHub(gem.getSource_code_uri());
        if (license == null || license.isEmpty()){
            license = licenseChecker.checkLicenseOnGitHub(gem.getHomepage_uri());
        }
        if (license != null && !license.isEmpty()){
            logger.info(" ---> license: " + license);
            String licenseLink = null;
            if (license.equals("MIT")){
                licenseLink = "http://opensource.org/licenses/mit-license.html";
            }
            licenseService.createLicenseIfNotExist(product, license, licenseLink, null, null);
        }
    }

    public Crawle getCrawle() {
        return crawle;
    }

    public String getName() {
        return name;
    }

    public String getCrawlerVersion() {
        return crawlerVersion;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public Repository getRepository() {
        if (repository == null && repositories != null && !repositories.isEmpty()){
            repository = repositories.get(0);
        }
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

    public void setVersionlinkDao(IVersionlinkDao versionlinkDao) {
        this.versionlinkDao = versionlinkDao;
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

    public void setLicenseChecker(LicenseChecker licenseChecker) {
        this.licenseChecker = licenseChecker;
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

    public void setArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
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

    public IDeveloperDao getDeveloperDao() {
        return developerDao;
    }

    public void setDeveloperDao(IDeveloperDao developerDao) {
        this.developerDao = developerDao;
    }
}
