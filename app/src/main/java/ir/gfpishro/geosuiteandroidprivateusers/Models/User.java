package ir.gfpishro.geosuiteandroidprivateusers.Models;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import ir.gfpishro.geosuiteandroidprivateusers.Activities.MissionsActivity;
import ir.gfpishro.geosuiteandroidprivateusers.Helpers.Utils;

public class User implements Serializable {
    private static User user;

    public static User getCurrentUser(Context context) {
        if (user != null) return user;
        String json = Utils.getSharedPref(context).getString(Keys.activeUser, "");
        user = new Gson().fromJson(json, User.class);
        return user;
    }

    public static boolean IsLoggedIn() {
        return user != null && user.getId() != -1;
    }

    public void setCurrentUser(Context context, User user) {
        SharedPreferences.Editor editor = Utils.getSharedPref(context).edit();
        editor.putString(Keys.activeUser, new Gson().toJson(user));
        editor.putString(Keys.lastUserName, user.getUsername());
        editor.putString(Keys.lastUserName + 2, user.getPassword());
        editor.apply();
        User.user = user;
    }

    public static String[] getLastUser(Context context) {
        return new String[]{Utils.getSharedPref(context).getString(Keys.lastUserName, ""), Utils.getSharedPref(context).getString(Keys.lastUserName + 2, "")};
    }

    public static void resetCurrentUser(Context context) {
        user = null;
        SharedPreferences pref = Utils.getSharedPref(context);
        pref.edit().putString(Keys.activeUser, "").apply();
        pref.edit().apply();
    }

    public static User getCachedUser(Context context, String username, String password) {
        String json = Utils.getSharedPref(context).getString(Keys.user(username), "");
        User user = new Gson().fromJson(json, User.class);
        return password.equals(user.password) ? user : null;
    }

    public void cacheUser(Context context, User user) {
        SharedPreferences.Editor editor = Utils.getSharedPref(context).edit();
        editor.putString(Keys.user(user.getUsername()), new Gson().toJson(user));
        editor.apply();
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getPhoneId(Context context) throws Exception {
        if (user != null) if (user.mId != null) return user.mId;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            throw new Exception("permission");
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) return telephonyManager.getDeviceId();
            else throw new NullPointerException();
        }
    }

    public static String getCredential(String username, String password) {
        return "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
    }

    public static String getCredential() {
        if (user == null) return "";
        return "Basic " + Base64.encodeToString((user.getUsername() + ":" + user.getPassword()).getBytes(), Base64.NO_WRAP);
    }

    private String mId;

    @SerializedName("user_id")
    @Expose
    private Integer id = -1;
    @SerializedName("first_name")
    @Expose
    private String firstName = "";
    @SerializedName("last_name")
    @Expose
    private String lastName = "";
    @SerializedName("National_code")
    @Expose
    private String nationalCode;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("Phone_number")
    @Expose
    private String phoneNumber;
    @SerializedName("birthday")
    @Expose
    private Integer birthday;
    @SerializedName("Personel_id")
    @Expose
    private String personalId;
    @SerializedName("Position")
    @Expose
    private String position;
    @SerializedName("city_code")
    @Expose
    private String cityCode = "001";
    @SerializedName("user_admin")
    @Expose
    private Boolean userAdmin;
    private Boolean isBusyWithMission = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getBirthday() {
        return birthday;
    }

    public void setBirthday(Integer birthday) {
        this.birthday = birthday;
    }

    public String getPersonalId() {
        return personalId;
    }

    public void setPersonalId(String personalId) {
        this.personalId = personalId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Boolean isAdmin() {
        return userAdmin;
    }

    public void setUserAdmin(Boolean userAdmin) {
        this.userAdmin = userAdmin;
    }

    public String getmId(Context context) {
        if ((user != null ? user.mId : null) != null)
            if (!user.mId.isEmpty())
                return user.mId;
        try {
            String mid = getPhoneId(context);
            if (user != null) setmId(mid);
            return mid;
        } catch (Exception e) {
            return null;
        }
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public static boolean IsUserValid(User u) {
        return !(u == null || u.getId() == -1);
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public Boolean getBusyWithMission() {
        return isBusyWithMission;
    }

    public void setBusyWithMission(Boolean busyWithMission) {
        isBusyWithMission = busyWithMission;
    }
}
