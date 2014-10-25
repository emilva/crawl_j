package versioneye.utils;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 8/9/13
 * Time: 6:00 PM
 */
public class MavenCentralUtilsTest {

    @Test
    public void doTest(){
        String groupId = "org.at.my.place";
        groupId = groupId.replaceAll("\\.", "/");
        assertEquals(groupId, "org/at/my/place");

        System.out.println(System.getenv("PATH"));
        System.out.println(System.getenv("LC_ALL"));
    }

//    @Test
//    public void doGetPomUrl() throws Exception {
//        MavenUrlUtils mavenUrlUtils = new MavenUrlUtils();
//        String url = mavenUrlUtils.getPomUrl("org.hibernate", "hibernate-core", "4.2.0.Final");
//        assertEquals(url, "http://search.maven.org/remotecontent?filepath=org/hibernate/hibernate-core/4.2.0.Final/hibernate-core-4.2.0.Final.pom");
//
//        System.out.println(url);
//
//        HttpUtils httpUtils = new HttpUtils();
//        int code = httpUtils.getResponseCode( url );
//        assertEquals(code, 200);
//
//        System.out.println("VersionEye!");
//    }

}
