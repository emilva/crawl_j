package versioneye.persistence;


import versioneye.domain.GlobalSetting;

public interface IGlobalSettingDao {

    GlobalSetting getBy(String environment, String key) throws Exception;

    boolean setValue(String environment, String key, String value) throws Exception;

}
