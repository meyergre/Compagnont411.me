package fr.lepetitpingouin.android.t411;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by gregory on 13/09/15.
 */
public class MainActivity2 extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    SharedPreferences prefs;
    SwipeRefreshLayout swrl;
    public static final String INTENT_ERROR = "fr.lepetitpingouin.android.t411.update.error";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Default.Appwidget_update);
        filter.addAction(Default.Appwidget_flag_updating);
        filter.addAction(INTENT_ERROR);
        try {
            registerReceiver(receiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final NavigationView drw = (NavigationView) findViewById(R.id.navview);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        drw.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();
                Intent i;


                switch (menuItem.getItemId()) {

                    case R.id.action_mailbox:
                        startActivity(new Intent(getApplicationContext(), messagesActivity.class));
                        break;
                    case R.id.action_future:
                        i = new Intent();
                        i.setClass(getApplicationContext(), torrentsActivity.class);
                        i.putExtra("url", Default.URL_BOOKMARKS);
                        i.putExtra("keywords", getString(R.string.bookmarks));
                        i.putExtra("showIcons", false);
                        i.putExtra("sender", "bookmarks");
                        startActivity(i);
                        break;
                    case R.id.action_calculator:
                        startActivity(new Intent(getApplicationContext(), CalculatorActivity.class));
                        break;
                    case R.id.action_friends:
                        startActivity(new Intent(getApplicationContext(), friendsActivity.class));
                        break;
                    case R.id.action_about:
                        startActivity(new Intent(getApplicationContext(), aboutActivity.class));
                        break;

                    case R.id.action_stats:
                        startActivity(new Intent(getApplicationContext(), statsActivity.class));
                        break;

                    case R.id.action_uploads:
                        i = new Intent();
                        i.setClass(getApplicationContext(), torrentsActivity.class);
                        i.putExtra("url", Default.URL_UPLOADS);
                        i.putExtra("keywords", getString(R.string.my_uploads));
                        i.putExtra("showIcons", false);
                        i.putExtra("sender", "uploads");
                        startActivity(i);
                        break;

                    case R.id.action_settings:
                        startActivity(new Intent(getApplicationContext(), UserPrefsActivity.class));
                        break;

                }
                return false;
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawview);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        swrl = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        swrl.setOnRefreshListener(this);
        swrl.setColorSchemeColors(R.color.t411_action_blue);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!prefs.getBoolean("firstLogin", false)) {
            Intent myIntent = new Intent(MainActivity2.this, FirstLoginActivity.class);
            startActivity(myIntent);
            finish();
        }

        initWidgets();


            }

            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case android.R.id.home:
                        drawerLayout.openDrawer(GravityCompat.START);
                        return true;
                }

                return super.onOptionsItemSelected(item);
            }

            public void initWidgets() {

                ((TextView)findViewById(R.id.widget_username)).setText(prefs.getString("lastUsername", "..."));
                ((TextView)findViewById(R.id.widget_username)).setTextColor(new Ratio(this).getTitleColor());

                String classe = prefs.getString("classe", "...");
                String titre = prefs.getString("titre", "");
                String status = classe + ((titre.length() > 1) ? ", " + titre : "");
                ((TextView)findViewById(R.id.widget_userclass)).setText(status);
                ((TextView)findViewById(R.id.widget_upload)).setText( "▲ " + new BSize(prefs.getString("lastUpload", "0.00 GB")).convert());
                ((TextView)findViewById(R.id.widget_download)).setText("▼ " + new BSize(prefs.getString("lastDownload", "0.00 GB")).convert());
                ((TextView)findViewById(R.id.widget_ratio)).setText("R " + prefs.getString("lastRatio", " "));
                ((ImageView)findViewById(R.id.widget_avatar)).setImageBitmap(new AvatarFactory().getFromPrefs(prefs));


                ((TextView)findViewById(R.id.widget_news1title)).setText(prefs.getString("title1", ""));
                ((TextView)findViewById(R.id.widget_news2title)).setText(prefs.getString("title2", ""));
                ((TextView)findViewById(R.id.widget_news3title)).setText(prefs.getString("title3", ""));

                String lastUpdate = getResources().getString(R.string.lastUpdate) + prefs.getString("lastDate", "");
                if(!lastUpdate.equals(""))
                    ((TextView)findViewById(R.id.tvUpdateTime)).setText(lastUpdate);

                findViewById(R.id.widget_news).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), newsActivity.class));
                    }
                });

                if(!prefs.getBoolean("showProxyAlert", true)) {
                    findViewById(R.id.proxyAlert).setVisibility(View.GONE);
                } else {
                    if(prefs.getBoolean("usePaidProxy", false)) {
                        ((ImageView)findViewById(R.id.iv_shieldBtn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_switch_on));
                        ((ImageView)findViewById(R.id.iv_shield)).setImageDrawable(getResources().getDrawable(R.drawable.img_pxybk_ok));
                        ((TextView) findViewById(R.id.tv_shield)).setText("Option anti-censure activée.");
                    }
                    else {
                        ((ImageView)findViewById(R.id.iv_shieldBtn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_switch_off));
                        ((ImageView)findViewById(R.id.iv_shield)).setImageDrawable(getResources().getDrawable(R.drawable.img_pxy));
                        ((TextView) findViewById(R.id.tv_shield)).setText("L'option de contournement de la censure est désactivée. Appuyez ici pour en savoir plus.");
                    }
                }

                findViewById(R.id.seedbox).setVisibility(prefs.getBoolean("seedbox", false) ? View.VISIBLE : View.GONE);

                ((TextView)findViewById(R.id.drw_username)).setText(prefs.getString("lastUsername", "Non connecté"));
                ((TextView)findViewById(R.id.drw_class)).setText(status);

            }

    @Override
    public void onResume() {
        initWidgets();
        super.onResume();
    }


            public void onSearch(View v) {

                Intent intent = new Intent(this, SearchActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(this, v, "searchWidget");
                if(Build.VERSION.SDK_INT >= 16)
                    startActivity(intent, options.toBundle());
                else startActivity(intent);
            }


            public void onGraph(View v) {
                Intent intent = new Intent(this, statsActivity.class);
                Pair<View, String> p1 = Pair.create((View) findViewById(R.id.statTop), "statTop");
                Pair<View, String> p2 = Pair.create((View) findViewById(R.id.statBtm), "statBtm");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1, p2);
                if(Build.VERSION.SDK_INT >= 16)
                    startActivity(intent, options.toBundle());
                else startActivity(intent);
            }

            public void onFastTorrent(View v) {
                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentsActivity.class);
                i.putExtra("url", Default.T411_TOP_100);
                i.putExtra("keywords", getString(R.string.fast_torrents));
                i.putExtra("showIcons", false);
                i.putExtra("sender", "100");
                startActivity(i);
            }


            public void onDailyTorrent(View v) {
                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentsActivity.class);
                i.putExtra("url", Default.T411_TOP_TODAY);
                i.putExtra("keywords", getString(R.string.daily_torrents));
                i.putExtra("showIcons", false);
                i.putExtra("sender", "top");
                startActivity(i);
            }


            public void onWeeklyTorrent(View v) {
                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentsActivity.class);
                i.putExtra("url", Default.T411_TOP_WEEK);
                i.putExtra("keywords", getString(R.string.weekly_torrents));
                i.putExtra("showIcons", false);
                i.putExtra("sender", "top");
                startActivity(i);
            }


            public void onMonthlyTorrent(View v) {
                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentsActivity.class);
                i.putExtra("url", Default.T411_TOP_MONTH);
                i.putExtra("keywords", getString(R.string.monthly_torrents));
                i.putExtra("showIcons", false);
                i.putExtra("sender", "top");
                startActivity(i);
            }

    public void onLogout(View v) {
        //Snackbar.make(toolbar, "Logging out... (no, kidding ;) )", Snackbar.LENGTH_LONG).show();
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

    public void onProxyClick(View v) {
        Intent intent = new Intent(this, ProxyActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, findViewById(R.id.iv_shield), "iv_shield");
        if(Build.VERSION.SDK_INT >= 16)
            startActivity(intent, options.toBundle());
        else startActivity(intent);
    }

    public void onSeedboxClick(View v) {
        Snackbar.make(toolbar, "Seedbox / Optique / Dédié déclaré(e)", Snackbar.LENGTH_LONG).show();

    }

    @Override
    public void onRefresh() {
        update();
    }

    public void update() {
        try {
            stopService(new Intent(MainActivity2.this, t411UpdateService.class));
        } catch (Exception ex) {

        }
        startService(new Intent(MainActivity2.this, t411UpdateService.class));

        initWidgets();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Default.Appwidget_flag_updating)) {
                swrl.setRefreshing(true);
            } else {
                swrl.setRefreshing(false);
                initWidgets();
            }

            if(intent.getAction().equals(INTENT_ERROR) && getIntent().hasExtra("message")) {
                Snackbar.make(toolbar, getIntent().getStringExtra("message"), Snackbar.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
