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
        Model model = PomReader.readSinglePom("/Users/robertreiz/workspace/maven/maven-core/pom.xml");
        System.out.println(model.getArtifactId());
        System.out.println(model.getParent().getArtifactId());
    }

    @Test
    public void doReadSinglePom_1() throws Exception {
        Model model = PomReader.readSinglePom("/Users/robertreiz/workspace/maven/pom.xml");
        System.out.println(model.getArtifactId());
        System.out.println(model.getParent().getArtifactId());
    }

    @Test
    public void doReadSinglePom_2() throws Exception {
        Model model = PomReader.readSinglePom("/Users/robertreiz/workspace/versioneye/crl/pom.xml");
        System.out.println(model.getArtifactId());
        System.out.println(model.getParent().getArtifactId());
    }

    @Test
    public void doRead_1() throws Exception {
        File repoDir = new File("/Users/robertreiz/.m2/repository");
        Model model = PomReader.readModel (repoDir, "org.apache.maven", "maven-core", "3.0.5");
        System.out.println(model.getArtifactId());
        System.out.println(model.getParent().getArtifactId());
    }

    @Test
    public void doRead_2() throws Exception {
        File repoDir = new File("/workspace/eol-globi-data/eol-globi-rest");
        Model model = PomReader.readModel (repoDir, "org.apache.maven", "maven-core", "3.0.5");
        System.out.println(model.getArtifactId());
        System.out.println(model.getParent().getArtifactId());
    }

}
