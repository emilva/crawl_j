package com.versioneye.crawler.crawl;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 5/3/12
 * Time: 9:12 AM
 */

public class CrawleRTest{

    private ICrawl crawlerR;
    private static ApplicationContext context;

    @Test
    public void init(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        crawlerR = (ICrawl) context.getBean("crawlerR");
    }

    @Test(dependsOnMethods = {"init"})
    public void runForDebug(){
        crawlerR.setRepository( crawlerR.getRepositories().get(0) );
        crawlerR.crawl();
        System.out.println("THE END");
    }

}
