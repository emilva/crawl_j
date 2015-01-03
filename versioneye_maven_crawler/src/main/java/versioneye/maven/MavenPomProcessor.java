package versioneye.maven;

import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import versioneye.domain.Product;
import versioneye.domain.Repository;
import versioneye.domain.Version;
import versioneye.persistence.IProductDao;
import versioneye.service.*;
import versioneye.utils.LogUtils;
import versioneye.utils.MavenCentralUtils;
import versioneye.utils.MavenUrlUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Robert Reiz
 * Date: 6/15/13
 * Time: 9:49 PM
 */
public class MavenPomProcessor {

    private IProductDao productDao;
    private ProductService productService;
    private DependencyModelService dependencyModelService;
    private DeveloperModelService developerModelService;
    private ArchiveService archiveService;
    private VersionLinkService versionLinkService;
    private LicenseService licenseService;
    private MavenCentralUtils mavenCentralUtils;
    private MavenUrlUtils mavenUrlUtils = new MavenUrlUtils();
    private Repository repository;
    private LogUtils logUtils;


    public boolean updateNode(String groupId, String artifactId, String version, Date lastModfied) {
        try{
            String urlToProduct = null;
            String urlToVersion = null;
            String urlToPom     = null;
            if (repository.getName().equals("central")){
                urlToProduct = mavenUrlUtils.getProductUrl( groupId, artifactId          );
                urlToVersion = mavenUrlUtils.getVersionUrl( groupId, artifactId, version );
                urlToPom     = mavenUrlUtils.getPomUrl(     groupId, artifactId, version );
            } else {
                urlToProduct = mavenUrlUtils.getProductUrl(repository.getSrc(),  groupId, artifactId          );
                urlToVersion = mavenUrlUtils.getVersionUrl(repository.getSrc(),  groupId, artifactId, version );
                urlToPom     = mavenUrlUtils.getPomUrl(    repository.getSrc(),  groupId, artifactId, version );
            }

            Model model = mavenCentralUtils.fetchModelFromUrl(urlToPom, repository.getUsername(), repository.getPassword());

            if (model != null && model.getParent() != null)
                model = mergeParent(model, version);

            HashMap<String, String> properties = getProperties(groupId, artifactId, version);

            Product product = buildProduct(groupId, artifactId, version, urlToProduct, model);
            productService.createProductIfNotExist(product, repository);

            Version versionObj = buildVersion(product, version, urlToVersion, urlToPom, lastModfied);
            productService.createVersionIfNotExist(product, versionObj, repository);

            archiveService.createArchivesIfNotExist(product, urlToPom);
            versionLinkService.createLinkIfNotExist( product.getLanguage(), product.getProd_key(), null, "Link to Repository", urlToProduct);

            if (model == null){
                System.out.println("model is null!!");
                return false;
            }

            createLicenses(model, product);
            dependencyModelService.createDependenciesIfNotExist(model, product, properties);
            developerModelService.createDevelopersIfNotExist(   model, product);
            versionLinkService.createLinkIfNotExist( product.getLanguage(), product.getProd_key(), version, "URL", model.getUrl());
            if (model.getScm() != null && model.getScm().getUrl() != null)
                versionLinkService.createLinkIfNotExist( product.getLanguage(), product.getProd_key(), version, "SCM", model.getScm().getUrl());
            if (model.getOrganization() != null){
                String organisationName = model.getOrganization().getName();
                String organisationUrl  = model.getOrganization().getUrl();
                versionLinkService.createLinkIfNotExist( product.getLanguage(), product.getProd_key(), null, organisationName, organisationUrl);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            String message = "ERROR in updateNode("+groupId+", "+artifactId+", "+version+")";
            logUtils.addError(message, ex.toString(), null);
            return false;
        }
    }

    private Model mergeParent(Model model, String version) throws Exception{
        Parent parent = model.getParent();
        String parentVersion = parent.getVersion();
        if (parentVersion == null || parentVersion.isEmpty())
            parentVersion = version;
        Model  parentModel  = mavenCentralUtils.fetchModel(parent.getGroupId(), parent.getArtifactId(), parentVersion );
        if (parentModel == null){
            System.out.println("parentModel is null -> " + parent.getGroupId() + "/" + parent.getArtifactId() + ":" + parentVersion);
            return model;
        }
        Model newHappyModel = PomReader.merge(parentModel, model);
        return newHappyModel;
    }

    private Version buildVersion(Product product, String version, String urlToVersion, String urlToPom, Date lastModified){
        Version versionObj = new Version();
        versionObj.setVersion(version);
        versionObj.setLink(urlToVersion);
        versionObj.setPom(urlToPom);
        versionObj.setProduct_key(product.getProd_key());
        versionObj.setCreatedAt(new java.sql.Date(new Date().getTime()));
        versionObj.setUpdatedAt(new java.sql.Date(new Date().getTime()));
        if (lastModified != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            versionObj.setReleased_at(lastModified);
            versionObj.setReleased_string(sdf.format(lastModified));
        }
        versionObj.setType(product.getProd_type());
        return versionObj;
    }

    private Product buildProduct(String groupId, String artifactId, String version, String urlToProduct, Model model){
        try {
            Product product = productDao.getByGA(groupId, artifactId);
            if (product == null) {
                String key = groupId.toLowerCase() + "/" + artifactId.toLowerCase();
                product = new Product();
                product.setProd_key(key.toLowerCase());
                product.setGroupId(groupId.toLowerCase());
                product.setArtifactId(artifactId.toLowerCase());
                product.setName(artifactId);
                if (repository != null){
                    product.addRepository(repository);
                    product.setProd_type(repository.getRepoType());
                    product.setLanguage(repository.getLanguage());
                } else {
                    product.setProd_type("Maven2");
                    product.setLanguage("Java");
                }
            }
            product.setVersion(version);
            product.setLink(urlToProduct);
            if (model != null)
                product.setDescription(model.getDescription());
            return product;
        } catch (Exception ex) {
            ex.printStackTrace();
            String message = "ERROR in buildProduct";
            logUtils.addError(message, ex.toString(), null);
            return null;
        }
    }

    private HashMap<String, String> getProperties(String groupId, String artifactId, String version){
        HashMap<String, String> properties = new LinkedHashMap<String, String>();

        properties.put("project.version", version);
        properties.put("version", version);

        properties.put("pom.groupid", groupId);
        properties.put("project.groupid", groupId);
        properties.put("groupid", groupId);

        properties.put("project.artifactid", artifactId);
        properties.put("artifactid", artifactId);

        return properties;
    }

    private void createLicenses(Model model, Product product){
        if (model.getLicenses() == null || model.getLicenses().isEmpty())
            return ;
        for (License license : model.getLicenses() )
            licenseService.createLicenseIfNotExist(product, license.getName(), license.getUrl(),
                    license.getComments(), license.getDistribution());
    }

    public void setMavenCentralUtils(MavenCentralUtils mavenCentralUtils) {
        this.mavenCentralUtils = mavenCentralUtils;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void setLogUtils(LogUtils logUtils) {
        this.logUtils = logUtils;
    }

    public void setArchiveService(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    public void setVersionLinkService(VersionLinkService versionLinkService) {
        this.versionLinkService = versionLinkService;
    }

    public void setDependencyModelService(DependencyModelService dependencyModelService) {
        this.dependencyModelService = dependencyModelService;
    }

    public void setDeveloperModelService(DeveloperModelService developerModelService) {
        this.developerModelService = developerModelService;
    }

    public IProductDao getProductDao() {
        return productDao;
    }

    public void setProductDao(IProductDao productDao) {
        this.productDao = productDao;
    }
}
