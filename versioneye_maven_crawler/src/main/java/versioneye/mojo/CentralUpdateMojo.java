package versioneye.mojo;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.domain.Repository;
import versioneye.maven.MavenIndexer;
import versioneye.maven.MavenPomProcessor;
import versioneye.maven.MavenProjectProcessor;
import versioneye.persistence.IMavenRepostoryDao;
import versioneye.persistence.IProductDao;
import versioneye.service.ProductService;

@Mojo( name = "central_update", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class CentralUpdateMojo extends CentralMojo {

    @Parameter( defaultValue = "org.apache.maven.indexer", property = "group", required = true)
    protected String group;

    @Parameter( defaultValue = "indexer-core", property = "artifact", required = true)
    protected String artifact;


    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            productService = (ProductService) context.getBean("productService");
            mavenProjectProcessor = (MavenProjectProcessor) context.getBean("mavenProjectProcessor");
            mavenPomProcessor = (MavenPomProcessor) context.getBean("mavenPomProcessor");
            mavenRepositoryDao = (IMavenRepostoryDao) context.getBean("mavenRepositoryDao");
            productDao = (IProductDao) context.getBean("productDao");
            mavenRepository = mavenRepositoryDao.findByName("central");
            Repository repository = repositoryUtils.convertRepository(mavenRepository);
            mavenProjectProcessor.setRepository(repository);
            mavenPomProcessor.setRepository(repository);
            addRepo(mavenRepository);
            doUpdateFromIndex();
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    protected void doUpdateFromIndex() {
        try{
            String centralCache = getCacheDirectory(mavenRepository.getName());
            String centralIndex = getIndexDirectory(mavenRepository.getName());

            MavenIndexer mavenIndexer = new MavenIndexer();
            mavenIndexer.initCentralContext(mavenRepository.getUrl(), centralCache, centralIndex);
            mavenIndexer.updateIndex();

            getLog().info("Search for: " + group + " " + artifact);
            IteratorSearchResponse response = mavenIndexer.executeGroupArtifactSearch(group, artifact, null);
            while (response.iterator().hasNext()){
                ArtifactInfo artifactInfo = response.iterator().next();
                resolveDependencies(artifactInfo);
                parseArtifact(artifactInfo);
            }

            mavenIndexer.closeIndexer();
        } catch (Exception ex){
            getLog().error(ex);
            getLog().error("ERROR in doUpdateFromIndex" + ex.getMessage());
        }
    }

}
