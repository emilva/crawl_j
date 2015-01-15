package versioneye.mojo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.htmlcleaner.TagNode;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.persistence.IPomDao;
import versioneye.service.RabbitMqService;
import versioneye.utils.MavenCentralUtils;

import java.util.*;

public class HtmlMojo extends SuperMojo {

    protected String username = "admin";
    protected String password = "admin";

    protected String split1Pattern = "(?i)href=\"";
    protected String split2Pattern = "\".*";
    protected String startPoint;

    protected final static String QUEUE_NAME = "html_worker";
    protected Connection connection;
    protected Channel channel;
    protected IPomDao pomDao;
    protected MavenCentralUtils mavenCentralUtils;
    private Set<String> urls = new HashSet<String>(); // Follow each UEL only once per crawl.


    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        if (context == null){
            context = new ClassPathXmlApplicationContext("applicationContext.xml");
        }
        pomDao = (IPomDao) context.getBean("pomDao");
        mavenCentralUtils = (MavenCentralUtils) context.getBean("mavenCentralUtils");
    }

    public void crawl() {
        initTheRabbit();
        if (repository.getFollowType().equals("text")){
            split1Pattern = "(?i)href=(?=.*['\"])(.*?)(?=.*['\"])>";
            split2Pattern = "<.*";
        }
        String src = repository.getSrc();
        if (startPoint == null || startPoint.equals("")){
            startPoint = src;
        }
        if (startPoint != null && !startPoint.endsWith("/")){
            startPoint = startPoint + "/";
        }
        follow(startPoint);
        closeTheRabbit();
    }

    public void follow(String currentUrl){
        List<String> links = getLinksFromPage(currentUrl);
        for (String href : links){
            if (isFollowable(href) && !urls.contains(href)){
                String newUrl = currentUrl + href;
                urls.add(newUrl);
                follow(newUrl);
            } else if (href.endsWith(".pom") && !href.contains("SNAPSHOT")) {
                String newUrl = currentUrl + href;
                sendPom(newUrl);
//                processPom(newUrl);
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

    protected void sendPom(String urlToPom){
        try{
            if (pomDao.existsAlready(urlToPom)){
                getLog().info("Skip pom, because it was already parsed! " + urlToPom);
                return ;
            }
            String message = repository.getName() + "::" + urlToPom;
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        } catch (Exception exception) {
            getLog().error("urlToPom: " + urlToPom);
            getLog().error(exception);
        }
    }

    protected void processPom(String urlToPom) {
        try{
            String groupId       = null;
            String artifactId    = null;
            String versionNumber = null;
            String packaging     = null;

            TagNode pom = httpUtils.getPageForResource(urlToPom, username, password);
            if (pom != null){
                HashMap<String, String> properties = mavenUrlProcessor.getProperties(pom, null);
                groupId       = mavenUrlProcessor.getGroupId(pom, properties);
                artifactId    = mavenUrlProcessor.getArtifactId(pom, properties);
                versionNumber = mavenUrlProcessor.getVersion(pom, properties);
                packaging     = mavenUrlProcessor.getPackaging(pom, properties);
            } else {
                Model model = mavenCentralUtils.fetchModelFromUrl(urlToPom, username, password);
                groupId       = model.getGroupId();
                artifactId    = model.getArtifactId();
                versionNumber = model.getVersion();
                packaging     = model.getPackaging();
            }

            if (groupId == null || artifactId == null || versionNumber == null){
                getLog().error("ERROR: could not fetch GAV for " + urlToPom);
                return ;
            }

            boolean existAlready = productDao.doesVersionExistAlreadyByGA( groupId.toLowerCase(), artifactId.toLowerCase(), versionNumber );
            if (existAlready){
                pomDao.create(urlToPom);
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

            resolveDependencies(artifactInfo);
            parseArtifact(artifactInfo);
            pomDao.create(urlToPom);
        } catch (Exception exception) {
            getLog().error("urlToPom: " + urlToPom);
            getLog().error(exception);
        }
    }

    protected void initTheRabbit(){
        try {
            Properties properties = getProperties();
            String rabbitmqAddr = properties.getProperty("rabbitmq_addr");
            String rabbitmqPort = properties.getProperty("rabbitmq_port");
            connection = RabbitMqService.getConnection(rabbitmqAddr, new Integer(rabbitmqPort));
            channel = connection.createChannel();
        } catch (Exception exception){
            getLog().error(exception);
        }
    }

    protected void closeTheRabbit(){
        try{
            channel.close();
            connection.close();
        } catch (Exception exception){
            getLog().error(exception);
        }
    }

}
