package com.versioneye.crawler.crawl;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Repository;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 4/11/12
 * Time: 3:21 PM
 */
public class CrawlerGitHubTest {

    private CrawlerGitHub crawlerGitHub;
    private static ApplicationContext context;

    @Test
    public void init(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        crawlerGitHub = (CrawlerGitHub) context.getBean("crawlerGitHub");
    }

    @Test(dependsOnMethods = {"init"})
    public void doCrawle(){
        Repository repository = crawlerGitHub.getRepositories().get(0);
        crawlerGitHub.setRepository(repository);
//        crawlerGitHub.crawlePackage("php/php-src");
        crawlerGitHub.crawl();
    }

}