package fr.lepetitpingouin.android.t411;

/**
 * Created by meyergre on 31/05/13.
 */
class BSize {

    private String rawData;
    private String unit;
    private String tmp[];
    private double koctets;

    public BSize(String input) {
        this.rawData = input.replaceAll(",", ".");
        try {
            tmp = rawData.split(" ");
            this.unit = tmp[tmp.length - 1];
            double raw = Double.valueOf(tmp[tmp.length - 2]);

            koctets = unit.contains("KB") ? raw : unit.contains("MB") ? raw * (1024) : unit.contains("GB") ? raw * (1024 * 1024) : unit.contains("TB") ? raw * (1024 * 1024 * 1024) : raw / 1024;
        } catch (Exception e) {

            this.unit = "KB";
            this.koctets = Double.parseDouble(rawData) / 1024;

        }
    }

    public static String quickConvert(String val) {
        Double koctets = Double.valueOf(val)/1024;
        if(koctets > 1024*1024*1024) {
            return String.format("%.2f", koctets/(1024*1024*1024)) + " TB";
        }
        if(koctets > 1024*1024) {
            return String.format("%.2f", koctets/(1024*1024)) + " GB";
        }
        if(koctets > 1024) {
            return String.format("%.2f", koctets/(1024)) + " MB";
        }
        else return String.format("%.2f",koctets) + " KB";
    }

    public String convert() {
        return convert(this.unit);

    }

    private String convert(String unit) {
        return unit.contains("KB") ? String.format("%.2f", getInKB()) + " KB" : unit.contains("MB") ? String.format("%.2f", getInMB()) + " MB" : unit.contains("GB") ? String.format("%.2f", getInGB()) + " GB" : unit.contains("TB") ? String.format("%.2f", getInTB()) + " TB" : String.format("%.2f", getInBytes()) + " B";
    }

    public double getInBaseUnit() {

        return Double.valueOf(tmp[tmp.length - 2]);
    }

    public String getBaseUnit() {
        return unit;
    }

    public double getInBytes() {
        return koctets * 1024;
    }

    public double getInKB() {
        return koctets;
    }

    public double getInMB() {
        return koctets / (1024);
    }

    public double getInGB() {
        return koctets / (1024 * 1024);
    }

    private double getInTB() {
        return koctets / (1024 * 1024 * 1024);
    }

    public double getInAuto() {

        return 0;
    }
}
