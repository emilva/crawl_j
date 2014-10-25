package versioneye.crawler;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Repository;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:58 PM
 */
public class CrawlerClojureTest {

    private ICrawl crawler;
    private static ApplicationContext context;

    @Test
    public void init(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        crawler = (ICrawl) context.getBean("crawlerClojure");
    }

    @Test(dependsOnMethods = {"init"})
    public void test(){
        Repository repository = (Repository) context.getBean("clojars");
        crawler.setRepository(repository);
        String current = "http://clojars.org/repo/actors/";
        crawler.crawlePackage(current);
    }

}
