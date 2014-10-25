package versioneye.mojo;


import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.jackson.map.ObjectMapper;
import org.htmlcleaner.TagNode;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.RemoteRepository;
import versioneye.domain.MavenRepository;
import versioneye.domain.Repository;
import versioneye.dto.ArtifactoryFile;
import versioneye.dto.ArtifactoryRepoDescription;
import versioneye.dto.ArtifactoryRepoFileList;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Mojo( name = "artifactory", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class ArtifactoryMojo extends SuperMojo {

    private MavenRepository mavenRepository;
    private String baseUrl;
    private Set<String> poms = new HashSet<String>();
    private String username = "admin";
    private String password = "admin";

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute(); // Init Processors and Daos

            fetchBaseUrl();
            fetchUserAndPassword();

            mavenRepository = mavenRepositoryDao.findByName("jcenter");
            addRepo(mavenRepository);
            setCurrentRepo(mavenRepository.getName(), mavenRepository.getUrl());

            ArtifactoryRepoDescription[] repositories = fetchRepoList();
            addCustomRepos(repositories);
            collectPoms(repositories);
//            processPoms();

            getLog().info("The End");
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    private void fetchBaseUrl(){
        String env = System.getenv("RAILS_ENV");
        try{
            baseUrl = globalSettingDao.getBy(env, "mvn_repo_1").getValue();
        } catch( Exception ex){
            ex.printStackTrace();
            baseUrl = "http://localhost:8081/artifactory";
        }
    }

    private void fetchUserAndPassword(){
        String env = System.getenv("RAILS_ENV");
        try{
            username = globalSettingDao.getBy(env, "mvn_repo_1_user").getValue();
            password = globalSettingDao.getBy(env, "mvn_repo_1_password").getValue();
        } catch( Exception ex){
            ex.printStackTrace();
            username = "admin";
            password = "password";
        }
    }

    private ArtifactoryRepoDescription[] fetchRepoList() throws Exception {
        String url = baseUrl + "/api/repositories";
        Reader resultReader = httpUtils.getResultReader( url, username, password );
        ObjectMapper mapper = new ObjectMapper();
        ArtifactoryRepoDescription[] repos = mapper.readValue(resultReader, ArtifactoryRepoDescription[].class);
        resultReader.close();
        return repos;
    }

    private void addCustomRepos(ArtifactoryRepoDescription[] repos){
        for (ArtifactoryRepoDescription repo: repos ){
            String url = baseUrl + "/" + repo.getKey();
            addAsRepo(repo.getKey(), url, true);
            getLog().info("Add custom repo: " + repo.getKey() + " url: " + url + " type: " + repo.getType());
        }
        getLog().info("There are " + this.repos.size() + " remote repositories in the list");
    }

    private void collectPoms(ArtifactoryRepoDescription[] repos){
        for (ArtifactoryRepoDescription repo: repos ){
            if (!repo.getType().equals("LOCAL")){
                continue;
            }
            getLog().info("Collect poms for: " + repo.getKey() + " url: " + repo.getUrl() + " type: " + repo.getType());
            listFiles( repo.getKey() );
        }
    }

    private void processPoms(){
        getLog().info(" ---");
        getLog().info(" --- " + poms.size() + " unique pom files found");
        getLog().info(" ---");
        for (String pom : poms){
            processPom(pom);
        }
    }

    private void addAsRepo(String name, String url, boolean withAuth){
        RemoteRepository remoteRepository = new RemoteRepository(name, "default", url);
        remoteRepository.getPolicy(false).setUpdatePolicy("always");
        if (withAuth){
            Authentication auth = new Authentication(username, password);
            remoteRepository.setAuthentication(auth);
        }
        repos.add(remoteRepository);
        for (RemoteRepository repo : repos){
            repo.getPolicy(false).setUpdatePolicy("always");
        }
    }

    private void listFiles(String repo){
        try{
            String url = baseUrl + "/api/storage/" + repo + "?list&deep=1&listFolders=0&mdTimestamps=1";
            Reader resultReader = httpUtils.getResultReader( url, username, password );
            ObjectMapper mapper = new ObjectMapper();
            ArtifactoryRepoFileList fileList = mapper.readValue(resultReader, ArtifactoryRepoFileList.class);
            resultReader.close();

            String repoUrl = baseUrl + "/" + repo;
            setCurrentRepo(repo, repoUrl);

            for (ArtifactoryFile file: fileList.getFiles() ){
                if (!file.getUri().endsWith(".pom")){
                    continue;
                }
                String pomUrl = baseUrl + "/" + repo + file.getUri();
                getLog().info(" - Add: " + pomUrl);
//                poms.add(pomUrl);
                processPom( pomUrl );
            }
        } catch (Exception ex) {
            getLog().error(ex);
        }
    }

    private void processPom(String urlToPom) {
        try{
            getLog().info("process file " + urlToPom);
            TagNode pom = httpUtils.getPageForResource(urlToPom, username, password);
            HashMap<String, String> properties = mavenUrlProcessor.getProperties(pom, null);
            String versionNumber = mavenUrlProcessor.getVersion(pom, properties);
            String groupId = mavenUrlProcessor.getGroupId(pom, properties);
            String artifactId = mavenUrlProcessor.getArtifactId(pom, properties);
            String packaging = mavenUrlProcessor.getPackaging(pom, properties);
            getLog().info(" -- " + groupId + ":" + artifactId + ":" + versionNumber + " - " + packaging);

            if (packaging != null && packaging.equalsIgnoreCase("pom")){
                getLog().info(" --- Skipp parent pom --- " + urlToPom);
                return ;
            }

            ArtifactInfo artifactInfo = new ArtifactInfo();
            artifactInfo.groupId = groupId;
            artifactInfo.artifactId = artifactId;
            artifactInfo.version = versionNumber;

            resolveDependencies(artifactInfo);
            parseArtifact(artifactInfo);
        } catch (Exception exception) {
            getLog().error(exception);
        }
    }

    private void setCurrentRepo(String name, String url){
        Repository repository = repositoryUtils.convertRepository(name, url, null);
        repository.setUsername(username);
        repository.setPassword(password);
        mavenProjectProcessor.setRepository(repository);
        mavenPomProcessor.setRepository(repository);
    }

}
