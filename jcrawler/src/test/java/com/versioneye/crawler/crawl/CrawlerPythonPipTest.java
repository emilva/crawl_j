package com.versioneye.crawler.crawl;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/16/12
 * Time: 3:35 PM
 */
public class CrawlerPythonPipTest {

    private ICrawl crawlerPythonPip;
    private static ApplicationContext context;

    @Test
    public void init(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        crawlerPythonPip = (ICrawl) context.getBean("crawlerPythonPip");
    }

    @Test(dependsOnMethods = {"init"})
    public void runForDebug(){
        crawlerPythonPip.crawlePackage("tweepy");
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

