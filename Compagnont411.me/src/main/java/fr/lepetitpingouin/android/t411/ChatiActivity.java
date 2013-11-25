package fr.lepetitpingouin.android.t411;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

public class ChatiActivity extends ActionBarActivity {

    SharedPreferences prefs;
    WebView www;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chati);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        getSupportActionBar().setIcon(R.drawable.ic_newlauncher);
        getSupportActionBar().setTitle("ShoutBox");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        www = (WebView) findViewById(R.id.www_chati);
        www.getSettings().setJavaScriptEnabled(true);
        www.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        www.setWebViewClient(new WebViewClient());
        www.setWebChromeClient(new WebChromeClient());

        new AsyncChati().execute();
    }

    private class AsyncChati extends AsyncTask<String, String, String> {
        Connection.Response res = null;
        Document doc = null;
        Map<String, String> Cookies;

        @Override
        protected String doInBackground(String... params) {
            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");


            try {
                res = Jsoup
                        .connect(Default.URL_LOGIN)
                        .data("login", username, "password", password)
                        .method(Connection.Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .execute();

                Cookies = res.cookies();

                res = Jsoup
                        .connect(Default.URL_CHATI)
                        .cookies(Cookies)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .method(Connection.Method.POST)
                        .execute();

                doc = res.parse();

            }
            catch (Exception e) {
                Log.e("ERREUR ASYNCTASK", e.toString());}
            return null;
        }

        @Override
        public void onPostExecute(String result) {
            try {
                String html = doc.html();
                html = html.replaceAll("=\"/", "=\""+Default.URL_INDEX+"/");
                www.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
                //www.loadUrl(Default.URL_CHATI, Cookies);
            }
            catch(Exception e) {
                Log.e("POSTEXECUTE", e.toString());
            }
            super.onPostExecute(result);
        }
    }
}
