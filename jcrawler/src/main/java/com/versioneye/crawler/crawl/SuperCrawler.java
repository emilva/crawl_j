package com.versioneye.crawler.crawl;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public abstract class SuperCrawler implements ICrawl {

    public static String encodeURI(String s) {
        String result = s;
        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }
        return result;
    }

}
