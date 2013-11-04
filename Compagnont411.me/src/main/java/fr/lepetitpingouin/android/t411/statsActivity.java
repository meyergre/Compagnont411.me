package fr.lepetitpingouin.android.t411;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class statsActivity extends ActionBarActivity {
    SharedPreferences prefs;
    WebView www;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        getSupportActionBar().setIcon(R.drawable.ic_stats);
        getSupportActionBar().setTitle(getResources().getString(R.string.statistics));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        www = (WebView) findViewById(R.id.webView1);
        www.getSettings().setJavaScriptEnabled(true);
        www.setWebViewClient(new WebViewClient());
        www.setWebChromeClient(new WebChromeClient());

        www.loadDataWithBaseURL(null, prefs.getString("lastGraph", "?"), "text/html", "utf-8", null);
    }
}
