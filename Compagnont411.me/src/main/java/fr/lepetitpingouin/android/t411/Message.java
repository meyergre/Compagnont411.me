package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by gregory on 22/11/2013.
 */
public class Message {

    public String id;
    private Context context;
    private SharedPreferences prefs;
    String t411message = "OK";

    public Message(Context context) {
        this.context = context.getApplicationContext();
    }

    public Message(Context context, String id) {
        this.context = context.getApplicationContext();
        this.id = id;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void delete() {
        // Remove mail here
        new mailDeleter().execute();
    }

    public void archive() {
        // Archive mail here
        new mailArchiver().execute();
    }


    private class mailDeleter extends AsyncTask<Void, String[], Void> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(
                    context,
                    context.getString(R.string.pleasewait)
                            + "...", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs.getString("password", "");

            Document doc;
            try {
                doc = Jsoup.parse(
                        new SuperT411HttpBrowser(context)
                                .login(username, password)
                                .connect(Default.URL_MESSAGE_DEL + id)
                                .executeInAsyncTask()
                );

                t411message = doc.select("#messages").first().text();
            } catch (Exception ex) {
                Log.e("Archivage message", ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(t411message != null)
                Toast.makeText(context, t411message,Toast.LENGTH_SHORT).show();
        }

    }

    private class mailArchiver extends AsyncTask<Void, String[], Void> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(
                    context,
                    context.getString(R.string.pleasewait)
                            + "...", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs.getString("password", "");

            Document doc;
            try {

                doc = Jsoup.parse(new SuperT411HttpBrowser(context)
                        .login(username, password)
                        .connect(Default.URL_MESSAGE_ARC + id)
                        .executeInAsyncTask());

                t411message = doc.select("#messages").first().text();
            } catch (Exception ex) {
                Log.e("Archivage message", ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(t411message != null)
                Toast.makeText(context, t411message,Toast.LENGTH_SHORT).show();
        }

    }

}

