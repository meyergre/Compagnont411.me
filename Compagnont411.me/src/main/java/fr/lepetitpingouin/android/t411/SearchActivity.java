package fr.lepetitpingouin.android.t411;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {
    private CheckBox sortMode;
    private SharedPreferences prefs;
    private Toolbar toolbar;

    private LinearLayout dropdown_category;
    private LinearLayout dropdown_sort;
    private LinearLayout dropdown_subcat;

    private subCatFetcher scF;
    private favoritesFetcher fF;

    private ImageView ivSort;
    private ImageView ivCat;
    private EditText tx_description;
    private EditText tx_uploader;
    private EditText tx_fichier;
    private AutoCompleteTextView keywords;
    private SearchHistory sh;

    private int icon_sort;
    private int icon_category = R.drawable.ic_new_t411;

    private ProgressBar loading;

    private String catCode = "";
    private String subCatCode = "";
    private String sort = "";

    private HashMap<String, String> mapCat;
    private HashMap<String, String> mapSort;
    private HashMap<String, String> mapSubcat;
    private HashMap<String, String> mapFav;
    private SimpleAdapter mScheduleCat;
    private SimpleAdapter mScheduleSort;
    private SimpleAdapter mScheduleSubcat;
    private SimpleAdapter mScheduleFav;

    private ListView maListViewPersoCat;
    private ListView maListViewPersoSort;
    private ListView maListViewPersoSubcat;
    private ListView getMaListViewPersoFav;
    private ArrayList<HashMap<String, String>> listItemCat;
    private ArrayList<HashMap<String, String>> listItemSort;
    private ArrayList<HashMap<String, String>> listItemSubcat;
    private ArrayList<HashMap<String, String>> listItemFav;
    private Dialog cat_dialog;
    private Dialog sort_dialog;
    private Dialog subcat_dialog;
    private Dialog favorites_dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        toolbar = (Toolbar)findViewById(R.id.searchtoolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        sh = new SearchHistory(getApplicationContext());

        keywords = (AutoCompleteTextView) findViewById(R.id.action_search_keywords);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sh.getValues());
        keywords.setAdapter(adapter);
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


        final LinearLayout advanced = (LinearLayout)findViewById(R.id.include_advanced);

        final FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_search);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                        if(advanced.getHeight() < 100) {
                            int targetSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics()));
                            MResizeAnimation mra = new MResizeAnimation(advanced, targetSize);
                            mra.setDuration(400);
                            advanced.startAnimation(mra);
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.ani_plus_minus));
                            ((AnimationDrawable)fab.getDrawable()).start();
                        }
                        else {
                            int targetSize = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()));
                            MResizeAnimation mra = new MResizeAnimation(advanced, targetSize);
                            mra.setDuration(400);
                            advanced.startAnimation(mra);
                            fab.setImageDrawable(getResources().getDrawable(R.drawable.ani_minus_plus));
                            ((AnimationDrawable)fab.getDrawable()).start();
                        }
                        advanced.requestLayout();

            }
        });










        loading = (ProgressBar) findViewById(R.id.subcat_progressbar);


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


        favorites_dialog = new Dialog(this, R.style.MyDialogTheme);
        favorites_dialog.setContentView(R.layout.dialog_listview);
        favorites_dialog.setTitle("Recherches personnalisées...");

        //getMaListViewPersoFav = (ListView) favorites_dialog.findViewById(R.id.dialoglistview);
        getMaListViewPersoFav = (ListView) findViewById(R.id.lv_mysearches);

        listItemFav = new ArrayList<HashMap<String, String>>();

        getMaListViewPersoFav.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) getMaListViewPersoFav
                        .getItemAtPosition(position);

                keywords.setText(map.get("codeName"));
                tx_description.setText(map.get("codeDesc"));
                tx_fichier.setText(map.get("codeFile"));
                tx_uploader.setText(map.get("codeUplo"));


                onSearch();

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

    private void onSearch() {

        sh.save(keywords.getText().toString());

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
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class favoritesFetcher extends AsyncTask<Void, String[], Void> {

        Document doc = null;

        @Override
        protected void onPreExecute() {
            listItemFav.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            try {

                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext())
                        .login(username, password)
                        .connect(Default.URL_SEARCH_GET)
                        .executeInAsyncTask());

            } catch (Exception e) {
                e.printStackTrace();
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
                        //mapFav.put("search", tds.get(5).select("a").first().attr("href").substring(tds.get(5).select("a").first().attr("href").indexOf("=") + 1));
                        String code = tds.get(3).text();
                        mapFav.put("code", code);
                        mapFav.put("codeName", code.replaceAll("^.*@name\\s([\\w\\s]*).*$", "$1").trim().replace(code, ""));
                        mapFav.put("codeDesc", code.replaceAll("^.*@description\\s([\\w\\s]*).*$", "$1").trim().replace(code, ""));
                        mapFav.put("codeFile", code.replaceAll("^.*@file\\s([\\w\\s]*).*$", "$1").trim().replace(code, ""));
                        mapFav.put("codeUplo", code.replaceAll("^.*@uploader\\s([\\w\\s]*).*$", "$1").trim().replace(code, ""));
                        listItemFav.add(mapFav);
                    }
                }

                mScheduleFav = new SimpleAdapter(
                        getBaseContext(), listItemFav,
                        R.layout.item_searchoptions, new String[]{"icon", "name",
                        "code"}, new int[]{R.id.lso_icon,
                        R.id.lso_title, R.id.lso_code});

                getMaListViewPersoFav.setAdapter(mScheduleFav);

                int oneRowInDp = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 58, getResources().getDisplayMetrics()));
                int targetSize = 0;

                targetSize = listItemFav.size()*oneRowInDp;

                ResizeAnimation ra = new ResizeAnimation(getMaListViewPersoFav, targetSize);
                ra.setDuration(400);
                ra.forceStartHeight(56);
                getMaListViewPersoFav.startAnimation(ra);

                getMaListViewPersoFav.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.pb_loadfav).setVisibility(View.GONE);
                    }
                }, 500);

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




    public class ResizeAnimation extends Animation {
        final int targetHeight;
        int startHeight;
        View view;

        public ResizeAnimation(View view, int targetHeight) {
            this.view = view;
            this.targetHeight = targetHeight;
            this.startHeight = view.getHeight();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int newHeight = (int) (startHeight + (targetHeight - startHeight) * interpolatedTime);
            view.getLayoutParams().height = newHeight;
            view.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

        public void forceStartHeight(int i) {
            this.startHeight = i;
        }
    }

    public class MResizeAnimation extends Animation {
        final int targetHeight;
        int startHeight;
        View view;

        public MResizeAnimation(View view, int targetHeight) {
            this.view = view;
            this.targetHeight = targetHeight;
            this.startHeight = view.getHeight();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int newHeight = (int) (startHeight + (targetHeight - startHeight) * interpolatedTime);
            view.getLayoutParams().height = newHeight;
            view.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

        public void forceStartHeight(int i) {
            this.startHeight = i;
        }
    }

}
