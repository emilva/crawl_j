package versioneye.crawler;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Product;
import versioneye.domain.Repository;
import versioneye.persistence.IProductDao;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:58 PM
 */
public class CrawlerClojureTest {

    private ICrawl crawler;
    private IProductDao productDao;
    private static ApplicationContext context;

    @Test
    public void init() throws Exception {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        crawler = (ICrawl) context.getBean("crawlerClojure");
        productDao = (IProductDao) context.getBean("productDao");
        productDao.drop();
    }

    @Test(dependsOnMethods = {"init"})
    public void test() throws Exception {
        Product product = productDao.getByGA("actors", "gen");
        assert product == null;

        Repository repository = (Repository) context.getBean("clojars");
        crawler.setRepository(repository);
        String current = "http://clojars.org/repo/actors/";
        crawler.crawlePackage(current);

        product = productDao.getByGA("actors", "gen");
        assert product != null;
    }

}
