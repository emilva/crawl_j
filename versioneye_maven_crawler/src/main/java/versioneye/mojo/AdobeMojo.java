package versioneye.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mojo( name = "adobe", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class AdobeMojo extends HtmlMojo {

    static final Logger logger = LogManager.getLogger(AdobeMojo.class.getName());

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute();

            username = null;
            password = null;

            setRepository("adobe");

            crawl();
        } catch( Exception exception ){
            logger.error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

}
