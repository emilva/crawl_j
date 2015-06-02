package versioneye.maven;

import org.htmlcleaner.TagNode;
import versioneye.domain.Crawle;
import versioneye.domain.Product;
import versioneye.domain.Repository;
import versioneye.domain.Version;
import versioneye.persistence.IProductDao;
import versioneye.service.*;
import versioneye.utils.HttpUtils;
import versioneye.utils.LogUtils;

import java.util.Date;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/27/13
 * Time: 7:53 PM
 */
public class MavenUrlProcessor {

    private static final String SLASH = "/";

    private ProductService productService;
    protected IProductDao productDao;
    private VersionLinkService versionLinkService;
    private ArchiveService archiveService;
    private DependencyService dependencyService;
    private LicenseService licenseService;
    private DeveloperService developerService;
    private HttpUtils httpUtils;
    private LogUtils logUtils;

    public void updateNode(String urlToPom, String urlToVersion, String urlToProduct, Repository repository, Crawle crawle) {
        if (productDao.doesPomUrlExistAlready(urlToPom))
            return ;
        try{
            TagNode pom = httpUtils.getPageForResource(urlToPom);

            HashMap<String, String> properties = getProperties(pom, null);

            String versionNumber = getVersion(pom, properties);

            if (versionNumber == null || versionNumber.contains("SNAPSHOT"))
                return ;

            properties.put("project.version", versionNumber);
            properties.put("version", versionNumber);

            String groupId = getGroupId(pom, properties);
            properties.put("pom.groupid", groupId);
            properties.put("project.groupid", groupId);
            properties.put("groupid", groupId);

            String artifactId = getArtifactId(pom, properties);
            properties.put("project.artifactid", artifactId);
            properties.put("artifactid", artifactId);

            String key = groupId.toLowerCase() + SLASH + artifactId.toLowerCase();

            if (groupId == null || artifactId == null ||  versionNumber == null || versionNumber.startsWith("$"))
                return;

            if (productDao.doesVersionExistAlreadyByGA(groupId, artifactId, versionNumber)){
                return ;
            }

            String licenseName = httpUtils.getSingleValue(pom.evaluateXPath("//project/licenses/license/name"), properties);
            String licenseLink = httpUtils.getSingleValue(pom.evaluateXPath("//project/licenses/license/url"), properties);
            String description = httpUtils.getSingleValue(pom.evaluateXPath("//project/description"), properties);
            String organisationName = httpUtils.getSingleValue(pom.evaluateXPath("//project/organization/name"), properties);
            String organisationUrl  = httpUtils.getSingleValue(pom.evaluateXPath("//project/organization/url"), properties);
            String scmUrl = httpUtils.getSingleValue(pom.evaluateXPath("//project/scm/url"), properties);
            String projectUrl = httpUtils.getSingleValue(pom.evaluateXPath("//project/url"), properties);

            Product product = new Product();
            product.setProd_type(repository.getRepoType());
            product.setLanguage(repository.getLanguage());
            product.setProd_key(key.toLowerCase());
            product.setName(artifactId);
            product.setVersion(versionNumber);
            product.setGroupId(groupId);
            product.setArtifactId(artifactId);
            product.addRepository(repository);
            product.setLink(urlToProduct);

            Version version = new Version();
            version.setVersion(versionNumber);
            version.setLink(urlToVersion);
            version.setPom(urlToPom);
            version.setProduct_key(key);
            version.setCreatedAt(new java.util.Date(new Date().getTime()));
            version.setUpdatedAt(new java.util.Date(new Date().getTime()));
            version.setType(product.getProd_type());

            productService.createProductIfNotExist(product, repository);
            boolean newVersion = productService.createVersionIfNotExist(product, version, repository);
            if ( newVersion == false )
                return ;

            dependencyService.createDependenciesIfNotExist(pom, product, properties);
            developerService.createDevelopersIfNotExist(pom, properties, product);
            archiveService.createArchivesIfNotExist(product, urlToPom);
            versionLinkService.createLinkIfNotExist( product.getLanguage(), product.getProd_key(), version.getVersion(), "URL", projectUrl);
            versionLinkService.createLinkIfNotExist( product.getLanguage(), product.getProd_key(), version.getVersion(), "SCM", scmUrl);
            versionLinkService.createLinkIfNotExist( product.getLanguage(), product.getProd_key(), null, organisationName, organisationUrl);
            versionLinkService.createLinkIfNotExist( product.getLanguage(), product.getProd_key(), null, "Link to Repository", urlToProduct);
            createLicenses(product, licenseName, licenseLink);
            if (description != null && !description.trim().equals(""))
                productDao.updateDescription(product.getLanguage(), product.getProd_key(), description);
        } catch (Exception ex) {
            ex.printStackTrace();
            String message = "ERROR in MavenUrlProcessor.updateNode(..) for pom file " + urlToPom;
            logUtils.addError(message, ex.toString(), crawle);
            return ;
        }
    }

