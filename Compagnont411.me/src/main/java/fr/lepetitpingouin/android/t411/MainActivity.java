package fr.lepetitpingouin.android.t411;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends ActionBarActivity {

    LinearLayout connectButton, mailButton,
            aboutButton, btnStats, hWidget, btnCalculator, btnThread, btnNews, btnTop100, btnDlLater, btnFriends, btnUploads, btnReport;
    TextView hLogin, dLogin, dClass, hMails, hUP, hDOWN, hRatio, hGoLeft, hUpLeft,
            hUP24, hDL24;
    Context context;
    ImageView hSmiley, hAvatar;

    SharedPreferences prefs;

    WebView www;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == Default.Appwidget_flag_updating) {
                setSupportProgressBarIndeterminateVisibility(true);
                getSupportActionBar().setSubtitle("Mise \u00e0 jour...");
            } else {
                setSupportProgressBarIndeterminateVisibility(false);
                updateValues();
            }

        }
    };

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        updateValues();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        trimCache(this);
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    public void onDisconnectPressed(View v) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_disconnect)
                .setTitle(R.string.disconnectConfirmTitle)
                .setMessage(R.string.disconnectConfirmMessage)
                .setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), R.string.button_disconnect, Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor edit = prefs.edit();
                        edit.remove("firstLogin");
                        edit.remove("login");
                        edit.remove("password");
                        edit.putBoolean("autoUpdate", false);
                        edit.commit();
                        finish();
                    }

                })
                .setNegativeButton(R.string.NO, null)
                .show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mDrawerLayout = (android.support.v4.widget.DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                MainActivity.this,
                mDrawerLayout,
                R.drawable.ic_actionbar_menu,
                R.string.app_name,
                R.string.app_name) {
            public void onDrawerClosed(View view) {
                ActivityCompat.invalidateOptionsMenu(MainActivity.this); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                ActivityCompat.invalidateOptionsMenu(MainActivity.this); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setSubtitle(prefs.getString("lastDate", ""));

        if (!prefs.getBoolean("firstLogin", false)) {
            Intent myIntent = new Intent(MainActivity.this, FirstLoginActivity.class);
            startActivity(myIntent);
            finish();
        }/* else if (!prefs.getBoolean("firstSettings", false)) {
            Intent myIntent = new Intent(MainActivity.this, Settings.class);
            startActivity(myIntent);
        }*/

        IntentFilter filter = new IntentFilter();
        filter.addAction(Default.Appwidget_update);
        filter.addAction(Default.Appwidget_flag_updating);
        try {
            registerReceiver(receiver, filter);
        } catch (Exception e) {
        }

        www = (WebView) findViewById(R.id.homeWebview);
        www.getSettings().setJavaScriptEnabled(true);
        www.setWebViewClient(new WebViewClient());
        www.setWebChromeClient(new WebChromeClient());
        www.loadDataWithBaseURL(null, prefs.getString("lastGraph", ""), "text/html", "utf-8", null);


        //nouvelle interface
        try {
            LinearLayout seedbox = (LinearLayout) findViewById(R.id.seedbox);
            seedbox.setVisibility(prefs.getBoolean("seedbox", false) ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
        }

        context = this.getApplicationContext();
        updateValues();

        btnThread = (LinearLayout) findViewById(R.id.btnForum);
        btnThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Default.URL_THREAD));
                startActivity(i);
            }
        });

        final Dialog dialogTOP = new Dialog(this, R.style.MyDialogTheme);
        dialogTOP.setContentView(R.layout.dialog_listview);
        dialogTOP.setTitle(getString(R.string.top_100));

        final ListView maListViewPersoSort = (ListView) dialogTOP.findViewById(R.id.dialoglistview);
        ArrayList listItemSort = new ArrayList<HashMap<String, String>>();

        HashMap<String, String> map;

        map = new HashMap<String, String>();
        map.put("icon", String.valueOf(R.drawable.ic_chrono));
        map.put("name", getString(R.string.fast_torrents));
        map.put("code", "http://www.t411.me/top/100/");
        map.put("sender", "100");
        listItemSort.add(map);

        map = new HashMap<String, String>();
        map.put("icon", String.valueOf(R.drawable.ic_calendar));
        map.put("name", getString(R.string.daily_torrents));
        map.put("code", "http://www.t411.me/top/today/");
        map.put("sender", "top");
        listItemSort.add(map);

        map = new HashMap<String, String>();
        map.put("icon", String.valueOf(R.drawable.ic_calendar));
        map.put("name", getString(R.string.weekly_torrents));
        map.put("code", "http://www.t411.me/top/week/");
        map.put("sender", "top");
        listItemSort.add(map);

        map = new HashMap<String, String>();
        map.put("icon", String.valueOf(R.drawable.ic_calendar));
        map.put("name", getString(R.string.monthly_torrents));
        map.put("code", "http://www.t411.me/top/month/");
        map.put("sender", "top");
        listItemSort.add(map);

        SimpleAdapter mSchedule = new SimpleAdapter(
                this.getBaseContext(), listItemSort,
                R.layout.item_searchoptions, new String[]{"icon", "name",
                "code"}, new int[]{R.id.lso_icon,
                R.id.lso_title, R.id.lso_code});

        maListViewPersoSort.setAdapter(mSchedule);
        maListViewPersoSort.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) maListViewPersoSort
                        .getItemAtPosition(position);

                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentsActivity.class);
                i.putExtra("url", map.get("code"));
                i.putExtra("keywords", map.get("name"));
                i.putExtra("showIcons", false);
                i.putExtra("sender", map.get("sender"));
                startActivity(i);

                dialogTOP.dismiss();
            }
        });

        btnTop100 = (LinearLayout) findViewById(R.id.btnTop100);
        btnTop100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogTOP.show();
            }
        });

        btnDlLater = (LinearLayout) findViewById(R.id.btnBookmarks);
        btnDlLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentsActivity.class);
                i.putExtra("url", Default.URL_BOOKMARKS);
                i.putExtra("keywords", getString(R.string.bookmarks));
                i.putExtra("showIcons", false);
                i.putExtra("sender", "bookmarks");
                startActivity(i);
            }
        });

        btnNews = (LinearLayout) findViewById(R.id.btn_news);
        btnNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this,
                        newsActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        btnCalculator = (LinearLayout) findViewById(R.id.btnCalculator);
        btnCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this,
                        CalculatorActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        mailButton = (LinearLayout) findViewById(R.id.btnMails);
        mailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on click
                Intent myIntent = new Intent(MainActivity.this,
                        messagesActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        btnStats = (LinearLayout) findViewById(R.id.btnStats);
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on click
                Intent myIntent = new Intent(MainActivity.this,
                        statsActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        hWidget = (LinearLayout) findViewById(R.id.hWidget);
        hWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateValues();
            }
        });

        connectButton = (LinearLayout) findViewById(R.id.btnService);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on click
                Intent myIntent = new Intent(MainActivity.this, UserPrefsActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        btnUploads = (LinearLayout) findViewById(R.id.btnUploads);
        btnUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentsActivity.class);
                i.putExtra("url", Default.URL_UPLOADS);
                i.putExtra("keywords", getString(R.string.my_uploads));
                i.putExtra("showIcons", false);
                i.putExtra("sender", "uploads");
                startActivity(i);
            }
        });

        aboutButton = (LinearLayout) findViewById(R.id.LinearLayout01);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on click
                Intent myIntent = new Intent(MainActivity.this,
                        aboutActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        if (!prefs.getBoolean("devPreview", false)) {
            // nothing to do
        }

        btnFriends = (LinearLayout) findViewById(R.id.btnFriends);
        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), friendsActivity.class);
                startActivity(i);
            }
        });

        btnReport = (LinearLayout) findViewById(R.id.btn_report);
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), ReportActivity.class);
                startActivity(i);
            }
        });

        updateView();
    }

    private void updateView() { /*
        btnTop100.setVisibility(prefs.getBoolean("menuTop100", false) ? View.GONE : View.VISIBLE);
        btnStats.setVisibility(prefs.getBoolean("menuStats", false) ? View.GONE : View.VISIBLE);
        btnNews.setVisibility(prefs.getBoolean("menuNews", false) ? View.GONE : View.VISIBLE);
        btnCalculator.setVisibility(prefs.getBoolean("menuCalc", false) ? View.GONE : View.VISIBLE);
        btnThread.setVisibility(prefs.getBoolean("menuForum", false) ? View.GONE : View.VISIBLE);
        mailButton.setVisibility(prefs.getBoolean("menuInbox", false) ? View.GONE : View.VISIBLE);
        btnDlLater.setVisibility(prefs.getBoolean("dlLater", false) ? View.GONE : View.VISIBLE);
        btnFriends.setVisibility(prefs.getBoolean("menuFriends", false) ? View.GONE : View.VISIBLE);*/
        www.loadDataWithBaseURL(null, prefs.getString("lastGraph", ""), "text/html", "utf-8", null);

    }

    public void updateValues() {
        www.loadDataWithBaseURL(null, prefs.getString("lastGraph", ""), "text/html", "utf-8", null);

        getSupportActionBar().setSubtitle(prefs.getString("lastDate", "???"));

        hUP24 = (TextView) findViewById(R.id.hUP24);
        hUP24.setText(" " + new BSize(prefs.getString("up24", "0.00 GB")).convert());

        hDL24 = (TextView) findViewById(R.id.hDL24);
        hDL24.setText(" " + new BSize(prefs.getString("dl24", "0.00 GB")).convert());

        hLogin = (TextView) findViewById(R.id.hLogin);
        hLogin.setText(prefs.getString("lastUsername", "???"));


        dLogin = (TextView) findViewById(R.id.drawer_username);
        dLogin.setText(prefs.getString("lastUsername", "???"));
        dClass = (TextView) findViewById(R.id.drawer_class);
        dClass.setText(prefs.getString("classe", "???"));

        hMails = (TextView) findViewById(R.id.hMails);
        hMails.setText(String.valueOf(prefs.getInt("lastMails", 0)));

        hUP = (TextView) findViewById(R.id.upload);
        hUP.setText(new BSize(prefs.getString("lastUpload", "0.00 GB")).convert());

        hDOWN = (TextView) findViewById(R.id.download);
        hDOWN.setText(new BSize(prefs.getString("lastDownload", "0.00 GB")).convert());

        hRatio = (TextView) findViewById(R.id.hRatio);
        hRatio.setText(String.format("%.2f", Double.valueOf(prefs.getString("lastRatio", "0.00"))));

        hGoLeft = (TextView) findViewById(R.id.hGoLeft);
        hGoLeft.setText(new BSize(prefs.getString("GoLeft", "0.00 B")).convert());

        hUpLeft = (TextView) findViewById(R.id.hUpLeft);
        hUpLeft.setText(new BSize(prefs.getString("UpLeft", "0.00 B")).convert());

        hLogin.setTextColor(context.getResources().getColor(R.color.t411_blue));
        hLogin.setTextColor(new Ratio(this).getTitleColor());


        hSmiley = (ImageView) findViewById(R.id.homeSmiley);
        hSmiley.setImageResource(new Ratio(this).getSmiley());

        hAvatar = (ImageView) findViewById(R.id.hAvatar);
        hAvatar.setImageBitmap(new AvatarFactory().getFromPrefs(prefs));

        try {
            String classe = prefs.getString("classe", "???");
            String titre = prefs.getString("titre", "");
            String status = " (" + classe
                    + ((titre.length() > 1) ? ", " + titre : "") + ")";
            TextView wclasse = (TextView) findViewById(R.id.wClasse);
            wclasse.setText(status);
        } catch (Exception e) {
        }
    }

    public void update() {
        try {
            stopService(new Intent(MainActivity.this, t411UpdateService.class));
        } catch (Exception ex) {

        }
        startService(new Intent(MainActivity.this, t411UpdateService.class));

        updateValues();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //mDrawerLayout.openDrawer(Gravity.START);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Intent updateIntent = new Intent(MainActivity.this, t411UpdateService.class);
                startService(updateIntent);
                return true;
            case R.id.action_search:
                Intent myIntent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
