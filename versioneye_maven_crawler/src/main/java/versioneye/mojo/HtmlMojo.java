package versioneye.mojo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.htmlcleaner.TagNode;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.versioneye.persistence.IPomDao;
import versioneye.service.RabbitMqService;
import versioneye.utils.MavenCentralUtils;

import java.util.*;

public class HtmlMojo extends SuperMojo {

    static final Logger logger = LogManager.getLogger(HtmlMojo.class.getName());

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
            if (href.startsWith(":")){
                href = href.replaceFirst(":", "");
            }
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
            logger.error("ERROR in CrawlerMavenDefaultHhtml.follow(.,.) " + ex.toString());
            logger.error(ex.getStackTrace());
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
                logger.info("Skip pom, because it was already parsed! " + urlToPom);
                return ;
            }
            String message = repository.getName() + "::" + urlToPom;
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            logger.info(" [x] Sent '" + message + "'");
        } catch (Exception exception) {
            logger.error("urlToPom: " + urlToPom + " - " + exception.toString() );
            logger.error(exception.getStackTrace());
        }
    }

    protected void processPom(String urlToPom) {
        try{
            String groupId       = null;
            String artifactId    = null;
            String versionNumber = null;
            String packaging     = null;

            urlToPom = urlToPom.replaceAll("/:", "/");
            TagNode pom = httpUtils.getPageForResource(urlToPom, username, password);
            if (pom == null){
                Model model   = mavenCentralUtils.fetchModelFromUrl(urlToPom, username, password);
                groupId       = model.getGroupId();
                artifactId    = model.getArtifactId();
                versionNumber = model.getVersion();
                packaging     = model.getPackaging();
            } else {
                HashMap<String, String> properties = mavenUrlProcessor.getProperties(pom, null);
                groupId       = mavenUrlProcessor.getGroupId(    pom, properties);
                artifactId    = mavenUrlProcessor.getArtifactId( pom, properties);
                versionNumber = mavenUrlProcessor.getVersion(    pom, properties);
                packaging     = mavenUrlProcessor.getPackaging(  pom, properties);
            }

            if (groupId == null || artifactId == null || versionNumber == null){
                logger.error("ERROR: could not fetch GAV for " + urlToPom);
                return ;
            }

            boolean existAlreadyLowerCase = productDao.doesVersionExistAlreadyByGA( groupId.toLowerCase(), artifactId.toLowerCase(), versionNumber );
            boolean existAlready          = productDao.doesVersionExistAlreadyByGA( groupId, artifactId, versionNumber );
            if (existAlreadyLowerCase || existAlready){
                logger.info(" --- Exists already: " + groupId + "/" + artifactId + ":" + versionNumber);
                pomDao.create(urlToPom);
                return ;
            }

            if (packaging != null && packaging.equalsIgnoreCase("pom")){
                logger.info(" --- Skipp parent pom " + urlToPom);
                return ;
            }

            logger.info(" --- Process: " + groupId + "/" + artifactId + ":" + versionNumber);
            Artifact artifact = getArtifact(groupId + ":" + artifactId + ":pom:" + versionNumber);
            ArtifactResult result = resolveArtifact(artifact);
            resolveDependencies(artifact);
            parseArtifact(result.getArtifact(), null);
            pomDao.create(urlToPom);
        } catch (Exception exception) {
            logger.error("urlToPom: " + urlToPom + " - " + exception.toString());
            logger.error(exception.getStackTrace());
        }
    }

    protected void initTheRabbit(){
        try {
            Properties properties = getProperties();
            String rabbitmqAddr = properties.getProperty("rabbitmq_addr");
            String rabbitmqPort = properties.getProperty("rabbitmq_port");

            if (rabbitmqAddr == null || rabbitmqAddr.isEmpty()){
                rabbitmqAddr = System.getenv("RM_PORT_5672_TCP_ADDR");
                rabbitmqPort = System.getenv("RM_PORT_5672_TCP_PORT");
            }
            logger.info("RM_PORT_5672_TCP_ADDR: " + rabbitmqAddr + " RM_PORT_5672_TCP_PORT: " + rabbitmqPort);

            connection = RabbitMqService.getConnection(rabbitmqAddr, new Integer(rabbitmqPort));
            channel = connection.createChannel();
        } catch (Exception exception){
            logger.error("ERROR in initTheRabbit - " + exception.toString());
            logger.error(exception.getStackTrace());
        }
    }

    protected void closeTheRabbit(){
        try{
            channel.close();
            connection.close();
        } catch (Exception exception){
            logger.error("ERROR in closeTheRabbit - " + exception.toString());
            logger.error(exception.getStackTrace());
        }
    }

}
