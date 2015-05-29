package versioneye.mojo;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.context.IndexUtils;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.domain.MavenRepository;
import versioneye.domain.Repository;
import versioneye.domain.Version;
import versioneye.maven.MavenIndexer;
import versioneye.service.ProductService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * Fetches the index from the maven central repository and walks through the index
 * to update the database.
 */
@Mojo( name = "central", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class CentralMojo extends SuperMojo {

    protected ProductService productService;
    protected MavenRepository mavenRepository;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute();
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            productService = (ProductService) context.getBean("productService");

            MavenRepository typesafeRepo = mavenRepositoryDao.findByName("typesafe");
            addRepo(typesafeRepo);

            mavenRepository = mavenRepositoryDao.findByName("central");
            Repository repository = repositoryUtils.convertRepository(mavenRepository);
            mavenProjectProcessor.setRepository(repository);
            mavenPomProcessor.setRepository(repository);

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

            IndexingContext context = mavenIndexer.getCentralContext();
            IndexSearcher searcher  = context.acquireIndexSearcher();
            final IndexReader ir    = searcher.getIndexReader();
            for ( int i = 0; i < ir.maxDoc(); i++ ) {
                processArtifact(context, ir, i);
            }
            context.releaseIndexSearcher(searcher);

            mavenIndexer.closeIndexer();
        } catch (Exception ex){
            getLog().error(ex);
            getLog().error("ERROR in doUpdateFromIndex" + ex.getMessage());
        }
    }

    protected void processArtifact(IndexingContext context, IndexReader indexReader, int i) {
        try {
            if ( indexReader.isDeleted( i ) )
                return ;

            final Document doc = indexReader.document( i );
            final ArtifactInfo artifactInfo = IndexUtils.constructArtifactInfo( doc, context );
            if (artifactInfo == null || artifactInfo.groupId == null || artifactInfo.artifactId == null){
                return ;
            }

            if (productDao.doesVersionExistAlreadyByGA(artifactInfo.groupId.toLowerCase(), artifactInfo.artifactId.toLowerCase(), artifactInfo.version)){
                getLog().info("skip " + artifactInfo.groupId + ":" + artifactInfo.artifactId + ":" + artifactInfo.version);
                return ;
            }

            processArtifact(artifactInfo);
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }


    protected void processArtifact(ArtifactInfo artifactInfo) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new MyCallable(artifactInfo);

        Future<Object> future = executor.submit(task);
        try {
            future.get(60, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            getLog().error("Timeout Exception: " + ex);
        } catch (InterruptedException e) {
            getLog().error("Interrupted Exception: " + e);
        } catch (ExecutionException e) {
            getLog().error("Execution Exception: " + e);
        } finally {
            future.cancel(true);
        }
    }


    public class MyCallable implements Callable<Object> {

        private ArtifactInfo artifactInfo;

        public MyCallable (ArtifactInfo artifactInf) {
            artifactInfo = artifactInf;
        }

        public Object call() throws Exception {
            getLog().info("call " + artifactInfo.groupId + ":" + artifactInfo.artifactId + ":" + artifactInfo.version);
            resolveDependencies(artifactInfo);
            parseArtifact(artifactInfo);
            return null;
        }
    }


    protected void updateReleasedDate(ArtifactInfo artifactInfo, String key){
        Version version = new Version();
        version.setLanguage(mavenRepository.getLanguage());
        version.setProduct_key(key);
        version.setVersion(artifactInfo.version);
        version.setReleased_at(new Date(artifactInfo.lastModified));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        version.setReleased_string(sdf.format(version.getReleased_at()));
        getLog().info(version.getReleased_string());
        productDao.updateVersionReleaseTime(version);
    }


}
