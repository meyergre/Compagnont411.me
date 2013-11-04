package fr.lepetitpingouin.android.t411;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

public class messagesActivity extends ActionBarActivity {
    static final String CONNECTURL = Default.URL_MAILS;
    public ProgressDialog dialog;

    SharedPreferences prefs;
    Editor edit;

    mailFetcher mF;

    HashMap<String, String> map;

    ListView maListViewPerso;
    ArrayList<HashMap<String, String>> listItem;

    @Override
    public void onDestroy() {
        mF = null;
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msglist);

        prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.button_mail));

        maListViewPerso = (ListView) findViewById(R.id.malistviewperso);

        listItem = new ArrayList<HashMap<String, String>>();

        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1991);
        } catch (Exception e) {
        }

        update();
    }

    public void update() {
        mF = new mailFetcher();
        dialog = ProgressDialog.show(messagesActivity.this, this.getString(R.string.getMesages), this.getString(R.string.pleasewait), true, true);
        try {
            mF.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class mailFetcher extends AsyncTask<Void, String[], Void> {

        Connection.Response res = null;
        Document doc = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            this.cancel(true);
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            listItem.clear();

            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");
            Log.v("Credentials :", username + "/" + password);

            String url = CONNECTURL;
            if (prefs.getBoolean("useHTTPS", false))
                url = CONNECTURL.replace("http://", "https://");

            try {
                res = Jsoup
                        .connect(url)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();
                doc = res.parse();

            } catch (Exception e) {
                Log.e("erreur", e.toString());
                Toast.makeText(getApplicationContext(),
                        "Erreur lors de la r�cup�ration des messages...",
                        Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            try {
                int unread = 0;
                for (Element table : doc.select("table.mailbox tbody")) {
                    for (Element row : table.select("tr")) {
                        Elements tds = row.select("td");
                        // Log.e("Test TD = ",tds.get(1).text());
                        int mailStatus;
                        if (row.hasAttr("class")) {
                            mailStatus = R.drawable.mail_unread;
                            unread++;
                        } else
                            mailStatus = R.drawable.mail_read;
                        Log.v("export de la ligne " + tds.get(2).text(),
                                row.className());
                        map = new HashMap<String, String>();
                        map.put("de", tds.get(1).text());
                        map.put("objet", tds.get(2).text());
                        map.put("etat", String.valueOf(mailStatus));
                        map.put("id", tds.get(0).select("input").val());
                        map.put("date", tds.get(3).text());
                        listItem.add(map);
                    }
                }
                edit = prefs.edit();
                edit.putString("mails", String.valueOf(unread));
                edit.commit();

            } catch (Exception ex) {
                Log.e("Erreur test TD", ex.toString());
            }

            SimpleAdapter mSchedule = new SimpleAdapter(
                    messagesActivity.this.getBaseContext(), listItem,
                    R.layout.item_msglist, new String[]{"de", "objet",
                    "etat", "date"}, new int[]{R.id.fromuser,
                    R.id.mailsubject, R.id.mailstate, R.id.maildate});

            try {
                maListViewPerso.setAdapter(mSchedule);
            } catch (Exception ex) {
            }

            maListViewPerso.setOnItemClickListener(new OnItemClickListener() {
                @Override
                @SuppressWarnings("unchecked")
                public void onItemClick(AdapterView<?> a, View v, int position,
                                        long id) {
                    HashMap<String, String> map = (HashMap<String, String>) maListViewPerso
                            .getItemAtPosition(position);

                    Intent myIntent = new Intent();
                    myIntent.setClass(getApplicationContext(),
                            readMailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("id", map.get("id"));
                    bundle.putString("de", map.get("de"));
                    bundle.putString("objet", map.get("objet"));
                    bundle.putString("date", map.get("date"));
                    myIntent.putExtras(bundle);
                    startActivity(myIntent);
                }
            });

            dialog.cancel();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_msglist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_compose:
                Intent myIntent = new Intent(messagesActivity.this, ComposeMessageActivity.class);
                startActivity(myIntent);
                return true;
            case R.id.action_update:
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
