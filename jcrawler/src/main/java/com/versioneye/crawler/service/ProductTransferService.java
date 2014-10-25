package com.versioneye.crawler.service;

import com.versioneye.crawler.dto.GitHubRepo;
import com.versioneye.crawler.dto.PipProduct;
import com.versioneye.crawler.dto.PipProductInfo;
import com.versioneye.crawler.dto.RubyGemProduct;
import versioneye.domain.Product;
import versioneye.domain.Repository;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/26/13
 * Time: 4:56 PM
 */
public class ProductTransferService {

    public void updateFromRubyGem(RubyGemProduct gem, Product product){
        product.setName(gem.getName());
        product.setProd_type("RubyGem");
        product.setLanguage("Ruby");
        product.setProd_key(gem.getName().toLowerCase());
        product.setAuthors(gem.getAuthors());
        product.setDescription(gem.getInfo());
        product.setLink("https://rubygems.org/gems/" + gem.getName() );
        product.setVersion_link("https://rubygems.org/gems/" + gem.getName() );
        product.setDownloads(gem.getDownloads());
        product.setVersion(gem.getVersion());

        Repository repository = new Repository();
        repository.setName("RubyGems");
        repository.setSrc("https://rubygems.org/");
        product.addRepository(repository);
    }

    public void updateFromPip(PipProduct pip, Product product){
        PipProductInfo info = pip.getInfo();
        product.setName(info.getName());
        product.setProd_type("PIP");
        product.setLanguage("Python");
        product.setProd_key(info.getName().toLowerCase());
        product.setAuthors(info.getAuthor());
        if (info.getSummary() != null && !info.getSummary().equals("UNKNOWN"))
            product.setDescription(info.getSummary());
        product.setLink(info.getPackage_url());
        product.setVersion_link(info.getRelease_url());
        product.setVersion(info.getVersion());
    }

    public void updateFromGitHub(GitHubRepo repo, Product product){
        product.setLanguage(repo.getLanguage());
        product.setProd_key( repo.getProd_key().toLowerCase() );
        product.setName(repo.getName());
        product.setDescription(repo.getDescription());
        product.setProd_type("GitHub");
        product.setLink(repo.getHtml_url());
        doReplacements(product);
    }

    private void doReplacements(Product product){
        if (product.getName().equals("php-src") || product.getName().equals("php/php-src") ){
            product.setName("php");
            product.setProd_key("php");
            product.setLanguage("PHP");
        }
    }

}
