package ir.gfpishro.geosuiteandroidprivateusers.Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import ir.gfpishro.geosuiteandroidprivateusers.BuildConfig;
import ir.gfpishro.geosuiteandroidprivateusers.Controls.UiControl;
import ir.gfpishro.geosuiteandroidprivateusers.Models.*;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Log.TeamLocation;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.GeoJson;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Map.MapFile;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Mission.Mission;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Api {
    String[] agent = new String[]{"User-Agent", "mobile/" + BuildConfig.VERSION_NAME};
    String[] contentType = new String[]{"Content-Type", "application/json"};

    /// GET
    @GET("/activity")
    Call<Void> ping();

    @GET("/views/statics/")
    Call<JsonElement> getStatics(@Header("Authorization") String auth);

    @GET("/views/mission_information/{mobile_key}")
    Call<List<Mission>> getMissions(@Header("Authorization") String auth, @Path("mobile_key") String mobile_key);

    @GET("/views/login/")
    Call<User> getUser(@Header("Authorization") String auth);

    @GET("/views/accessibility_layer/{uid}")
    Call<JsonElement> getAccessibilityLayers(@Header("Authorization") String auth, @Path("uid") Integer uid);

    //Sample Data: {"user_id":"1","user":[{"form_id" : "1","access" : "1"}]}
    @GET("/views/accessibility_form/{uid}")
    Call<JsonElement> getAccessibilityForms(@Header("Authorization") String auth, @Path("uid") Integer uid);

    @GET("/views/street_search/")
    Call<GeoJson> getSearchStreet(@Header("Authorization") String auth,
                                  @Query("name") String name,
                                  @Query("limit") int limit,
                                  @Query("city_code") String city_code);

    @GET("/views/riser_search/")
    Call<GeoJson> getSearchRiser(@Header("Authorization") String auth,
                                 @Query("r_num") String r_num,
                                 @Query("limit") int limit,
                                 @Query("output") int outputType, // 1 for geo-json / else for json
                                 @Query("geom") String geom,
                                 @Query("city_code") String city_code);

    @GET("/views/parcel_search/")
    Call<GeoJson> getSearchParcel(@Header("Authorization") String auth,
                                  @Query("code_address") String code_address,
                                  @Query("limit") int limit,
                                  @Query("geom") String geom,
                                  @Query("city_code") String city_code);

    @GET("/views/valve_search/")
    Call<GeoJson> getSearchValve(@Header("Authorization") String auth,
                                 @Query("v_num") String v_num,
                                 @Query("limit") int limit,
                                 @Query("type") String type,//pb,bg
                                 @Query("city_code") String city_code);

    @GET("/views/route/")
    Call<JsonElement> getRoute(@Header("Authorization") String auth,
                               @Query("x1") double lng1, @Query("y1") double lat1,
                               @Query("x2") double lng2, @Query("y2") double lat2);

    @GET("/views/user_layer_information/")
    Call<JsonArray> getEditLayers(@Header("Authorization") String auth,
                                  @Query("id") int userId);

    @GET("/views/from_valve_to_valve_region/")
    Call<GeoJson> getRegionFromValve(@Header("Authorization") String auth,
                                     @Query("id") String valveId,
                                     @Query("type") String bg_pg,
                                     @Query("result") String region); //riser or parcel or pgpipe or bgpipe

    @GET("/views/from_valve_region_to_valve/")
    Call<GeoJson> getValveFromRegion(@Header("Authorization") String auth,
                                     @Query("id") String regionId,
                                     @Query("type") String bg_pg,
                                     @Query("input") String region); //riser or parcel or pgpipe or bgpipe

    @GET("/views/offlinemaps_list/")
    Call<List<MapFile>> getMapFiles(@Header("Authorization") String auth);

    @GET("/views/from_geom_to_valve/")
    Call<JsonElement> getValveFromGeom(@Header("Authorization") String credential, @Query("geom") String geom, @Query("output") String outType);

    @GET("/views/steal_information/")
    Call<Steal[]> getSteals(@Header("Authorization") String auth,
                            @Query("geom") String geom,
                            @Query("status") String status,
                            @Query("distance") long distance);

    @GET("/views/min_distance/")
    Call<List<GeoJson>> getEverythingInDistance(@Header("Authorization") String auth, @Query("geom") String geom);

    @GET("/views/customer_search/")
    Call<JsonArray> getCustomers(@Header("Authorization") String authKey,
                                 @Query("code_address") String code_address,
                                 @Query("city_code") String cityCode,
                                 @Query("geom") String geom);
    // END GET

    // POST

    @POST("/views/update_objects/")
    Call<Void> postUpdateValues(@Header("Authorization") String auth, @Body JsonObject json);

    @POST("/views/add_edit_steal/")
    Call<Void> postSteal(@Header("Authorization") String auth, @Body Steal[] steals);

    @POST("/views/add_edit_user_layer/")
    Call<Void> postEditLayers(@Header("Authorization") String auth, @Body JsonArray jsonLayer);

    @POST("/views/teamlocation/")
    Call<ServerStatus> postLocation(@Header("Authorization") String auth, @Body List<TeamLocation> location);

    @POST("/views/mission_reports/")
    Call<JsonArray> postReport(@Header("Authorization") String auth, @Body List<UiControl[]> jsonUiControls);

    @POST("/views/add_edit_cps/")
    Call<Void> postCPS(@Header("Authorization") String auth, @Body CPSObject object);

    @POST("/views/add_edit_soil_resistance/")
    Call<Void> postSoilResistance(@Header("Authorization") String auth, @Body SOILObject object);

    @POST("/views/add_edit_hse/")
    Call<Void> postHSE(@Header("Authorization") String auth, @Body HSEObject bindBack);

    @POST("/views/add_edit_mission/")
    Call<Mission> postEditMission(@Header("Authorization") String auth, @Body Mission mission);

    @POST("/views/add_edit_code/")
    Call<Void> postCodeInfo(@Header("Authorization") String auth, @Body CodeInfo codeInfo);
}