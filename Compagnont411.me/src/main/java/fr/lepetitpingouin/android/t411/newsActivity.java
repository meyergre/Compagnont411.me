package fr.lepetitpingouin.android.t411;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class newsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newspaper);

        prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        TextView titre1 = (TextView) findViewById(R.id.news_titre1);
        titre1.setText(prefs.getString("title1", "-"));

        TextView titre2 = (TextView) findViewById(R.id.news_titre2);
        titre2.setText(prefs.getString("title2", "-"));

        TextView titre3 = (TextView) findViewById(R.id.news_titre3);
        titre3.setText(prefs.getString("title3", "-"));

        final String mimeType = "text/html";
        final String encoding = "utf-8";
        String customCSS = "<style>.readmore {display: none;}</style>";

        WebView warticle1 = (WebView) findViewById(R.id.www_article1);
        warticle1.loadDataWithBaseURL(null, customCSS + prefs.getString("article1", "...").replaceAll("<[aA]", "<u").replaceAll("</[aA]>", "</u>"), mimeType, encoding, "");

        WebView warticle2 = (WebView) findViewById(R.id.www_article2);
        warticle2.loadDataWithBaseURL(null, customCSS + prefs.getString("article2", "...").replaceAll("<[aA]", "<u").replaceAll("</[aA]>", "</u>"), mimeType, encoding, "");

        WebView warticle3 = (WebView) findViewById(R.id.www_article3);
        warticle3.loadDataWithBaseURL(null, customCSS + prefs.getString("article3", "...").replaceAll("<[aA]", "<u").replaceAll("</[aA]>", "</u>"), mimeType, encoding, "");

        Button readMore1 = (Button) findViewById(R.id.news_readmore1);
        readMore1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i;
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(prefs.getString("readMore1", "")));
                startActivity(i);
            }
        });

        Button readMore2 = (Button) findViewById(R.id.news_readmore2);
        readMore2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i;
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(prefs.getString("readMore2", "")));
                startActivity(i);
            }
        });

        Button readMore3 = (Button) findViewById(R.id.news_readmore3);
        readMore3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i;
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(prefs.getString("readMore3", "")));
                startActivity(i);
            }
        });
    }
}
