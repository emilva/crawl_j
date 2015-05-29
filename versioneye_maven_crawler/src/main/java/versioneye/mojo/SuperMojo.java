package versioneye.mojo;

import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.domain.MavenRepository;
import versioneye.domain.Repository;
import versioneye.maven.MavenPomProcessor;
import versioneye.maven.MavenProjectProcessor;
import versioneye.maven.MavenUrlProcessor;
import versioneye.persistence.IGlobalSettingDao;
import versioneye.persistence.IMavenRepostoryDao;
import versioneye.persistence.IProductDao;
import versioneye.utils.DependencyUtils;
import versioneye.utils.HttpUtils;
import versioneye.utils.PropertiesUtils;
import versioneye.utils.RepositoryUtils;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * The Mother of all Mojos!
 */
public abstract class SuperMojo extends AbstractMojo {

    @Component
    protected RepositorySystem system;

    @Component
    protected ProjectBuilder projectBuilder;

    @Parameter(defaultValue = "${localRepository}" )
    protected ArtifactRepository localRepository;

    @Parameter( defaultValue="${project}" )
    protected MavenProject project;

    @Parameter( defaultValue="${repositorySystemSession}" )
    protected RepositorySystemSession session;

    @Parameter( defaultValue = "${project.remoteProjectRepositories}")
    protected List<RemoteRepository> repos;

    @Parameter( defaultValue = "${basedir}", property = "basedir", required = true)
    protected File projectDirectory;

