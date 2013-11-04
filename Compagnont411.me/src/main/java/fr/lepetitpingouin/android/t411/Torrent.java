package fr.lepetitpingouin.android.t411;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class Torrent {

    public String name, id, url;
    private Context context;
    private SharedPreferences prefs;
    private torrentFileGetter tDL;
    private AsyncDlLater dll;
    private AsyncDlLaterNot dllNot;

    public Torrent(Context context, String name, String id) {
        this.context = context.getApplicationContext();
        this.name = name;
        this.id = id;
        this.url = Default.URL_GET_TORRENT + id;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void download() {
        tDL = new torrentFileGetter();
        try {
            tDL.execute();
        } catch (Exception e) {
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

    public void doNotify(int icon, String title, String subtitle, int id, PendingIntent pendingIntent) {
        try {
            if (pendingIntent == null)
                pendingIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(subtitle);
            //TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            //stackBuilder.addParentStack(Torrent.class);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, mBuilder.build());

            /*
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            final Notification notification = new Notification(icon, title, System.currentTimeMillis());
            final String notificationTitle = title;
            final String notificationDesc = subtitle;
            notification.setLatestEventInfo(context, notificationTitle, notificationDesc, pendingIntent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(id, notification);*/
        } finally {
            try {
                Toast.makeText(this.context, subtitle, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            }
        }
    }

    private class AsyncDlLaterNot extends AsyncTask<Void, String[], Void> {

        String msg;

        @Override
        protected Void doInBackground(Void... arg0) {
            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            Connection.Response res = null;
            Document doc = null;

            try {
                res = Jsoup
                        .connect(Default.URL_LOGIN)
                        .data("login", username, "password", password)
                        .method(Connection.Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .execute();

                Map<String, String> Cookies = res.cookies();

                res = Jsoup
                        .connect(Default.URL_UNBOOKMARK)
                        .cookies(Cookies)
                        .data("id", "", "submit", "Supprimer", "ids[]", id)
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .method(Connection.Method.POST)
                        .execute();

                doc = res.parse();

                msg = doc.select("#messages ").first().text();

            } catch (Exception e) {
                Log.e("Erreur connect :", e.toString());
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
            String username = prefs.getString("login", ""), password = prefs.getString("password", "");

            Connection.Response res = null;
            Document doc = null;

            try {
                res = Jsoup
                        .connect(Default.URL_BOOKMARK + id)
                        .data("login", username, "password", password)
                        .method(Connection.Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();

                msg = doc.select("#messages ").first().text();

            } catch (Exception e) {
                Log.e("Erreur connect :", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

    }

    private class torrentFileGetter extends AsyncTask<Void, String[], Void> {

        byte[] torrentFileContent;

        @Override
        protected void onPreExecute() {
            doNotify(R.drawable.ic_notif_torrent_downloading, name, "Téléchargement...", Integer.valueOf(id), null);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            Connection.Response resTorrent = null;

            try {
                resTorrent = Jsoup
                        .connect(Default.URL_GET_TORRENT + id)
                        .data("login", username, "password", password)
                        .method(Connection.Method.POST)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)

                        .ignoreContentType(true).execute();

                torrentFileContent = resTorrent.bodyAsBytes();

            } catch (Exception e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String path = prefs.getString("savePath", Environment.getExternalStorageDirectory().getPath());
            File file = new File(path + File.separator + name.replaceAll("/", "-") + ".torrent");
            try { file.createNewFile(); } catch (Exception e) {}
            try {
                OutputStream fo = new FileOutputStream(file);
                fo.write(torrentFileContent);
                fo.close();

                Intent i = new Intent();
                i.setAction(android.content.Intent.ACTION_VIEW);
                //i.setDataAndType(Uri.fromFile(file), MimeTypeMap.getSingleton().getMimeTypeFromExtension(".torrent"));
                i.setDataAndType(Uri.fromFile(file), "application/x-bittorrent");
                //i.setData(Uri.fromFile(file));
                PendingIntent pI = PendingIntent.getActivity(context, 0, i, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT);
                doNotify(R.drawable.ic_notif_torrent_done, name, "Téléchargement terminé !", Integer.valueOf(id), pI);
            } catch (IOException e) {
                Intent i = new Intent();
                i.setClass(context, Settings.class);
                PendingIntent pI = PendingIntent.getActivity(context, 0, i, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT);
                doNotify(R.drawable.ic_notif_torrent_failure, name, "Le téléchargement a échoué...\nAccès au répertoire choisi impossible.", Integer.valueOf(id), pI);
            } catch (Exception e) {
                Intent i = new Intent();
                i.setClass(context, null);
                PendingIntent pI = PendingIntent.getActivity(context, 0, i, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT);
                if (file.exists() && file.length() == 0) {
                    doNotify(R.drawable.ic_notif_torrent_failure, name, "Le téléchargement a échoué...\nErreur réseau : impossible de télécharger le contenu du fichier.", Integer.valueOf(id), pI);
                } else if (!file.exists()) {
                    doNotify(R.drawable.ic_notif_torrent_failure, name, "Le téléchargement a échoué...\nImpossible de créer le fichier.", Integer.valueOf(id), pI);
                } else {
                    doNotify(R.drawable.ic_notif_torrent_failure, name, "Le téléchargement a échoué...\nErreur inconnue.", Integer.valueOf(id), pI);
                }
            }
        }
    }
}
