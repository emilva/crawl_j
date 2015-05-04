package com.versioneye.crawler.crawl;

import org.htmlcleaner.TagNode;
import versioneye.domain.*;
import versioneye.persistence.IProductDao;
import versioneye.persistence.IVersionarchiveDao;
import versioneye.persistence.IVersionlinkDao;
import versioneye.service.ArchiveService;
import versioneye.service.LicenseService;
import versioneye.service.ProductService;
import versioneye.service.VersionLinkService;
import versioneye.utils.HttpUtils;
import versioneye.utils.LogUtils;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 5/3/12
 * Time: 8:58 AM
 *
 */
public class CrawlerR implements ICrawl {

    private String crawlerName = "R";
    private String crawlerVersion = "0.1";
    private List<Repository> repositories;
    private Repository repository;
    private LogUtils logUtils;
    private HttpUtils httpUtils;
    private ProductService productService;
    private VersionLinkService versionLinkService;
    private ArchiveService archiveService;
    private LicenseService licenseService;
    private Crawle crawle;
    private String execGroup;
    private IProductDao productDao;
    private IVersionlinkDao versionlinkDao;
    private IVersionarchiveDao versionarchiveDao;
    private boolean threadable = false;


    public void run() {
        crawl();
    }

    public void crawl() {
        Date start = new Date();
        logUtils.logStart(start, crawlerName, getRepository().getSrc());

        Set<String> names = getFirstLevelList();
        for (String rPackage : names){
            crawlePackage(rPackage);
        }

        logUtils.logStop(start, crawlerName, getRepository().getSrc());
    }

    public Set<String> getFirstLevelList(){
        try{
            Set<String> names = new HashSet<String>();
            TagNode page = httpUtils.getPageForResource(getRepository().getSrc());
            Object[] objects = page.evaluateXPath("//body/table/tbody/tr/td/a");
            for (Object object : objects){
                TagNode node = (TagNode) object;
                String href = node.getAttributeByName("href");
                names.add(href.replaceAll("../../", ""));
            }
            return names;
        } catch (Exception exception) {
            logUtils.addError("ERROR in CrawlerR.getGemNames()", exception.toString(), crawle);
            return null;
        }
    }

    public void crawlePackage(String rPackage){
        try{
            System.out.println("rPackage: " + rPackage);
            if (rPackage.contains("web/packagindex.html"))
                return;
            String name = rPackage;
            name = name.replace("/index.html", "").replace("web/packages/", "");
            String resource = "http://cran.r-project.org/" + rPackage;
            TagNode page = httpUtils.getPageForResource(resource);
            TagNode description = httpUtils.getSingleNode(page.evaluateXPath("//body/p"));

            Product product = new Product();
            product.setName(name);
            product.setDescription(description.getText().toString());
            product.setProd_key(name.replace(".", "").toLowerCase());
            product.setLanguage("R");
            product.setProd_type("R");
            product.setLink(resource);

            Object[] objects = page.evaluateXPath("//body/table[1]/tbody/tr");
            for (Object object : objects){
                TagNode node = (TagNode) object;
                List children = node.getChildren();
                Object child1 = children.get(0);
                Object child2 = children.get(1);
                TagNode label = (TagNode) child1;
                TagNode value = (TagNode) child2;
                if (label.getText().toString().contains("Version")){
                    product.setVersion(value.getText().toString());
                    product.setVersion_link(resource);
                } else if (label.getText().toString().contains("License")){
                    List licenseChildren = value.getChildren();
                    for (Object obi : licenseChildren){
                        if (obi instanceof TagNode ){
                            TagNode link = (TagNode) obi;
                            licenseService.createLicenseIfNotExist(product, link.getText().toString(), link.getAttributeByName("href"), null, null);
                        }
                    }
                } else if (label.getText().toString().contains("Author")){
                    product.setAuthors(value.getText().toString());
                }
            }

            if (product.getVersion() == null || product.getVersion().trim().equals("")){
                return ;
            }

            productService.createProductIfNotExist(product, getRepository());

            Version version = new Version();
            version.setVersion(product.getVersion());
            version.setProduct_key(product.getProd_key());
            version.setLink(product.getLink());
            version.setType("R");
            productService.createVersionIfNotExist(product, version, null);

            Object[] downloads = page.evaluateXPath("//body/table[2]/tbody/tr");
            for (Object object : downloads){
                TagNode node = (TagNode) object;
                List children = node.getChildren();
                Object child1 = children.get(0);
                Object child2 = children.get(1);
                TagNode label = (TagNode) child1;
                TagNode value = (TagNode) child2;
                String labelValue = label.getText().toString();
                if (labelValue.contains("Package source") || labelValue.contains("MacOS X binary") || labelValue.contains(" Windows binary") ){
                    List valuechilds = value.getChildren();
                    for (Object obj : valuechilds) {
                        if (obj instanceof TagNode){
                            TagNode link = (TagNode) obj;
                            createVersionArchive(labelValue, link, product);
                            break;
                        }
                    }
                } else if (labelValue.contains("Reference manual")){
                    List valuechilds = value.getChildren();
                    for (Object obj : valuechilds) {
                        if (obj instanceof TagNode){
                            TagNode link = (TagNode) obj;
                            String href = "http://cran.r-project.org/web/packages/" + name + "/";
                            href = href + link.getAttributeByName("href");
                            versionLinkService.createLinkIfNotExist( product.getLanguage(), product.getProd_key(), product.getVersion(), link.getText().toString(), href);
                            break;
                        }
                    }
                } else if (labelValue.contains("ChangeLog")){
                    TagNode changes = (TagNode)child2;
                    List childrenChanges = changes.getChildren();
                    for (Object ch : childrenChanges){
                        if (ch instanceof TagNode){
                            TagNode change = (TagNode) ch;
                            String href = "http://cran.r-project.org/web/packages/" + name + "/";
                            href = href + change.getAttributeByName("href");
                            versionLinkService.createLinkIfNotExist( product.getLanguage(), product.getProd_key(), product.getVersion(), change.getText().toString(), href);
                        }
                    }
                }
            }
        } catch (Exception ex){
            logUtils.addError("ERROR in crawlerR.crawlePackage("+rPackage+")", ex.toString(), crawle);
        }
    }

    private void createVersionArchive(String label, TagNode link, Product product){
        String name = label.replaceAll(" ", "_") + "_" + link.getText().toString();
        String href = link.getAttributeByName("href");
        href = href.replace("../../../", "");
        href = "http://cran.r-project.org/" + href;
        Versionarchive archive = new Versionarchive(product.getLanguage(), product.getProd_key(),
                name, href);
        archive.setVersion_id(product.getVersion());
        archiveService.createArchiveIfNotExist(product, archive);
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

    public IProductDao getProductDao() {
        return productDao;
    }

    public void setProductDao(IProductDao productDao) {
        this.productDao = productDao;
    }

    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    public IVersionlinkDao getVersionlinkDao() {
        return versionlinkDao;
    }

    public void setVersionlinkDao(IVersionlinkDao versionlinkDao) {
        this.versionlinkDao = versionlinkDao;
    }

    public IVersionarchiveDao getVersionarchiveDao() {
        return versionarchiveDao;
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
}
