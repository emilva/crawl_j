package com.versioneye.crawler.crawl;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Product;
import versioneye.persistence.IProductDao;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 5/3/12
 * Time: 9:12 AM
 */

public class CrawleRTest{

    private ICrawl crawlerR;
    private static ApplicationContext context;
    private IProductDao productDao;

    @Test
    public void init(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        productDao = (IProductDao) context.getBean("productDao");
        crawlerR = (ICrawl) context.getBean("crawlerR");
        productDao.drop();
    }

    @Test(dependsOnMethods = {"init"})
    public void runForDebug() throws Exception {
        Product product = productDao.getByKey("R", "mvc");
        assert product == null;

        crawlerR.setRepository( crawlerR.getRepositories().get(0) );
        crawlerR.crawlePackage("web/packages/mvc/index.html");

        product = productDao.getByKey("R", "mvc");
        assert product != null;

        System.out.println("THE END");
    }

}
