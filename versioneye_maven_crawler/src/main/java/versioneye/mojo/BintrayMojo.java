package versioneye.mojo;


import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.repository.RemoteRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.domain.Repository;
import versioneye.maven.MavenPomProcessor;
import versioneye.maven.MavenProjectProcessor;
import versioneye.persistence.IMavenRepostoryDao;
import versioneye.persistence.IProductDao;
import versioneye.service.ProductService;

@Mojo( name = "bintray", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class BintrayMojo extends CentralMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
            productService = (ProductService) context.getBean("productService");
            mavenProjectProcessor = (MavenProjectProcessor) context.getBean("mavenProjectProcessor");
            mavenPomProcessor = (MavenPomProcessor) context.getBean("mavenPomProcessor");
            mavenRepositoryDao = (IMavenRepostoryDao) context.getBean("mavenRepositoryDao");
            productDao = (IProductDao) context.getBean("productDao");
            mavenRepository = mavenRepositoryDao.findByName("bintray");
            Repository repository = repositoryUtils.convertRepository(mavenRepository);
            mavenProjectProcessor.setRepository(repository);
            mavenPomProcessor.setRepository(repository);
            addRepo(mavenRepository);
//            super.doUpdateFromIndex();

            for (RemoteRepository repo: repos){
                getLog().info("repo: " + repo.getUrl());
            }
            ArtifactInfo artifactInfo = new ArtifactInfo();
            artifactInfo.groupId = "org.ploin.web";
            artifactInfo.artifactId = "ploinFaces";
            artifactInfo.version = "2.2.1";

            getLog().info("Resolve for " + artifactInfo.toString());
            resolveDependencies(artifactInfo);
            getLog().info("Resolved for " + artifactInfo.toString());

            MavenProject project = buildProjectModel(artifactInfo);
            getLog().info(project.toString());
            getLog().info(" " + project.getDependencies().size());
            for (Dependency dep : project.getDependencies()){
                getLog().info(" - dep: " + dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion() + ":" + dep.getScope() ) ;
            }
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

}
