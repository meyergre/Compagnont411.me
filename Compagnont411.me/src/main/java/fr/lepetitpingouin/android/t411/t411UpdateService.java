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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Calendar;

public class t411UpdateService extends Service {

    static final String CONNECTURL = Default.URL_USERPROFILE;
    public Integer mails, oldmails;
    public double ratio;
    public String upload, download, username, conError = "", usernumber;
    public String pagecontent;
    public Handler handler;
    AsyncUpdate upd;
    SuperT411HttpBrowser browser;
    AlarmManager alarmManager;
    SharedPreferences prefs;
    boolean timeout = true;
    Connection.Response res = null;
    Document doc = null;

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
        handler = new Handler();


        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if (prefs.getBoolean("autoUpdate", false)) {
            planRefresh();
        }

        refreshWidget();


        if (!prefs.getBoolean("wifiOnly", false) || (prefs.getBoolean("wifiOnly", false) && isConnectedToWifi())) {


            upd = new AsyncUpdate();
            upd.execute();
            new newsFetcher().execute();
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    public void planRefresh() {

        alarmManager = (AlarmManager) getBaseContext().getSystemService(ALARM_SERVICE);

        Intent myIntent = new Intent(getBaseContext(), t411UpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getBaseContext(), 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        int freq = Integer.valueOf(prefs.getString("updateFreq", Default.UpdateFreq));
        freq = (freq < 1) ? 1 : freq;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, freq);


        int RTC_mode = (prefs.getBoolean("rtcMode", false) ? 1 : 0);
        alarmManager.set(RTC_mode, calendar.getTimeInMillis(), pendingIntent);
    }

    public void update(String login, String password) throws IOException, ClassCastException {


        sendBroadcast(new Intent(Default.Appwidget_flag_updating));
        timeout = false;


        String mUrl = CONNECTURL;
        Log.e("t411UPDATER-URL", mUrl);


        browser = new SuperT411HttpBrowser(getApplicationContext());
        doc = Jsoup.parse(browser.login(login, password).connect(mUrl).executeInAsyncTask());

        if (doc.select("title").contains("503"))
            doNotify(R.drawable.ic_maintenance, "Maintenance", "t411 est actuellement indisponible.", 411, null);
        else
            cancelNotify(411);

        if (!browser.getErrorMessage().equals("")) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(browser.getErrorMessage().contains("aptcha"))
                        Log.e("Update error", browser.getErrorMessage());
                    else
                        Toast.makeText(t411UpdateService.this, browser.getErrorMessage(), Toast.LENGTH_LONG).show();
                    Log.e("t411BROWSER", browser.getErrorMessage());
                }
            });

        }
        try {
            ratio = Math.round(Float.valueOf(doc.select(".rate").first().text().replace(',', '.')) * 100.0) / 100.0;

            username = doc.select(".avatar-big").attr("alt");

            String avatarPath = doc.select(".avatar-big").attr("src");

            Connection.Response avatarRes = Jsoup
                    .connect(Default.URL_INDEX + avatarPath)
                    .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                    .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                    .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                    .ignoreContentType(true).execute();
            String avatar = Base64.encodeBytes(avatarRes.bodyAsBytes());


            upload = doc.select(".up").first().text();

            download = doc.select(".down").first().text();

            oldmails = (mails != null) ? mails : prefs.getInt("lastMails", 0);


            String rawMail = null;
            try {
                rawMail = doc.select(".mail  > strong").first().text();

            } catch (Exception ex) {
                rawMail = doc.select(".newpm > strong").first().text();

            }

            mails = Integer.valueOf(rawMail);

            String[] tmp = doc.select(".ajax").attr("href").split("=");
            usernumber = tmp[1];


            String classe = "";
            String up24 = "";
            String dl24 = "";
            String titre = "";

            String val = "";

            String seedbox = "";

            for (int iterator = 0; iterator < doc.select(".block > div > dl > dt").size(); iterator++) {
                val = doc.select(".block > div > dl > dt").get(iterator).text();
                if (val.contains("Classe:"))
                    classe = doc.select(".block > div > dl > dd").get(iterator).text();
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


            double beforeLimit = 0;
            try {

                double upData = new BSize(upload).getInGB();

                double dlData = new BSize(download).getInGB();

                double lowRatio = Double.valueOf(prefs.getString("ratioMinimum", "1"));


                beforeLimit = (upData - dlData * lowRatio) / lowRatio;

            } catch (Exception ex) {
                ex.printStackTrace();

            }
            String GoLeft = null;

            double toLimit = 0;
            try {


                double upData = new BSize(upload).getInGB();
                double dlData = new BSize(download).getInGB();

                double curRatio = upData / dlData;


                double targetRatio = Double.valueOf(prefs.getString("ratioCible", "1"));


                toLimit = (targetRatio * upData / curRatio) - upData;

            } catch (Exception ex) {
            }

            String UpLeft = null;

            GoLeft = (beforeLimit > 500) ?
                    String.format("%.2f", beforeLimit / 1024) + " TB" :
                    String.format("%.2f", beforeLimit) + " GB";

            UpLeft = (toLimit > 500) ?
                    String.format("%.2f", toLimit / 1024) + " TB" :
                    String.format("%.2f", toLimit) + " GB";

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

            if (prefs.getBoolean("ratioAlert", false)) {
                if (ratio < Double.valueOf(prefs.getString("ratioMinimum", "0"))) {
                    Intent ratioIntent = new Intent(getApplicationContext(), UserPrefsActivity.class);
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
            } catch (Exception ex) {
            }


            editor.commit();


            new NotificationWidget(getApplicationContext()).updateNotificationWidget();
        } catch (Exception ex) {
        }

        refreshWidget();
    }


    public void refreshWidget() {
        try {
            Intent i = new Intent(Default.Appwidget_update);

            sendBroadcast(i);
        } catch (Exception ex) {

        }
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

        return null;
    }

    private class AsyncUpdate extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                update(prefs.getString("login", ""),
                        prefs.getString("password", ""));


            } catch (ClassCastException ccex) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(t411UpdateService.this, "Mise à jour impossible. Veuillez désinstaller/réinstaller l'application pour corriger le problème.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception ex) {


                Intent i = new Intent(Default.Appwidget_update);
                sendBroadcast(i);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

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



                /* res = Jsoup.connect(url)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .execute();
                doc = res.parse(); */
                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext()).login(prefs.getString("login", ""), prefs.getString("password", "")).connect(url).executeInAsyncTask());

                if (doc != null) {
                    edit = prefs.edit();
                    edit.putString("title1", doc.select(".newsWrapper .title").get(0).text());
                    edit.putString("article1", doc.select(".newsWrapper .announce").get(0).html());
                    edit.putString("readMore1", url + doc.select(".newsWrapper .readmore").get(0).attr("href"));
                    edit.commit();

                    edit = prefs.edit();
                    edit.putString("title2", doc.select(".newsWrapper  .title").get(1).text());
                    edit.putString("article2", doc.select(".newsWrapper  .announce").get(1).html());
                    edit.putString("readMore2", url + doc.select(".newsWrapper  .readmore").get(1).attr("href"));
                    edit.commit();

                    edit = prefs.edit();
                    edit.putString("title3", doc.select(".newsWrapper  .title").get(2).text());
                    edit.putString("article3", doc.select(".newsWrapper  .announce").get(2).html());
                    edit.putString("readMore3", url + doc.select(".newsWrapper  .readmore").get(2).attr("href"));
                    edit.commit();

                }
            } catch (Exception ex) {

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
            pagecontent = "";
        }

        @Override
        protected String doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");


            Connection.Response res = null;
            Document doc = null;

            String url = Default.URL_STATS;


            try {
                /* res = Jsoup
                        .connect(url + prefs.getString("usernumber", "0"))
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true).execute();
                doc = res.parse(); */

                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext())
                        .login(username, password)
                        .connect(url + prefs.getString("usernumber", "0"))
                        .executeInAsyncTask());

                try {
                    pagecontent = "<html><head>"
                            + scripts
                            + "</head><body><div id=\"chart\" style=\"display: block; height: 100%;\"></div><script>"
                            + doc.select("div#chart").first().nextElementSibling().html()
                            + "</script></body></html>";

                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            } catch (Exception e) {

            }

            return pagecontent;
        }

        @Override
        public void onPostExecute(String value) {
            if (!pagecontent.equals(""))
                prefs.edit().putString("lastGraph", pagecontent).commit();
        }
    }
}