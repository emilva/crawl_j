package com.versioneye.crawler.crawl;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Product;
import versioneye.persistence.IProductDao;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/16/12
 * Time: 3:35 PM
 */
public class CrawlerPythonPipTest {

    private ICrawl crawlerPythonPip;
    private IProductDao productDao;
    private static ApplicationContext context;

    @Test
    public void init() throws Exception {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        productDao = (IProductDao) context.getBean("productDao");
        crawlerPythonPip = (ICrawl) context.getBean("crawlerPythonPip");

        productDao.drop();
    }

    @Test(dependsOnMethods = {"init"})
    public void runForDebug() throws Exception {
        Product product = productDao.getByKey("Python", "tweepy");
        assert product == null;

        crawlerPythonPip.crawlePackage("tweepy");

        product = productDao.getByKey("Python", "tweepy");
        assert product != null;
        assert product.getVersions().size() > 1;
        System.out.println("THIS IS THE END");
    }

    @Test(dependsOnMethods = {"init"})
    public void getLastElement(){
        String url = "http://www.bela.de/fobi";
        String[] parts = url.split("/");
        assert parts[parts.length - 1].equals("fobi");
        System.out.println("THIS IS THE END");
    }

}
