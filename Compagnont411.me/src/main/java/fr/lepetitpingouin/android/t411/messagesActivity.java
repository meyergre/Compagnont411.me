package fr.lepetitpingouin.android.t411;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

public class messagesActivity extends AppCompatActivity {
    private static final String CONNECTURL = Default.URL_MAILS;

    private SharedPreferences prefs;
    private Editor edit;

    private mailFetcher mF;

    private HashMap<String, String> map;
    private HashMap<String, String> itemMap;

    private ListView maListViewPerso;
    private ArrayList<HashMap<String, String>> listItem;

    private FloatingActionButton fab;

    private SwipeRefreshLayout srl;

    PackageManager pkgMgr;
    ComponentName cn;

    @Override
    public void onDestroy() {
        mF = null;
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msglist);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        pkgMgr = getPackageManager();
        cn = new ComponentName(getPackageName(), getPackageName()+".launcherMessages");

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.button_mail));

        fab = (FloatingActionButton) findViewById(R.id.fabcompose);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(messagesActivity.this, ComposeMessageActivity.class);
                startActivity(myIntent);
            }
        });

        srl = (SwipeRefreshLayout)findViewById(R.id.srl);
        srl.measure(50, 50);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
            }
        });

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

    private void update() {
        mF = new mailFetcher();
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
        menu.findItem(R.id.msg_showHome).setChecked(pkgMgr.getComponentEnabledSetting(cn)!=PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.msg_showHome:
                pkgMgr.setComponentEnabledSetting(cn, item.isChecked()?PackageManager.COMPONENT_ENABLED_STATE_DISABLED:PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                item.setChecked(!item.isChecked());
                Toast.makeText(getApplicationContext(), "L'icône va "+(item.isChecked()?"apparaître":"disparaître")+" dans quelques instants...", Toast.LENGTH_SHORT).show();
                return true;
                case R.id.msg_deleteAll:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete) + " ?")
                        .setMessage(getString(R.string.confirmDeleteAllMessage))
                        .setPositiveButton(getString(R.string.delete).toUpperCase(), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO: find way to call real deleteall function on site (if exist)
                                for (int i=0; i<maListViewPerso.getCount();i++) {
                                    // Remove mail here
                                    itemMap = (HashMap<String, String>) maListViewPerso.getItemAtPosition(i);
                                    new Message(getApplicationContext(), itemMap.get("id")).delete();
                                }
                                update();
                            }

                        })
                        .setNegativeButton(getString(R.string.cancel).toUpperCase(), null)
                        .show();
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
        if(info.position>maListViewPerso.getCount()) return false;
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
            srl.setRefreshing(true);
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

            Log.e("LAUNCHING", "BROWSER....");

            try {
                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext())
                        .login(username, password)
                        .connect(url)
                        .executeInAsyncTask());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            srl.setRefreshing(false);

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

        }
    }
}
