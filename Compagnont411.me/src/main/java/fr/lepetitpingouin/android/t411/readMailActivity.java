package fr.lepetitpingouin.android.t411;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class readMailActivity extends ActionBarActivity {

    public final String URL = "http://www.t411.me/users/login/?returnto=%2Fmailbox%2Fmail%2F%3Fid%3D";
    public final String DELURL = "http://www.t411.me/users/login/?returnto=%2Fmailbox%2Fdelete%2F%3Fid%3D";
    public final String ARCURL = "http://www.t411.me/users/login/?returnto=%2Fmailbox%2Farchive%2F%3Fid%3D";

    SharedPreferences prefs;

    mailDeleter mD;
    mailArchiver mA;
    mailGetter mG;

    ProgressDialog dialog;

    String id, message;

    WebView tvmsg;

    String t411message = "???";

    ImageView btnReply;

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

        mD = new mailDeleter();
        mA = new mailArchiver();
        mG = new mailGetter();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Message");

        Bundle bundle = this.getIntent().getExtras();
        id = bundle.getString("id");
        final String de = bundle.getString("de");
        String objet = bundle.getString("objet");
        String date = bundle.getString("date");

        final TextView tvexp = (TextView) findViewById(R.id.readMailSender);
        final TextView tvobj = (TextView) findViewById(R.id.readMailSubject);
        TextView tvdate = (TextView) findViewById(R.id.readMailDate);
        tvmsg = (WebView) findViewById(R.id.www);

        tvexp.setText(de);
        tvobj.setText(objet);
        tvdate.setText(date);

        btnReply = (ImageView) findViewById(R.id.btnReply);
        btnReply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), ComposeMessageActivity.class);
                i.putExtra("to", tvexp.getText());

                String text = Jsoup.parse(message.replaceAll("(?i)<br[^>]*>", "br2n")).text();
                text = text.replaceAll("br2n", "\n");

                i.putExtra("msg", text);
                i.putExtra("subject", "Re: " + tvobj.getText());
                startActivity(i);
            }
        });

        dialog = ProgressDialog.show(this,
                this.getString(R.string.getMesageContent),
                this.getString(R.string.pleasewait), true, false);

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
            try {
                res = Jsoup
                        .connect(DELURL + id)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();

                t411message = doc.select("#messages").first().text();
                Log.v("archive link :", DELURL + id);
            } catch (Exception ex) {
                Log.e("Archivage message", ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), t411message,
                    Toast.LENGTH_SHORT).show();

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
                res = Jsoup
                        .connect(ARCURL + id)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();

                t411message = doc.select("#messages").first().text();
                Log.v("archive link :", ARCURL + id);
            } catch (Exception ex) {
                Log.e("Archivage message", ex.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), t411message,
                    Toast.LENGTH_SHORT).show();

            finish();
        }

    }

    private class mailGetter extends AsyncTask<Void, String[], Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            Connection.Response res = null;
            Document doc = null;
            Log.v("URL", URL + id);
            try {
                res = Jsoup
                        .connect(URL + id)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();

                message = doc.select(".msg").first().html();

                final String mimeType = "text/html";
                final String encoding = "utf-8";

                tvmsg.loadDataWithBaseURL(null, message, mimeType, encoding, "");

            } catch (Exception e) {
                Log.e("Erreur connect :", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
        }
    }
}
