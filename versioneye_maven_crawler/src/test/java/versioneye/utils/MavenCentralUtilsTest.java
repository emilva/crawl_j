package versioneye.utils;

import com.versioneye.utils.HttpUtils;
import org.apache.maven.model.Model;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.util.Date;

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

        String url = "my_rep/";
        String nu = url.substring(0, url.length() - 1);
        System.out.println(nu);


        String groupId = "org.at.my.place";
        groupId = groupId.replaceAll("\\.", "/");
        assertEquals(groupId, "org/at/my/place");

        System.out.println(System.getenv("PATH"));
        System.out.println(System.getenv("LC_ALL"));
    }

    @Test
    public void doTest2(){

        Timestamp stamp = new Timestamp( 1455243639000L );
        Date date = new Date(stamp.getTime());
        System.out.println("date: " + date);

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
