package versioneye.crawler;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Repository;
import versioneye.utils.MavenCentralUtils;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:58 PM
 */
public class CrawlerMavenDefaultJsonTest {

    private MavenCentralUtils crawler;
    private static ApplicationContext context;

    @Test
    public void init(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        crawler = (MavenCentralUtils) context.getBean("mavenCentralUtils");
    }

    @Test
    public void test(){
        Repository repository = (Repository) context.getBean("mavenCentral");
        crawler.setRepository(repository);
        crawler.crawleArtifact("org.apache.cxf", "cxf-rt-bindings-coloc");
        System.out.println("THIS IS THE END");
    }

}
