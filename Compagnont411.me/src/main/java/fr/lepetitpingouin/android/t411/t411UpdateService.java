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
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Calendar;

public class t411UpdateService extends Service {

    private static final String CONNECTURL = Default.URL_USERPROFILE;
    private Integer mails;
    private Integer oldmails;
    private double ratio;
    private String upload;
    private String download;
    private String username;
    private String usernumber;
    private String pagecontent;
    private Handler handler;
    private AsyncUpdate upd;
    private SuperT411HttpBrowser browser;
    private AlarmManager alarmManager;
    private SharedPreferences prefs;
    private boolean timeout = true;
    Connection.Response res = null;
    private Document doc = null;
    private BillingProcessor bp;
    private int retry = 0;

    private boolean isConnectedToWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    @Override
    public void onCreate() {
        handler = new Handler();
        this.retry = 0;
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        super.onCreate();
    }

    private void initBp() {
        bp = new BillingProcessor(getApplicationContext(), Private.API_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String s, TransactionDetails transactionDetails) {
            }

            @Override
            public void onPurchaseHistoryRestored() {
            }

            @Override
            public void onBillingError(int i, Throwable throwable) {
            }

            @Override
            public void onBillingInitialized() {
                new T411Logger(getApplicationContext()).writeLine("InAppBilling initialisé");
                bp.loadOwnedPurchasesFromGoogle();
                new T411Logger(getApplicationContext()).writeLine("Proxy " + (bp.isSubscribed(Private.PROXY_ITEM_ID)?"souscrit":"non-souscrit"));
                new T411Logger(getApplicationContext()).writeLine("Proxy " + (prefs.getBoolean("usePaidProxy", false)?"activé":"non-activé"));
                if(!bp.isSubscribed(Private.PROXY_ITEM_ID) && prefs.getBoolean("usePaidProxy", false)) {
                    new T411Logger(getApplicationContext()).writeLine("Proxy actif en l'absence de souscription !! Désactivation de l'option", T411Logger.FATAL);
                    if(!BuildConfig.DEBUG) prefs.edit().putBoolean("usePaidProxy", false).apply();
                }
            }
        });
        bp.loadOwnedPurchasesFromGoogle();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        initBp();

        new T411Logger(getApplicationContext()).writeLine("Lancement du service de mise à jour");

        if (prefs.getBoolean("autoUpdate", false)) {
            planRefresh();
        }
        //refreshWidget();

        if (!prefs.getBoolean("wifiOnly", false) || (prefs.getBoolean("wifiOnly", false) && isConnectedToWifi())) {

            new asyncApiLogin().execute();
            new asyncApiUpdate().execute();

            upd = new AsyncUpdate();
            upd.execute();
            try {
                new newsFetcher().execute();
            } catch(Exception ex) {ex.printStackTrace();}

        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    private void planRefresh() {

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

    private void update(final String login, final String password) throws IOException, ClassCastException {


        new T411Logger(getApplicationContext()).writeLine("Début de la mise à jour");
        sendBroadcast(new Intent(Default.Appwidget_flag_updating));
        timeout = false;


        String mUrl = CONNECTURL;

        new T411Logger(getApplicationContext()).writeLine("Connexion à l'adresse " + mUrl);


        browser = new SuperT411HttpBrowser(getApplicationContext());
        doc = Jsoup.parse(browser.login(login, password).connect(mUrl).executeInAsyncTask());

        if (doc.select("title").text().contains("503"))
            doNotify(R.drawable.ic_maintenance, "Maintenance", "t411 est actuellement indisponible.", 411, null);
        else
            cancelNotify(411);

        if (!browser.getErrorMessage().equals("")) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sendBroadcast(new Intent(MainActivity2.INTENT_ERROR).putExtra("message", browser.getErrorMessage()));
                    new T411Logger(getApplicationContext()).writeLine(browser.getErrorMessage(), T411Logger.ERROR);
                }
            });

        }

        try {
            ratio = Math.round(Float.valueOf(doc.select(".rate").first().text().replace(',', '.').replace(" ", "")) * 100.0) / 100.0;
            new T411Logger(getApplicationContext()).writeLine("Récupération du ratio : "+ ratio);

            username = doc.select(".avatar-big").attr("alt");
            new T411Logger(getApplicationContext()).writeLine("Récupération du nom d'utilisateur : "+username);

            String avatarPath = doc.select(".avatar-big").attr("src");
            new T411Logger(getApplicationContext()).writeLine("Récupération de l'avatar...");

            String tturl = avatarPath;
            if(!tturl.startsWith("http"))
                tturl = Default.URL_INDEX + tturl;

                        String avatar = "";

            try {

                Connection.Response avatarRes = Jsoup
                        .connect(tturl)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .ignoreContentType(true).execute();
                avatar = Base64.encodeToString(avatarRes.bodyAsBytes(), 0);
                //Log.e("avatar", avatar);
            } catch(Exception e) {
                e.printStackTrace();
            }

            upload = doc.select(".up").first().text();
            new T411Logger(getApplicationContext()).writeLine("Récupération de l'upload : " + upload);

            download = doc.select(".down").first().text();
            new T411Logger(getApplicationContext()).writeLine("Récupération du download : " + download);

            oldmails = (mails != null) ? mails : prefs.getInt("lastMails", 0);

            String rawMail = null;
            try {
                rawMail = doc.select(".mail  > strong").first().text();

            } catch (Exception ex) {
                rawMail = doc.select(".newpm > strong").first().text();

            }

            mails = Integer.valueOf(rawMail);
            new T411Logger(getApplicationContext()).writeLine("Récupération des MP : " + mails);

            String[] tmp = doc.select(".ajax").attr("href").split("=");
            usernumber = tmp[1];
            new T411Logger(getApplicationContext()).writeLine("Récupération de l'ID utilisateur : " + usernumber);


            String classe = "";
            String up24 = "";
            String dl24 = "";
            String titre = "";

            String val = "";

            String seedbox = "";

            for (int iterator = 0; iterator < doc.select(".block > div > dl > dt").size(); iterator++) {
                val = doc.select(".block > div > dl > dt").get(iterator).text();
                if (val.contains("Classe:")) {
                    classe = doc.select(".block > div > dl > dd").get(iterator).text();
                    new T411Logger(getApplicationContext()).writeLine("Récupération de la classe : " + classe);
                }
                if (val.contains("Titre personnalis")) {
                    titre = doc.select(".block > div > dl > dd").get(iterator).text();
                    new T411Logger(getApplicationContext()).writeLine("Récupération du titre personnalisé : " + titre);
                }
                if (val.contains("Total") && val.contains("(24h") && val.contains("charg")) {
                    dl24 = doc.select(".block > div > dl > dd").get(iterator).text();
                    new T411Logger(getApplicationContext()).writeLine("Récupération du dl.24h : " + dl24);
                }
                if (val.contains("Total") && val.contains("(24h") && val.contains("Upload")) {
                    up24 = doc.select(".block > div > dl > dd").get(iterator).text();
                    new T411Logger(getApplicationContext()).writeLine("Récupération de l'up.24h : " + up24);
                }

                if (val.contains("Seedbox")) {
                    seedbox = doc.select(".block > div > dl > dd").get(iterator).text();
                    new T411Logger(getApplicationContext()).writeLine("Récupération de l'état seedbox : " + seedbox);
                }
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

                prefs.edit().putString("lastUpload",BSize.quickConvert(String.valueOf(new BSize(upload).getInBytes()))).commit();
                prefs.edit().putString("lastDownload",BSize.quickConvert(String.valueOf(new BSize(download).getInBytes()))).commit();
                prefs.edit().putString("lastRatio", String.format("%.2f", curRatio)).commit();

            } catch (Exception ex) {
                ex.printStackTrace();
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
                editor.putBoolean("seedbox", seedbox.contains("ui"));

            if (mails != null)
                editor.putInt("lastMails", mails);

            editor.putString("usernumber", usernumber);

            if (mails < prefs.getInt("lastMails", 0))
                editor.putBoolean("mailsNeedRefresh", false);

            if (prefs.getBoolean("ratioAlert", false)) {
                if (ratio < Double.valueOf(prefs.getString("ratioMinimum", "0"))) {
                    Intent ratioIntent = new Intent(getApplicationContext(), UserPrefsActivity.class);
                    PendingIntent pI = PendingIntent.getActivity(getApplicationContext(), 0, ratioIntent, 0);

                    new T411Logger(getApplicationContext()).writeLine("Le ratio est faible, envoi d'une notification");

                    doNotify(R.drawable.ic_stat_ratio, getString(R.string.notif_ratio_title), getString(R.string.notif_ratio_content), 1990, pI);
                }
            }

            if (prefs.getBoolean("mailAlert", false)) {
                if (mails > prefs.getInt("lastMails", 0)) {
                    Intent msgIntent = new Intent(getApplicationContext(), messagesActivity.class);
                    PendingIntent pI = PendingIntent.getActivity(getApplicationContext(), 0, msgIntent, 0);

                    new T411Logger(getApplicationContext()).writeLine("Nouveau MP reçu, envoi d'une notification");

                    doNotify(R.drawable.ic_notif_message, getString(R.string.notif_msg_title), getString(R.string.notif_msg_content), 2907, pI);
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
                ex.printStackTrace();
            }


            editor.commit();


            new NotificationWidget(getApplicationContext()).updateNotificationWidget();
        } catch (Exception ex) {
            new T411Logger(getApplicationContext()).writeLine(ex.getMessage(), T411Logger.ERROR);
            ex.printStackTrace();
        }

        new T411Logger(getApplicationContext()).writeLine("Fin de la mise à jour des données");
        refreshWidget();
        bp.release();
    }


    private void refreshWidget() {
        try {
            Intent i = new Intent(Default.Appwidget_update);

            sendBroadcast(i);
        } catch (Exception ex) {

        }
    }

    private void doNotify(int icon, String title, String subtitle, int id, PendingIntent pendingIntent) {
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

    private void cancelNotify(int id) {
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
                        sendBroadcast(new Intent(MainActivity2.INTENT_ERROR).putExtra("message", "Mise à jour impossible. Veuillez désinstaller/réinstaller l'application pour corriger le problème."));
                    }
                });
            } catch (Exception ex) {

                new T411Logger(getApplicationContext()).writeLine("Impossible d'atteindre le serveur", T411Logger.ERROR);
                //Intent i = new Intent(Default.Appwidget_update);
                //sendBroadcast(i);
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

                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext()).login(prefs.getString("login", ""), prefs.getString("password", "")).connect(url).executeInAsyncTask());

                if (doc != null) {
                    edit = prefs.edit();
                    edit.putString("title1", doc.select(".newsWrapper .title").get(0).text());
                    edit.putString("article1", doc.select(".newsWrapper .announce").get(0).html());
                    edit.putString("readMore1", doc.select(".newsWrapper .readmore").get(0).attr("href"));

                    edit = prefs.edit();
                    edit.putString("title2", doc.select(".newsWrapper  .title").get(1).text());
                    edit.putString("article2", doc.select(".newsWrapper  .announce").get(1).html());
                    edit.putString("readMore2", doc.select(".newsWrapper  .readmore").get(1).attr("href"));

                    edit = prefs.edit();
                    edit.putString("title3", doc.select(".newsWrapper  .title").get(2).text());
                    edit.putString("article3", doc.select(".newsWrapper  .announce").get(2).html());
                    edit.putString("readMore3", doc.select(".newsWrapper  .readmore").get(2).attr("href"));

                    edit.commit();

                }
            } catch (Exception ex) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            getApplicationContext().sendBroadcast(new Intent(Default.Intent_Refresh_Newspaper));
        }
    }

    private class asyncApiLogin extends AsyncTask<Void, JSONObject[], JSONObject> {

        @Override
        protected void onPreExecute() {
            new T411Logger(getApplicationContext()).writeLine("Début de la mise à jour");
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {

            String apiUrl = Default.API_T411 + "/auth";
            APIBrowser api_browser = new APIBrowser(getApplicationContext());
            new T411Logger(getApplicationContext()).writeLine("Connexion à l'adresse " + apiUrl);
            String username = prefs.getString("login", ""), password = prefs.getString("password", "");
            return api_browser.connect(apiUrl).addPOSTParam("username", username).addPOSTParam("password", password).loadObject();
        }

        @Override
        public void onPostExecute(JSONObject value) {
            try {
                prefs.edit().putString("APIToken",value.getString("token")).commit();
                prefs.edit().putString("uid",value.getString("uid")).commit();
                new T411Logger(getApplicationContext()).writeLine("Token API enregistré : " + value.getString("token"));
            } catch (JSONException e) {
                e.printStackTrace();
                new T411Logger(getApplicationContext()).writeLine("Erreur de récupération du token : " + e.getMessage());
            }
        }

    }

    private class asyncApiUpdate extends AsyncTask<Void, JSONObject[], JSONObject> {

        @Override
        protected void onPreExecute() {
            new T411Logger(getApplicationContext()).writeLine("Début de la mise à jour");

        }

        @Override
        protected JSONObject doInBackground(Void... voids) {

            String apiUrl = Default.API_T411 + "/users/profile/"+prefs.getString("uid", "");
            APIBrowser api_browser = new APIBrowser(getApplicationContext());
            return api_browser.connect(apiUrl).loadObject();
        }

        @Override
        public void onPostExecute(JSONObject value) {
            new grapher().execute();
            try {
                if(value.has("error")) {
                    new T411Logger(getApplicationContext()).writeLine("Erreur API : " + value.get("error"), T411Logger.ERROR);
                    new T411Logger(getApplicationContext()).writeLine("Fallback sur l'ancienne méthode d'update", T411Logger.INFO);
                    cancelNotify(12345);
                    //doNotify(0, getResources().getString(R.string.notif_apierror), getResources().getString(R.string.notif_apierror_fallback), 12345, null);
                    //Toast.makeText(getApplicationContext(), getResources().getString(R.string.notif_apierror) + " - " + getResources().getString(R.string.notif_apierror_fallback), Toast.LENGTH_LONG).show();
                    new AsyncUpdate().execute();
                } else {

                    prefs.edit().putString("lastUpload",BSize.quickConvert(value.getString("uploaded"))).commit();
                    prefs.edit().putString("lastDownload",BSize.quickConvert(value.getString("downloaded"))).commit();

                    Time today = new Time(Time.getCurrentTimezone());
                    today.setToNow();
                    prefs.edit().putString("lastDate", today.format("%d/%m/%Y %k:%M:%S")).commit();
                    prefs.edit().putString("lastUsername", value.get("username").toString()).commit();


                    prefs.edit().putString(
                            "lastRatio",
                            String.format(
                                    "%.2f",
                                    (Float.parseFloat(value.getString("uploaded")) / Float.parseFloat(value.getString("downloaded")))
                            )
                    ).commit();

                    new NotificationWidget(getApplicationContext()).updateNotificationWidget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //sendBroadcast(new Intent(Default.Appwidget_update));
        }

    }

    private class grapher extends AsyncTask<Void, String[], String> {

        String scripts = "<script src=\"file:///android_asset/jquery1.6.4.min.js\" type=\"text/javascript\"></script><script src=\"file:///android_asset/highcharts.min.js\" type=\"text/javascript\"></script>";
        String userID = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            new T411Logger(getApplicationContext()).writeLine("Récupération des données du graphique...");
            this.userID = prefs.getString("uid", "");
            if(this.userID.equals("")) {
                new asyncApiLogin().execute();
            }
            pagecontent = "";
        }

        @Override
        protected String doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs.getString("password", "");

            Document doc = null;

            String url = Default.URL_STATS + this.userID;

            try {

                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext())
                        .login(username, password)
                        .connect(url)
                        .executeInAsyncTask());

                Log.e("DOCJSOUP", doc.body().outerHtml());

                try {
                    pagecontent = "<html><head>"
                            + this.scripts
                            + "</head><body><div id=\"chart\" data-highcharts-chart=\"0\" style=\"display: block; height: 100%;\">"
                            + "<div class=\"highcharts-container\" id=\"highcharts-0\"></div>"
                            + "</div><script>"
                            + doc.select("#chart").first().nextElementSibling().html()
                            + "</script></body></html>";

                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e("HTML", pagecontent);
            return pagecontent;
        }

        @Override
        public void onPostExecute(String value) {
            new T411Logger(getApplicationContext()).writeLine("Enregistrement du graphique");
            if (!pagecontent.equals(""))
                prefs.edit().putString("lastGraph", pagecontent).commit();
        }
    }
}