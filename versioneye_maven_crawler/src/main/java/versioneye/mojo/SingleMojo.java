package versioneye.mojo;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import versioneye.domain.MavenRepository;
import versioneye.domain.Repository;
import versioneye.maven.MavenIndexer;

/**
 * Crawles 1 single project which one is configured in the pom.xml
 */
@Mojo( name = "single", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class SingleMojo extends SuperMojo {

    @Parameter( property = "single.groupid", defaultValue = "javax.mail" )
    private String groupid;

    @Parameter( property = "single.artifactid", defaultValue = "" )
    private String artifactid;

    @Parameter( property = "single.repoName", defaultValue = "central" )
    private String repoName;

    @Parameter( property = "single.indexDirectory", defaultValue = "central" )
    private String indexDirectory;

    @Parameter( property = "single.skipKnownVersions", defaultValue = "true" )
    private boolean skipKnownVersions;

    protected MavenRepository mavenRepository;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute();
            mavenRepository = mavenRepositoryDao.findByName(repoName);
            Repository repository = repositoryUtils.convertRepository(mavenRepository);
            mavenProjectProcessor.setRepository(repository);
            mavenPomProcessor.setRepository(repository);
            addAllRepos();
            doUpdateProject();
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    private void doUpdateProject(){
        try{
            String centralCache = getCacheDirectory(indexDirectory);
            String centralIndex = getIndexDirectory(indexDirectory);

            MavenIndexer mavenIndexer = new MavenIndexer();
            mavenIndexer.initCentralContext(mavenRepository.getUrl(), centralCache, centralIndex);
            mavenIndexer.updateIndex();

            IteratorSearchResponse response = mavenIndexer.executeGroupArtifactSearch(groupid, artifactid, null);
            for ( ArtifactInfo ai : response ) {
                String key = ai.groupId + "/" + ai.artifactId;
                if (skipKnownVersions && productDao.doesVersionExistAlready(mavenRepository.getLanguage(), key, ai.version)){
                    getLog().info(" - " + ai.groupId + ":"+ ai.artifactId + ": " + ai.version + " exist already! ");
                    continue;
                }
                resolveDependencies(ai);
                parseArtifact(ai);
            }
            mavenIndexer.closeIndexer();
        } catch (Exception ex){
            getLog().error(ex);
            getLog().error("ERROR in doUpdateProject" + ex.getMessage());
        }
    }

}