    protected MavenRepository mavenRepository;
    protected Repository repository;
    protected MavenProjectProcessor mavenProjectProcessor;
    protected MavenPomProcessor mavenPomProcessor;
    protected MavenUrlProcessor mavenUrlProcessor;
    protected IMavenRepostoryDao mavenRepositoryDao;
    protected IProductDao productDao;
    protected IGlobalSettingDao globalSettingDao;
    protected RepositoryUtils repositoryUtils = new RepositoryUtils();
    protected HttpUtils httpUtils;
    protected ApplicationContext context;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            context = new ClassPathXmlApplicationContext("applicationContext.xml");
            mavenProjectProcessor = (MavenProjectProcessor) context.getBean("mavenProjectProcessor");
            mavenPomProcessor = (MavenPomProcessor) context.getBean("mavenPomProcessor");
            mavenUrlProcessor = (MavenUrlProcessor) context.getBean("mavenUrlProcessor");
            mavenRepositoryDao = (IMavenRepostoryDao) context.getBean("mavenRepositoryDao");
            productDao = (IProductDao) context.getBean("productDao");
            globalSettingDao = (IGlobalSettingDao) context.getBean("globalSettingDao");
            httpUtils = (HttpUtils) context.getBean("httpUtils");
        } catch (Exception ex){
            getLog().error(ex);
        }
    }

    protected void parseLicenses(ArtifactInfo artifactInfo) throws Exception {
        MavenProject projectModel = buildProjectModel( artifactInfo );
        if (projectModel != null){
            getLog().info("projectModels key is " + projectModel.getGroupId() + "/" + projectModel.getArtifactId() + " : " + projectModel.getVersion());
            mavenProjectProcessor.updateLicense(projectModel);
        } else {
            getLog().error("projectModel is null. Try 2nd way!");
            mavenPomProcessor.updateLicense(artifactInfo.groupId, artifactInfo.artifactId, artifactInfo.version);
        }
    }

    protected void parseArtifact(ArtifactInfo artifactInfo) throws Exception {
        Date releasedAt = null;
        if (artifactInfo.lastModified > 0){
            releasedAt = new Date(artifactInfo.lastModified);
        }
        MavenProject projectModel = buildProjectModel( artifactInfo );
        if (projectModel != null){
            if (projectModel.getVersion().startsWith("${")){
                projectModel.setVersion(artifactInfo.version);
                getLog().info("------ 42 is not true ------");
                getLog().info("------ Upsi! ProjectModel doesn't seems to be complete! The Maven God is mad today!");
                getLog().info("------ 42 is not true ------");
            }
            getLog().info("projectModels key is " + projectModel.getGroupId() + "/" + projectModel.getArtifactId() + " : " + projectModel.getVersion());
            mavenProjectProcessor.updateProject( projectModel, releasedAt );
        } else {
            getLog().error("projectModel is null. Try 2nd way!");
            mavenPomProcessor.updateNode(artifactInfo.groupId, artifactInfo.artifactId, artifactInfo.version, releasedAt);
        }
    }

    protected void resolveDependencies(ArtifactInfo artifactInfo) throws Exception{
        try {
            DependencyUtils dependencyUtils = new DependencyUtils();
            CollectRequest collectRequest = dependencyUtils.getCollectRequest( artifactInfo, repos );
            DependencyNode root = system.collectDependencies(session, collectRequest).getRoot();
            DependencyRequest dependencyRequest = new DependencyRequest(root, null);
            system.resolveDependencies(session, dependencyRequest);
            root.accept(new PreorderNodeListGenerator());
        } catch (Exception ex) {
            for (RemoteRepository rr : repos){
                getLog().error("with repo: " + rr.getUrl());
            }
            getLog().error("error in resolveDependencies ", ex);
        }
    }

    protected MavenProject buildProjectModel(ArtifactInfo artifactInfo) throws Exception {
        return buildProjectModelFor(artifactInfo.groupId, artifactInfo.artifactId, artifactInfo.version);
    }

    public MavenProject buildProjectModelFor(String groupId, String artifactId, String versionNumber) throws Exception {
        try {
            ProjectBuildingRequest configuration = new DefaultProjectBuildingRequest();
            configuration.setLocalRepository( localRepository );
            configuration.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );
            configuration.setProcessPlugins( false );
            configuration.setRepositoryMerging( ProjectBuildingRequest.RepositoryMerging.REQUEST_DOMINANT );
            Properties properties = new Properties();
            for ( String key : session.getSystemProperties( ).keySet() ){
                properties.put( key, session.getSystemProperties().get(key) );
            }
            configuration.setSystemProperties( properties );
            configuration.setRepositorySession( session );

            org.apache.maven.artifact.Artifact artifact = new org.apache.maven.artifact.DefaultArtifact(
                    groupId, artifactId, versionNumber, "compile", "", "", new DefaultArtifactHandler());

            MavenProject project = projectBuilder.build(artifact, configuration).getProject();
            return project;
        } catch (Exception ex) {
            getLog().error("error in buildProjectModel ", ex);
        }
        return null;
    }

    protected void addRepo(MavenRepository repository){
        if (repository == null){
            return ;
        }
        for (RemoteRepository rr : repos ){
            if (rr.getId().equals(repository.getName())){
                return ;
            }
        }
        RemoteRepository remoteRepository = new RemoteRepository(repository.getName(), "default", repository.getUrl());
        remoteRepository.getPolicy(false).setUpdatePolicy("always");
        if (repository.getUsername() != null && !repository.getUsername().isEmpty() && repository.getPassword() != null && !repository.getPassword().isEmpty()){
            Authentication auth = new Authentication(repository.getUsername(), repository.getPassword());
            remoteRepository.setAuthentication(auth);
        }
        repos.add(remoteRepository);
        for (RemoteRepository repo : repos) {
            repo.getPolicy(false).setUpdatePolicy("always");
        }
        getLog().info("There are " + repos.size() + " remote repositories in the list");
    }

    protected void addAllRepos(){
        List<MavenRepository> repositories = mavenRepositoryDao.loadAll();
        for (MavenRepository repository : repositories){
            if (repository.getName().equals("central"))
                continue;
            if (repository.getUrl().equals("http://download.java.net/maven/2/"))
                continue;
            RemoteRepository remoteRepository = new RemoteRepository(repository.getName(), "default", repository.getUrl());
            remoteRepository.getPolicy(false).setUpdatePolicy("always");
            repos.add(remoteRepository);
        }
        getLog().info("There are " + repos.size() + " remote repositories in the list");
    }

    protected String getCacheDirectory(String name) throws Exception {
        Properties properties = getProperties();
        String baseDir = properties.getProperty("base_cache");
        File directory = new File(baseDir + "/" + name + "-cache");
        if (directory.exists()){
            directory.delete();
        }
        directory.mkdir();
        getLog().info("cache directory for Indexer: " + directory.getAbsolutePath());
        return directory.getAbsolutePath();
    }

    protected String getIndexDirectory(String name) throws Exception {
        Properties properties = getProperties();
        String baseDir = properties.getProperty("base_index");
        File directory = new File(baseDir + "/" + name + "-index");
        if (directory.exists()){
            directory.delete();
        }
        directory.mkdir();
        getLog().info("index directory for Indexer: " + directory.getAbsolutePath());
        return directory.getAbsolutePath();
    }

    protected Properties getProperties() throws Exception {
        PropertiesUtils propertiesUtils = new PropertiesUtils();
        String propFile = projectDirectory + "/src/main/resources/settings.properties";

        File file = new File(propFile);
        if (!file.exists())
            throw new MojoExecutionException(propFile + " is missing!");

        return propertiesUtils.readProperties(propFile);
    }

    protected void setRepository(String repoName){
        if (context == null){
            context = new ClassPathXmlApplicationContext("applicationContext.xml");
        }
        repository = (Repository) context.getBean(repoName);
        mavenProjectProcessor.setRepository(repository);
        mavenPomProcessor.setRepository(repository);

        mavenRepository = mavenRepositoryDao.findByName(repoName);
        addRepo(mavenRepository);
    }

}
