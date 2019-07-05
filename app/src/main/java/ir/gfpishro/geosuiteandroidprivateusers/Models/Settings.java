package ir.gfpishro.geosuiteandroidprivateusers.Models;

import org.oscim.core.GeoPoint;

import java.io.Serializable;

public class Settings implements Serializable {
    public int zoom = 15;
    public String cityCode = "001";
    private double[] currentLocation = new double[]{35.835530, 50.979760};

    public GeoPoint getCurrentLocationPoint() {
        return new GeoPoint(currentLocation[0], currentLocation[1]);
    }

    public double[] getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(double lat, double lon) {
        currentLocation[0] = lat;
        currentLocation[1] = lon;
    }

    public int maxZoom = 25;
    public int minZoom = 10;
    public int syncIntervalSecond = 120;
    public int timeOutSecond = 60;
    public double mapLimitMinX = 40;
    public double mapLimitMinY = 20;
    public double mapLimitMaxX = 60;
    public double mapLimitMaxY = 40;
    public boolean isTiltEnable = false;
    public boolean isRotationEnable = true;
    //    public String mapServerIp = "http://192.168.88.50:3393";
//    public String serverIp = "http://192.168.88.50:3393";
    public String mapServerIp = "http://192.168.1.2:8080";
    public String serverIp = "http://192.168.1.2";
    //DoNotSaveInCache
    public boolean isOnline = true;
    public boolean isLocationFound = false;
    public boolean isLocationMocked = false;
    public boolean isHSEEnabled = false;
    public boolean isCathodeEnabled = false;
    public transient boolean pingSwitchChanged = false;

    public String getNetworkMessage() {
        return isOnline ? "انلاین" : "افلاین";
    }
}
