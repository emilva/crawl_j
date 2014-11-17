package versioneye.mojo;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.domain.Repository;

@Mojo( name = "jcenter", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class JcenterMojo extends HtmlMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute();

            username = null;
            password = null;

            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            repository = (Repository) context.getBean("jcenter");
            mavenProjectProcessor.setRepository(repository);
            mavenPomProcessor.setRepository(repository);

            mavenRepository = mavenRepositoryDao.findByName("jcenter");
            addRepo(mavenRepository);

            crawl();
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

}
