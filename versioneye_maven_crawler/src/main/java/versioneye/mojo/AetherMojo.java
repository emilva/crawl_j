package versioneye.mojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;

import java.util.Date;

@Mojo( name = "aether", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class AetherMojo extends SuperMojo {

    static final Logger logger = LogManager.getLogger(AetherMojo.class.getName());

    /**
     * The {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>} of the artifact to resolve.
     *
     * @parameter expression="${aether.artifactCoords}"
     */
    private String artifactCoords = "org.apache.maven.plugins:maven-checkstyle-plugin:pom:2.15";

    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
    }


    protected void processGav(String artifactCoords, Date lastModified) throws MojoExecutionException, MojoFailureException {
        Artifact artifact = getArtifact(artifactCoords);

        ArtifactResult artifactResult = resolveArtifact(artifact);
        resolveDependencies(artifact);

        try {
            parseArtifact(artifactResult.getArtifact(), lastModified);
        } catch ( Exception e ) {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

}
