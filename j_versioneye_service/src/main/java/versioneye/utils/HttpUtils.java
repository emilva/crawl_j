package versioneye.utils;

import org.apache.commons.codec.binary.Base64;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 6/14/13
 * Time: 3:34 PM
 */
public class HttpUtils {

    private final HtmlCleaner cleaner = new HtmlCleaner();
    public static String userAgent = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";

    public TagNode getSingleNode(Object[] objects){
        if (objects == null || objects.length == 0)
            return null;
        return (TagNode) objects[0];
    }

    public String getHttpResponse(String address) throws Exception{
        URL url = new URL(address);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(30000); // 30 seconds time out

        String line = "";
        StringBuffer sb = new StringBuffer();
        BufferedReader input =  new BufferedReader(new InputStreamReader(conn.getInputStream()) );
        while((line = input.readLine())!=null)
            sb.append(line);
        input.close();
        return sb.toString();
    }

    public String getHttpResponse(String address, String username, String password) throws Exception {
        URL url = new URL(address);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(30000); // 30 seconds time out
        if (username != null && password != null){
            String user_pass = username + ":" + password;
            String encoded = Base64.encodeBase64String( user_pass.getBytes() );
            conn.setRequestProperty("Authorization", "Basic " + encoded);
        }
        String line = "";
        StringBuffer sb = new StringBuffer();
        BufferedReader input =  new BufferedReader(new InputStreamReader(conn.getInputStream()) );
        while((line = input.readLine())!=null)
            sb.append(line);
        input.close();
        return sb.toString();
    }

    public static int getResponseCode(String urlString) throws MalformedURLException, IOException {

        System.setProperty("http.agent", userAgent);
        URL url = new URL(urlString);
        HttpURLConnection huc =  (HttpURLConnection) url.openConnection();
        huc.setRequestMethod("GET");
        huc.setConnectTimeout(5000); // 5 seconds
        huc.setRequestProperty("User-Agent", userAgent);
        huc.connect();
        return huc.getResponseCode();
    }

    public TagNode getPageForResource(String resource) throws Exception {
        return getPageForResource(resource, null, null);
    }

    public TagNode getPageForResource(String resource, String username, String password) throws Exception {
        System.setProperty("http.agent", userAgent);
        URL url = new URL(resource);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000); // 5 seconds time out
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", userAgent);
        if (username != null && password != null){
            String user_pass = username + ":" + password;
            String encoded = Base64.encodeBase64String( user_pass.getBytes() );
            conn.setRequestProperty("Authorization", "Basic " + encoded);
        }
        int code = conn.getResponseCode();
        if (code == 200){
            return cleaner.clean(conn.getInputStream());
        }
        System.out.print("ERROR response code " + code + " for " + resource);
        return null;
    }

    public Object[] getObjectsFromPage(String resource, String xpath) throws Exception {
        URL url = new URL(resource);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(30000); // 30 seconds time out
        TagNode page = cleaner.clean(conn.getInputStream());
        Object[] objects = page.evaluateXPath("//table[@class=\"list\"]/tbody/tr/td/a");
        return objects;
    }

    public Reader getResultReader(String resource) throws Exception {
        return getResultReader(resource, null, null);
    }

    public Reader getResultReader(String resource, String username, String password) throws Exception {
        URL url = new URL(resource);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(30000); // 30 seconds time out
        if (username != null && password != null){
            String user_pass = username + ":" + password;
            String encoded = Base64.encodeBase64String( user_pass.getBytes() );
            connection.setRequestProperty  ("Authorization", "Basic " + encoded);
        }
        return new InputStreamReader(connection.getInputStream());
    }

    public String getSingleValue(Object[] objects, HashMap<String, String> properties){
        if (objects == null || objects.length == 0)
            return null;
        TagNode node = (TagNode) objects[0];
        if (node.getText() == null)
            return null;
        String value = node.getText().toString().trim();
        if (properties != null && !properties.isEmpty())
            value = checkVariables(value, properties);
        return value;
    }

    public String checkVariables(String val, HashMap<String, String> properties){
        if (val == null || !val.startsWith("${"))
            return val;
        String key = val.replaceFirst(Pattern.quote("$"), "");
        key = key.replaceFirst(Pattern.quote("{"), "");
        key = key.replaceAll(Pattern.quote("}"), "");
        String propertyValue = properties.get(key.toLowerCase());
        if (propertyValue != null && !propertyValue.trim().equals(""))
            return propertyValue;
        else
            return val;
    }

}
