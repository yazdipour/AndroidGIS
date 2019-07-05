package ir.gfpishro.geosuiteandroidprivateusers.Models;

public enum SearchType {
    STREET("خیابان", "", "name", "خیابان"),
    PG_NUM("شماره شیر توزیع", "GasNet_pg_valve", "v_num", "شیر"),
    BG_NUM("شماره شیر تغذیه", "GasNet_bg_valve", "v_num", "شیر تغذیه"),
    PARCEL_CODE("کد ادرس", "GasNet_parcel", "code_address", "پارسل"),
    RISER_NUM("شماره علمک", "GasNet_serviceriser", "r_num", "علمک"),
    CUSTOMERS("مشترکین","customer","code_address","مشترکین"),
    CUSTOMERS_BY_COORDINATE("مشترکین-مختصات","customer","code_address","مشترکین");

    private final String title, table, field, title2;

    SearchType(String _title, String _table, String _field, String _title2) {
        title = _title;
        table = _table;
        field = _field;
        title2 = _title2;
    }

    public String getTitle() {
        return this.title;
    }

    public String getTitle2() {
        return this.title2;
    }

    public String getTable() {
        return table;
    }

    public String getField() {
        return field;
    }
}