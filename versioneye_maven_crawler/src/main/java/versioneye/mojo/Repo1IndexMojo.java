package versioneye.mojo;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.domain.GlobalSetting;
import versioneye.domain.Repository;
import versioneye.maven.MavenPomProcessor;
import versioneye.maven.MavenProjectProcessor;
import versioneye.persistence.IGlobalSettingDao;
import versioneye.persistence.IMavenRepostoryDao;
import versioneye.persistence.IProductDao;
import versioneye.service.ProductService;

@Mojo( name = "repo1index", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class Repo1IndexMojo extends CentralMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

            productDao = (IProductDao) context.getBean("productDao");
            mavenRepositoryDao = (IMavenRepostoryDao) context.getBean("mavenRepositoryDao");
            globalSettingDao = (IGlobalSettingDao) context.getBean("globalSettingDao");

            productService = (ProductService) context.getBean("productService");

            mavenProjectProcessor = (MavenProjectProcessor) context.getBean("mavenProjectProcessor");
            mavenPomProcessor = (MavenPomProcessor) context.getBean("mavenPomProcessor");

            mavenRepository = mavenRepositoryDao.findByName("central");
            mavenRepository.setUrl(fetchBaseUrl());
            Repository repository = repositoryUtils.convertRepository(mavenRepository);

            mavenProjectProcessor.setRepository(repository);
            mavenPomProcessor.setRepository(repository);

            addRepo(mavenRepository);

            super.doUpdateFromIndex();
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    private String fetchBaseUrl(){
        String env = System.getenv("RAILS_ENV");
        getLog().info("fetchBaseUrl for env: " + env );
        try{
            GlobalSetting gs = globalSettingDao.getBy(env, "mvn_repo_1");
            String url = gs.getValue();
            getLog().info(" - mvn_repo_1: " + url);
            return url;
        } catch( Exception ex){
            ex.printStackTrace();
            return "http://repo.maven.apache.org/maven2";
        }
    }

}
