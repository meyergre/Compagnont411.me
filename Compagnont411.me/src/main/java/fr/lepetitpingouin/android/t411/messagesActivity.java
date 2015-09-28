package fr.lepetitpingouin.android.t411;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.jsoup.Connection;
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
    HashMap<String, String> itemMap;

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
        registerForContextMenu(maListViewPerso);

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
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.malistviewperso) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.message_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        itemMap = (HashMap<String, String>) maListViewPerso.getItemAtPosition(info.position);
        switch (item.getItemId()) {
            case R.id.messages_context_menu_read:
                // Open mail here
                Intent myIntent = new Intent();
                myIntent.setClass(getApplicationContext(), readMailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", itemMap.get("id"));
                bundle.putString("de", itemMap.get("de"));
                bundle.putString("objet", itemMap.get("objet"));
                bundle.putString("date", itemMap.get("date"));
                myIntent.putExtras(bundle);
                startActivity(myIntent);
                return true;
            case R.id.messages_context_menu_delete:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete) + " ?")
                        .setMessage(getString(R.string.confirmDeleteMessage).replace("%MESSAGE%", itemMap.get("objet")))
                        .setPositiveButton(getString(R.string.delete).toUpperCase(), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Remove mail here
                                new Message(getApplicationContext(), itemMap.get("id")).delete();
                                update();
                            }

                        })
                        .setNegativeButton(getString(R.string.cancel).toUpperCase(), null)
                        .show();
                return true;
            case R.id.messages_context_menu_archive:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.archive) + " ?")
                        .setMessage(getString(R.string.confirmArchiveMessage).replace("%MESSAGE%", itemMap.get("objet")))
                        .setPositiveButton(getString(R.string.archive).toUpperCase(), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Archive mail here
                                new Message(getApplicationContext(), itemMap.get("id")).archive();
                                update();

                            }

                        })
                        .setNegativeButton(getString(R.string.cancel).toUpperCase(), null)
                        .show();
                return true;
            default:
                return super.onContextItemSelected(item);
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


            String url = CONNECTURL;
            //if (prefs.getBoolean("useHTTPS", false))
            //    url = CONNECTURL.replace("http://", "https://");

            try {/*
                res = Jsoup
                        .connect(url)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();
                doc = res.parse();*/
                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext())
                        .login(username, password)
                        .connect(url)
                        .executeInAsyncTask());

            } catch (Exception e) {

                //Toast.makeText(getApplicationContext(), "Erreur lors de la récupération des messages...", Toast.LENGTH_SHORT).show();
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
                        int mailStatus;
                        if (row.hasAttr("class")) {
                            mailStatus = R.drawable.mail_unread;
                            unread++;
                        } else
                            mailStatus = R.drawable.mail_read;
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
                    myIntent.setClass(getApplicationContext(), readMailActivity.class);
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
}
