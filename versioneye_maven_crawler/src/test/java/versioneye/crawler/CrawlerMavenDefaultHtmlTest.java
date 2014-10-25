package versioneye.crawler;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Repository;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:57 PM
 */
public class CrawlerMavenDefaultHtmlTest {

    private ICrawl crawler;
    private static ApplicationContext context;

    @Test
    public void init(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        crawler = (ICrawl) context.getBean("crawlerMavenDefaultHtml");
    }

    @Test
    public void test(){
        Repository repository = (Repository) context.getBean("gradle");
        crawler.setRepository(repository);
//        String current = "http://gradle.artifactoryonline.com/gradle/libs/org/apache/aries/blueprint/";
        String current = "http://gradle.artifactoryonline.com/gradle/libs/org/apache/aries/blueprint/org.apache.aries.blueprint.api/";
        crawler.crawlePackage(current);
        System.out.println("THIS IS THE END");
    }

}
