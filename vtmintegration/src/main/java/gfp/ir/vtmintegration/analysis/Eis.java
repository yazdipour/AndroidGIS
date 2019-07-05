package gfp.ir.vtmintegration.analysis;

public class Eis {
    private int id;
    private String type = "NAN";
    private String msg = "";
    private String geoJson = "";

    public Eis() {

    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMsg() {
        switch (getType()) {
            case "A":
                break;
            case "B":
                msg = "از طریق ایزولاسیون گاز پایین دست قطع میشود";
                break;
            default:
                msg = "برای ایزولاسیون بیش از یک شیر باید قطع شود";
                break;
        }
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getGeoJson() {
        return geoJson;
    }

    public void setGeoJson(String geoJson) {
        this.geoJson = geoJson;
    }
}