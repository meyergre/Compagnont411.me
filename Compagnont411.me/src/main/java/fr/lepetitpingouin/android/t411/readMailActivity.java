package fr.lepetitpingouin.android.t411;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class readMailActivity extends AppCompatActivity {

    private SuperT411HttpBrowser browser;

    private SharedPreferences prefs;

    private mailDeleter mD;
    private mailArchiver mA;
    private mailGetter mG;

    private TextView tvobj; //objet du mail

    private ProgressDialog dialog;

    private String id;
    private String message;

    private WebView tvmsg;

    private String t411message;

    @Override
    public void onDestroy() {
        mD.cancel(true);
        mA.cancel(true);
        mG.cancel(true);
        super.onDestroy();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msgread);

        browser = new SuperT411HttpBrowser(getApplicationContext());

        mD = new mailDeleter();
        mA = new mailArchiver();
        mG = new mailGetter();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Message");

        Bundle bundle = this.getIntent().getExtras();
        id = bundle.getString("id");
        final String de = bundle.getString("de");
        String objet = bundle.getString("objet");
        String date = bundle.getString("date");

        tvobj = (TextView) findViewById(R.id.readMailSubject);

        tvmsg = (WebView) findViewById(R.id.www);

        tvobj.setText(objet);
        getSupportActionBar().setTitle(de);
        getSupportActionBar().setSubtitle(date);

        //dialog = ProgressDialog.show(this,this.getString(R.string.getMesageContent), this.getString(R.string.pleasewait), true, false);

        dialog = new ProgressDialog(this, R.style.AdTitleDialog);
        dialog.setMessage(this.getString(R.string.pleasewait));
        AdView mAdView;
        AdRequest adRequest;
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.adtitlebar, null);
        mAdView = (AdView) view.findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice(Private.REAL_DEVICE).build();
        dialog.setCustomTitle(view);
        mAdView.loadAd(adRequest);
        dialog.show();

        mG.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_msgread, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_delete:
                mD = new mailDeleter();
                mD.execute();
                return true;
            case R.id.action_archive:
                mA = new mailArchiver();
                mA.execute();
                return true;
            case R.id.action_answer:
                Intent i = new Intent();
                i.setClass(getApplicationContext(), ComposeMessageActivity.class);
                i.putExtra("to", getSupportActionBar().getTitle());

                String text = Jsoup.parse(message.replaceAll("(?i)<br[^>]*>", "br2n")).text();
                text = text.replaceAll("br2n", "\n");

                i.putExtra("msg", text);
                i.putExtra("subject", "Re: " + tvobj.getText());
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class mailDeleter extends AsyncTask<Void, String[], Void> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(
                    getApplicationContext(),
                    readMailActivity.this.getString(R.string.pleasewait)
                            + "...", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            Connection.Response res = null;
            Document doc = null;
            try {/*
                res = Jsoup
                        .connect(DELURL + id)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();*/
                //doc = Jsoup.parse(
                browser.login(username, password)
                        .connect(Default.URL_MESSAGE_DEL + id)
                        .executeInAsyncTask();
                //);

                t411message = browser.getFadeMessage();
            } catch (Exception ex) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (t411message != null)
                Toast.makeText(getApplicationContext(), t411message, Toast.LENGTH_SHORT).show();

            finish();
        }

    }

    private class mailArchiver extends AsyncTask<Void, String[], Void> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(
                    getApplicationContext(),
                    readMailActivity.this.getString(R.string.pleasewait)
                            + "...", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            Connection.Response res = null;
            Document doc = null;
            try {
                /*res = Jsoup
                        .connect(ARCURL + id)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();*/

                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext())
                        .login(username, password)
                        .connect(Default.URL_MESSAGE_ARC + id)
                        .executeInAsyncTask());

                t411message = doc.select("#messages").first().text();
            } catch (Exception ex) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (t411message != null)
                Toast.makeText(getApplicationContext(), t411message, Toast.LENGTH_SHORT).show();

            finish();
        }

    }

    private class mailGetter extends AsyncTask<Void, String[], Void> {

        String messageBody;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs.getString("password", "");

            //Connection.Response res = null;
            Document doc;
            try {
                /*res = Jsoup
                        .connect(URL + id)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();*/

                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext())
                        .login(username, password)
                        .connect(Default.URL_MESSAGE + id)
                        .executeInAsyncTask());

                message = "";

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("msg_style.css"), "UTF-8"));
                    String mLine = reader.readLine();
                    while (mLine != null) {
                        message += mLine + "\n";
                        mLine = reader.readLine();
                    }
                    reader.close();
                } catch (IOException e) {
                }

                messageBody = doc.select(".msg").first().html();

                // 'stylage' du message
                messageBody = messageBody.replaceAll(Default.MSG_SEPARATOR, Default.MSG_REPLACEMENT);

                message = message + messageBody;

            } catch (Exception e) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            final String mimeType = "text/html";
            final String encoding = "utf-8";
            tvmsg.loadDataWithBaseURL(null, message, mimeType, encoding, "");
            dialog.dismiss();
            if (messageBody == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.noConError), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
