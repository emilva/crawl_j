package versioneye.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import versioneye.domain.Crawle;
import versioneye.domain.Repository;
import versioneye.maven.MavenUrlProcessor;
import versioneye.utils.HttpUtils;
import versioneye.utils.LogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:32 PM
 */
public class CrawlerMavenDefaultHtml implements ICrawl {

    private static final String crawlerName = "crawlerMavenDefaultHtml";
    private static final String version = "0.2";
    private List<Repository> repositories;
    private Repository repository;
    private MavenUrlProcessor mavenUrlProcessor;
    private LogUtils logUtils;
    private HttpUtils httpUtils;
    private Crawle crawle;
    private String split1Pattern = "(?i)href=\"";
    private String split2Pattern = "\".*";
    private String execGroup;
    private String startPoint;
    private boolean threadable = true;

    static final Logger logger = LogManager.getLogger(CrawlerMavenDefaultHtml.class.getName());

    public CrawlerMavenDefaultHtml(){
        logger.info("new CrawlerMavenDefaultHtml");
    }

    public void run() {
        crawl();
    }

    public void crawl() {
        Repository repository = getRepository();
        if (repository.getFollowType().equals("text")){
            split1Pattern = "(?i)href=(?=.*['\"])(.*?)(?=.*['\"])>";
            split2Pattern = "<.*";
        }
        String src = repository.getSrc();
        if (startPoint == null || startPoint.equals("")){
            startPoint = src;
        }
        Date start = new Date();
        logUtils.logStart(start, crawlerName, startPoint);

        follow(startPoint, "");

        logUtils.logStop(start, crawlerName, startPoint);
    }

    public void crawlePackage(String name) {
        follow(name, "");
    }

    public List<String> getFirstLevelList() {
        String src = getRepository().getSrc();
        List<String> links = getLinksFromPage(src);
        List<String> results = new ArrayList<String>();
        for (String href : links){
            if (isFollowable(href)){
                String newUrl = src + href;
                results.add(newUrl);
            }
        }
        return results;
    }

    public void follow(String currentUrl, String prevUrl){
        List<String> links = getLinksFromPage(currentUrl);
        for (String href : links){
            if (href.startsWith(":")){
                href = href.replaceFirst(":", "");
            }
            if (isFollowable(href)){
                String newUrl = currentUrl + href;
                follow(newUrl, currentUrl);
            } else if (href.endsWith(".pom") && !href.contains("SNAPSHOT")) {
                String newUrl = currentUrl + href;
//                mavenUrlProcessor.updateNode(newUrl, currentUrl, prevUrl, getRepository(), crawle);
                return ;
            }
        }
    }

    public List<String> getLinksFromPage(String src){
        List<String> links = new ArrayList<String>();
        try{
            String html = httpUtils.getHttpResponse(src);
            String[] firstS = html.split(split1Pattern);
            for (String element: firstS){
                String link = element.replaceAll(split2Pattern, "");
                if (repository.isReplaceWithRepoSrc())
                    link = link.replaceFirst(getRepository().getSrc(), "");
                else
                    link = link.replaceFirst(src, "");
                if (link.startsWith("#")){
                    link = link.replaceFirst("#", "");
                }
                links.add(link);
            }
        } catch (Exception ex) {
            logUtils.addError("ERROR in CrawlerMavenDefaultHtml.follow(.,.)", ex.toString(), crawle);
            return new ArrayList<String>();
        }
        return links;
    }

    private boolean isFollowable(String url){
        return url.length() > 1 && url.endsWith("/") &&
                !url.startsWith("Parent Directory") &&
                !url.endsWith("../") &&
                !url.startsWith(".") &&
                !url.startsWith("www.") &&
                !url.startsWith("<") &&
                !url.startsWith("?") &&
                !url.startsWith("/") &&
                !url.startsWith("http://") &&
                !url.startsWith("https://");
    }

    public String getName() {
        return crawlerName;
    }

    public Crawle getCrawle() {
        return crawle;
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

    public Repository getRepository() {
        if (repository == null && repositories != null && !repositories.isEmpty()){
            repository = repositories.get(0);
        }
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
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

    public void setHttpUtils(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

    public void setLogUtils(LogUtils logUtils) {
        this.logUtils = logUtils;
    }

}