    private void createLicenses(Product product, String name, String url){
        if (name == null || name.isEmpty())
            return ;
        licenseService.createLicenseIfNotExist(product, name, url, null, null);
    }

    public HashMap<String, String> getProperties(TagNode pom, HashMap<String, String> properties) throws Exception{
        if (properties == null)
            properties = new HashMap<String, String>();
        Object[] objects = pom.evaluateXPath("//project/properties");
        if (objects != null || objects.length != 0){
            for (Object object : objects){
                TagNode node = (TagNode) object;
                TagNode[] nodes = node.getAllElements(true);
                for (TagNode child : nodes ){
                    properties.put(child.getName().toLowerCase(), child.getText().toString());
                }
            }
        }
//        properties = getPropertiesFromParent(pom, properties);
        return properties;
    }

    public HashMap<String, String> getPropertiesFromParent(TagNode pom, HashMap<String, String> properties) throws Exception {
        if (properties == null)
            properties = new HashMap<String, String>();

        String parentPomUrl = getParentPom(pom, properties);
        if (parentPomUrl == null){
            return properties;
        }
        try{
            TagNode parentPom = httpUtils.getPageForResource(parentPomUrl);
            if (parentPom != null)
                properties = getProperties(parentPom, properties);
        } catch (Exception ex) {
            ex.printStackTrace();
            logUtils.addError("error in CrawlerMavenDefaultJson.getProperties -> parentPomUrl: " + parentPomUrl, ex.toString(), null);
        }
        return properties;
    }

    private String getParentPom(TagNode pom, HashMap<String, String> properties) throws Exception {
        if (pom == null)
            return null;

        TagNode parent = httpUtils.getSingleNode(pom.evaluateXPath("//project/parent")) ;
        if (parent == null)
            return null;

        String group    = httpUtils.getSingleValue(parent.evaluateXPath("/groupId"), properties);
        String artifact = httpUtils.getSingleValue(parent.evaluateXPath("/artifactId"), properties);
        String version  = httpUtils.getSingleValue(parent.evaluateXPath("/version"), properties);
        Product product = productDao.getByGA(group, artifact);
        if (product == null)
            return null;

        if (version == null)
            version = product.getVersion();
        Version versionObj = product.getVersion(version);
        if (versionObj == null)
            return null;
        return versionObj.getPom();
    }

    public String getGroupId(TagNode pom, HashMap<String, String> properties) throws Exception {
        String groupId = httpUtils.getSingleValue(pom.evaluateXPath("//project/groupId"), properties);
        if (groupId == null)
            groupId = httpUtils.getSingleValue(pom.evaluateXPath("//project/parent/groupId"), properties);
        return groupId;
    }

    public String getArtifactId(TagNode pom, HashMap<String, String> properties) throws Exception {
        String artifactId = httpUtils.getSingleValue(pom.evaluateXPath("//project/artifactId"), properties);
        if (artifactId == null)
            artifactId = httpUtils.getSingleValue(pom.evaluateXPath("//project/parent/artifactId"), properties);
        return artifactId;
    }

    public String getVersion(TagNode pom, HashMap<String, String> properties) throws Exception {
        String versionNumber = httpUtils.getSingleValue(pom.evaluateXPath("//project/version"), properties);
        if (versionNumber == null)
            versionNumber = httpUtils.getSingleValue(pom.evaluateXPath("//project/parent/version"), properties);
        return versionNumber;
    }

    public String getPackaging(TagNode pom, HashMap<String, String> properties) throws Exception {
        String packaging = httpUtils.getSingleValue(pom.evaluateXPath("//project/packaging"), properties);
        if (packaging == null)
            packaging = httpUtils.getSingleValue(pom.evaluateXPath("//project/parent/packaging"), properties);
        return packaging;
    }


    public void setProductDao(IProductDao productDao) {
        this.productDao = productDao;
    }

    public void setHttpUtils(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

    public void setLogUtils(LogUtils logUtils) {
        this.logUtils = logUtils;
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

    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public void setDeveloperService(DeveloperService developerService) {
        this.developerService = developerService;
    }

}
