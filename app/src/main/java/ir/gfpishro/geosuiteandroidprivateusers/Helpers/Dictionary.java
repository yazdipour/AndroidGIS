package ir.gfpishro.geosuiteandroidprivateusers.Helpers;

import java.util.LinkedHashMap;
import java.util.Map;

public class Dictionary {
    private Map<String, String> models = new LinkedHashMap<>(); // Feature Name
    private Map<String, String> fields = new LinkedHashMap<>(); // Fields Name
    private Map<String, String> values = new LinkedHashMap<>();

    public Dictionary() {
        initModels();
        initFields();
        initValues();
    }

    private void initModels() {
        models.put("GasNet_", "");
        models.put("personel", "کاربر");
        models.put("emergencysubzone", "محدوده امداد");
        models.put("zone", "ناحیه");
        models.put("area_station", "محدوده ایستگاه");
        models.put("planning", "طرح");
        models.put("mission_report_form", "فرم گزارش ماموریت");
        models.put("accessibility", "دسترسی");
        models.put("area", "منطقه");
        models.put("div", "محدوده");
        models.put("form11", "فرم 11");
        models.put("hse", "بوسنجی");
        models.put("emergencyteam", "تیم امداد");
        models.put("teamlocation", "موقعیت تیم");
        models.put("gas_request", "درخواست گاز رسانی");
        models.put("form12", "فرم 12");
        models.put("serviceriser", "علمک");
        models.put("block", "بلوک");
        models.put("side", "ساید");
        models.put("special", "فرم ویژه");
        models.put("pg_point", "نقطه توزیع");
        models.put("parcel", "پارسل");
        models.put("pt", "پرژنتی");
        models.put("customer", "مشترک");
        models.put("cps", "حفاظت کاتدی");
        models.put("soil_resistance", "مقاومت خاک");
        models.put("missions", "ماموریت");
        models.put("layers_access", "دسترسی لایه");
        models.put("user_layer", "لایه کاربر");
        models.put("bg_point", "نقطه تغذیه");
        models.put("pg_gaspipe", "لوله توزیع");
        models.put("bg_gaspipe", "لوله تغذیه");
        models.put("serviceline", "کف خواب");
        models.put("docs", "مستندات");
        models.put("pg_valve", "شیر توزیع");
        models.put("bg_valve", "شیر تغذیه");
        models.put("public_features_line", "عارضه خطی");
        models.put("public_features_point", "عارضه نقطه ای");
        models.put("public_features_polygon", "عارضه چندوجهی");
        models.put("tp", "نقطه تست");
        models.put("steal", "سرقت گاز");
        models.put("form", "فرم");
        models.put("layer", "لایه");
    }

    private void initFields() {
        fields.put("register", "شناسه جمع ثبت اسناد");
        fields.put("contract_num", "شماره پیمان");
        fields.put("archive", "کد فایل ازبیلت");
        fields.put("gnaf", "کد GNAF");
        fields.put("code_address", "کد آدرس");
        fields.put("php", "مصرف فعلی");
        fields.put("phf", "مصرف آینده");
        fields.put("area", "مساحت");
        fields.put("city_code", "کد شهر");
        fields.put("address", "آدرس");
        fields.put("plaque", "پلاک");
        fields.put("seir", "سیر");
        fields.put("desc", "توضیحات");
        fields.put("des", "توضیحات");
        fields.put("name", "نام");
        fields.put("marketing", "بازاریابی");
        fields.put("r_giscodemain", "کد علمک اصلی");

        fields.put("edit_date", "تاریخ ویرایش");
        fields.put("create_date", "تاریخ ایجاد");
        fields.put("mcode", "MCODE");

        fields.put("zone_id", "شناسه ناحیه");
        fields.put("deleted", "حذف شده");
        fields.put("num", "شماره");
        fields.put("noo", "شماره");
        fields.put("size", "اندازه");
        fields.put("material", "جنس");
        fields.put("in_srv_date", "تاریخ بهره برداری");

        fields.put("multi_code", "کد مشترک پرژنتی");
        fields.put("parcel_code", "کد پارسل");
        fields.put("pipe", "لوله");
        fields.put("node_code", "کد گره");
        fields.put("mesc", "کد طبقه بندی کالا");
        fields.put("angle", "زاویه");

        fields.put("role", "نقش");
        fields.put("batch_no", "بچ نامبر");
        fields.put("factory", "کارخانه ی سازنده");
        fields.put("install", "نوع استقرار");
        fields.put("survey", "نوع علمک");
        fields.put("depth", "عمق");
        fields.put("purgetee", "پرژنتی");
        fields.put("direction", "جهت");
        fields.put("box", "جعبه ای");
        fields.put("status", "وضعیت");

        fields.put("layer", "لایه");
        fields.put("class", "کلاس");
        fields.put("built", "بیلت");

        fields.put("length", "طول");

        fields.put("flows", "جریان");

        fields.put("flow", "جریان");
        fields.put("line", "خط");
        fields.put("grade", "گرید");
        fields.put("coating", "پوشش");
        fields.put("diameter", "قطر");

        fields.put("station_type", "نوع ایستگاه");
        fields.put("calc_type", "نوع محاسبه");

        fields.put("station", "ایستگاه");

        fields.put("type", "نوع");
        fields.put("enabled", "فعال");

        fields.put("eis_", "");
        fields.put("pa_", "");
        fields.put("r_", "");
        fields.put("f_", "");
        fields.put("v_", "");
        fields.put("p_", "");

        fields.put("p", "فشار");
        fields.put("v", "حجم");
        fields.put("a", "منطقه");
        fields.put("d", "محدوده");
        fields.put("b", "بلوک");
        fields.put("s", "ساید");
    }

    private void initValues() {
        values.put("false", "خیر");
        values.put("true", "بله");
        values.put("NAN", "نامشخص");
    }

//    private void initConflicts() {
//        conflicts.put("Personel id", "شماره پرسنلی");
//        conflicts.put("Personel", "کاربر");
//        conflicts.put("National code", "کد ملی");
//        conflicts.put("Username", "نام کاربری");
//    }

    public String translateModel(String s) {
        for (Map.Entry<String, String> entry : models.entrySet()) {
            s = s.replaceAll(entry.getKey(), entry.getValue());
        }
        return s;
    }

    public String translateField(String s) {
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            s = s.replaceAll(entry.getKey(), entry.getValue());
        }
        return s;
    }

    public String translateValues(String s) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            s = s.replaceAll(entry.getKey(), entry.getValue());
        }
        return s;
    }
}