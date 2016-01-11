package versioneye.mojo;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import versioneye.domain.GlobalSetting;
import versioneye.domain.Repository;
import versioneye.dto.ArtifactoryFile;
import versioneye.dto.ArtifactoryRepoDescription;
import versioneye.dto.ArtifactoryRepoFileList;

import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

@Mojo( name = "artifactory", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class ArtifactoryMojo extends HtmlMojo {

    private String baseUrl;
    private Set<String> poms = new HashSet<String>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute(); // Init Processors and Daos

            String env = System.getenv("RAILS_ENV");
            GlobalSetting gs = globalSettingDao.getBy(env, "mvn_repo_1_type");
            if (!gs.getValue().equals("artifactory")){
                getLog().info("Skip artifactory because mvn_repo_1_type is not artifactory");
                return ;
            }

            fetchBaseUrl();
            fetchUserAndPassword();

            mavenRepository = mavenRepositoryDao.findByName("jcenter");
            addRepo(mavenRepository);
            setCurrentRepo(mavenRepository.getName(), mavenRepository.getUrl());

            ArtifactoryRepoDescription[] repositories = fetchRepoList();
            addCustomRepos(repositories);
            collectPoms(repositories);
            processPoms();

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
        RemoteRepository.Builder builder = new RemoteRepository.Builder(name, "default", url);
        if (withAuth){
            AuthenticationBuilder authBuilder =  new AuthenticationBuilder();
            authBuilder.addUsername(username);
            authBuilder.addPassword(password);
            builder.setAuthentication(authBuilder.build());
        }

        RemoteRepository remoteRepository = builder.build();
        repos.add(remoteRepository);
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

    private void setCurrentRepo(String name, String url){
        Repository repository = repositoryUtils.convertRepository(name, url, null);
        repository.setUsername(username);
        repository.setPassword(password);
        mavenProjectProcessor.setRepository(repository);
        mavenPomProcessor.setRepository(repository);
    }

}
