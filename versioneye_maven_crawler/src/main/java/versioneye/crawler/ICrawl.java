package versioneye.crawler;

import versioneye.domain.Crawle;
import versioneye.domain.Repository;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:35 PM
 */
public interface ICrawl extends Runnable {

    void crawl();

    Crawle getCrawle();

    void crawlePackage(String name);

    List<String> getFirstLevelList();

    String getName();

    public void setStartPoint(String startPoint);
    public String getStartPoint();

    String getCrawlerVersion();

    List<Repository> getRepositories();

    void setRepositories(List<Repository> repositories);

    Repository getRepository();

    void setRepository(Repository repository);

    public String getExecGroup();

    public void setExecGroup(String execGroup);

    boolean isThreadable();

    void setThreadable(boolean threadable);

}
