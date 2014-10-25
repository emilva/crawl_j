package versioneye.service;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;
import versioneye.domain.Product;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 6/14/13
 * Time: 7:34 PM
 */
public class ArchiveServiceTest {

    private static ApplicationContext context;
    private versioneye.service.ArchiveService archiveService;

    @Test
    public void init(){
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        archiveService = (versioneye.service.ArchiveService) context.getBean("archiveService");
    }

    @Test(dependsOnMethods = {"init"})
    public void createArchivesIfNotExist(){
        Product product = new Product();
        product.setProd_key("junit/junit");
        product.setGroupId("junit");
        product.setArtifactId("junit");
        product.setVersion("4.9");
        archiveService.createArchivesIfNotExist(product, "http://gradle.artifactoryonline.com/gradle/libs/junit/junit/4.9/junit-4.9.pom");
    }

}
