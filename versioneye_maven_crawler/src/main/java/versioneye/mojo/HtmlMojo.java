package versioneye.mojo;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.htmlcleaner.TagNode;
import versioneye.domain.MavenRepository;
import versioneye.domain.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HtmlMojo extends SuperMojo {

    protected MavenRepository mavenRepository;
    protected String username = "admin";
    protected String password = "admin";

    protected String split1Pattern = "(?i)href=\"";
    protected String split2Pattern = "\".*";
    protected String startPoint;

    protected Repository repository;

    public void execute() throws MojoExecutionException, MojoFailureException {
       super.execute();
    }

    public void crawl() {
        if (repository.getFollowType().equals("text")){
            split1Pattern = "(?i)href=(?=.*['\"])(.*?)(?=.*['\"])>";
            split2Pattern = "<.*";
        }
        String src = repository.getSrc();
        if (startPoint == null || startPoint.equals("")){
            startPoint = src;
        }
        follow(startPoint, "");
    }

    public void follow(String currentUrl, String prevUrl){
        List<String> links = getLinksFromPage(currentUrl);
        for (String href : links){
            if (isFollowable(href)){
                String newUrl = currentUrl + href;
                follow(newUrl, currentUrl);
            } else if (href.endsWith(".pom") && !href.contains("SNAPSHOT")) {
                String newUrl = currentUrl + href;
                processPom(newUrl);
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
                    link = link.replaceFirst(repository.getSrc(), "");
                else
                    link = link.replaceFirst(src, "");
                if (link.startsWith("#")){
                    link = link.replaceFirst("#", "");
                }
                links.add(link);
            }
        } catch (Exception ex) {
            getLog().error("ERROR in CrawlerMavenDefaultHhtml.follow(.,.) " + ex.toString());
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

    protected void processPom(String urlToPom) {
        try{
            getLog().info("process file " + urlToPom);
            TagNode pom = httpUtils.getPageForResource(urlToPom, username, password);
            HashMap<String, String> properties = mavenUrlProcessor.getProperties(pom, null);
            String groupId       = mavenUrlProcessor.getGroupId(pom, properties);
            String artifactId    = mavenUrlProcessor.getArtifactId(pom, properties);
            String versionNumber = mavenUrlProcessor.getVersion(pom, properties);
            String packaging     = mavenUrlProcessor.getPackaging(pom, properties);

            String prodKey = groupId + "/" + artifactId;
            boolean existAlready = productDao.doesVersionExistAlready( "Java", prodKey, versionNumber );
            if (existAlready){
                getLog().info(" --- Skip package " + groupId + ":" + artifactId + ":" + versionNumber + " because already in DB.");
                return ;
            }

            if (packaging != null && packaging.equalsIgnoreCase("pom")){
                getLog().info(" --- Skipp parent pom " + urlToPom);
                return ;
            }

            ArtifactInfo artifactInfo = new ArtifactInfo();
            artifactInfo.groupId    = groupId;
            artifactInfo.artifactId = artifactId;
            artifactInfo.version    = versionNumber;

            getLog().info("process pom -- " + groupId + ":" + artifactId + ":" + versionNumber + " packaging: " + packaging);

            resolveDependencies(artifactInfo);
            parseArtifact(artifactInfo);
        } catch (Exception exception) {
            getLog().error(exception);
        }
    }

}
