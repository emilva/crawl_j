package com.versioneye.crawler.crawl;

import com.versioneye.crawler.dto.GitHubRepo;
import com.versioneye.crawler.dto.GitHubTagVersion;
import com.versioneye.crawler.service.ProductTransferService;
import org.codehaus.jackson.map.ObjectMapper;
import versioneye.domain.*;
import versioneye.persistence.IProductResourceDao;
import versioneye.service.ArchiveService;
import versioneye.service.ProductService;
import versioneye.service.VersionLinkService;
import versioneye.utils.HttpUtils;
import versioneye.utils.LogUtils;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 4/11/12
 * Time: 12:31 PM
 */
public class CrawlerGitHub implements ICrawl {

    private String crawlerName = "GitHub";
    private String crawlerVersion = "0.2";
    private List<Repository> repositories;
    private Repository repository;
    private LogUtils logUtils;
    private HttpUtils httpUtils;
    private ProductService productService;
    private ProductTransferService productTransferService;
    private VersionLinkService versionLinkService;
    private ArchiveService archiveService;
    private Crawle crawle;
    private String execGroup;
    private boolean threadable = false;
    private IProductResourceDao productResourceDao;

    public void run() {
        crawl();
    }

    public void crawl() {
        Date start = new Date();
        logUtils.logStart(start, crawlerName, getRepository().getSrc());
        List<ProductResource> resources = productResourceDao.getGitHubResources();
        for (ProductResource resource : resources){
            Product product = crawleRepo(resource.getName());
            if (product == null)
                continue;
            resource.setLanguage(product.getLanguage());
            productResourceDao.updateCrawledForGithub(resource);
            try {
                Thread.sleep(5000);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        logUtils.logStop(start, crawlerName, getRepository().getSrc());
    }

    public void crawlePackage(String name){
        crawleRepo(name);
    }

    public Product crawleRepo(String name){
        System.out.println("crawle package: " + name);
        try {
            GitHubRepo repo = fetchRepo(name);
            repo.setProd_key( name );
            Product product = fetchProduct(repo);
            productService.createProductIfNotExist(product, getRepository());
            String prodKey = new String(product.getProd_key());
            product.setProd_key(prodKey);
            createLinks(repo, product, name);
            GitHubTagVersion[] tags = fetchTags(name);
            for (GitHubTagVersion tag: tags){
                Version version = new Version();
                version.setVersion(tag.getName());
                version.setProduct_key(prodKey);
                doReplacements(version);
                productService.createVersionIfNotExist(product, version, null);
                createArchives(repo, tag, version, product, prodKey);
            }
            return product;
        }  catch (Exception ex){
            logUtils.addError("ERROR in CrawlerGithub.crawlePackage("+name+")", ex.toString(), crawle);
            return null;
        }
    }

    private Product fetchProduct(GitHubRepo repo){
        Product product = new Product();
        productTransferService.updateFromGitHub(repo, product);
        return product;
    }

    private GitHubRepo fetchRepo(String name) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(getRepository().getSrc());
        sb.append(name);
        String url = sb.toString();
        Reader resultReader = httpUtils.getResultReader( url );
        ObjectMapper mapper = new ObjectMapper();
        GitHubRepo repo = mapper.readValue(resultReader, GitHubRepo.class);
        resultReader.close();
        return repo;
    }

    private GitHubTagVersion[] fetchTags(String name) throws Exception{
        StringBuilder sbTag = new StringBuilder();
        sbTag.append(getRepository().getSrc());
        sbTag.append(name);
        sbTag.append("/tags");
        String urlTag = sbTag.toString();
        Reader resultReader = httpUtils.getResultReader( urlTag );
        ObjectMapper mapper = new ObjectMapper();
        GitHubTagVersion[] tags = mapper.readValue(resultReader, GitHubTagVersion[].class);
        resultReader.close();
        return tags;
    }

    private void doReplacements(Version version){
        if (version.getVersion().contains("PHP")){
            String newVersion = version.getVersion().replace("PHP", "");
            newVersion = newVersion.trim();
            version.setVersion(newVersion);
        } else if (version.getVersion().contains("php-")){
            String newVersion = version.getVersion().replace("php-", "");
            newVersion = newVersion.trim();
            version.setVersion(newVersion);
        }
    }

    private void createLinks( GitHubRepo repo, Product product, String name ) {
        Versionlink homepage = new Versionlink(repo.getLanguage(), product.getProd_key(), "Homepage", repo.getHomepage());
        String homepageUrl = repo.getHomepage();
        if (homepageUrl != null && !homepageUrl.trim().equals("")){
            if (!homepageUrl.startsWith("http://") && !homepageUrl.startsWith("https://")){
                homepageUrl = "http://" + homepageUrl;
            }
            homepage.setLink(homepageUrl);
            versionLinkService.convertAndPersistIfNotExist(product.getProd_key(), homepage);
        }
        String gitHubUrl = "https://github.com/" + name;
        if (!homepage.getLink().equals(gitHubUrl)){
            Versionlink gitHubPage = new Versionlink(repo.getLanguage(), product.getProd_key(), "GitHub", gitHubUrl);
            versionLinkService.convertAndPersistIfNotExist(product.getProd_key(), gitHubPage);
        }
    }

    private void createArchives( GitHubRepo repo, GitHubTagVersion tag, Version version, Product product, String prodKey ){
        String zipName = repo.getName() + "-" + tag.getName() + ".zip";
        Versionarchive zip = new Versionarchive(repo.getLanguage(), prodKey,
                zipName, tag.getZipball_url());
        zip.setVersion_id(version.getVersion());
        archiveService.createArchiveIfNotExist(product, zip);

        String tarGzName = repo.getName() + "-" + tag.getName() + ".tar.gz";
        Versionarchive tar = new Versionarchive(repo.getLanguage(), prodKey,
                tarGzName, tag.getTarball_url());
        tar.setVersion_id(version.getVersion());
        archiveService.createArchiveIfNotExist(product, tar);
    }

    public Set<String> getFirstLevelList() {
        return null;
    }

    public List<String> getOldFirstLevelList(){
        List names = new ArrayList();
        names.add("php/php-src");
        names.add("jquery/jquery");
        names.add("jquery/jquery-mobile");
        names.add("tonytomov/jqGrid");
        names.add("sstephenson/prototype");
        names.add("phonegap/phonegap");
        names.add("documentcloud/underscore");
        names.add("madrobby/scriptaculous");
        names.add("mootools/mootools-core");
        names.add("DmitryBaranovskiy/raphael");
        names.add("sorccu/cufon");
        names.add("ztellman/aleph");
        names.add("ztellman/lamina");
        names.add("ztellman/gloss");
        names.add("chaos/slurm");
        names.add("chaos/lustre");
        names.add("chaos/zfs");
        names.add("chaos/diod");
        return names;
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

    public void setStartPoint(String startPoint) {}
    public String getStartPoint(){return "";}

    public boolean isThreadable() {
        return threadable;
    }

    public void setThreadable(boolean threadable) {
        this.threadable = threadable;
    }

    public void setProductResourceDao(IProductResourceDao productResourceDao) {
        this.productResourceDao = productResourceDao;
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

    public void setArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    public void setProductTransferService(ProductTransferService productTransferService) {
        this.productTransferService = productTransferService;
    }
}
