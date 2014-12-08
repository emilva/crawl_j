package versioneye.mojo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.domain.MavenRepository;
import versioneye.domain.Product;
import versioneye.domain.Repository;
import versioneye.dto.Document;
import versioneye.dto.Response;
import versioneye.dto.ResponseJson;
import versioneye.maven.MavenIndexer;
import versioneye.maven.MavenPomProcessor;
import versioneye.maven.MavenProjectProcessor;
import versioneye.persistence.IMavenRepostoryDao;
import versioneye.persistence.IProductDao;
import versioneye.persistence.mongodb.MongoDB;
import versioneye.service.ProductService;
import versioneye.utils.HttpUtils;

import java.io.Reader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Crawles Ibiblio M2 Repository for Clojure
 */
@Mojo( name = "central_update_known", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class CentralUpdateKnown extends CentralMojo {

    protected MavenRepository mavenRepository;
    private MongoDB mongoDB;
    private HttpUtils httpUtils;
    private MavenIndexer indexer;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            productService = (ProductService) context.getBean("productService");
            mavenProjectProcessor = (MavenProjectProcessor) context.getBean("mavenProjectProcessor");
            mavenPomProcessor = (MavenPomProcessor) context.getBean("mavenPomProcessor");
            mavenRepositoryDao = (IMavenRepostoryDao) context.getBean("mavenRepositoryDao");
            productDao = (IProductDao) context.getBean("productDao");
            mongoDB = (MongoDB) context.getBean("mongoDb");
            httpUtils = (HttpUtils) context.getBean("httpUtils");
            mavenRepository = mavenRepositoryDao.findByName("central");
            Repository repository = repositoryUtils.convertRepository(mavenRepository);
            mavenProjectProcessor.setRepository(repository);
            mavenPomProcessor.setRepository(repository);
            indexer = getMavenIndexer( mavenRepository ) ;
            if (indexer == null){
                getLog().error("indexer can not be null!");
                return ;
            }
            addAllRepos();
            updateKnownProducts();
            closeIndexer(indexer);
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    private void updateKnownProducts(){
        List<String> javaPackages = getListOfJavaPackages();
        getLog().info(javaPackages.size() + " Java packages found in the database");
        for (String prod_key : javaPackages ){
            String[] splitted = prod_key.split("::");
            String groupId = splitted[0];
            String artifactId = splitted[1];
            fetchFromCentralApi(groupId, artifactId);
        }
    }

    private List<String> getListOfJavaPackages(){
        List<String> result = new ArrayList<String>();
        BasicDBObject query = new BasicDBObject();
        query.put(Product.LANGUAGE, "Java");
        DBCollection products = mongoDB.getDb().getCollection("products");
        DBCursor cursor = products.find(query);
        while (cursor.hasNext()){
            DBObject productDB = cursor.next();
            String groupId = (String) productDB.get( Product.GROUPID );
            String artifactId = (String) productDB.get( Product.ARTIFACTID );
            if (groupId == null || artifactId == null || groupId.startsWith("${") || artifactId.startsWith("${"))
                continue;
            result.add(groupId + "::" + artifactId);
        }
        return result;
    }

    private void fetchFromCentralApi(String groupId, String artifactId){
        try {
            Response response = fetchApiResponse(groupId, artifactId);
            if (response == null || response.getNumFound() == null || response.getNumFound() == 0){
                return ;
            }

            for (Document document : response.getDocs()){
                String versionNumber = document.getV();

                if (productDao.doesVersionExistAlreadyByGA(groupId, artifactId, versionNumber)){
                    getLog().info(" --- exist already " + groupId + ":" + artifactId + ":" + versionNumber);
                    return ;
                } else {
                    getLog().info(" --- check new version" + groupId + ":"+ artifactId + ": " + versionNumber);
                    processNewVersion(groupId, artifactId, versionNumber, document.getTimestamp());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Response fetchApiResponse(String groupId, String artifactId) {
        String url = "http://search.maven.org/solrsearch/select?q=g:%22"+encode(groupId)+"%22+AND+a:%22"+encode(artifactId)+"%22&core=gav&rows=20&wt=json";
        try {
            Reader resultReader = httpUtils.getResultReader( url );
            ObjectMapper mapper = new ObjectMapper();
            ResponseJson json = mapper.readValue(resultReader, ResponseJson.class);
            resultReader.close();
            return json.getResponse();
        } catch (Exception ex) {
            getLog().error("Exception for URL: " + url);
            getLog().error(ex);
            return null;
        }
    }

    private void processNewVersion(String groupId, String artifactId, String v, Integer lastModified) throws Exception {
        try {
            Date date = null;
            if (lastModified != null){
                date = new Date(lastModified);
            }
            IteratorSearchResponse response = indexer.executeGroupArtifactSearch(groupId, artifactId, null);
            if (response == null || response.iterator().hasNext() == false){
                getLog().error(" - indexer returns null for " + groupId + ":" + artifactId);
                mavenPomProcessor.updateNode(groupId, artifactId, v, date);
                return ;
            }
            ArtifactInfo artifactInfo = response.iterator().next();
            artifactInfo.version = v;
            resolveDependencies(artifactInfo);
            parseArtifact(artifactInfo);
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private MavenIndexer getMavenIndexer(MavenRepository repository){
        MavenIndexer mavenIndexer = null;
        try {
            String centralCache = getCacheDirectory("central_update_known");
            String centralIndex = getIndexDirectory("central_update_known");

            mavenIndexer = new MavenIndexer();
            mavenIndexer.initCentralContext( repository.getUrl(), centralCache, centralIndex);
            mavenIndexer.updateIndex();
        } catch (Exception ex) {
            ex.printStackTrace();
            closeIndexer(mavenIndexer);
            mavenIndexer = null;
        }
        return mavenIndexer;
    }

    private void closeIndexer(MavenIndexer mavenIndexer){
        try{
            mavenIndexer.closeIndexer();
        } catch (Exception ex){
            getLog().error(ex);
        }
    }

    private String encode(String value){
        try{
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception ex){
            return value;
        }
    }

}
