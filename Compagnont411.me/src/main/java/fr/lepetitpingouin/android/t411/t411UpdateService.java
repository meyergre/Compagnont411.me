package fr.lepetitpingouin.android.t411;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Calendar;

public class t411UpdateService extends Service {

    public Integer mails, oldmails;
    public double ratio;
    public String upload, download, username, conError = "", usernumber;
    public String pagecontent;

    public Handler handler;

    AsyncUpdate upd;

    AlarmManager alarmManager;

    SharedPreferences prefs;

    boolean timeout = true;
    Connection.Response res = null;
    Document doc = null;

    // La page de login t411 :
    static final String CONNECTURL = "http://www.t411.me/users/login/?returnto=%2Fusers%2Fprofile%2F";

    public boolean isOnline() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        Intent i = new Intent(Default.Appwidget_update);
        i.putExtra("LED_T411", true);
        i.putExtra("LED_Net", false);
        sendBroadcast(i);
        return false;
    }

    public boolean isConnectedToWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            return true;
        }
        Intent i = new Intent(Default.Appwidget_update);
        i.putExtra("LED_T411", true);
        i.putExtra("LED_Net", false);
        sendBroadcast(i);
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // on charge les pr�f�rences de l'application
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if (prefs.getBoolean("autoUpdate", false)) {
            planRefresh();
        }

        refreshWidget();

        Log.d("Wifi check...", prefs.getBoolean("wifiOnly", false)+"");

        if (!prefs.getBoolean("wifiOnly", false) || (prefs.getBoolean("wifiOnly", false) && isConnectedToWifi())) {
            Log.d("", "LANCEMENT DES ASYNCTASKS");
            upd = new AsyncUpdate();
            upd.execute();
            new newsFetcher().execute();
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    public void planRefresh() {
        Log.d("service", "autoRefresh ON");
        alarmManager = (AlarmManager) getBaseContext().getSystemService(ALARM_SERVICE);

        Intent myIntent = new Intent(getBaseContext(), t411UpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getBaseContext(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        int freq = Integer.valueOf(prefs.getString("updateFreq", Default.UpdateFreq));
        freq = (freq < 1) ? 1 : freq;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, freq);

        // RTC = 1 (AlarmManager.RTC)
        // RTC_WAKEUP = 0 (AlarmManager.RTC_WAKEUP)
        int RTC_mode = (prefs.getBoolean("rtcMode", false)?1:0);
        alarmManager.set(RTC_mode, calendar.getTimeInMillis(), pendingIntent);
    }

    public void update(String login, String password) throws IOException {
        Log.v("Service t411", "Update()...");
        sendBroadcast(new Intent(Default.Appwidget_flag_updating));
        timeout = false;
        // on ex�cute la requ�te HTTP, en passant le login et le password en
        // POST.
        Log.d("update()", "Connecting...");

        // is HTPPS ?
        String url = CONNECTURL;
        if (prefs.getBoolean("useHTTPS", false))
            url = CONNECTURL.replace("http://", "https://");

        res = Jsoup
                .connect(url)
                .data("login", login, "password", password)
                .method(Method.POST)
                .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                .execute();
        Log.v("Service t411", "JSoup ex�cut�");
        Log.d("update()", "Parsing...");
        doc = res.parse();

        if (doc.title().contains("503"))
            doNotify(R.drawable.ic_maintenance, "Maintenance", "t411 est actuellement indisponible.", 411, null);
        else
            cancelNotify(411);

        try {
            conError = doc.select("#messages").first().text();
            timeout = true;
        } catch (Exception ex) {
            Log.e("conError", ex.toString());
            timeout = true;
        }
        if (!conError.equals("")) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(t411UpdateService.this.getApplicationContext(), conError, Toast.LENGTH_LONG).show();
                }
            });
            Log.e("conError :", conError);
        }
        try {
            ratio = Math.round(Float.valueOf(doc.select(".rate").first().text()
                    .replace(',', '.')) * 100.0) / 100.0;
            Log.d("Ratio :", String.valueOf(ratio));

            username = doc.select(".avatar-big").attr("alt");
            Log.v("username :", username);

            // r�cup�ration de l'avatar
            String avatarPath = doc.select(".avatar-big").attr("src");
            Log.d("avatarPath", Default.URL_INDEX + avatarPath);
            Connection.Response avatarRes = Jsoup
                    .connect(Default.URL_INDEX + avatarPath)
                    .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                    .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                    .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                    .ignoreContentType(true).execute();
            String avatar = Base64.encodeBytes(avatarRes.bodyAsBytes());
            Log.d("avatar", avatar);
            // fin

            // on r�cup�re la chaine de l'upload sans la traiter, avec la fl�che
            // et l'unit�
            upload = doc.select(".up").first().text();
            Log.v("upload :", upload);

            // idem pour le download
            download = doc.select(".down").first().text();
            Log.v("download :", download);

            // et enfin le nombre de mails, sous forme d'entier
            oldmails = (mails != null) ? mails : prefs.getInt("lastMails", 0);
            Log.v("mails (avant check) :", oldmails.toString());

            String rawMail = null;
            try {
                rawMail = doc.select(".mail  > strong").first().text();
                Log.d("MAILS .newpm", rawMail);
            } catch (Exception ex) {
                rawMail = doc.select(".newpm > strong").first().text();
                Log.d("MAILS .mail", rawMail);
            }

            mails = Integer.valueOf(rawMail);

            Log.v("mails (apr�s check) :", mails.toString());

            // On r�cup�re aussi le N� utilisateur pour les statistiques
            String[] tmp = doc.select(".ajax").attr("href").split("=");
            usernumber = tmp[1];

            // t�l�chargements (24h)

            String classe = "";
            String up24 = "";
            String dl24 = "";
            String titre = "";

            String val = "";

            String seedbox = "";

            for (int iterator = 0; iterator < doc.select(".block > div > dl > dt").size(); iterator++) {
                val = doc.select(".block > div > dl > dt").get(iterator).text();
                if (val.contains("Classe:"))
                    classe = doc.select(".block > div > dl > dd").get(iterator)
                            .text();
                if (val.contains("Titre personnalis"))
                    titre = doc.select(".block > div > dl > dd").get(iterator)
                            .text();
                if (val.contains("Total") && val.contains("(24h")
                        && val.contains("charg"))
                    dl24 = doc.select(".block > div > dl > dd").get(iterator)
                            .text();
                if (val.contains("Total") && val.contains("(24h") && val.contains("Upload"))
                    up24 = doc.select(".block > div > dl > dd").get(iterator).text();

                if (val.contains("Seedbox"))
                    seedbox = doc.select(".block > div > dl > dd").get(iterator).text();
            }
            Log.d("Classe", classe);
            Log.d("Titre", titre);
            Log.d("DT", up24);
            Log.d("DD", dl24);
            Log.d("Seedbox", seedbox);

            // Calcul du restant possible t�l�chargeable avant d'atteindre la
            // limite de ratio fix�e
            double beforeLimit = 0;
            try {
                //double upData = getGigaOctetData(prefs.getString("lastUpload", "U 0 GB"));
                double upData = new BSize(upload).getInGB();
                //double dlData = getGigaOctetData(prefs.getString( "lastDownload", "D 0 GB"));
                double dlData = new BSize(download).getInGB();

                double lowRatio = Double.valueOf(prefs.getString("ratioMinimum", "1"));
                Log.d("Current Ratio :", String.valueOf(lowRatio));

                beforeLimit = (upData - dlData * lowRatio) / lowRatio;
                Log.d("beforeLimit :", String.valueOf(beforeLimit));
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("RATIORATIO", ex.toString());
            }
            String GoLeft = null;

            // Calcul de l'upload e faire avant d'atteindre la limite de ratio
            double toLimit = 0;
            try {
                //double upData = getGigaOctetData(prefs.getString("lastUpload", "U 0 GB"));
                //double dlData = getGigaOctetData(prefs.getString("lastDownload", "D 0 GB"));
                double upData = new BSize(upload).getInGB();
                double dlData = new BSize(download).getInGB();

                double curRatio = upData / dlData;
                Log.d("Current Ratio :", String.valueOf(curRatio));

                double targetRatio = Double.valueOf(prefs.getString("ratioCible", "1"));
                Log.d("Target Ratio :", String.valueOf(targetRatio));

                toLimit = (targetRatio * upData / curRatio) - upData;
                Log.d("toRatio :", String.valueOf(toLimit));
            } catch (Exception ex) {
            }

            String UpLeft = null;

            // Prise en compte des quantit�s restantes en Tera-octets
            GoLeft = (beforeLimit > 500) ?
                    String.format("%.2f", beforeLimit / 1024) + " TB" :
                    String.format("%.2f", beforeLimit) + " GB";

            UpLeft = (toLimit > 500) ?
                    String.format("%.2f", toLimit / 1024) + " TB" :
                    String.format("%.2f", toLimit) + " GB";

            Log.d("left2DL : ", UpLeft);
            // on stocke tout ce petit monde (si non nul) dans les pr�f�rences
            Editor editor = prefs.edit();

            editor.putString("avatar", avatar);

            editor.putString("classe", classe);
            editor.putString("up24", up24);
            editor.putString("dl24", dl24);
            editor.putString("titre", titre);
            editor.putString("GoLeft", (beforeLimit > 0) ? GoLeft : "0.00 GB");
            editor.putString("UpLeft", (toLimit > 0) ? UpLeft : "0.00 GB");

            if (seedbox != null)
                editor.putBoolean("seedbox", seedbox.contains("ui") ? true : false);

            if (mails != null)
                editor.putInt("lastMails", mails);
            if (upload != null)
                editor.putString("lastUpload", upload);
            if (download != null)
                editor.putString("lastDownload", download);
            if (ratio != Double.NaN)
                editor.putString("lastRatio", String.valueOf(ratio));
            editor.putString("usernumber", usernumber);

            if (mails < prefs.getInt("lastMails", 0))
                editor.putBoolean("mailsNeedRefresh", false);

            // Notifications
            if (prefs.getBoolean("ratioAlert", false)) {
                if (ratio < Double.valueOf(prefs.getString("ratioMinimum", "0"))) {
                    Intent ratioIntent = new Intent(getApplicationContext(), Settings.class);
                    PendingIntent pI = PendingIntent.getActivity(getApplicationContext(), 0, ratioIntent, 0);

                    doNotify(R.drawable.ic_stat_ratio, getString(R.string.notif_ratio_title), getString(R.string.notif_ratio_content), 1990, pI);
                }
            }

            if (prefs.getBoolean("mailAlert", false)) {
                if (mails > prefs.getInt("lastMails", 0)) {
                    Intent msgIntent = new Intent(getApplicationContext(), messagesActivity.class);
                    PendingIntent pI = PendingIntent.getActivity(getApplicationContext(), 0, msgIntent, 0);
                    doNotify(R.drawable.ic_launcher_messages, getString(R.string.notif_msg_title), getString(R.string.notif_msg_content), 2907, pI);
                }
            }

            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();

            editor.putString("lastDate", today.format("%d/%m/%Y %k:%M:%S"));
            editor.putString("lastUsername", username);

            try {
                grapher grfx = new grapher();
                grfx.execute();
            } catch (Exception e) {}


            editor.commit();

            //updateMails();

            Log.v("t411 Error :", conError);
            Log.v("INFOS T411 :", "Mails (" + String.valueOf(mails) + ") "
                    + upload + " " + download + " " + String.valueOf(ratio));

            refreshWidget();
            new NotificationWidget(getApplicationContext()).updateNotificationWidget();
        } catch (Exception e) {
        }
    }


    public void refreshWidget() {
        try {
            Intent i = new Intent(Default.Appwidget_update);
            i.putExtra("LED_T411", isT411Online());
            i.putExtra("LED_Net", isOnline());
            Log.v("t411UpdateService", "Envoi du Broadcast Intent");
            sendBroadcast(i);
        } catch (Exception ex) {
            Log.v("Broadcast Sender", ex.toString());
        }
    }

    public boolean isT411Online() {
        return (res.statusCode() == 200) ? true : false;
    }

    public void doNotify(int icon, String title, String subtitle, int id, PendingIntent pendingIntent) {
        try {
            if (pendingIntent == null)
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(subtitle);

            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setAutoCancel(true);

            mBuilder.setDefaults(Notification.DEFAULT_ALL);
            mBuilder.setOnlyAlertOnce(true);

            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, mBuilder.build());

        } finally {
            try {
                Toast.makeText(getApplicationContext(), subtitle, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            }
        }
    }

    public void cancelNotify(int id) {
        try {
            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(id);
        } catch (Exception e) {
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v("Binder", intent.toString());
        return null;
    }

    private class AsyncUpdate extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                update(prefs.getString("login", ""),
                        prefs.getString("password", ""));
            } catch (Exception ex) {
                Log.v("Credentials :", prefs.getString("login", "") + ":"
                        + prefs.getString("password", ""));
                Log.e("update", ex.toString());

                Intent i = new Intent(Default.Appwidget_update);
                i.putExtra("LED_T411", false);
                i.putExtra("LED_Net", isOnline());
                sendBroadcast(i);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d("PostExecute", "");
            //stopSelf();
            super.onPostExecute(result);
        }
    }


    private class newsFetcher extends AsyncTask<Void, Void, Void> {

        Editor edit;
        Connection.Response res;
        Document doc;
        String url;

        @Override
        protected Void doInBackground(Void... args) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                url = Default.URL_INDEX;

                if (prefs.getBoolean("useHTTPS", false))
                    url = url.replace("http://", "https://");

                res = Jsoup.connect(url)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .execute();
                doc = res.parse();

                if (doc != null) {
                    edit = prefs.edit();
                    edit.putString("title1", doc.select(".newsWrapper .title")
                            .get(0).text());
                    edit.putString("article1",
                            doc.select(".newsWrapper .announce").get(0).html());
                    edit.putString("readMore1", url + doc.select(".newsWrapper .readmore").get(0).attr("href"));
                    edit.commit();
                    Log.d("news", "OK");
                    edit = prefs.edit();
                    edit.putString("title2", doc.select(".newsWrapper  .title")
                            .get(1).text());
                    edit.putString("article2",
                            doc.select(".newsWrapper  .announce").get(1).html());
                    edit.putString("readMore2",
                            url
                                    + doc.select(".newsWrapper  .readmore")
                                    .get(1).attr("href"));
                    edit.commit();
                    Log.d("news", "OK");
                    edit = prefs.edit();
                    edit.putString("title3", doc.select(".newsWrapper  .title")
                            .get(2).text());
                    edit.putString("article3",
                            doc.select(".newsWrapper  .announce").get(2).html());
                    edit.putString("readMore3",
                            url
                                    + doc.select(".newsWrapper  .readmore")
                                    .get(2).attr("href"));
                    edit.commit();
                    Log.d("news", "OK");
                }
            } catch (Exception ex) {
                Log.e("news ERREUR :", ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            getApplicationContext().sendBroadcast(
                    new Intent(Default.Intent_Refresh_Newspaper));
        }
    }

    private class grapher extends AsyncTask<Void, String[], String> {

        String scripts = "<script src=\"file:///android_asset/jquery1.6.4.min.js\" type=\"text/javascript\"></script><script src=\"file:///android_asset/highcharts.min.js\" type=\"text/javascript\"></script>";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pagecontent = "?";
        }

        @Override
        protected String doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");
            Log.v("Credentials :", username + "/" + password);

            Connection.Response res = null;
            Document doc = null;

            String url = Default.URL_STATS;
            if (prefs.getBoolean("useHTTPS", false)) {
                url = url.replace("http://", "https://");
            }

            try {
                res = Jsoup
                        .connect(url + prefs.getString("usernumber", "0"))
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true).execute();
                doc = res.parse();

                try {
                    pagecontent = "<html><head>"
                            + scripts
                            + "</head><body><div id=\"chart\" style=\"height: 100%;\"></div><script>"
                            + doc.select("div.content > script").get(2).html()
                            + "</script></body></html>";
                    //Log.d("page content :", pagecontent);
                } catch (Exception ex) {
                    Log.e("Erreur get tabledata", ex.toString());
                }

            } catch (Exception e) {
                Log.e("erreur", e.toString());
            }

            return pagecontent;
        }

        @Override
        public void onPostExecute(String value) {
            prefs.edit().putString("lastGraph", pagecontent).commit();
        }
    }
}