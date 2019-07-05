package ir.gfpishro.geosuiteandroidprivateusers.Models.Map;

import android.os.Environment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.liulishuo.okdownload.DownloadTask;

import java.io.File;

import ir.gfpishro.geosuiteandroidprivateusers.Helpers.FileUtils;
import ir.gfpishro.geosuiteandroidprivateusers.Models.Keys;

public class MapFile {
    private DownloadTask downloadTask = null;
    @SerializedName("name")
    @Expose
    private String name = "";
    @SerializedName("url")
    @Expose
    private String url = "";
    @SerializedName("size")
    @Expose
    private Integer size = 0;
    @SerializedName("data modified")
    @Expose
    private Double dataModified = 0d;
    @SerializedName("SHA")
    @Expose
    private String SHA = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public long getDataModified() {
        return Math.round(dataModified);
    }

    public void setDataModified(Double dataModified) {
        this.dataModified = dataModified;
    }

    public boolean needsUpdate() {
        File file = new File(Environment.getExternalStorageDirectory() + Keys.mapsFolder + getName());
        boolean equalSHA = false;
        try {
            String sha = FileUtils.fileSha1(file);
            equalSHA = sha.equalsIgnoreCase(getSHA());
        } catch (Exception ignored) {
        }
        return !file.exists() || !equalSHA;
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }

    public void setDownloadTask(DownloadTask downloadTask) {
        this.downloadTask = downloadTask;
    }

    public String getSHA() {
        return SHA;
    }

    public void setSHA(String SHA) {
        this.SHA = SHA;
    }
}