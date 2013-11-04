package fr.lepetitpingouin.android.t411;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FilesListActivity extends ActionBarActivity {

    WebView www;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);

        getSupportActionBar().setIcon(R.drawable.ic_list_files);
        getSupportActionBar().setTitle("Liste des fichiers");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        www = (WebView) findViewById(R.id.www_listfiles);
        www.setWebViewClient(new WebViewClient());
        www.setWebChromeClient(new WebChromeClient());
        www.loadDataWithBaseURL(null, getIntent().getStringExtra("listHtml"), "text/html", "utf-8", null);
    }
}
