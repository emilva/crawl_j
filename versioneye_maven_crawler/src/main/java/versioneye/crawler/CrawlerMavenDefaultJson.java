package versioneye.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import versioneye.domain.Crawle;
import versioneye.domain.Repository;
import versioneye.dto.Document;
import versioneye.dto.ResponseJson;
import versioneye.maven.MavenPomProcessor;
import versioneye.maven.MavenUrlProcessor;
import versioneye.utils.HttpUtils;
import versioneye.utils.LogUtils;
import versioneye.utils.MavenCentralUtils;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:31 PM
 */
public class CrawlerMavenDefaultJson implements ICrawl {

    static final Logger logger = LogManager.getLogger(CrawlerMavenDefaultJson.class.getName());

    private String name = "crawlerSearchMavenOrg";
    private String version = "0.2";
    private List<Repository> repositories;
    private Repository repository;
    private MavenUrlProcessor mavenUrlProcessor;
    private MavenPomProcessor mavenPomProcessor;
    private LogUtils logUtils;
    private HttpUtils httpUtils;
    private Crawle crawle;
    private String execGroup;
    private String startPoint;
    private boolean threadable = true;

    private static final String LINK = "http://search.maven.org/#browse|";
    private static final String URL_PART_1 = "http://search.maven.org/solrsearch/select?q=parentId%3A\"";
    private static final String URL_PART_2 = "\"&rows=100000&core=filelisting&wt=json";
    private static final String FOUR_SEVEN = "47";

    public void run() {
        crawl();
    }

    public void crawl(){
        String parentId = startPoint;
        if (parentId == null)
            parentId = FOUR_SEVEN;

        Date start = new Date();
        logUtils.logStart(start, name, getRepository().getSrc());

        logger.info("Start with " + parentId);
        follow(parentId, "");

        logUtils.logStop(start, name, getRepository().getSrc());
    }

    public void crawlePackage(String name) {
        follow(name, "");
    }

    public List<String> getFirstLevelList(){
        List<String> list = new ArrayList<String>();
        ResponseJson resp = getResponse(FOUR_SEVEN);
        for (Document doc: resp.getResponse().getDocs()){
            String newPath = doc.getPath();
            if ( isFollowable(newPath) ){
                list.add(doc.getId());
            }
        }
        return list;
    }

    private void follow(String parentId, String grandParentId){
        ResponseJson resp = getResponse(parentId);
        for (Document doc: resp.getResponse().getDocs()){
            String newPath = doc.getPath();
            if ( isFollowable(newPath) ){
                follow(doc.getId(), parentId);
            } else if (newPath.endsWith(".pom") && !newPath.contains("SNAPSHOT")) {
                String urlToProduct = LINK + grandParentId;
                String urlToVersion = LINK + parentId;
                String urlToPom = MavenCentralUtils.LINK_FILE + newPath;
                mavenUrlProcessor.updateNode(urlToPom, urlToVersion, urlToProduct, getRepository(), crawle);
                return ;
            }
        }
    }

    private ResponseJson getResponse(String parentId){
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(URL_PART_1);
            sb.append(parentId);
            sb.append(URL_PART_2);
            String url = sb.toString();
            Reader resultReader = httpUtils.getResultReader( url );
            ObjectMapper mapper = new ObjectMapper();
            ResponseJson json = mapper.readValue(resultReader, ResponseJson.class);
            resultReader.close();
            return json;
        }  catch (Exception ex){
            logUtils.addError("ERROR in CrawlerMavenDefaultJson.getResponse(.,.)", ex.toString(), crawle);
            return null;
        }
    }

    private boolean isFollowable(String url){
        return url.endsWith("/") && !url.endsWith("../");
    }

    public Repository getRepository() {
        if (repository == null && repositories != null && !repositories.isEmpty()){
            repository = repositories.get(0);
        }
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public String getName() {
        return name;
    }

    public String getCrawlerVersion() {
        return version;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public Crawle getCrawle() {
        return crawle;
    }

    public void setCrawle(Crawle crawle) {
        this.crawle = crawle;
    }

    public String getExecGroup() {
        return execGroup;
    }

    public void setExecGroup(String execGroup) {
        this.execGroup = execGroup;
    }

    public void setMavenUrlProcessor(MavenUrlProcessor mavenUrlProcessor) {
        this.mavenUrlProcessor = mavenUrlProcessor;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public boolean isThreadable() {
        return threadable;
    }

    public void setThreadable(boolean threadable) {
        this.threadable = threadable;
    }

    public void setLogUtils(LogUtils logUtils) {
        this.logUtils = logUtils;
    }

    public void setHttpUtils(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

    public void setMavenPomProcessor(MavenPomProcessor mavenPomProcessor) {
        this.mavenPomProcessor = mavenPomProcessor;
    }
}
