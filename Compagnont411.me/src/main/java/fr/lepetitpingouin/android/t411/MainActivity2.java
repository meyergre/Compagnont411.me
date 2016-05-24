package fr.lepetitpingouin.android.t411;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Created by gregory on 13/09/15.
 */
public class MainActivity2 extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String INTENT_ERROR = "fr.lepetitpingouin.android.t411.update.error";
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    SharedPreferences prefs;
    SwipeRefreshLayout swrl;
    AdView mAdView;
    NavigationView drw;
    View navHeader;
    BillingProcessor bp;
    IntentFilter filter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Default.Appwidget_flag_updating)) {
                swrl.setRefreshing(true);
            } else {
                swrl.setRefreshing(false);
                initWidgets();
            }

            if (intent.getAction().equals(INTENT_ERROR) && getIntent().hasExtra("message")) {
                Snackbar.make(toolbar, getIntent().getStringExtra("message"), Snackbar.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getIntent().hasExtra("message"))
            Snackbar.make(toolbar, getIntent().getStringExtra("message"), Snackbar.LENGTH_SHORT).show();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        filter = new IntentFilter();
        filter.addAction(Default.Appwidget_update);
        filter.addAction(Default.Appwidget_flag_updating);
        filter.addAction(INTENT_ERROR);

        drw = (NavigationView) findViewById(R.id.navview);
        navHeader = drw.getHeaderView(0);
        //navHeader = drw.inflateHeaderView(R.layout.drawer_header);

        mAdView = (AdView)navHeader.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) // Any emulator device
                .addTestDevice(Private.REAL_DEVICE) // Real device : Oneplus One Greg
                .build();

        mAdView.loadAd(adRequest);



        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mAdView.animate()
                    .alpha(0.3f)
                    .setDuration(5000)
                    .setStartDelay(15000)
                    .start();
        }
        */

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

                    case R.id.action_torrentslist:
                        startActivity(new Intent(getApplicationContext(), TorrentsListActivity.class));
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
            Intent myIntent = new Intent(getApplicationContext(), FirstLoginActivity.class);
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

                String displayedDate = "";
                if (!prefs.getString("lastDate", "").equals("")) {
                    String lastDate = prefs.getString("lastDate", "");

                    try {
                        Long shortLastDate = new SimpleDateFormat("dd/MM/yyyy h:m:s", Locale.FRENCH).parse(lastDate).getTime();
                        lastDate = DateUtils.getRelativeDateTimeString(getApplicationContext(), shortLastDate, DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, 0).toString();

                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }

                    displayedDate = getResources().getString(R.string.lastUpdate) + lastDate;

                    ((TextView) findViewById(R.id.tvUpdateTime)).setText(displayedDate);
                }

                findViewById(R.id.widget_news).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), newsActivity.class));
                    }
                });

                if(!prefs.getBoolean("showProxyAlert", true)) {
                    findViewById(R.id.proxyAlert).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.proxyAlert).setVisibility(View.VISIBLE);
                    if (prefs.getBoolean("usePaidProxy", false) || prefs.getBoolean("userProxy", false)) {
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

                if (prefs.getBoolean("usePaidProxy", false)) mAdView.setVisibility(View.GONE);

                findViewById(R.id.seedbox).setVisibility(prefs.getBoolean("seedbox", false) ? View.VISIBLE : View.GONE);

                ((TextView)navHeader.findViewById(R.id.drw_username)).setText(prefs.getString("lastUsername", "Non connecté"));
                ((TextView)navHeader.findViewById(R.id.drw_class)).setText(status);

                findViewById(R.id.helpButtonPopup).setVisibility(prefs.getBoolean("hideHelpButton", false)?View.GONE:View.VISIBLE);

            }

    public void removeHelpButton(View v) {
        prefs.edit().putBoolean("hideHelpButton", true).apply();
        findViewById(R.id.helpButtonPopup).setVisibility(View.GONE);
    }

    @Override
    public void onResume() {

        initBP();
        initWidgets();

        registerReceiver(receiver, filter);

        View permissionStorage = (View)findViewById(R.id.storage_permission);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionStorage.setVisibility(View.VISIBLE);
            permissionStorage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            });
        }
        else {
            permissionStorage.setVisibility(View.GONE);
        }

        super.onResume();
    }

    @Override
    public void onPause() {

        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    private void initBP() {
        bp = new BillingProcessor(this, Private.API_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String s, TransactionDetails transactionDetails) {
            }

            @Override
            public void onPurchaseHistoryRestored() {
            }

            @Override
            public void onBillingError(int i, Throwable throwable) {
            }

            @Override
            public void onBillingInitialized() {
            }
        });
        try {
            bp.loadOwnedPurchasesFromGoogle();
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        new T411Logger(getApplicationContext()).writeLine("Vérification des achats in-app :", T411Logger.INFO);
        if(bp.isSubscribed(Private.PROXY_ITEM_ID)) {
            new T411Logger(getApplicationContext()).writeLine("Proxy souscrit", T411Logger.INFO);
        } else {
            new T411Logger(getApplicationContext()).writeLine("Proxy non souscrit", T411Logger.INFO);

        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            bp.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
