package com.versioneye.crawler.crawl;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Repository;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 3/15/12
 * Time: 11:15 AM
 */

public class CrawlerRubyGemsTest {

    private ICrawl crawlerRubyGems;
    private static ApplicationContext context;

    @Test
    public void init(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        crawlerRubyGems = (ICrawl) context.getBean("crawlerRubyGems");
    }

    @Test(dependsOnMethods = {"init"})
    public void runForDebug(){
        List<Repository> keys = crawlerRubyGems.getRepositories();
        for (Repository repository : keys ){
            crawlerRubyGems.setRepository(repository);
            crawlerRubyGems.crawlePackage("rails");
            System.out.println("THIS IS THE END");
        }
    }

}
