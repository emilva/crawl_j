package versioneye.mojo;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.crawler.CrawlerMavenDefaultHtml;
import versioneye.crawler.ICrawl;
import versioneye.domain.Product;
import versioneye.domain.Repository;
import versioneye.maven.MavenIndexer;
import versioneye.persistence.IProductDao;

import java.util.List;

/**
 * This Mojo loads all Java products which have at least
 * 1 follower and crawles them again for new versions and other meta informations.
 */
@Mojo( name = "popular", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class PopularMojo extends SuperMojo {

    private IProductDao productDao;
    private ICrawl crawlerMavenDefaultHtml;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            productDao = (IProductDao) context.getBean("productDao");
            crawlerMavenDefaultHtml = (CrawlerMavenDefaultHtml) context.getBean("crawlerMavenDefaultHtml");
            super.execute();
            addAllRepos();
            processPopularProjects();
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    protected void processPopularProjects(){
        List<ObjectId> productIds = productDao.getUniqueFollowedJavaIds();
        getLog().info("There are currently " + productIds.size() + " popular Java products. Yahooo!");
        for (ObjectId productId : productIds) {
           fetchProduct(productId);
        }
    }

    protected void fetchProduct(ObjectId productId){
        try{
            Product product = productDao.getById( productId.toString() );
            if (product == null){
                return;
            }
            getLog().info("Prozess popular Java product " + product.getProd_key());
            iterateProductRepos(product);
        } catch (Exception ex){
            getLog().error(ex);
            getLog().error("ERROR in doUpdateFromIndex" + ex.getMessage());
        }
    }

    protected void iterateProductRepos( Product product ) throws Exception {
        if (product == null || product.getRepositories() == null || product.getRepositories().isEmpty())
            return ;
        for (String key : product.getRepositories().keySet()){
            Repository repository = product.getRepositories().get(key);
            mavenPomProcessor.setRepository(repository);
            mavenProjectProcessor.setRepository(repository);
            boolean updated = processRepoForProduct(repository, product);
            if (updated == false){
                getLog().info("Update via maven indexer failed. Let's crawle HTML!");
                crawlerMavenDefaultHtml.setRepository(repository);
                getLog().info("product.link: " + product.getLink() );
                if (!product.getLink().startsWith("http")){
                    product.setLink("http://" + product.getLink());
                    getLog().info("updated product.link: " + product.getLink() );
                }
                crawlerMavenDefaultHtml.crawlePackage(product.getLink());
            }
        }
    }

    private boolean processRepoForProduct(Repository repository, Product product) {
        MavenIndexer mavenIndexer = null;
        try {
            String centralCache = getCacheDirectory(repository.getName());
            String centralIndex = getIndexDirectory(repository.getName());

            mavenIndexer = new MavenIndexer();
            mavenIndexer.initCentralContext( repository.getSrc(), centralCache, centralIndex);
            mavenIndexer.updateIndex(null, null);

            IteratorSearchResponse response = mavenIndexer.executeGroupArtifactSearch(product.getGroupId(), product.getArtifactId(), null);
            for ( ArtifactInfo artifactInfo : response ) {
                super.parseArtifact(artifactInfo);
            }
            mavenIndexer.closeIndexer();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            closeIndexer(mavenIndexer);
            return false;
        }
    }

    private void closeIndexer(MavenIndexer mavenIndexer){
        try{
            mavenIndexer.closeIndexer();
        } catch (Exception ex){
            getLog().error(ex);
        }
    }

}
