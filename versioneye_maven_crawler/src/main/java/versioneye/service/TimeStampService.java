package versioneye.service;

import org.codehaus.jackson.map.ObjectMapper;
import versioneye.dto.ResponseJson;
import versioneye.utils.HttpUtils;

import java.io.Reader;
import java.util.Date;


public class TimeStampService {

    private HttpUtils httpUtils;

    public Date getTimeStampFor(String g, String a, String v){
        try{
            String url = "http://search.maven.org/solrsearch/select?q=g:%22" + g + "%22+AND+a:%22" + a + "%22+AND+v:%22" + v + "%22&rows=20&wt=json";
            Reader resultReader = httpUtils.getResultReader( url );
            ObjectMapper mapper = new ObjectMapper();
            ResponseJson json = mapper.readValue(resultReader, ResponseJson.class);
            resultReader.close();
            long timestamp = json.getResponse().getDocs()[0].getTimestamp();
            return new Date(timestamp);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public HttpUtils getHttpUtils() {
        return httpUtils;
    }

    public void setHttpUtils(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }
}
