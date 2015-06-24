package versioneye.mojo;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.crawler.CrawlerMavenDefaultHtml;
import versioneye.crawler.ICrawl;
import versioneye.domain.Repository;
import versioneye.service.ProductService;

import java.io.File;

/**
 * Crawles all repositories in group1.
 */
@Mojo( name = "group1", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class Group1Mojo extends AetherMojo {

    @Parameter( defaultValue = "lamina", property = "groupIdC")
    protected String groupIdC;

    @Parameter( defaultValue = "lamina", property = "artifactIdC")
    protected String artifactIdC;

    @Parameter( defaultValue = "0.5.0", property = "versionC")
    protected String versionC;

    protected ProductService productService;
    private ICrawl crawlerMavenDefaultHtml;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute();

            productService = (ProductService) context.getBean("productService");
            crawlerMavenDefaultHtml = (CrawlerMavenDefaultHtml) context.getBean("crawlerMavenDefaultHtml");
            super.execute();

            setRepository("CloJars", "http://clojars.org/repo");

            getLog().info("run for " + groupIdC + ":" + artifactIdC + ":pom:" + versionC);
            Artifact artifactInfo = getArtifact( groupIdC + ":" + artifactIdC + ":pom:" + versionC);

            ArtifactResult artifactResult = resolveArtifact(artifactInfo);
            resolveDependencies(artifactInfo);
            parseArtifact(artifactResult.getArtifact(), null);
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    private void setRepository(String name, String url){
        Repository repo = new Repository();
        repo.setName(name);
        repo.setRepoType("Maven2");
        repo.setSrc(url);
        mavenPomProcessor.setRepository(repo);
        mavenProjectProcessor.setRepository(repo);
        crawlerMavenDefaultHtml.setRepository(repo);
        crawlerMavenDefaultHtml.setStartPoint(repo.getSrc());
    }

}
