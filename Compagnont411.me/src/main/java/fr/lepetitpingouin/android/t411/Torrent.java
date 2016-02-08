package fr.lepetitpingouin.android.t411;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Torrent {

    public String name, id, url;
    public String size, uploader, age, seeders, leechers, avis, complets, ratioa, ratiob;
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

    public void cancelNotify(int id) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

    private class AsyncDlLaterNot extends AsyncTask<Void, String[], Void> {

        String msg = "";

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

                msg = doc.select("div#messages").first().text();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!msg.equals(""))
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

    }

    private class AsyncDlLater extends AsyncTask<Void, String[], Void> {

        String msg;

        @Override
        protected Void doInBackground(Void... arg0) {
            String username = prefs.getString("login", ""), password = prefs.getString("password", "");

            Connection.Response res;
            Document doc;

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
                        .connect(Default.URL_BOOKMARK + id)
                        .cookies(Cookies)
                        .data("login", username, "password", password)
                        .method(Connection.Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();

                //doc = Jsoup.parse(new SuperT411HttpBrowser(context).login(username, password).connect(Default.URL_BOOKMARK + id).executeInAsyncTask());

                //Log.e("bookmark", doc.body().toString());
                msg = doc.select("div.fade").first().text();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

    }

    private class torrentFileGetter extends AsyncTask<Void, String[], Void> {

        Connection.Response resTorrent;
        byte[] torrentFileContent;
        String torrentContent;

        @Override
        protected void onPreExecute() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                doNotify(R.drawable.ic_notif_torrent_failure, name, "Permissions nécessaires", Integer.valueOf(id), null);
            }

            doNotify(R.drawable.ic_notif_torrent_downloading, name, "Téléchargement...", Integer.valueOf(id), null);
            new T411Logger(context).writeLine("Initialisation du téléchargement");
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs.getString("password", "");

            try {

                boolean proxy = prefs.getBoolean("usePaidProxy", false);
                new T411Logger(context).writeLine(proxy?"Proxy dédié actif":"Proxy dédié inactif");

                /*

                new T411Logger(context).writeLine("Connexion par login/password pour " + username);
                Connection.Response res = Jsoup
                        .connect(Default.URL_LOGIN)
                        .data("login", username, "password", password)
                        .method(Connection.Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .execute();

                Map<String, String> Cookies = res.cookies();

                new T411Logger(context).writeLine("Lecture du fichier torrent...");


                resTorrent = Jsoup.connect(Default.URL_GET_TORRENT + id)
                        .data("login", username, "password", password)
                        .method(Connection.Method.POST)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                                //.timeout(prefs.getInt("timeoutValue", Default.timeout) * 1000)
                        .cookies(Cookies)
                        .execute();
                        torrentFileContent = resTorrent.bodyAsBytes();
                        */
                SuperT411HttpBrowser browser = new SuperT411HttpBrowser(context)
                        .connect(Default.URL_GET_TORRENT + id)
                        .login(username, password);
                torrentContent = browser.executeInAsyncTask();

                torrentFileContent = browser.getByteResponse();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            new T411Logger(context).writeLine("Ecriture du fichier torrent...");
            String path = prefs.getString("filePicker", Environment.getExternalStorageDirectory().getPath());

            File file = new File(path, name.replaceAll("/", "_") + ".torrent");
            file.setWritable(true, false);


            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {

                FileOutputStream fo = new FileOutputStream(file);
                fo.write(torrentFileContent);
                fo.close();

                /*OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
                osw.write(torrentContent);
                osw.close();*/

                Intent i = new Intent();
                i.setAction(android.content.Intent.ACTION_VIEW);
                //i.setDataAndType(Uri.fromFile(file), MimeTypeMap.getSingleton().getMimeTypeFromExtension("torrent"));
                if (prefs.getBoolean("addMimeType", false))
                    i.setDataAndType(Uri.fromFile(file), "application/x-bittorrent");
                else //auto-detect
                    //i.setDataAndType(Uri.fromFile(file), MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.getName().substring(file.getName().lastIndexOf(".")+1)));
                    i.setData(Uri.fromFile(file));

                PendingIntent pI = PendingIntent.getActivity(context, 0, Intent.createChooser(i, context.getResources().getString(R.string.open_with_app)), PendingIntent.FLAG_UPDATE_CURRENT);
                doNotify(R.drawable.ic_notif_torrent_done, name, "Téléchargement terminé !", Integer.valueOf(id), pI);
                if (prefs.getBoolean("openAfterDl", false)) {
                    //ouvrir le fichier
                    try {
                        context.getApplicationContext().startActivity(i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_UPDATE_CURRENT | Intent.FLAG_FROM_BACKGROUND));
                        if (prefs.getBoolean("openAfterDlCancelNotify", false))
                            cancelNotify(Integer.valueOf(id));
                    } catch (Exception e) {
                        doNotify(R.drawable.ic_notif_torrent_failure, name, "Erreur d'ouverture du torrent\nAucune application trouvée.", Integer.valueOf(id), null);
                    }
                }

            } catch (IOException e) {
                Intent i = new Intent();
                i.setClass(context, UserPrefsActivity.class);
                PendingIntent pI = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                doNotify(R.drawable.ic_notif_torrent_failure, name, "Le téléchargement a échoué...\nAccès au répertoire choisi impossible.", Integer.valueOf(id), pI);
                e.printStackTrace();
                new T411Logger(context).writeLine("Accès au répertoire choisi impossible : " + prefs.getString("filePicker", Environment.getExternalStorageDirectory().getPath()));
            } catch (Exception e) {
                Intent i = new Intent();
                i.setClass(context, UserPrefsActivity.class);
                PendingIntent pI = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                if (file.exists() && file.length() == 0) {
                    doNotify(R.drawable.ic_notif_torrent_failure, name, "Le téléchargement a échoué...\nErreur réseau : impossible de télécharger le contenu du fichier. Veuillez réessayer.", Integer.valueOf(id), pI);
                    e.printStackTrace();
                    new T411Logger(context).writeLine("Impossible de lire le contenu du fichier", T411Logger.ERROR);
                } else if (!file.exists()) {
                    doNotify(R.drawable.ic_notif_torrent_failure, name, "Le téléchargement a échoué...\nImpossible de créer le fichier.", Integer.valueOf(id), pI);
                } else {
                    doNotify(R.drawable.ic_notif_torrent_failure, name, "Le téléchargement a échoué...\nErreur inconnue.", Integer.valueOf(id), pI);
                }
            }
        }
    }
}
