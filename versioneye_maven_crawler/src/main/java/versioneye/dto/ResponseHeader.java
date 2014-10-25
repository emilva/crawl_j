package versioneye.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created with IntelliJ IDEA.
 * User: robertreiz
 * Date: 7/29/13
 * Time: 6:44 PM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseHeader {

    private Integer status;
    private String QTime;
    private Params params;

    static class Params{

        private String fl;
        private String sort;
        private String indent;
        private String q;
        private String core;
        private String wt;
        private String version;
        private String rows;

        public String getFl() {
            return fl;
        }
        public void setFl(String fl) {
            this.fl = fl;
        }
        public String getSort() {
            return sort;
        }
        public void setSort(String sort) {
            this.sort = sort;
        }
        public String getIndent() {
            return indent;
        }
        public void setIndent(String indent) {
            this.indent = indent;
        }
        public String getQ() {
            return q;
        }
        public void setQ(String q) {
            this.q = q;
        }
        public String getCore() {
            return core;
        }
        public void setCore(String core) {
            this.core = core;
        }
        public String getWt() {
            return wt;
        }
        public void setWt(String wt) {
            this.wt = wt;
        }
        public String getVersion() {
            return version;
        }
        public void setVersion(String version) {
            this.version = version;
        }
        public String getRows() {
            return rows;
        }
        public void setRows(String rows) {
            this.rows = rows;
        }
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @JsonProperty("QTime")
    public String getQTime() {
        return QTime;
    }

    @JsonProperty("QTime")
    public void setQTime(String qTime) {
        QTime = qTime;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

}
