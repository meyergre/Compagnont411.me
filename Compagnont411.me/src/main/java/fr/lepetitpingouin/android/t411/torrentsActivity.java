package fr.lepetitpingouin.android.t411;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class torrentsActivity extends ActionBarActivity {
    String connectUrl, searchTerms;
    String name = null;

    String id;

    AlertDialog.Builder alert;

    public ProgressDialog dialog;

    SharedPreferences prefs;
    Handler handler;

    String order, type;

    torrentFetcher mF;

    HashMap<String, String> map;

    ImageButton prev, next;
    LinearLayout dropdown_pages, navbar, dropdown_categories;

    String strNext, strPrev, paginator, pageName;

    Dialog pages_dialog, cat_dialog;

    GridView maListViewPerso;
    ListView PagesList, catList;
    ArrayList<HashMap<String, String>> listItem, pageListItem, catListItem;

    SimpleAdapter mSchedule, mTorrentsAdapter;

    @Override
    public void onDestroy() {
        mF = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            Intent i = new Intent(Default.Appwidget_update);
            i.putExtra("LED_T411", true);
            i.putExtra("LED_Net", false);
            sendBroadcast(i);

            //Toast.makeText(getApplicationContext(), getString(R.string.noConError), Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrentslist);

        handler = new Handler();

        connectUrl = getIntent().getStringExtra("url");
        searchTerms = getIntent().getStringExtra("keywords");

        order = getIntent().getStringExtra("order");
        type = getIntent().getStringExtra("type");

        getSupportActionBar().setTitle(searchTerms);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        navbar = (LinearLayout) findViewById(R.id.navbar);
        navbar.setVisibility(View.GONE);

        dropdown_categories = (LinearLayout) findViewById(R.id.category_filter);
        dropdown_categories.setVisibility(getIntent().getStringExtra("sender").contains("top") ? View.VISIBLE : View.GONE);
        dropdown_categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cat_dialog.show();
            }
        });

        cat_dialog = new Dialog(this);
        cat_dialog.setContentView(R.layout.dialog_listview);
        cat_dialog.setTitle("Filtrer...");

        catList = (ListView) cat_dialog.findViewById(R.id.dialoglistview);
        catListItem = new ArrayList<HashMap<String, String>>();

        catList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) catList
                        .getItemAtPosition(position);

                TextView tv = (TextView) findViewById(R.id.tvCatListFilter);
                tv.setText(map.get("name"));
                ImageView iv = (ImageView) findViewById(R.id.ivCatListFilter);
                iv.setImageResource(Integer.valueOf(map.get("icon")));
                cat_dialog.dismiss();
                switchList(map.get("code"));
            }
        });

        pages_dialog = new Dialog(this);
        pages_dialog.setContentView(R.layout.dialog_listview);
        pages_dialog.setTitle("Aller à...");

        PagesList = (ListView) pages_dialog.findViewById(R.id.dialoglistview);
        pageListItem = new ArrayList<HashMap<String, String>>();

        PagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) PagesList
                        .getItemAtPosition(position);

                TextView tv = (TextView) findViewById(R.id.navbar_pagesText);
                tv.setText(map.get("name"));
                paginator = map.get("code");
                pages_dialog.dismiss();
                update();
            }
        });

        prev = (ImageButton) findViewById(R.id.navbtn_prev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paginator = strPrev;
                update();
            }
        });

        next = (ImageButton) findViewById(R.id.navbtn_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paginator = strNext;
                update();
            }
        });

        dropdown_pages = (LinearLayout) findViewById(R.id.navbtn_list);
        dropdown_pages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pages_dialog.show();
            }
        });

        prev.setVisibility(View.INVISIBLE);
        next.setVisibility(View.INVISIBLE);

        maListViewPerso = (GridView) findViewById(R.id.malistviewperso);
        listItem = new ArrayList<HashMap<String, String>>();

        id = getIntent().getStringExtra("id");

        maListViewPerso.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> map = (HashMap<String, String>) maListViewPerso
                        .getItemAtPosition(i);

                if (prefs.getBoolean("lpShare", false)) {

                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, Default.URL_GET_PREZ + map.get("ID") + "\n\n-\n" + getApplicationContext().getString(R.string.shareSignature));
                    share.putExtra(Intent.EXTRA_SUBJECT, "[t411] " + map.get("nom"));

                    startActivity(Intent.createChooser(share,
                            getApplicationContext().getString(R.string.Share)));
                } else if (prefs.getBoolean("lpDownload", false)) {
                    Torrent torrent = new Torrent(getApplicationContext(), map.get("nomComplet"), map.get("ID"));
                    torrent.download();
                } else if (prefs.getBoolean("lpFuture", false)) {
                    Toast.makeText(getApplicationContext(), "Envoi en cours...", Toast.LENGTH_LONG).show();
                    Torrent torrent = new Torrent(getApplicationContext(), map.get("nomComplet"), map.get("ID"));
                    torrent.bookmark();
                }
                return true;
            }
        });

        maListViewPerso.setOnItemClickListener(new OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                HashMap<String, String> map = (HashMap<String, String>) maListViewPerso
                        .getItemAtPosition(position);

                Intent i;
                if (prefs.getBoolean("dlModeDirect", false)) {
                    i = new Intent();
                    i.setClass(getApplicationContext(), torrentDetailsActivity.class);
                    i.putExtra("url", Default.URL_GET_PREZ + map.get("ID"));
                    i.putExtra("nom", map.get("nomComplet"));
                    i.putExtra("ID", map.get("ID"));
                    i.putExtra("icon", Integer.valueOf(map.get("icon")));
                    i.putExtra("DlLater", connectUrl.equals(Default.URL_BOOKMARKS));
                } else {
                    i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(Default.URL_GET_PREZ.replace("torrents/torrents/", "torrents/") + map.get("ID")));
                }
                startActivity(i);

            }
        });
        update();
    }

    public void switchList(String catCode) {

        mSchedule = mTorrentsAdapter;
        ArrayList<HashMap<String, String>> filterListItem = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < mSchedule.getCount(); i++) {
            HashMap<String, String> nmap = (HashMap<String, String>) mSchedule.getItem(i);
            if (nmap.get("cat").contains(catCode))
                filterListItem.add(nmap);
        }

        SimpleAdapter mFilterAdapter = new SimpleAdapter(
                torrentsActivity.this.getBaseContext(), filterListItem,
                R.layout.item_torrent, new String[]{"nomComplet",
                "age", "taille", "avis", "seeders", "leechers", "uploader", "ratio", "completed", "ratioBase", "icon"}, new int[]{R.id.tNom
                , R.id.tAge, R.id.tTaille, R.id.tComments, R.id.tSeeders, R.id.tLeechers, R.id.tUploader, R.id.tRatio, R.id.tCompleted, R.id.tRatioBase, R.id.tIcon});

        maListViewPerso.setAdapter(mFilterAdapter);


        //Toast.makeText(getApplicationContext(), "Load category "+catCode+" ("+lv.getCount()+")", Toast.LENGTH_SHORT).show();

    }

    public void update() {
        dialog = ProgressDialog.show(this, "t411.me",
                this.getString(R.string.pleasewait), true, true);
        mF = new torrentFetcher();
        try {
            mF.execute();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Erreur inattendue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private class AsyncRemoveFavorite extends AsyncTask<Void, String[], Void> {

        String msg;

        Connection.Response res;
        Document doc;
        Map<String, String> Cookies;

        @Override
        protected Void doInBackground(Void... arg0) {
            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            try {
                res = Jsoup
                        .connect(Default.URL_LOGIN)
                        .data("login", username, "password", password)
                        .method(Connection.Method.POST)
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .execute();

                Cookies = res.cookies();

                doc = Jsoup
                        .connect(Default.URL_UNFAVORITE)
                        .cookies(Cookies)
                        .data("submit", "Supprimer")
                        .data("ids[]", id)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .execute().parse();

                msg = doc.select("#messages ").first().text();

            } catch (Exception e) {
                Log.e("Erreur connect :", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

    }

    private class addToFavorites extends AsyncTask<Void, String[], Void> {

        Connection.Response res = null;
        Document doc = null;
        String msg = null;
        Map<String, String> mCookies;
        String username, password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            username = prefs.getString("login", "");
            password = prefs.getString("password", "");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                res = Jsoup
                        .connect(Default.URL_LOGIN)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .execute();

                mCookies = res.cookies();

                handler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            res = Jsoup
                                    .connect(Default.URL_SEARCH_SAVE + (connectUrl.substring(connectUrl.lastIndexOf("=")) + "&order=" + order + "&type=" + type))
                                    .header("Content-Type", "application/x-www-form-urlencoded")
                                    .header("Referer", connectUrl)
                                    .header("Cookie", mCookies.toString().replace("{", "").replace("}", ""))
                                    .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                                    .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                                    .data("submit", "Valider", "name", name)
                                    .method(Method.POST)
                                    .execute();
                        } catch (Exception e) {
                        }
                    }
                }, 999);

                doc = res.parse();

                msg = doc.select(".content ").first().text();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), "COOKIE :" + mCookies.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            Log.e("Cookie", mCookies.toString());
        }
    }

    private class torrentFetcher extends AsyncTask<Void, String[], Void> {

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

            try {

                pageListItem.clear();

                String url = connectUrl;

                url += "&order=" + order + "&type=" + type + "&page=" + paginator;

                res = Jsoup.connect(url)
                        .cookies(Jsoup
                                .connect(Default.URL_LOGIN)
                                .data("login", prefs.getString("login", ""), "password", prefs.getString("password", ""))
                                .method(Connection.Method.POST)
                                .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                                .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)

                                .maxBodySize(0).followRedirects(true).ignoreContentType(true).execute().cookies())
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .execute();

                doc = res.parse();

            } catch (Exception e) {
                e.printStackTrace();
            }
            //dialog.cancel();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            try {
                try {

                    map = new HashMap<String, String>();
                    map.put("icon", String.valueOf(R.drawable.ic_new_t411));
                    map.put("name", "-- Tout --");
                    map.put("code", "");
                    catListItem.add(map);

                    for (Element hCat : doc.select("h3")) {
                        map = new HashMap<String, String>();
                        map.put("icon", String.valueOf(new CategoryIcon(hCat.nextElementSibling().select("img").first().attr("class").substring(4)).getIcon()));
                        map.put("name", hCat.text());
                        map.put("code", hCat.nextElementSibling().select("img").last().attr("class").substring(4));
                        catListItem.add(map);
                    }

                    mSchedule = new SimpleAdapter(
                            getBaseContext(), catListItem,
                            R.layout.item_searchoptions, new String[]{"icon", "name",
                            "code"}, new int[]{R.id.lso_icon,
                            R.id.lso_title, R.id.lso_code});

                    catList.setAdapter(mSchedule);

                    pageName = doc.select(".pagebar").select("span").first().text();

                    for (Element page : doc.select(".pagebar").select("a")) {
                        if (!page.hasAttr("rel")) {
                            map = new HashMap<String, String>();
                            map.put("icon", String.valueOf(R.drawable.file));
                            map.put("name", page.text());
                            map.put("code", page.attr("href").substring(page.attr("href").lastIndexOf("=") + 1));
                            pageListItem.add(map);
                        }
                    }

                    try {
                        Element elmtPrev = doc.select(".pagebar a").first();
                        strPrev = null;
                        if (elmtPrev.hasAttr("rel")) {
                            strPrev = elmtPrev.attr("href").substring(elmtPrev.attr("href").lastIndexOf("=") + 1);
                            prev = (ImageButton) findViewById(R.id.navbtn_prev);
                            prev.setVisibility(View.VISIBLE);
                        }

                        Element elmtNext = doc.select(".pagebar a").last();
                        strNext = null;
                        if (elmtNext.hasAttr("rel")) {
                            strNext = elmtNext.attr("href").substring(elmtNext.attr("href").lastIndexOf("=") + 1);
                            next = (ImageButton) findViewById(R.id.navbtn_next);
                            next.setVisibility(View.VISIBLE);
                        }

                        mSchedule = new SimpleAdapter(
                                getBaseContext(), pageListItem,
                                R.layout.item_searchoptions, new String[]{"icon", "name",
                                "code"}, new int[]{R.id.lso_icon,
                                R.id.lso_title, R.id.lso_code});

                        PagesList.setAdapter(mSchedule);
                        navbar.setVisibility(PagesList.getCount() > 0 ? View.VISIBLE : View.GONE);

                        TextView tv = (TextView) findViewById(R.id.navbar_pagesText);
                        tv.setText(pageName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                int base = connectUrl.equals(Default.URL_BOOKMARKS) ? 1 : 0;

                for (Element table : doc.select("table.results tbody")) {
                    for (Element row : table.select("tr")) {
                        Elements tds = row.select("td");

                        String catCode = tds.get(base + 0).select(".category img").first().attr("class").replace("cat-", "");

                        try {
                            map = new HashMap<String, String>();
                            map.put("nomComplet", tds.get(base + 1).select("a").first().attr("title").toString());
                            map.put("ID", tds.get(base + 2).select("a").attr("href").split("=")[1]);
                            map.put("age", tds.get(base + 4).text());
                            map.put("taille", new BSize(tds.get(base + 5).text()).convert());
                            map.put("avis", tds.get(base + 3).text());
                            map.put("seeders", tds.get(base + 7).text());
                            map.put("leechers", tds.get(base + 8).text());
                            map.put("uploader", tds.get(base + 1).select("dd > a.profile").text());
                            map.put("completed", tds.get(base + 6).text());
                            map.put("cat", catCode);

                            String tSize = tds.get(base + 5).text();
                            double estimatedDl = new BSize(prefs.getString("lastDownload", "? 0.00 GB")).getInMB() + new BSize(tSize).getInMB();
                            String estimatedRatio = String.format("%.2f", (new BSize(prefs.getString("lastUpload", "? 0.00 GB")).getInMB() / estimatedDl) - 0.01);

                            map.put("ratio", estimatedRatio);
                            map.put("icon", String.valueOf(new CategoryIcon(catCode).getIcon()));
                            map.put("ratioBase", String.format("%.2f", Float.valueOf(prefs.getString("lastRatio", ""))));
                            listItem.add(map);

                        } catch (Exception e) {
                        }
                    }
                }

                mTorrentsAdapter = new SimpleAdapter(
                        torrentsActivity.this.getBaseContext(), listItem,
                        R.layout.item_torrent, new String[]{"nomComplet",
                        "age", "taille", "avis", "seeders", "leechers", "uploader", "ratio", "completed", "ratioBase", "icon"}, new int[]{R.id.tNom
                        , R.id.tAge, R.id.tTaille, R.id.tComments, R.id.tSeeders, R.id.tLeechers, R.id.tUploader, R.id.tRatio, R.id.tCompleted, R.id.tRatioBase, R.id.tIcon});

                maListViewPerso.setAdapter(mTorrentsAdapter);

                if ((doc == null) || (res == null)) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_data_from_server), Toast.LENGTH_LONG).show();
                }

                if (maListViewPerso.getCount() < 1) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_result), Toast.LENGTH_LONG).show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 1000);
                }
                dialog.cancel();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_data_from_server), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
