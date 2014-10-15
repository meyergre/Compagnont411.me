package fr.lepetitpingouin.android.t411;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends ActionBarActivity {
    CheckBox sortMode;
    SharedPreferences prefs;

    LinearLayout dropdown_category, dropdown_sort, dropdown_subcat, dropdown_favorites;

    subCatFetcher scF;
    favoritesFetcher fF;

    ImageView ivSort, ivCat;
    EditText keywords, tx_description, tx_uploader, tx_fichier;

    int icon_sort, icon_category = R.drawable.ic_new_t411;

    ProgressBar loading, loadingFav;

    String catCode = "";
    String subCatCode = "";
    String sort = "";

    HashMap<String, String> mapCat, mapSort, mapSubcat, mapFav;
    SimpleAdapter mScheduleCat, mScheduleSort, mScheduleSubcat, mScheduleFav;

    ListView maListViewPersoCat, maListViewPersoSort, maListViewPersoSubcat, getMaListViewPersoFav;
    ArrayList<HashMap<String, String>> listItemCat, listItemSort, listItemSubcat, listItemFav;
    Dialog cat_dialog, sort_dialog, subcat_dialog, favorites_dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(R.layout.action_search);
        keywords = (EditText) getSupportActionBar().getCustomView().findViewById(R.id.action_search_keywords);
        keywords.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onSearch();
                    return true;
                }
                return false;
            }
        });
        keywords.requestFocus();

        tx_fichier = (EditText) findViewById(R.id.et_fichier);
        tx_description = (EditText) findViewById(R.id.et_description);
        tx_uploader = (EditText) findViewById(R.id.et_uploader);

        loading = (ProgressBar) findViewById(R.id.subcat_progressbar);

        loadingFav = (ProgressBar) findViewById(R.id.dropdown_favorites_loading);


        sortMode = (CheckBox) findViewById(R.id.sortOrder);

        cat_dialog = new Dialog(this, R.style.MyDialogTheme);
        cat_dialog.setContentView(R.layout.dialog_listview);
        cat_dialog.setTitle("Choisir une catégorie...");

        maListViewPersoCat = (ListView) cat_dialog.findViewById(R.id.dialoglistview);
        listItemCat = new ArrayList<HashMap<String, String>>();

        mapCat = new HashMap<String, String>();
        mapCat.put("icon", String.valueOf(R.drawable.ic_new_t411));
        mapCat.put("name", getString(R.string.search_cat_all));
        mapCat.put("code", "");
        listItemCat.add(mapCat);

        mapCat = new HashMap<String, String>();
        mapCat.put("icon", String.valueOf(R.drawable.ic_new_music));
        mapCat.put("name", getString(R.string.search_cat_audio));
        mapCat.put("code", "395");
        listItemCat.add(mapCat);

        mapCat = new HashMap<String, String>();
        mapCat.put("icon", String.valueOf(R.drawable.ic_new_ebook));
        mapCat.put("name", getString(R.string.search_cat_eBooks));
        mapCat.put("code", "404");
        listItemCat.add(mapCat);

        mapCat = new HashMap<String, String>();
        mapCat.put("icon", String.valueOf(R.drawable.ic_new_emulation));
        mapCat.put("name", "Emulation");
        mapCat.put("code", "340");
        listItemCat.add(mapCat);

        mapCat = new HashMap<String, String>();
        mapCat.put("icon", String.valueOf(R.drawable.ic_new_game));
        mapCat.put("name", getString(R.string.search_cat_videogames));
        mapCat.put("code", "624");
        listItemCat.add(mapCat);

        mapCat = new HashMap<String, String>();
        mapCat.put("icon", String.valueOf(R.drawable.ic_new_gps));
        mapCat.put("name", getString(R.string.search_cat_GPS));
        mapCat.put("code", "392");
        listItemCat.add(mapCat);

        mapCat = new HashMap<String, String>();
        mapCat.put("icon", String.valueOf(R.drawable.ic_new_mobile));
        mapCat.put("name", getString(R.string.search_cat_applications));
        mapCat.put("code", "233");
        listItemCat.add(mapCat);

        mapCat = new HashMap<String, String>();
        mapCat.put("icon", String.valueOf(R.drawable.ic_new_film));
        mapCat.put("name", getString(R.string.search_cat_movies));
        mapCat.put("code", "210");
        listItemCat.add(mapCat);

        mapCat = new HashMap<String, String>();
        mapCat.put("icon", String.valueOf(R.drawable.ic_new_xxx));
        mapCat.put("name", "xXx");
        mapCat.put("code", "456");
        listItemCat.add(mapCat);

        mScheduleCat = new SimpleAdapter(
                this.getBaseContext(), listItemCat,
                R.layout.item_searchoptions, new String[]{"icon", "name",
                "code"}, new int[]{R.id.lso_icon,
                R.id.lso_title, R.id.lso_code});

        maListViewPersoCat.setAdapter(mScheduleCat);
        maListViewPersoCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) maListViewPersoCat
                        .getItemAtPosition(position);

                catCode = map.get("code");
                subCatCode = "";
                TextView tv = (TextView) findViewById(R.id.ddl_category);
                tv.setText(map.get("name"));
                ivCat = (ImageView) findViewById(R.id.ddl_icon);
                ivCat.setImageResource(Integer.valueOf(map.get("icon")));
                icon_category = Integer.valueOf(map.get("icon"));
                dropdown_subcat.setVisibility(View.VISIBLE);
                tv = (TextView) findViewById(R.id.subcat_title);
                tv.setText("-- Tous --");
                scF = new subCatFetcher();
                try {
                    scF.execute();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Erreur...", Toast.LENGTH_SHORT).show();
                }
                cat_dialog.dismiss();
            }
        });

        dropdown_category = (LinearLayout) findViewById(R.id.ll_category);
        dropdown_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cat_dialog.show();
            }
        });

        sort_dialog = new Dialog(this, R.style.MyDialogTheme);
        sort_dialog.setContentView(R.layout.dialog_listview);
        sort_dialog.setTitle("Choisir un mode de tri...");

        maListViewPersoSort = (ListView) sort_dialog.findViewById(R.id.dialoglistview);
        listItemSort = new ArrayList<HashMap<String, String>>();

        mapSort = new HashMap<String, String>();
        mapSort.put("icon", String.valueOf(R.drawable.ic_sort_default));
        mapSort.put("name", getString(R.string.sort_none));
        mapSort.put("code", "");
        listItemSort.add(mapSort);

        mapSort = new HashMap<String, String>();
        mapSort.put("icon", String.valueOf(R.drawable.ic_sort_comments));
        mapSort.put("name", getString(R.string.sort_comm));
        mapSort.put("code", "comments");
        listItemSort.add(mapSort);

        mapSort = new HashMap<String, String>();
        mapSort.put("icon", String.valueOf(R.drawable.ic_calendar));
        mapSort.put("name", getString(R.string.sort_date));
        mapSort.put("code", "added");
        listItemSort.add(mapSort);

        mapSort = new HashMap<String, String>();
        mapSort.put("icon", String.valueOf(R.drawable.ic_sort_size));
        mapSort.put("name", getString(R.string.sort_size));
        mapSort.put("code", "size");
        listItemSort.add(mapSort);

        mapSort = new HashMap<String, String>();
        mapSort.put("icon", String.valueOf(R.drawable.ic_sort_complete));
        mapSort.put("name", getString(R.string.sort_done));
        mapSort.put("code", "times_completed");
        listItemSort.add(mapSort);

        mapSort = new HashMap<String, String>();
        mapSort.put("icon", String.valueOf(R.drawable.ic_sort_seed));
        mapSort.put("name", getString(R.string.sort_seed));
        mapSort.put("code", "seeders");
        listItemSort.add(mapSort);

        mapSort = new HashMap<String, String>();
        mapSort.put("icon", String.valueOf(R.drawable.ic_sort_leech));
        mapSort.put("name", getString(R.string.sort_leech));
        mapSort.put("code", "leechers");
        listItemSort.add(mapSort);

        mScheduleSort = new SimpleAdapter(
                this.getBaseContext(), listItemSort,
                R.layout.item_searchoptions, new String[]{"icon", "name",
                "code"}, new int[]{R.id.lso_icon,
                R.id.lso_title, R.id.lso_code});

        maListViewPersoSort.setAdapter(mScheduleSort);
        maListViewPersoSort.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) maListViewPersoSort
                        .getItemAtPosition(position);

                sort = map.get("code");
                TextView tv = (TextView) findViewById(R.id.lst_sort);
                tv.setText(map.get("name"));
                ivSort = (ImageView) findViewById(R.id.lst_sortIcon);
                ivSort.setImageResource(Integer.valueOf(map.get("icon")));
                icon_sort = Integer.valueOf(map.get("icon"));
                sort_dialog.dismiss();
            }
        });

        dropdown_sort = (LinearLayout) findViewById(R.id.ll_sort);
        dropdown_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sort_dialog.show();
            }
        });

        dropdown_favorites = (LinearLayout) findViewById(R.id.dropdown_favorites);
        dropdown_favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favorites_dialog.show();
            }
        });

        favorites_dialog = new Dialog(this, R.style.MyDialogTheme);
        favorites_dialog.setContentView(R.layout.dialog_listview);
        favorites_dialog.setTitle("Recherches personnalisées...");

        getMaListViewPersoFav = (ListView) favorites_dialog.findViewById(R.id.dialoglistview);
        listItemFav = new ArrayList<HashMap<String, String>>();

        getMaListViewPersoFav.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) getMaListViewPersoFav
                        .getItemAtPosition(position);

                favorites_dialog.dismiss();

                //String url = Default.URL_SEARCH + map.get("code");

                keywords.setText(map.get("code"));
                tx_description.setText("");
                tx_fichier.setText("");
                tx_uploader.setText("");

                //fF = null;
                //scF = null;

                onSearch();

                //if (prefs.getBoolean("useHTTPS", false))
                //url = url.replace("http://", "https://");

                /*
                Intent i;
                i = new Intent();
                i.setClass(getApplicationContext(), torrentsActivity.class);
                i.putExtra("url", url);
                i.putExtra("keywords", map.get("name"));
                i.putExtra("showIcons", false);
                i.putExtra("id", map.get("id"));
                i.putExtra("order", "added");
                i.putExtra("sender", "search");
                i.putExtra("type", "DESC");
                i.putExtra("tx_order", ((TextView) findViewById(R.id.lst_sort)).getText());

                //i.putExtra("icon_category", icon_category);
                //i.putExtra("icon_sort", icon_sort);
                startActivity(i);
                */

            }
        });


        dropdown_subcat = (LinearLayout) findViewById(R.id.ll_subcat);
        dropdown_subcat.setVisibility(View.GONE);
        dropdown_subcat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subcat_dialog.show();
            }
        });

        subcat_dialog = new Dialog(this, R.style.MyDialogTheme);
        subcat_dialog.setContentView(R.layout.dialog_listview);
        subcat_dialog.setTitle("Choisir une sous-catégorie...");

        maListViewPersoSubcat = (ListView) subcat_dialog.findViewById(R.id.dialoglistview);
        listItemSubcat = new ArrayList<HashMap<String, String>>();

        mScheduleSubcat = new SimpleAdapter(
                getBaseContext(), listItemSubcat,
                R.layout.item_searchoptions, new String[]{"icon", "name",
                "code"}, new int[]{R.id.lso_icon,
                R.id.lso_title, R.id.lso_code});

        maListViewPersoSubcat.setAdapter(mScheduleSubcat);
        maListViewPersoSubcat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) maListViewPersoSubcat
                        .getItemAtPosition(position);

                subCatCode = map.get("code");
                TextView tv = (TextView) findViewById(R.id.subcat_title);
                tv.setText(map.get("name"));
                ivCat = (ImageView) findViewById(R.id.ddl_icon);
                ivCat.setImageResource(Integer.valueOf(map.get("icon")));
                subcat_dialog.dismiss();
            }
        });

        fF = new favoritesFetcher();
        try {
            fF.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        /*fF = new favoritesFetcher();
        try {
            fF.execute();
        } catch (Exception e) {
        }*/

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            Intent i = new Intent(Default.Appwidget_update);
            i.putExtra("LED_T411", true);
            i.putExtra("LED_Net", false);
            sendBroadcast(i);

            Toast.makeText(getApplicationContext(), getString(R.string.noConError), Toast.LENGTH_SHORT).show();
            finish();
        }

        super.onResume();
    }

    public void onSearch() {

        String searchTerms = (keywords.getText().toString()
                + "&file=" + tx_fichier.getText().toString()
                + "&description=" + tx_description.getText().toString()
                + "&uploader=" + tx_uploader.getText().toString()
                + "&search="
                + (!keywords.getText().toString().equals("") ? "%40name+" + keywords.getText().toString() : "")
                + (!tx_description.getText().toString().equals("") ? "+%40description+" + tx_description.getText().toString() : "")
                + (!tx_fichier.getText().toString().equals("") ? "+%40file+" + tx_fichier.getText().toString() : "")
                + (!tx_uploader.getText().toString().equals("") ? "+%40user+" + tx_uploader.getText().toString() : ""))
                .replaceAll(" ", "%20");
        searchTerms = searchTerms.replaceAll("[/\\|]", "");
        searchTerms = searchTerms.replaceAll("[éÉ]", "%E9");
        searchTerms = searchTerms.replaceAll("[èÈ]", "%E8");
        searchTerms = searchTerms.replaceAll("[êÊ]", "%EA");
        searchTerms = searchTerms.replaceAll("[ëË]", "%EB");
        searchTerms = searchTerms.replaceAll("[àÀ]", "%E0");
        searchTerms = searchTerms.replaceAll("[âÂ]", "%E2");
        searchTerms = searchTerms.replaceAll("[ùÙ]", "%F9");
        searchTerms = searchTerms.replaceAll("[ûÛ]", "%FB");
        searchTerms = searchTerms.replaceAll("[ïÏ]", "%EF");

        String url = Default.URL_SEARCH
                + searchTerms
                + "&cat=" + catCode + "&submit=Recherche&subcat=" + subCatCode;

        //if (prefs.getBoolean("useHTTPS", false))
        //url = url.replace("http://", "https://");

        Intent i;
        i = new Intent();
        i.setClass(getApplicationContext(), torrentsActivity.class);
        i.putExtra("url", url);
        i.putExtra("keywords", keywords.getText().toString());
        i.putExtra("order", sort);
        i.putExtra("sender", "search");
        i.putExtra("type", sortMode.isChecked() ? "DESC" : "ASC");
        i.putExtra("tx_order", ((TextView) findViewById(R.id.lst_sort)).getText());
        i.putExtra("icon_category", icon_category);
        i.putExtra("icon_sort", icon_sort);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                onSearch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class favoritesFetcher extends AsyncTask<Void, String[], Void> {

        Connection.Response res = null;
        Document doc = null;

        @Override
        protected void onPreExecute() {
            loadingFav.setVisibility(View.VISIBLE);
            listItemFav.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            try {
                /*res = Jsoup
                        .connect(Default.URL_SEARCH_GET)
                        .data("login", username, "password", password)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .method(Connection.Method.POST)
                        .execute();

                doc = res.parse();*/

                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext())
                        .login(username, password)
                        .connect(Default.URL_SEARCH_GET)
                        .executeInAsyncTask());

            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                for (Element table : doc.select("table.results tbody")) {
                    for (Element row : table.select("tr")) {
                        Elements tds = row.select("td");

                        mapFav = new HashMap<String, String>();
                        mapFav.put("icon", String.valueOf(R.drawable.ic_favoritesearch_new));
                        mapFav.put("name", tds.get(2).text());
                        mapFav.put("id", tds.get(0).select("input").attr("value"));
                        mapFav.put("code", tds.get(5).select("a").first().attr("href").substring(tds.get(5).select("a").first().attr("href").indexOf("=") + 1));
                        listItemFav.add(mapFav);
                    }
                }

                mScheduleFav = new SimpleAdapter(
                        getBaseContext(), listItemFav,
                        R.layout.item_searchoptions, new String[]{"icon", "name",
                        "code"}, new int[]{R.id.lso_icon,
                        R.id.lso_title, R.id.lso_code});

                getMaListViewPersoFav.setAdapter(mScheduleFav);
                loadingFav.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private class subCatFetcher extends AsyncTask<Void, String[], Void> {

        @Override
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
            listItemSubcat.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Document doc;

            try {

                /*doc = Jsoup.connect(Default.URL_GET_SUBCAT + catCode)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)

                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .cookies(Jsoup
                                .connect(Default.URL_LOGIN)
                                .data("login", prefs.getString("login", ""), "password", prefs.getString("password", ""))
                                .method(Connection.Method.POST)
                                .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                                .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                                .execute().cookies())
                        .get();*/

                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext())
                        .login(prefs.getString("login", ""), prefs.getString("password", ""))
                        .connect(Default.URL_GET_SUBCAT + catCode)
                        .executeInAsyncTask());

                Elements options = doc.select("#search-subcat > option");

                for (Element option : options) {
                    mapSubcat = new HashMap<String, String>();
                    mapSubcat.put("icon", String.valueOf(icon_category));
                    mapSubcat.put("name", option.text());
                    mapSubcat.put("code", option.attr("value"));
                    listItemSubcat.add(mapSubcat);
                }


            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mScheduleSubcat = new SimpleAdapter(
                    getBaseContext(), listItemSubcat,
                    R.layout.item_searchoptions, new String[]{"icon", "name",
                    "code"}, new int[]{R.id.lso_icon,
                    R.id.lso_title, R.id.lso_code});

            maListViewPersoSubcat.setAdapter(mScheduleSubcat);

            loading.setVisibility(View.INVISIBLE);
        }
    }

}
