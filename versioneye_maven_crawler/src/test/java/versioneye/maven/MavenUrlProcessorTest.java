package versioneye.maven;

import org.htmlcleaner.TagNode;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Crawle;
import versioneye.domain.Repository;
import versioneye.utils.HttpUtils;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 7:00 PM
 */
public class MavenUrlProcessorTest {

    private static ApplicationContext context;
    private HttpUtils httpUtils;
    private MavenUrlProcessor mavenUrlProcessor;
    private Repository mavenCentral;

    @Test
    public void init(){
        context   = new ClassPathXmlApplicationContext("applicationContext.xml");
        httpUtils = (HttpUtils)  context.getBean("httpUtils");
        mavenUrlProcessor = (MavenUrlProcessor) context.getBean("mavenUrlProcessor");
        mavenCentral = (Repository) context.getBean("mavenCentral");
    }

    @Test(dependsOnMethods = {"init"})
    public void getProperties() throws Exception {
        String  url = "http://repo.maven.apache.org/maven2/org/ploin/web/ploinFaces/2.2.1/ploinFaces-2.2.1.pom";
        TagNode pom = httpUtils.getPageForResource(url);
        HashMap<String, String> properties = mavenUrlProcessor.getProperties(pom ,null);
        assert properties.size() == 1;
    }

    @Test(dependsOnMethods = {"init"})
    public void updateNode_1(){
        String urlToPom     = "http://repo.maven.apache.org/maven2/org/ploin/web/ploinFaces/2.2.1/ploinFaces-2.2.1.pom";
        String urlToVersion = "http://repo.maven.apache.org/maven2/org/ploin/web/ploinFaces/2.2.1/";
        String urlToProduct = "http://repo.maven.apache.org/maven2/org/ploin/web/ploinFaces/";
        Repository repo = new Repository();
        repo.setName("repp");
        repo.setSrc("http://asgfasfg.de");
        Crawle crawle = new Crawle();
        crawle.setCrawlerName("NameCrawler");
        mavenUrlProcessor.updateNode(urlToPom, urlToVersion, urlToProduct, repo, crawle);
    }

}
