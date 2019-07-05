package ir.gfpishro.geosuiteandroidprivateusers.Models.Mission;

public enum ReportStatus {
    PENDING("PENDING"), SENT("SENT"), NEW("NEW");
    private String name;

    ReportStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
