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
import versioneye.domain.MavenRepository;
import versioneye.domain.Product;
import versioneye.domain.Repository;
import versioneye.maven.MavenIndexer;
import versioneye.persistence.IProductDao;

import java.util.List;

/**
 * Crawles popular packages on central.
 */
@Mojo( name = "central_popular", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class CentralPopularMojo extends SuperMojo {

    private IProductDao productDao;
    protected MavenRepository mavenRepository;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            productDao = (IProductDao) context.getBean("productDao");
            super.execute();
            mavenRepository = mavenRepositoryDao.findByName("central");
            addAllRepos();
            Repository repository = repositoryUtils.convertRepository(mavenRepository);
            mavenProjectProcessor.setRepository(repository);
            mavenPomProcessor.setRepository(repository);
            processPopularProjects();
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    protected void processPopularProjects(){
        MavenIndexer indexer = getMavenIndexer( mavenRepository ) ;
        if (indexer == null){
            getLog().error("Indexer can not be initialized! ");
            return ;
        }
        List<ObjectId> productIds = productDao.getUniqueFollowedJavaIds();
        getLog().info("There are currently " + productIds.size() + " popular Java products. Yahooo!");
        for (ObjectId productId : productIds) {
            updateProduct(productId, indexer);
        }
        closeIndexer(indexer);
    }

    protected void updateProduct(ObjectId productId, MavenIndexer mavenIndexer){
        try{
            Product product = productDao.getById( productId.toString() );
            if (product == null){
                return;
            }
            getLog().info("Prozess popular Java product " + product.getProd_key());
            IteratorSearchResponse response = mavenIndexer.executeGroupArtifactSearch(product.getGroupId(), product.getArtifactId(), null);
            for ( ArtifactInfo artifactInfo : response ) {
                resolveDependencies(artifactInfo);
                parseArtifact(artifactInfo);
            }
        } catch (Exception ex){
            getLog().error("ERROR in updateProduct " + ex.getMessage());
        }
    }

    private MavenIndexer getMavenIndexer(MavenRepository repository){
        MavenIndexer mavenIndexer = null;
        try {
            String centralCache = getCacheDirectory(repository.getName());
            String centralIndex = getIndexDirectory(repository.getName());

            mavenIndexer = new MavenIndexer();
            mavenIndexer.initCentralContext( repository.getUrl(), centralCache, centralIndex);
            mavenIndexer.updateIndex(mavenRepository.getUsername(), mavenRepository.getPassword());

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

}
