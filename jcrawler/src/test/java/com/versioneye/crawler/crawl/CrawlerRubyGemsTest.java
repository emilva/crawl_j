package com.versioneye.crawler.crawl;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import com.versioneye.domain.Product;
import com.versioneye.domain.Repository;
import com.versioneye.persistence.IProductDao;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/15/12
 * Time: 11:15 AM
 */

public class CrawlerRubyGemsTest {

    private ICrawl crawlerRubyGems;
    private IProductDao productDao;
    private static ApplicationContext context;

    @Test
    public void init() throws Exception {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        productDao = (IProductDao) context.getBean("productDao");
        crawlerRubyGems = (ICrawl) context.getBean("crawlerRubyGems");

        productDao.drop();
    }

    @Test(dependsOnMethods = {"init"})
    public void runForDebug() throws Exception {
        Product product = productDao.getByKey("Ruby", "rails");
        assert product == null;
        List<Repository> keys = crawlerRubyGems.getRepositories();
        for (Repository repository : keys ){
            crawlerRubyGems.setRepository(repository);
            crawlerRubyGems.crawlePackage("rails");
            System.out.println("THIS IS THE END");
        }
        product = productDao.getByKey("Ruby", "rails");
        assert product != null;
        assert product.getVersions().size() > 1;
    }

}
