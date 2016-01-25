package versioneye.mojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo( name = "sprayio", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class SprayioMojo extends HtmlMojo {

    static final Logger logger = LogManager.getLogger(SprayioMojo.class.getName());

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute();

            username = null;
            password = null;

            setRepository("sprayio");

            crawl();
        } catch( Exception exception ){
            logger.error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

}
