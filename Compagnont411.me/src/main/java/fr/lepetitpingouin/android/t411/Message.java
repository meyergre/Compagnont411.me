package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by gregory on 22/11/2013.
 */
class Message {

    private String id;
    private String t411message = "";
    private SuperT411HttpBrowser browser;
    private Context context;
    private SharedPreferences prefs;

    public Message(Context context, String id) {
        this.context = context;
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
            //Toast.makeText(context,context.getString(R.string.pleasewait)+ "...", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs.getString("password", "");

            Log.d("Message", "Delete");
            String html = "";
            try {
                browser = new SuperT411HttpBrowser(context);
                browser.login(username, password)
                        .connect(Default.URL_MESSAGE_DEL + id)
                        .executeInAsyncTask();

                t411message = browser.getFadeMessage();
            } catch (Exception ex) {
                Log.e("Delete message", ex.toString());
            }
            Log.d("Message", t411message);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //if(t411message != null)
            //Toast.makeText(context, t411message,Toast.LENGTH_SHORT).show();
        }

    }

    private class mailArchiver extends AsyncTask<Void, String[], Void> {

        @Override
        protected void onPreExecute() {
            //Toast.makeText(context,context.getString(R.string.pleasewait)+ "...", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs.getString("password", "");
            try {

                browser = new SuperT411HttpBrowser(context);
                browser.login(username, password)
                        .connect(Default.URL_MESSAGE_ARC + id)
                        .executeInAsyncTask();

                t411message = browser.getFadeMessage();
            } catch (Exception ex) {
                Log.e("Archivage message", ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //if(!t411message.equals(""))
            //Toast.makeText(context, t411message,Toast.LENGTH_SHORT).show();
        }

    }

}

