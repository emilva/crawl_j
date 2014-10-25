package versioneye.utils;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 8/11/13
 * Time: 11:22 PM
 */
public class MavenUrlUtils {

    public static final String LINK_FILE = "http://search.maven.org/remotecontent?filepath=";

    public String getProductUrl(String groupId, String artifactId){
        return "http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22"+ groupId +"%22%20AND%20a%3A%22"+ artifactId +"%22";
    }

    public String getProductJsonUrl(String groupId, String artifactId){
        return "http://search.maven.org/solrsearch/select?q=g:%22"+groupId+"%22+AND+a:%22"+artifactId+"%22&core=gav&rows=20&wt=json";
    }

    public String getVersionUrl(String groupId, String artifactId, String version){
        return "http://search.maven.org/#artifactdetails%7C"+ groupId +"%7C"+ artifactId +"%7C"+ version +"%7Cbundle";
    }

    public String getPomUrl(String groupId, String artifactId, String version){
        return LINK_FILE + groupId.replaceAll("\\.", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
    }

    public String getPomUrl(String baseUrl, String groupId, String artifactId, String version){
        StringBuffer pom = new StringBuffer();
        pom.append(baseUrl);
        if (!baseUrl.endsWith("/")){
            pom.append("/");
        }
        pom.append(groupId.replaceAll("\\.", "/"));
        pom.append("/");
        pom.append(artifactId);
        pom.append("/");
        pom.append(version);
        pom.append("/");
        pom.append(artifactId);
        pom.append("-");
        pom.append(version);
        pom.append(".pom");
        return pom.toString();
    }

    public String getVersionUrl(String baseUrl, String groupId, String artifactId, String version){
        StringBuffer pom = new StringBuffer();
        pom.append(baseUrl);
        if (!baseUrl.endsWith("/")){
            pom.append("/");
        }
        pom.append(groupId.replaceAll("\\.", "/"));
        pom.append("/");
        pom.append(artifactId);
        pom.append("/");
        pom.append(version);
        return pom.toString();
    }

    public String getProductUrl(String baseUrl, String groupId, String artifactId){
        StringBuffer pom = new StringBuffer();
        pom.append(baseUrl);
        if (!baseUrl.endsWith("/")){
            pom.append("/");
        }
        pom.append(groupId.replaceAll("\\.", "/"));
        pom.append("/");
        pom.append(artifactId);
        return pom.toString();
    }

}
