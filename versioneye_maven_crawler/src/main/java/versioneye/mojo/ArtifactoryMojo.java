package versioneye.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import com.versioneye.domain.GlobalSetting;
import com.versioneye.domain.Repository;
import versioneye.dto.ArtifactoryFile;
import versioneye.dto.ArtifactoryRepoDescription;
import versioneye.dto.ArtifactoryRepoFileList;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mojo( name = "artifactory", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class ArtifactoryMojo extends HtmlMojo {

    static final Logger logger = LogManager.getLogger(ArtifactoryMojo.class.getName());
    private String baseUrl;
    private Set<String> poms = new HashSet<String>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute(); // Init Processors and Daos

            String env = System.getenv("RAILS_ENV");
            GlobalSetting gs = globalSettingDao.getBy(env, "mvn_repo_1_type");
            if (!gs.getValue().equals("artifactory")){
                logger.info("Skip artifactory because mvn_repo_1_type is not artifactory");
                return ;
            }

            fetchBaseUrl();
            fetchUserAndPassword();

            mavenRepository = mavenRepositoryDao.findByName("jcenter");
            addRepo(mavenRepository);
            setCurrentRepo(mavenRepository.getName(), mavenRepository.getUrl());

            gs = globalSettingDao.getBy(env, "mvn_art_single_repo");
            if (gs != null && !gs.getValue().trim().isEmpty()){
                logger.info("Crawl a single repo: " + gs.getValue());
                parseFilesFromRepo(gs.getValue());
            } else {
                logger.info("Crawl all repos");
                ArtifactoryRepoDescription[] repositories = fetchRepoList();
                if (repositories == null){
                    logger.info("No repositories found to scan.");
                    return ;
                }
                addCustomRepos(repositories);
                collectPoms(repositories);
            }

            logger.info("The End");
        } catch( Exception exception ){
            logger.error(exception);
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


    private ArtifactoryRepoDescription[] fetchRepoList() {
        try {
            String url = baseUrl + "/api/repositories";
            logger.info("fetch Repo list from: " + url);
            Reader resultReader = httpUtils.getResultReader( url, username, password );
            ObjectMapper mapper = new ObjectMapper();
            ArtifactoryRepoDescription[] repos = mapper.readValue(resultReader, ArtifactoryRepoDescription[].class);
            resultReader.close();
            return repos;
        } catch (Exception exception) {
            logger.error("ERROR in fetchRepoList " + exception.getMessage());
            logger.error(exception);
            return null;
        }
    }

    private void addCustomRepos(ArtifactoryRepoDescription[] repos){
        for (ArtifactoryRepoDescription repo: repos ){
            String url = baseUrl + "/" + repo.getKey();
            addAsRepo(repo.getKey(), url, true);
            logger.info("Add custom repo key: " + repo.getKey() + " repo url: " + url + " type: " + repo.getType());
        }
        logger.info("There are " + this.repos.size() + " remote repositories in the list");
    }

    private void collectPoms(ArtifactoryRepoDescription[] repos){
        for (ArtifactoryRepoDescription repo: repos ){
            crawlRepo(repo);
        }
    }

    private void crawlRepo(ArtifactoryRepoDescription repo){
        String repoType = repo.getType();
        if (repoType.equals("LOCAL") && getIgnoreLocale().equals("true")){
            logger.info("skip repo " + repo.getKey() + " because repo type is LOCAL");
            return;
        }
        if (repoType.equals("REMOTE") && getIgnoreRemote().equals("true")){
            logger.info("skip repo " + repo.getKey() + " because repo type is REMOTE");
            return;
        }
        if (repoType.equals("VIRTUAL") && getIgnoreVirtual().equals("true")){
            logger.info("skip repo " + repo.getKey() + " because repo type is VIRTUAL");
            return;
        }
        List<String> ignoreKeys = Arrays.asList( getIgnoreRepoKeys().split(",") );
        if (ignoreKeys.contains(repo.getKey())){
            logger.info("skip repo " + repo.getKey() + " because it is on ignore list.");
            return;
        }
        logger.info("Collect poms for: " + repo.getKey() + " url: " + repo.getUrl() + " type: " + repo.getType());
        parseFilesFromRepo(repo.getKey());
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

    // https://www.jfrog.com/confluence/display/RTF3X/Artifactory+REST+API#ArtifactoryRESTAPI-FileInfo
    //  -> File List
    private void parseFilesFromRepo(String repo){
        try{
            logger.info("Fetch files for repo: " + repo);
            String url = baseUrl + "/api/storage/" + repo + "?list&deep=1&listFolders=0&mdTimestamps=1";
            Reader resultReader = httpUtils.getResultReader( url, username, password );
            ObjectMapper mapper = new ObjectMapper();
            ArtifactoryRepoFileList fileList = mapper.readValue(resultReader, ArtifactoryRepoFileList.class);
            resultReader.close();

            logger.info("Found " + fileList.getFiles().length + " files in repo " + repo );

            String repoUrl = baseUrl + "/" + repo;
            setCurrentRepo(repo, repoUrl);

            int pomCount = 0;
            for (ArtifactoryFile file: fileList.getFiles() ){
                if (!file.getUri().endsWith(".pom")){
                    continue;
                }
                String pomUrl = baseUrl + "/" + repo + file.getUri();
                processPom( pomUrl );
                pomCount = pomCount + 1;
            }
            logger.info("pom files found in " + repo + ": " + pomCount);
        } catch (Exception ex) {
            logger.error("ERROR in parseFilesFromRepo " + ex.getMessage());
            logger.error(ex);
        }
    }

    private void setCurrentRepo(String name, String url){
        Repository repository = repositoryUtils.convertRepository(name, url, null);
        repository.setUsername(username);
        repository.setPassword(password);
        mavenProjectProcessor.setRepository(repository);
        mavenPomProcessor.setRepository(repository);
    }

    private String getIgnoreRemote(){
        try{
            String env = System.getenv("RAILS_ENV");
            GlobalSetting gs = globalSettingDao.getBy(env, "mvn_art_ignore_remote_repos");
            if (gs != null)
                return gs.getValue();
        } catch (Exception ex) {
            logger.error(ex);
        }
        return "true";
    }

    private String getIgnoreLocale(){
        try{
            String env = System.getenv("RAILS_ENV");
            GlobalSetting gs = globalSettingDao.getBy(env, "mvn_art_ignore_local_repos");
            if (gs != null)
                return gs.getValue();
        } catch (Exception ex) {
            logger.error(ex);
        }
        return "false";
    }

    private String getIgnoreVirtual(){
        try{
            String env = System.getenv("RAILS_ENV");
            GlobalSetting gs = globalSettingDao.getBy(env, "mvn_art_ignore_virtual_repos");
            if (gs != null)
                return gs.getValue();
        } catch (Exception ex) {
            logger.error(ex);
        }
        return "false";
    }

    private String getIgnoreRepoKeys(){
        try{
            String env = System.getenv("RAILS_ENV");
            GlobalSetting gs = globalSettingDao.getBy(env, "mvn_art_ignore_keys");
            if (gs != null)
                return gs.getValue();
        } catch (Exception ex) {
            logger.error(ex);
        }
        return "";
    }

}
