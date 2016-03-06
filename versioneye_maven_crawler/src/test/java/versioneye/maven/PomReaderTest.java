package versioneye.maven;

import org.apache.maven.model.Model;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:59 PM
 */
public class PomReaderTest {

    @Test
    public void doReadSinglePom() throws Exception {
        Model model = PomReader.readSinglePom("pom.xml");
        assert model != null;
        System.out.println(model.getArtifactId());
        System.out.println(model.getParent().getArtifactId());
    }

    @Test
    public void doRead_1() throws Exception {
        String home = System.getProperty("user.home");
        File repoDir = new File( home + "/.m2/repository");
        Model model = PomReader.readModel (repoDir, "junit", "junit", "3.8.2");
        assert model != null;
        assert model.getArtifactId().equals("junit");
    }

}
