package versioneye.service;

import com.versioneye.service.DependencyService;
import org.apache.maven.model.Model;
import com.versioneye.domain.Dependency;
import com.versioneye.domain.Product;
import com.versioneye.utils.HttpUtils;

import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 8/11/13
 * Time: 10:42 AM
 */
public class DependencyModelService {

    private DependencyService dependencyService;
    private HttpUtils httpUtils;

    public void createDependenciesIfNotExist(Model model, Product product, HashMap<String, String> properties){
        for (org.apache.maven.model.Dependency dependency: model.getDependencies()){
            processDependency(dependency, product, model, properties);
        }
    }

    private void processDependency(org.apache.maven.model.Dependency dependency, Product product, Model model,
                                   HashMap<String, String> propertiesMap){
        try {
            String groupId    = dependency.getGroupId();
            String artifactId = dependency.getArtifactId();
            if (groupId == null || groupId.trim().equals("") || artifactId == null || artifactId.trim().equals(""))
                return;
            String version = parseVersion(model, dependency, propertiesMap);
            String scope = fetchScope(dependency);
            Dependency dependencyObj = dependencyService.buildDependency(groupId, artifactId, version, scope, product);
            dependencyService.createDependencyIfNotExist(dependencyObj);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private String parseVersion(Model model, org.apache.maven.model.Dependency dependency, HashMap<String, String> propertiesMap){
        String version = dependency.getVersion();
        if (version == null){
            version = fetchVersionFromDependencyManagement(model, dependency);
        }
        String parsedVersionFromModel = parseVariable(version, model.getProperties());
        return httpUtils.checkVariables(parsedVersionFromModel, propertiesMap);
    }

    private String fetchVersionFromDependencyManagement(Model model, org.apache.maven.model.Dependency dependency){
        if (model == null || model.getDependencyManagement() == null){
            return null;
        }
        for (org.apache.maven.model.Dependency dep : model.getDependencyManagement().getDependencies() ){
            if ( dep.getGroupId().equals(dependency.getGroupId() ) && dep.getArtifactId().equals(dependency.getArtifactId()) ){
                return dep.getVersion();
            }
        }
        return null;
    }

    private String parseVariable(String val, Properties properties){
        if (val == null || !val.startsWith("${"))
            return val;
        String key = val.replaceFirst(Pattern.quote("$"), "");
        key = key.replaceFirst(Pattern.quote("{"), "");
        key = key.replaceAll(Pattern.quote("}"), "");
        String new_val = properties.getProperty(key);
        if (new_val != null && !new_val.equals(val))
            return new_val;
        return val;
    }

    private String fetchScope(org.apache.maven.model.Dependency dependency){
        String scope = dependency.getScope();
        if (scope == null)
            scope = "compile";
        return scope;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public void setHttpUtils(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

}
