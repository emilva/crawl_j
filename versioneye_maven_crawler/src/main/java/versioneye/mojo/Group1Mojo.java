package versioneye.mojo;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.crawler.CrawlerMavenDefaultHtml;
import versioneye.crawler.ICrawl;
import versioneye.domain.MavenRepository;
import versioneye.domain.Repository;
import versioneye.service.ProductService;

import java.util.List;

/**
 * Crawles all repositories in group1.
 */
@Mojo( name = "group1", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class Group1Mojo extends SuperMojo {

    protected ProductService productService;
    private ICrawl crawlerMavenDefaultHtml;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            productService = (ProductService) context.getBean("productService");
            crawlerMavenDefaultHtml = (CrawlerMavenDefaultHtml) context.getBean("crawlerMavenDefaultHtml");
            super.execute();
//            addAllRepos();

            setRepository("CloJars", "http://clojars.org/repo");

            ArtifactInfo artifactInfo = new ArtifactInfo();
            artifactInfo.groupId = "lamina";
            artifactInfo.artifactId = "lamina";
            artifactInfo.version = "0.5.0";

            resolveDependencies(artifactInfo);
            parseArtifact(artifactInfo);

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
