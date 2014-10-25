package versioneye.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.domain.MavenRepository;
import versioneye.domain.Repository;
import versioneye.maven.MavenPomProcessor;
import versioneye.maven.MavenProjectProcessor;
import versioneye.persistence.IMavenRepostoryDao;
import versioneye.persistence.IProductDao;
import versioneye.service.ProductService;


@Mojo( name = "nuxeo-public-releases", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class NuxeoPublicReleases extends CentralMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            productService = (ProductService) context.getBean("productService");
            mavenProjectProcessor = (MavenProjectProcessor) context.getBean("mavenProjectProcessor");
            mavenPomProcessor = (MavenPomProcessor) context.getBean("mavenPomProcessor");
            mavenRepositoryDao = (IMavenRepostoryDao) context.getBean("mavenRepositoryDao");
            productDao = (IProductDao) context.getBean("productDao");
            MavenRepository publicNuxeoRepo = mavenRepositoryDao.findByName("nuxeo");
            mavenRepository                 = mavenRepositoryDao.findByName("nuxeo-public-releases");
            Repository repository = repositoryUtils.convertRepository(mavenRepository);
            mavenProjectProcessor.setRepository(repository);
            mavenPomProcessor.setRepository(repository);
            addRepo(mavenRepository);
            addRepo(publicNuxeoRepo);
            super.doUpdateFromIndex();
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

}
