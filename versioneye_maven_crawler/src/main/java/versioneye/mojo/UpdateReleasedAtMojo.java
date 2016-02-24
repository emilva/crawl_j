package versioneye.mojo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import versioneye.domain.Product;
import versioneye.domain.Version;

import java.util.Date;
import java.util.List;

@Mojo( name = "updateRA", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class UpdateReleasedAtMojo extends SuperMojo {

    static final Logger logger = LogManager.getLogger(UpdateReleasedAtMojo.class.getName());

    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        List<Product> products = productDao.fetchProductsFromRepo("Java", "http://repo.maven.apache.org/maven2");
        for (Product product : products){
            for (String versionString : product.getVersions().keySet()){
                Date date = timeStampService.getTimeStampFor(product.getGroupId(), product.getArtifactId(), versionString);
                if (date == null){
                    continue;
                }
                String logLine = product.getGroupId() + "/" + product.getArtifactId() + ":" + versionString + " - " + date.toString();
                System.out.println(logLine);
                logger.info(logLine);
                Version version = product.getVersions().get(versionString);
                version.setReleased_at(date);
                productDao.updateVersionReleaseTime(version);
            }
        }
    }

}
