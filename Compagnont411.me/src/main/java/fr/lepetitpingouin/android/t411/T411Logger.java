package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by gregory on 26/09/15.
 */
class T411Logger {
    private Context context;
    private SharedPreferences prefs;
    private FileWriter fw;

    public static String ERROR = "ERR";
    public static String WARN = "WRN";
    public static String INFO = "INF";

    public T411Logger(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String logFilePath() {
        String filename = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString() + "_T411.log";

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

        return file.getAbsolutePath();
    }

    public void writeLine(String str) {
        writeLine(str, INFO);

    }
    public void writeLine(String str, String level) {

        if(!prefs.getBoolean("appLogs", false)) return;

        String filename = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString() + "_T411.log";

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);

        String logLine = "[" + android.text.format.DateFormat.format("HH:mm:ss", new java.util.Date()).toString() + "] " + "[" + level + "]" + str + "\r\n";
        Log.e("T411Logger", logLine);

        try {

            if (!file.exists()) {
                if(file.createNewFile()) {
                    initLog(logLine);
                }
            }
            else {
                fw = new FileWriter(file, true);
                fw.append(logLine);
                fw.close();
            }

        } catch(Exception e) {
            e.printStackTrace();
            Log.e("T411Logger", e.getMessage());
        }
    }

    private void initLog(String logLine) {

        initLog();
        writeLine(logLine);
    }
    private void initLog() {

        writeLine("-- LOG START --");
        logDeviceInfos();
        logConnectivity();
        logAppVersion();
        logPreferences();
    }

    private void logDeviceInfos() {
        writeLine("-- DEVICE INFOS --");
        writeLine("Constructeur : " + Build.MANUFACTURER);
        writeLine("Modèle : " + Build.MODEL + "(" + Build.PRODUCT + ")");
        writeLine("Version : " + Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE);
    }

    private void logConnectivity() {
        writeLine("-- NETWORK --");

        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        writeLine("Etat de la connexion cellulaire : " + (is3g?"ON":"OFF"));

        boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        writeLine("Etat de la connexion Wi-Fi : " + (isWifi?"ON":"OFF"));
    }

    private void logAppVersion() {
        writeLine("-- APP VERSION --");

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            writeLine("Version de l'application : " + version);
            if(BuildConfig.DEBUG)
                writeLine("Version débug détectée", WARN);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void logPreferences() {
        writeLine("-- APP SETTINGS --");
        writeLine("Connexion HTTPS : " + (prefs.getBoolean("useHTTPS", false)?"ON":"OFF"));
        writeLine("Etat du proxy : " + (prefs.getBoolean("usePaidProxy", false)?"ON":"OFF"));

        writeLine("Dossier de téléchargement : " + prefs.getString("filePicker", Environment.getExternalStorageDirectory().getPath()));
    }
}
