package versioneye.mojo;

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
            addAllRepos();
            processGroup1();
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    private void processGroup1(){
        List<MavenRepository> repositories = mavenRepositoryDao.loadAll();
        for (MavenRepository repository : repositories ){
            if (repository.getUrl().equals("http://repo.maven.apache.org/maven2"))
                continue;
            processRepository(repository);
        }
    }

    private void processRepository(MavenRepository repository){
        getLog().info("Process Repo: " + repository.getName());
        Repository repo = new Repository();
        repo.setName(repository.getName());
        repo.setRepoType("Maven2");
        repo.setSrc(repository.getUrl());
        mavenPomProcessor.setRepository(repo);
        mavenProjectProcessor.setRepository(repo);
        crawlerMavenDefaultHtml.setRepository(repo);
        crawlerMavenDefaultHtml.setStartPoint(repo.getSrc());
        crawlerMavenDefaultHtml.crawl();
    }

}
