package ir.gfpishro.geosuiteandroidprivateusers.Models;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.StringUtils;

public class Keys {
    final public static String settings = "SETTINGS";
    final public static String activeUser = "active_user";
    final public static String lastUserName = "last_username";
    final public static String mapsFolder = "/gfp/";
    final public static String offlineDataBase = "gfp_offline.sqlite";
    final public static String pathGraphHopper = "iran-gh";
    final public static String mapCodeFolder = "mapCode/";
    final public static String marketingCodeDirectory = "marketing_code";
    final public static String riserCode = "riser_code";
    //statics
    final public static String MARKETING_CODE = "MARKETINGCODE";
    final public static String STEAL_TYPE = "STEAL_TYPE";
    final public static String videosDirectory = "videos/";
    final public static String onlineMapCacheFolder = "onlineMapCacheFolder/";

    public static String layers(int uid) {
        return "layers_" + uid;
    }

    public static String user(String userName) {
        return "USER_" + userName;
    }

    public static String missionsList(int uid) {
        return "MISSIONS" + uid;
    }

    public static String editLayer(int uid) {
        return "EDIT_LAYER" + uid;
    }

    public static String report(Integer missionId) {
        return "REPORT_" + missionId;
    }

    public static String lastMissionUpdate(Integer uid) {
        return "last_mission_update" + uid;
    }

    public static String missingInReport(Integer mission_id) {
        return "M_REPORT_" + mission_id;
    }

    public static String statics(int uid) {
        return "statics_" + uid;
    }

    public static String mapCodeCsvFileName(int id, String type) {
//        return type + "_" + (id / 100) * 100 + ".csv";
        return type + StringUtils.getTodayFormatterDate("yyyyMMdd") + ".csv";
    }

    public static String accessibleLayers(Integer id) {
        return "accessibility_" + id;
    }

    public static String accessibleForms(Integer id) {
        return "accessibility_forms_" + id;
    }
}
