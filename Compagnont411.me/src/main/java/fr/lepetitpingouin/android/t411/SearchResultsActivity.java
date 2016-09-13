package fr.lepetitpingouin.android.t411;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gregory on 08/09/2016.
 */
public class SearchResultsActivity extends AppCompatActivity {

    private String searchTerms;
    private ProgressDialog dialog;
    private SharedPreferences prefs;
    private List<Torrent> torrents;
    private GridView lv;
    private Integer offset=0, limit = 25, results=0;
    public String catCode, order, type;
    private TorrentAdapter adapter;
    private Button loadMore;
    private boolean standardSearch = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        this.prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.searchTerms = getIntent().getStringExtra("keywords");

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(searchTerms);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.loadMore = (Button)findViewById(R.id.btn_loadmore);

        this.catCode = getIntent().getStringExtra("cat");
        this.order = getIntent().getStringExtra("order");
        this.type = getIntent().getStringExtra("type");

        this.torrents = new ArrayList<>();

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
            new asyncApiSearch().execute(getIntent().getStringExtra("url"));
        } else {
            new asyncApiSearch().execute();
        }

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
                    System.out.println(date);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                this.torrents.add(t);
            }

            if(lv.getAdapter() == null) {
                adapter = new TorrentAdapter(getApplicationContext(), this.torrents);
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
                apiUrl = Default.API_T411 + "/torrents/search/" + URLEncoder.encode(searchTerms) + "&cid=" + catCode;
                if (!order.isEmpty()) apiUrl += "&order=" + order;
                if (!type.isEmpty()) apiUrl += "&type=" + type;
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
}
