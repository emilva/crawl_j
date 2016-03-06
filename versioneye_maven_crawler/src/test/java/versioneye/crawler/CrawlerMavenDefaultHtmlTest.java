package versioneye.crawler;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Repository;

import java.util.List;

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

    @Test(dependsOnMethods = {"init"})
    public void getLinksFromPage(){
        Repository repository = (Repository) context.getBean("gradle");
        crawler.setRepository(repository);

        CrawlerMavenDefaultHtml htmlCrawler = (CrawlerMavenDefaultHtml) crawler;
        List<String> links = htmlCrawler.getLinksFromPage("http://jcenter.bintray.com/");

        for ( String link : links ){
            System.out.println( "Link: " + link );
        }

        assert !links.isEmpty();
        assert links.get(6).equals(":ColumnPack/");

        System.out.println("THIS IS THE END");
    }

    @Test(dependsOnMethods = {"init"})
    public void getFirstLevelList(){
        Repository repository = (Repository) context.getBean("jcenter");
        crawler.setRepository(repository);

        CrawlerMavenDefaultHtml htmlCrawler = (CrawlerMavenDefaultHtml) crawler;
        List<String> links = htmlCrawler.getFirstLevelList();

        for (String link: links){
            System.out.println( "Link: " + link );
        }

        assert !links.isEmpty();
        System.out.println("Link6: " + links.get(6));
        assert links.get(6).equals("http://jcenter.bintray.com/:CustomHistory/");

        System.out.println("THIS IS THE END");
    }

    @Test(dependsOnMethods = {"init"})
    public void follow(){
        Repository repository = (Repository) context.getBean("jcenter");
        crawler.setRepository(repository);

        CrawlerMavenDefaultHtml htmlCrawler = (CrawlerMavenDefaultHtml) crawler;
        htmlCrawler.follow("http://jcenter.bintray.com/ColumnPack/", "");

        System.out.println("THIS IS THE END");
    }

}
