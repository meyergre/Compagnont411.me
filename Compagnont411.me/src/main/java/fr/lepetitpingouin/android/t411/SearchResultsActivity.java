package fr.lepetitpingouin.android.t411;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gregory on 08/09/2016.
 */
public class SearchResultsActivity extends AppCompatActivity {

    private String searchTerms;
    private ProgressDialog dialog;
    private SharedPreferences prefs;
    private List<Torrent> torrents, torrents_filtered;
    private GridView lv;
    private Integer offset=0, limit = 25, results=0;
    public String catCode, order, type;
    private TorrentAdapter adapter;
    private Button loadMore;
    private boolean standardSearch = true;
    private Comparator<? super Torrent> comparator;
    private Dialog openDialog, catDialog;
    private List catList;
    private ListView maListViewPersoCat;

//    private String[] filters = new String[]{"Seeders ▼", "Seeders ▲", "Leechers ▼", "Leechers ▲", "Date ▼", "Date ▲", "Taille ▼", "Taille ▲"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        this.prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.searchTerms = getIntent().getStringExtra("keywords");

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(searchTerms);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.openDialog = new Dialog(this);
        this.openDialog.setContentView(R.layout.dialog_api_filters);

        this.loadMore = (Button)findViewById(R.id.btn_loadmore);

        this.catList = new ArrayList<>();

        this.catCode = getIntent().getStringExtra("cat");
        this.order = getIntent().getStringExtra("order");
        this.type = getIntent().getStringExtra("type");

        this.torrents = new ArrayList<>();
        this.torrents_filtered = new ArrayList<>();

        final AdView filtermAdView;
        final AdRequest filteradRequest;
        filtermAdView = (AdView)openDialog.findViewById(R.id.adview);
        filteradRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice(Private.REAL_DEVICE).build();

