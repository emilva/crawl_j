package com.versioneye.crawler.crawl;

import com.versioneye.domain.Crawle;
import com.versioneye.domain.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: reiz
 * Date: 11/14/11
 * Time: 9:17 PM
 */
public interface ICrawl extends Runnable{

    void crawl();

    Crawle getCrawle();

    void crawlePackage(String name);

    Set<String> getFirstLevelList();

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