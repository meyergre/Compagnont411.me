package fr.lepetitpingouin.android.t411;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

public class statsActivity extends ActionBarActivity {
    SharedPreferences prefs;
    WebView www;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        www = (WebView) findViewById(R.id.webView1);
        www.getSettings().setJavaScriptEnabled(true);
        www.setWebViewClient(new WebViewClient());
        www.setWebChromeClient(new WebChromeClient());

        www.loadDataWithBaseURL(null, prefs.getString("lastGraph", getString(R.string.errorGraph)), "text/html", "utf-8", null);



        findViewById(R.id.seedbox).setVisibility(prefs.getBoolean("seedbox", false) ? View.VISIBLE : View.GONE);


        ((TextView)findViewById(R.id.stat_username)).setText(prefs.getString("lastUsername", "..."));
        ((TextView)findViewById(R.id.stat_username)).setTextColor(new Ratio(this).getTitleColor());

        String classe = prefs.getString("classe", "...");
        String titre = prefs.getString("titre", "");
        String status = classe + ((titre.length() > 1) ? ", " + titre : "");
        ((TextView)findViewById(R.id.stat_class)).setText(status);
        ((TextView)findViewById(R.id.stat_upload)).setText("▲ " + new BSize(prefs.getString("lastUpload", "0.00 GB")).convert());
        ((TextView)findViewById(R.id.stat_download)).setText("▼ " + new BSize(prefs.getString("lastDownload", "0.00 GB")).convert());
        ((TextView)findViewById(R.id.stat_ratio)).setText("R " + prefs.getString("lastRatio", " "));
        ((ImageView)findViewById(R.id.stat_avatar)).setImageBitmap(new AvatarFactory().getFromPrefs(prefs));

        ((TextView)findViewById(R.id.tv_up24)).setText(new BSize(prefs.getString("up24", "0.00 GB")).convert());
        ((TextView)findViewById(R.id.tv_dl24)).setText(new BSize(prefs.getString("dl24", "0.00 GB")).convert());
        ((TextView)findViewById(R.id.tv_dlleft)).setText(new BSize(prefs.getString("GoLeft", "0.00 GB")).convert() + " " + getResources().getString(R.string.DownloadLeft));
        ((TextView)findViewById(R.id.tv_upleft)).setText(new BSize(prefs.getString("UpLeft", "0.00 GB")).convert() + " " +getResources().getString(R.string.UploadLeft));
    }

    public void onSeedboxClick(View v) {
        Snackbar.make(www, "Seedbox / Optique / Dédié déclaré(e)", Snackbar.LENGTH_LONG).show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
