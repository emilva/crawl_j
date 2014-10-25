package versioneye.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:41 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseJson {

    private ResponseHeader responseHeader;
    private Response response;

    public ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(ResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

}
