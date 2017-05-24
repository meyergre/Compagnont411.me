package fr.lepetitpingouin.android.t411;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.BoolRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Comparator;

public class Torrent implements Comparable {

    public static final String INTENT_UPDATE_STATUS = "INTENT_TORRENT_UPDATE_STATUS";
    public static String DOWNLOAD_FOLDER = "Torrents t411";

    public String name;
    public String id;
    public String url;
    public String category="";
    public Long download_date=0L;
    public String size, uploader, age, seeders, leechers, avis, complets, ratioa, ratiob;
    public Long date = 0L;
    private Context context;
    private SharedPreferences prefs;
    private torrentFileGetter tDL;
    private AsyncDlLater dll;
    private AsyncDlLaterNot dllNot;

    public static final Comparator<Torrent> DATE_COMPARATOR = new Comparator<Torrent>() {
        public int compare(Torrent t, Torrent t1) {
            if((t.date) > (t1.date)) {
                return 1;
            } else {
                return -1;
            }
        }
    };
    public static final Comparator<Torrent> SEEDERS_COMPARATOR = new Comparator<Torrent>() {
        public int compare(Torrent t, Torrent t1) {
            return Integer.parseInt(t.seeders) - Integer.parseInt(t1.seeders);
        }
    };
    public static final Comparator<Torrent> LEECHERS_COMPARATOR = new Comparator<Torrent>() {
        public int compare(Torrent t, Torrent t1) {
            return Integer.parseInt(t.leechers) - Integer.parseInt(t1.leechers);
        }
    };
    public static final Comparator<Torrent> DOWNLOAD_DATE_COMPARATOR = new Comparator<Torrent>() {
        public int compare(Torrent t, Torrent t1) {
            if(t.download_date > t1.download_date) {
                return 1;
            } else {
                return -1;
            }
        }
    };
    public static final Comparator<Torrent> SIZE_COMPARATOR = new Comparator<Torrent>() {
        public int compare(Torrent t, Torrent t1) {
            if(Long.parseLong(t.size) > Long.parseLong(t1.size)) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    public JSONArray json;

    public Torrent(Context context, String name, String id) {
        this.context = context.getApplicationContext();
        this.name = name;
        this.id = id;
        this.url = Default.URL_GET_TORRENT + id;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        checkFolder();
    }

    public Torrent(Context context, String name, String id, String size, String uploader, String category) {
        this.context = context.getApplicationContext();
        this.name = name;
        this.id = id;
        this.size = size;
        this.category = category;
        this.uploader = uploader;
        this.url = Default.URL_GET_TORRENT + id;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.json = new JSONArray();
        try {
            this.json = new JSONArray(prefs.getString("jsonTorrentList", "[]"));
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        checkFolder();
    }

    private void checkFolder() {
        File dir = new File(Environment.getExternalStorageDirectory(), DOWNLOAD_FOLDER);
        if(!dir.exists()) {
            if(!dir.mkdir()) {
                doNotify(R.drawable.ic_notif_torrent_failure, "Erreur dossier", "Création du dossier de téléchargements impossible.", 90, null);
            }
        }
    }

    public void download() {
        tDL = new torrentFileGetter();
        try {
            tDL.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bookmark() {
        dll = new AsyncDlLater();
        try {
            dll.execute();
        } catch (Exception e) {
        }
    }

    public void unbookmark() {
        dllNot = new AsyncDlLaterNot();
        try {
            dllNot.execute();
        } catch (Exception e) {
        }
    }

    public void delete() {
        new T411Logger(context).writeLine("Suppression du torrent "+getTorrentName());
        File file = new File(getTorrentPath(), getTorrentName());
        if(file.exists()) {
            file.delete();
        }
        JSONArray newJson = new JSONArray();
        Boolean deleted = false;
        for(int i = 0; i < json.length(); i++) {

            try {
                //Log.e("json ", json.get(i).toString() + "/ID+ "+this.id);
                if(deleted || !((JSONObject)json.get(i)).get("id").equals(this.id)) {
                    newJson.put(json.get(i));
                } else {
                    deleted = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString("jsonTorrentList",newJson.toString()).commit();
    }

    public void share() {

        String url = Default.URL_SHARE;
        if (prefs.getBoolean("shareDirectLink", false))
            url = Default.URL_GET_PREZ;

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, url + this.id + "\n\n-\n" + context.getString(R.string.shareSignature));
        share.putExtra(Intent.EXTRA_SUBJECT, "[t411] " + this.name);

        context.startActivity(Intent.createChooser(share, context.getString(R.string.Share)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void doNotify(int icon, String title, String subtitle, int id, PendingIntent pendingIntent) {
        try {
            if (pendingIntent == null)
                pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(icon)
                            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), new CategoryIcon(category).getIcon()))
                            .setContentTitle(title)
                            .setContentText(subtitle);

            if(icon == R.drawable.ic_notif_torrent_done) {
                Intent dlIntent = new Intent(context.getApplicationContext(), TorrentsListActivity.class);
                PendingIntent dlpI = PendingIntent.getActivity(context.getApplicationContext(), 0, dlIntent, 0);
                mBuilder.addAction(R.drawable.ic_downloads, "Téléchargements", dlpI);
            }

            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, mBuilder.build());

        } finally {
            try {
                Toast.makeText(this.context, subtitle, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            }
        }
    }

    private void cancelNotify(int id) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

    public void launchUrl() {
        Intent i = new Intent();
        i.setAction(android.content.Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setData(Uri.parse(Default.URL_GET_PREZ+this.id));
        context.startActivity(i);
    }

    public void open() {
        Intent i = new Intent();
        i.setAction(android.content.Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //i.setType("application/x-bittorrent");

        File file = new File(this.getTorrentPath(), this.getTorrentName());
        i.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(".torrent"));
        if(Build.VERSION.SDK_INT >= 24) {
            i.setDataAndNormalize(FileProvider.getUriForFile(context.getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file));
        }
        else {
            i.setData(Uri.fromFile(file));
        }

        context.startActivity(Intent.createChooser(i, context.getResources().getString(R.string.open_with_app)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    private class AsyncDlLaterNot extends AsyncTask<Void, String[], Void> {

        String msg;

        @Override
        protected Void doInBackground(Void... arg0) {
            APIBrowser apiBrowser = new APIBrowser(context).connect(Default.API_T411 + Default.URL_API_BOOKMARK_DELETE + id);
            new T411Logger(context).writeLine("Suppression du bookmark du torrent...");
            JSONObject o = apiBrowser.loadObject();

            this.msg = context.getString(R.string.bookmark_delete_success);

            if(o.has("code")) {
                try {
                    if(o.getInt("code") == 1401
                            || o.getInt("code") == 401) {
                        new T411Logger(context).writeLine("Bookmark introuvable", T411Logger.WARN);
                        this.msg = context.getString(R.string.bookmark_notfound);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

    }

    private class AsyncDlLater extends AsyncTask<Void, String[], Void> {

        String msg;

        @Override
        protected Void doInBackground(Void... arg0) {
            APIBrowser apiBrowser = new APIBrowser(context).connect(Default.API_T411 + Default.URL_API_BOOKMARK_TORRENT + id);
            new T411Logger(context).writeLine("Bookmark du torrent...");
            JSONObject o = apiBrowser.loadObject();
            if(o.has("id")) {
                new T411Logger(context).writeLine("Bookmark du torrent réussi");
                this.msg = context.getString(R.string.bookmark_torrent_success);
            } else {
                if(o.has("code")) {
                    try {
                        if(o.getInt("code") == 1402
                            || o.getInt("code") == 402) {
                            new T411Logger(context).writeLine("Doublon de bookmark", T411Logger.WARN);
                            this.msg = context.getString(R.string.bookmark_torrent_duplicate);
                        } else if(o.getInt("code") == 1403
                                || o.getInt("code") == 403) {
                            new T411Logger(context).writeLine("Erreur interne T411", T411Logger.ERROR);
                            this.msg = context.getString(R.string.bookmark_torrent_error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

    }

    private class torrentFileGetter extends AsyncTask<Object, Object, Boolean> {

        JSONArray json;
        APIBrowser apiBrowser;
        File file;

        public torrentFileGetter() {
            try {
                json = new JSONArray(prefs.getString("jsonTorrentList", "[]"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                doNotify(R.drawable.ic_notif_torrent_failure, name, "Permissions nécessaires", Integer.valueOf(id), null);
            }
            doNotify(R.drawable.ic_notif_torrent_downloading, name, "Téléchargement...", Integer.valueOf(id), null);
            new T411Logger(context).writeLine("Initialisation du téléchargement");

            file = new File(getTorrentPath(), getTorrentName());
        }

        @Override
        protected Boolean doInBackground(Object... arg0) {
            apiBrowser = new APIBrowser(context).connect(Default.API_T411 + Default.URL_API_GET_TORRENT + id);
            new T411Logger(context).writeLine("Téléchargement du fichier torrent...");
            boolean ret = apiBrowser.download(file);
            if(!ret && prefs.getBoolean("safemode_fallback", true)) {
                String login = prefs.getString("login", ""),
                        password = prefs.getString("password", "");
                new T411Logger(context).writeLine("Echec, tentative par la méthode alternative...");
                ret = new SuperT411HttpBrowser(context).login(login, password).connect(Default.URL_GET_TORRENT + id).download(file);
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            new T411Logger(context).writeLine("Procédure de téléchargement terminée (" + (result?"OK":"ERROR") + ")", (result?T411Logger.INFO:T411Logger.ERROR));

            Intent dlstatus = new Intent(Torrent.INTENT_UPDATE_STATUS);

            if(!result) {
                String error = apiBrowser.errorMessage;
                if(error == null) {
                    error = "Serveur API injoignable, reessayez ultérieurement.";
                }
                new T411Logger(context).writeLine(apiBrowser.errorMessage, T411Logger.ERROR);
                dlstatus.putExtra("message", error);
                dlstatus.putExtra("success", false);
                context.sendBroadcast(dlstatus);
                cancelNotify(Integer.valueOf(id));
            } else {

                try {
                    json.put(new JSONObject("{'title':'" + name.replaceAll("\'", "\\\\'") + "','uploader':'" + uploader + "','size':'" + size + "','id':'" + id + "', 'url':'" + url + "', 'category':'" + category + "', download_date: " + System.currentTimeMillis() + "}"));
                    prefs.edit().putString("jsonTorrentList", json.toString()).apply();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                try {

                    Intent i = new Intent();
                    i.setAction(android.content.Intent.ACTION_VIEW);

                    i.setDataAndType(Uri.fromFile(file), "application/x-bittorrent");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    if(Build.VERSION.SDK_INT >= 24) {
                        i.setDataAndType(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file), "application/x-bittorrent");
                    }

                    PendingIntent pI = PendingIntent.getActivity(context, 0, Intent.createChooser(i, context.getResources().getString(R.string.open_with_app)), PendingIntent.FLAG_UPDATE_CURRENT);
                    doNotify(R.drawable.ic_notif_torrent_done, name, "Téléchargement terminé !", Integer.valueOf(id), pI);
                    dlstatus.putExtra("message", "Téléchargement terminé");
                    dlstatus.putExtra("downloads", true);
                    dlstatus.putExtra("success", true);

                    if (prefs.getBoolean("openAfterDl", false)) {
                        //ouvrir le fichier
                        try {
                            context.getApplicationContext().startActivity(i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                            if (prefs.getBoolean("openAfterDlCancelNotify", false))
                                cancelNotify(Integer.valueOf(id));
                        } catch (Exception e) {
                            doNotify(R.drawable.ic_notif_torrent_failure, name, "Erreur d'ouverture du torrent\nAucune application trouvée.", Integer.valueOf(id), null);
                        }
                    }
                } finally {
                    context.sendBroadcast(dlstatus);
                }
            }
        }
    }

    public static String getTorrentPath() {
        return new File(Environment.getExternalStorageDirectory().getPath(), DOWNLOAD_FOLDER).getPath();
    }

    public String getTorrentName() {
        return name.replaceAll("/", "_") + ".torrent";
    }
}
