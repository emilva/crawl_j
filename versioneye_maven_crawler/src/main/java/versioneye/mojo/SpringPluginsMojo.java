package versioneye.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo( name = "springPlugins", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class SpringPluginsMojo extends HtmlMojo  {

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            super.execute();

            username = null;
            password = null;

            setRepository("springPlugins");

            crawl();
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

}