        findViewById(R.id.btn_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog.show();
                filtermAdView.loadAd(filteradRequest);
            }
        });

        lv = (GridView)findViewById(R.id.malistviewperso);
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if((i2 - 1) == absListView.getLastVisiblePosition() && results > 0) {
                    loadMore.setVisibility(absListView.getCount() == results?View.GONE:View.VISIBLE);
                } else {
                    loadMore.setVisibility(View.GONE);
                }
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                Torrent t = (Torrent)lv.getItemAtPosition(position);

                Intent i;
                if (!prefs.getBoolean("dlModeRedirect", false)) {
                    i = new Intent();
                    i.setClass(getApplicationContext(), torrentDetailsActivity.class);
                    i.putExtra("url", Default.URL_GET_PREZ + t.id);
                    i.putExtra("nom", t.name);
                    i.putExtra("ID", t.id);
                    i.putExtra("icon", Integer.valueOf(t.category));
                    //i.putExtra("DlLater", connectUrl.equals(Default.URL_BOOKMARKS));
                } else {
                    i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(Default.URL_GET_PREZ.replace("torrents/torrents/", "torrents/") + t.id));
                }
                startActivity(i);

            }
        });
        registerForContextMenu(lv);

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

        if(getIntent().getStringExtra("sender").equals("top")) {
            this.standardSearch = false;
            findViewById(R.id.view_catFilter).setVisibility(View.VISIBLE);
            new asyncApiSearch().execute(getIntent().getStringExtra("url"));
        } else {
            findViewById(R.id.view_catFilter).setVisibility(View.GONE);
            new asyncApiSearch().execute();
        }

        findViewById(R.id.dropdown_catlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                catDialog.show();
            }
        });

        this.catDialog = new Dialog(this);
        this.catDialog.setContentView(R.layout.dialog_listview);

        maListViewPersoCat = (ListView) catDialog.findViewById(R.id.dialoglistview);

        maListViewPersoCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) maListViewPersoCat.getItemAtPosition(position);
                catCode = map.get("code");
                catDialog.dismiss();
                ((ImageView)findViewById(R.id.iv_catIcon)).setImageResource(Integer.parseInt(map.get("icon")));
                ((TextView)findViewById(R.id.tv_catLabel)).setText(map.get("name"));

                torrents_filtered = new ArrayList<>();
                for(Torrent t : torrents) {
                    if(t.category.equals(catCode) || catCode.isEmpty()) {
                        torrents_filtered.add(t);
                    }
                }
                adapter = new TorrentAdapter(getApplicationContext(), torrents_filtered);
                lv.setAdapter(adapter);
            }
        });

    }

    public void onLoadMore(View v) {
        this.loadMore.setVisibility(View.GONE);
        this.offset++;
        new asyncApiSearch().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onSort(View v) {
        switch(v.getId()) {

            case R.id.order_seeders_desc :
                this.comparator = Collections.reverseOrder(Torrent.SEEDERS_COMPARATOR);
                break;
            case R.id.order_seeders_asc :
                this.comparator = Torrent.SEEDERS_COMPARATOR;
                break;

            case R.id.order_leechers_desc :
                this.comparator = Collections.reverseOrder(Torrent.LEECHERS_COMPARATOR);
                break;
            case R.id.order_leechers_asc :
                this.comparator = Torrent.LEECHERS_COMPARATOR;
                break;

            case R.id.order_date_desc :
                this.comparator = Collections.reverseOrder(Torrent.DATE_COMPARATOR);
                break;
            case R.id.order_date_asc :
                this.comparator = Torrent.DATE_COMPARATOR;
                break;

            case R.id.order_size_desc :
                this.comparator = Collections.reverseOrder(Torrent.SIZE_COMPARATOR);
                break;
            case R.id.order_size_asc :
                this.comparator = Torrent.SIZE_COMPARATOR;
                break;

            default:
                break;
        }

        Collections.sort(this.torrents_filtered, this.comparator);
        adapter.notifyDataSetChanged();
        openDialog.dismiss();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.malistviewperso) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.torrent_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Torrent torrent_item =  (Torrent)lv.getItemAtPosition(info.position);
        switch (item.getItemId()) {
            case R.id.torrent_context_menu_open:

                Intent i;
                if (!prefs.getBoolean("dlModeRedirect", false)) {
                    i = new Intent();
                    i.setClass(getApplicationContext(), torrentDetailsActivity.class);
                    i.putExtra("url", Default.URL_GET_PREZ + torrent_item.id);
                    i.putExtra("nom", torrent_item.name);
                    i.putExtra("ID", torrent_item.id);
                    i.putExtra("icon", Integer.valueOf(new CategoryIcon(torrent_item.category).getIcon()));
                    i.putExtra("DlLater", false);
                } else {
                    i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(Default.URL_GET_PREZ.replace("torrents/torrents/", "torrents/") + torrent_item.id));
                }
                startActivity(i);

                return true;
            case R.id.torrent_context_menu_download:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if(shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    }
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                else {
                    torrent_item.download();
                }
                return true;
            case R.id.torrent_context_menu_download_later:
                Toast.makeText(getApplicationContext(), "Envoi en cours...", Toast.LENGTH_LONG).show();
                torrent_item.bookmark();
                return true;
            case R.id.torrent_context_menu_download_later_not:
                Toast.makeText(getApplicationContext(), "Demande en cours...", Toast.LENGTH_LONG).show();
                torrent_item.unbookmark();
                return true;
            case R.id.torrent_context_menu_share:
                torrent_item.share();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void JsonToTorrents(JSONArray json_torrents) {
        try {
            for (int i = 0; i < json_torrents.length(); i++) {
                JSONObject j = json_torrents.getJSONObject(i);
                Torrent t = new Torrent(getApplicationContext(),"", "");
                t.name = j.getString("name");
                t.id = j.getString("id");
                t.category= j.getString("category");
                t.seeders = j.getString("seeders");
                t.leechers = j.getString("leechers");
                t.avis = j.getString("comments");
                t.size = j.getString("size");
                t.complets= j.getString("times_completed");
                t.uploader = j.getString("username");

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
                try {
                    Date date = format.parse(j.getString("added"));
                    t.age= DateFormat.format(getResources().getString(R.string.dateFormat), date).toString();
                    t.date = date.getTime();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                this.torrents.add(t);
                this.torrents_filtered.add(t);
            }

            try {
                if(prefs.getBoolean("sortByDate", false)) {
                    this.comparator = Collections.reverseOrder(Torrent.DATE_COMPARATOR);
                }
                Collections.sort(this.torrents_filtered, this.comparator);
            } catch(Exception e) {
                e.printStackTrace();
            }

            if(lv.getAdapter() == null) {
                adapter = new TorrentAdapter(getApplicationContext(), this.torrents_filtered);
                lv.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

            //lv.setSelection(scrollTo - 1);

            if(standardSearch)
                getSupportActionBar().setSubtitle(this.torrents.size() + " sur " + results + " résultats");
            else
                getSupportActionBar().setSubtitle(this.torrents.size() + " résultats");


        } catch (Exception e) {
            e.printStackTrace();
        }
        new asyncApiGetCategories().execute();
    }

    private class asyncApiSearch extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage(getString(R.string.pleasewait));
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            publishProgress("Connexion API...");

            String apiUrl = "";

            if(standardSearch) {
                apiUrl = Default.API_T411 + "/torrents/search/" + searchTerms;
                if (!catCode.isEmpty()) apiUrl += "&cid=" + catCode;
                if (!order.isEmpty()) apiUrl += "&order=" + order;
                if (!type.isEmpty()) apiUrl += "&type=" + type.toLowerCase();
                if (limit > 0) apiUrl += "&limit=" + limit;
                if (offset > 0) apiUrl += "&offset=" + offset * limit;

            } else {
                apiUrl = strings[0];
            }


            APIBrowser api_browser = new APIBrowser(getApplicationContext());
            String o = api_browser.connect(apiUrl).load();
            return o;
        }

        @Override
        public void onPostExecute(String o) {
            JSONArray ret = new JSONArray();
            if(standardSearch) {
                try {
                    JSONObject value = new JSONObject(o);
                    results = Integer.parseInt(value.getString("total"));
                    ret = new JSONArray(value.getString("torrents"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    ret = new JSONArray(o);
                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
            JsonToTorrents(ret);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.cancel();
                }
            }, 500);
        }

        @Override
        protected void onProgressUpdate(String... value) {
            try {
                dialog.setMessage(value[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            super.onProgressUpdate();
        }
    }

    private class asyncApiGetCategories extends AsyncTask<Void, String, JSONObject> {

        private HashMap<String, String> catMap;

        public asyncApiGetCategories() {
            catList = new ArrayList<>();
            catMap = new HashMap<>();
            catMap.put("icon", String.valueOf(new CategoryIcon("").getIcon()));
            catMap.put("name", "Tout");
            catMap.put("code", "");
            catList.add(catMap);
        }

        private void extractFromJson(JSONObject json) {
            try {

                Iterator<String> iter = json.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        JSONObject value = new JSONObject(json.getString(key));
                        catMap = new HashMap<>();
                        catMap.put("icon", String.valueOf(new CategoryIcon(value.getString("id")).getIcon()));
                        catMap.put("name", value.getString("name"));
                        catMap.put("code", value.getString("id"));

                        Boolean hasTorrent = false;
                        for(Torrent t : torrents) {
                            if(t.category.equals(value.getString("id"))) {
                                hasTorrent = true;
                                break;
                            }
                        }

                        if(hasTorrent) catList.add(catMap);

                        if(value.has("cats")) {
                            extractFromJson(value.getJSONObject("cats"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                SimpleAdapter adapter = new SimpleAdapter(
                        getApplicationContext(), catList,
                        R.layout.item_searchoptions,
                        new String[]{"icon", "name", "code"},
                        new int[]{R.id.lso_icon, R.id.lso_title, R.id.lso_code});
                maListViewPersoCat.setAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {

            String apiUrl = Default.API_T411 + "/categories/tree";
            APIBrowser api_browser = new APIBrowser(getApplicationContext());
            JSONObject o = api_browser.connect(apiUrl).loadObject();
            return o;
        }

        @Override
        public void onPostExecute(JSONObject json) {
            extractFromJson(json);
        }

    }


}
