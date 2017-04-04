package fr.lepetitpingouin.android.t411;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class torrentDetailsActivity extends AppCompatActivity {

    private String html_filelist = "";
    private SharedPreferences prefs;
    private torrentDetailsGetter tG;
    private AsyncThx thx;
    private ProgressDialog dialog;

    private WebView details_www;

    private ImageView btnShare;
    private ImageView btnThx;
    private ImageView btnDlLater;
    private ImageView rmDlLater;
    private Button btnList;

    private String torrent_URL;
    private String torrent_NFO;
    private String torrent_ID;
    private String torrent_Name;
    private String t_taille;
    private String t_cat;

    String t_uploader;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Torrent.INTENT_UPDATE_STATUS)) {
                Snackbar sb = Snackbar.make(details_www, intent.getStringExtra("message"), 3000);
                sb.setActionTextColor(getResources().getColor(android.R.color.white));
                if(intent.getBooleanExtra("success",false)) {
                    sb.getView().setBackgroundColor(getResources().getColor(R.color.t411_green));
                } else {
                    sb.getView().setBackgroundColor(getResources().getColor(R.color.t411_red));
                }
                if(intent.getBooleanExtra("downloads",false)) {
                    sb.setAction(getResources().getString(R.string.list), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getApplicationContext(), TorrentsListActivity.class));
                        }
                    });
                }
                sb.show();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
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

            Toast.makeText(getApplicationContext(), getString(R.string.noConError), Toast.LENGTH_SHORT).show();
            finish();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Torrent.INTENT_UPDATE_STATUS);
        registerReceiver(receiver, filter);
        super.onResume();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrentdetails);

        prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        details_www = (WebView) findViewById(R.id.prez);
        details_www.getSettings().setUseWideViewPort(true);
        details_www.getSettings().setLoadWithOverviewMode(true);

        details_www.getSettings().setJavaScriptEnabled(true);

        //dialog = ProgressDialog.show(this, "t411.ch", this.getString(R.string.pleasewait), true, true);
        dialog = new ProgressDialog(this, R.style.AdTitleDialog);
        dialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialog.setMessage(this.getString(R.string.pleasewait));
        AdView mAdView;
        AdRequest adRequest;
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.adtitlebar, null);
        mAdView = (AdView) view.findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice(Private.REAL_DEVICE).build();
        dialog.setCustomTitle(view);
        dialog.show();


        mAdView.loadAd(adRequest);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(false);


        torrent_URL = getIntent().getStringExtra("url");
        torrent_Name = getIntent().getStringExtra("nom");
        torrent_ID = getIntent().getStringExtra("ID");

        new T411Logger(getApplicationContext()).writeLine("Ouverture de la fiche torrent #"+torrent_ID);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            torrent_URL = getIntent().getData().toString();

            //test

            String htmlpage = new SuperT411HttpBrowser(getApplicationContext()).connect(torrent_URL).execute();
            String href = Jsoup.parse(htmlpage).select(".shortlink").attr("href");
            String _id = href.substring(href.lastIndexOf("/") + 1);
            String _title = Jsoup.parse(htmlpage).select("span:has(a.shortlink)").html().replaceAll("<a\\b[^>]+>([^<]*(?:(?!</a)<[^<]*)*)</a>", "");

            //test

            //torrent_ID = torrent_URL.split("=")[1];
            torrent_ID = _id;
            //torrent_Name = torrent_ID;
            torrent_Name = _title;
            //Toast.makeText(getApplicationContext(),torrent_URL, Toast.LENGTH_SHORT).show();
        }

        //getSupportActionBar().setTitle(torrent_Name);

        btnShare = (ImageView) findViewById(R.id.btnTorrentShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Torrent torrent = new Torrent(getApplicationContext(), torrent_Name, torrent_ID);
                torrent.share();
            }
        });

        btnDlLater = (ImageView) findViewById(R.id.btnDlLater);
        btnDlLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Demande en cours...", Toast.LENGTH_SHORT).show();
                Torrent torrent = new Torrent(getApplicationContext(), torrent_Name, torrent_ID);
                torrent.bookmark();
            }
        });

        rmDlLater = (ImageView) findViewById(R.id.btnDlLaterNot);
        rmDlLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Demande en cours...", Toast.LENGTH_SHORT).show();
                Torrent torrent = new Torrent(getApplicationContext(), torrent_Name, torrent_ID);
                torrent.unbookmark();
            }
        });

        if (getIntent().getBooleanExtra("DlLater", false)) {
            rmDlLater.setVisibility(View.VISIBLE);
            btnDlLater.setVisibility(View.GONE);
        } else {
            rmDlLater.setVisibility(View.GONE);
            btnDlLater.setVisibility(View.VISIBLE);
        }


        btnThx = (ImageView) findViewById(R.id.btnThx);
        btnThx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thx = new AsyncThx();
                Toast.makeText(getApplicationContext(), "Envoi en cours...", Toast.LENGTH_SHORT).show();
                try {
                    thx.execute();
                } catch (Exception e) {
                }
            }
        });

        btnList = (Button) findViewById(R.id.btn_listfiles);
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), FilesListActivity.class).putExtra("listHtml", html_filelist));
            }
        });

        tG = new torrentDetailsGetter();
        try {
            tG.execute();
        } catch (Exception e) {
        }

        Button btn_NFO = (Button) findViewById(R.id.btn_nfo);
        btn_NFO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(getApplicationContext(), NfoActivity.class).putExtra("nfo", torrent_NFO));

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onDownloadClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

            }
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        else {
            Torrent torrent = new Torrent(getApplicationContext(), torrent_Name, torrent_ID, t_taille, t_uploader, t_cat);
            torrent.download();
        }
    }

    public void onFakemenuClick(View v) {
        final LinearLayout fakemenu = (LinearLayout) findViewById(R.id.fakemenu);


            if (fakemenu.getVisibility()==View.GONE) {
                ((FloatingActionButton)v).setImageDrawable(getResources().getDrawable(R.drawable.ani_plus_minus));
                ((AnimationDrawable)((FloatingActionButton)v).getDrawable()).start();
                fakemenu.setVisibility(View.VISIBLE);
            } else {
                ((FloatingActionButton)v).setImageDrawable(getResources().getDrawable(R.drawable.ani_minus_plus));
                ((AnimationDrawable)((FloatingActionButton)v).getDrawable()).start();
                fakemenu.setVisibility(View.GONE);
            }

    }

    private class AsyncThx extends AsyncTask<Void, String[], Void> {

        String msg;

        @Override
        protected Void doInBackground(Void... arg0) {
            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            Connection.Response res = null;
            Document doc = null;

            try {
                /*res = Jsoup
                        .connect(Default.URL_SAY_THANKS + torrent_ID)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();*/
                doc = Jsoup.parse(new SuperT411HttpBrowser(getApplicationContext())
                        .login(username, password)
                        .connect(Default.URL_SAY_THANKS + torrent_ID)
                        .executeInAsyncTask());

                msg = doc.select(".content ").first().text();

            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
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

    private class torrentDetailsGetter extends AsyncTask<Void, String[], Void> {

        final String mimeType = "text/html";
        final String encoding = "utf-8";
        String tdt_seeders, tdt_leechers, tdt_note, tdt_votes, tdt_complets, tdt_taille;
        double note = 0;
        String prez = "<meta name=\"viewport\" content=\"width=400; user-scalable=no\" />Erreur lors de la récupération des données...";
        String tduploader = "";
        Connection.Response res = null;
        Document doc = null;
        int nbHadopi = 0;

        @Override
        protected Void doInBackground(Void... arg0) {

            String username = prefs.getString("login", ""), password = prefs.getString("password", "");

            try {

                /*res = Jsoup
                        .connect(Default.URL_LOGIN)
                        .data("login", prefs.getString("login", ""), "password", prefs.getString("password", ""))
                        .method(Connection.Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .execute();

                Map<String, String> Cookies = res.cookies();

                res = Jsoup
                        .connect(torrent_URL)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .cookies(Cookies)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();*/

                SuperT411HttpBrowser browser = new SuperT411HttpBrowser(getApplicationContext())
                        .login(username, password)
                        .connect(torrent_URL);

                if (!CategoryIcon.isPrOn(getIntent().getIntExtra("icon", R.drawable.ic_new_t411))) {
                    //browser.skipLogin();
                } else {
                    new T411Logger(getApplicationContext()).writeLine("Torrent XXX, connexion nécessaire");
                }

                doc = Jsoup.parse(browser.executeInAsyncTask());

                Elements blocsCentre = doc.select("article > div.align-center");
                if(blocsCentre.size() > 2) {
                    Element blocCentre = blocsCentre.get(0);
                    if (blocCentre.children().size() == 2)
                        blocCentre.attr("style", "display: none;");

                    blocCentre = blocsCentre.get(blocsCentre.size() - 1);
                    if (blocCentre.outerHtml().contains("élécharg") || blocCentre.outerHtml().contains("ratuit"))
                        blocCentre.attr("style", "display: none;");
                }

                try {
                    html_filelist = doc.select(".accordion div").get(1).outerHtml();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    tduploader = doc.select(".profile").first().text();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Element comments = null;
                Element commentsA = null;
                try {
                    comments = doc.select("table.comment").last();
                    commentsA = doc.select("table.comment").last();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String qualite = "";
                try {
                    qualite = "<center><span class='qualite'>" + doc.select(".terms-type-7").first().text() + "</span></center>";
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String hadopi = "";
                if (prefs.getBoolean("hadopi", false)) {
                    try {
                        nbHadopi = commentsA.text().split("[Hh][Aa][Dd][Oo][Pp][Ii]").length - 1;
                        if (nbHadopi > 0) {
                            hadopi = "<div onclick=\"this.style.display = 'none';\" style='position: fixed; top: 0px; right: 0px; left: 0px; background: red; opacity: 0.666; color: white; padding: 10px 7px 13px 7px; border-bottom: 1px solid darkred;'><img src='file:///android_asset/picts/hadopi_red.png' style='vertical-align: bottom;' /> <b>Hadopi</b> <small style='font-size: 0.5em;'>( mentionnée " + nbHadopi + " fois dans les commentaires de cette page)</small></div>";
                        }
                    } catch (Exception e) {
                    }
                }

                String viewport = "400";
                if ((getResources().getConfiguration().screenLayout &
                        Configuration.SCREENLAYOUT_SIZE_MASK) ==
                        Configuration.SCREENLAYOUT_SIZE_XLARGE || (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
                    viewport = "720";

                }

                String customCSS = "<meta name=\"viewport\" content=\"width=" + viewport + "; user-scalable=no\" /><style>body {width: 100%; overflow: none; margin: 0px; padding: 0px;} * {font-size: 1em; text-wrap: unrestricted; word-wrap:break-word;} h1,h2,h3,h4 {font-size: 1.5em;} img, * {max-width: 360px; max-width: 100%;} .up {color: green;} .down {color: red;} .data {font-weight: normal; color: grey; font-size: 0.7em;} .qualite {background: #008A00; color: white; padding: 4px 20px 4px 20px; margin-top: 50px; font-weight: bold; border: 1px solid #007700; border-radius: 25px;} .verify{position: absolute; top: 32px; right: 6px; width:128px; height: 128px; background: url('file:///android_asset/picts/verify.png')} h2.align-center{display: none;}</style>";
                prez = customCSS + "<body>" + hadopi + "<br/>" + qualite + "<br/>" + doc.select("article").first().html() + "<br/><table width=100%>";// + comments+"</body></html>";
                prez = prez.replaceAll("<noscript>", "");
                prez = prez.replaceAll("</noscript>", "");


                // Pubs

                prez = prez.replaceAll("\" href=\"http://adprovider.adlure.net", " display: none;\" href=\"http://adprovider.adlure.net");
                prez = prez.replaceAll("href=\"http://adprovider.adlure.net", "style=\"display: none;\" href=\"http://adprovider.adlure.net");

                torrent_NFO = doc.select("pre").first().text();

                //prez += "<img src=\"file:///android_asset/picts/top.png\" onclick=\"scroll(0,0);\" style='z-index: 99999; position: fixed; top: 2px; right: 2px;' />";
                //prez += "<img src=\"file:///android_asset/picts/bottom.png\" onclick=\"scroll(0,document.body.scrollHeight);\" style='z-index: 99999; position: fixed; bottom: 2px; right: 2px;' />";

                Elements objects;
                try {
                    //commentaires
                    objects = comments.select("tr");


                    for (Element object : objects) {
                        String cusername = object.select("th").first().select("a").first().text();

                        String colorPseudo = "darkblue";
                        String arrowPict = "arrow.png";
                        String bubbleStyle = "border: 1px solid #dfdfdf; padding: 3px; border-radius: 5px; background: #f6f6f6; font-size: 0.8em";

                        if (cusername.equals(tduploader) && !cusername.equals("")) {
                            colorPseudo = "darkgrey";
                            cusername = cusername + " (uploader)";
                            arrowPict = "arrowBlack.png";
                            bubbleStyle = "border: 1px solid #000000; padding: 3px; border-radius: 5px; background: #303030; color: #EFEFEF; font-size: 0.8em";
                        }

                        String comm_username = "<b style='color: " + colorPseudo + ";'>" + cusername + "</b>";
                        String comm_avatar= "";
                        try {
                            String avatarpathtmp = object.select("img.avatar").first().attr("src");
                            comm_avatar = "<img width=50 src=\"https://"+Default.IP_T411+"/" + avatarpathtmp + "\" />";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String comm_up = "";
                        String comm_down = "";
                        String comm_ratio = "";

                        try {
                            comm_up = object.select("th").first().select("span").get(1).outerHtml();
                            comm_down = object.select("th").first().select("span").get(2).outerHtml();
                            comm_ratio = object.select("th").first().select("span").get(3).outerHtml();
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }

                        String comm_comm = "<img src=\'file:///android_asset/picts/" + arrowPict + "\' width=10 style='position: relative; left: -13px; top: 3px;' />";
                        comm_comm += object.select("td").first().select("p").first().html();

                        String comm_date = "<div style='text-align: right;'>" + object.select("td").first().select("div").first().html() + "</div>";


                        try {
                            String comment = ""
                                    + "<tr><td colspan=2><br/>" + comm_username + "</td></tr>"
                                    + "<tr valign='top' style='margin-top: -5px;'>"
                                    + "<td style='font-size: 0.5em;'>" + comm_avatar + "<br/>" + comm_up + "<br/>" + comm_down + "<br/>" + comm_ratio + "<br/></td>"
                                    + "<td><div style='" + bubbleStyle + " word-wrap:break-word; overflow-wrap: break-word; max-width: 300px; font-size: 0.8em;'>" + comm_comm + "<br/>" + comm_date + "</div></td>"
                                    + "</tr>";
                            prez += comment;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                prez += "</table></body></html>";

                prez = prez.replaceAll("_____", "");

                try {
                    //vidéos youtube
                    objects = doc.select("object");
                    for (Element object : objects) {
                        try {
                            String youtube_link = object.select("embed").first().attr("src");
                            prez = prez.replace(object.outerHtml(), "<a href=\"" + youtube_link + "\"><img src=\"file:///android_asset/picts/yt_play_vid.png\"/></a>");
                        } catch (Exception e) {
                            prez = prez.replace(object.outerHtml(), "");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //vidéos youtube 2

                objects = doc.select("iframe[src~=youtube]");
                for (Element object : objects) {
                    try {
                        String youtube_src = object.attr("src");

                        //String[] youtube_array = youtube_src.split("/");
                        //String youtube_id = youtube_array[youtube_array.length-1];
                        String youtube_id = youtube_src.substring(youtube_src.lastIndexOf("/") + 1);

                        String youtube_thumb = "http://img.youtube.com/vi/" + youtube_id + "/0.jpg";
                        String youtube_link = "http://www.youtube.com/watch?v=" + youtube_id;
                        //file:///android_asset/picts/yt_play_vid.png
                        prez = prez.replace(object.outerHtml(),
                                "<a href=\"" + youtube_link + "\" style='position: relative;'>" +
                                        "<span style='position: absolute; bottom: 20px; right: 0px; color: white; background: red; border-top-left-radius: 6px; border-bottom-left-radius: 6px;  padding: 6px 24px 6px 6px;'> ▶  Voir sur youtube</span>" +
                                        "<img src=\"" + youtube_thumb + "\"/>" +
                                        "</a>");
                    } catch (Exception e) {
                        e.printStackTrace();
                        prez = prez.replace(object.outerHtml(), "");
                    }

                }


                /*
                //liens
                objects = doc.select("a");
                for (Element object : objects) {
                    try {
                        prez = prez.replace(object.outerHtml(), object.text());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                */

                prez = prez.replaceAll("src=\"/", "src=\"https://"+Default.IP_T411+"/");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {


            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            details_www.loadDataWithBaseURL(null, "<style></style>"+prez, mimeType, encoding, "");

            try {
                getSupportActionBar().setTitle("Prez " + (tduploader.length() > 0 ? "(" + tduploader + ")" : ""));
                getSupportActionBar().setSubtitle(torrent_Name);

                t_uploader = tduploader;

                tdt_seeders = doc.select(".details table tr td.up").first().text();
                tdt_leechers = doc.select(".details table tr td.down").first().text();
                tdt_note = doc.select("div.accordion div table tr").get(8).select("td").first().text().split(" ", 2)[0];
                note = Double.valueOf(tdt_note.split("/")[0].replace(",", "."));
                tdt_votes = doc.select("div.accordion div table tr").get(8).select("td").first().text().split(" ", 2)[1];
                tdt_complets = doc.select(".details table tr td.down").first().parent().select("td").last().text();
                tdt_taille = doc.select("div.accordion table tr").get(3).select("td").first().text();

                try {
                    t_cat = doc.select(".details table tr .terms-list a").first().attr("href").split("subcat=")[1].split("&")[0];
                } catch(Exception ex) {
                    t_cat = "???";
                }

                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Palette.from(BitmapFactory.decodeResource(getResources(), new CategoryIcon(t_cat).getIcon())).generate().getVibrantColor(getResources().getColor(R.color.t411_action_blue))));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(Palette.from(BitmapFactory.decodeResource(getResources(), new CategoryIcon(t_cat).getIcon())).generate().getVibrantColor(getResources().getColor(R.color.t411_action_blue_darker)));
                    getWindow().setNavigationBarColor(Palette.from(BitmapFactory.decodeResource(getResources(), new CategoryIcon(t_cat).getIcon())).generate().getVibrantColor(getResources().getColor(R.color.t411_action_blue)));
                }

                t_taille = tdt_taille;


                TextView tdtSeeders, tdtLeechers, tdtNote, tdtVotes, tdtComplets, tdtTaille;
                ImageView star1, star2, star3, star4, star5, star;

                tdtSeeders = (TextView) findViewById(R.id.tdt_seeders);
                tdtSeeders.setText(tdt_seeders + " Seeders");

                tdtLeechers = (TextView) findViewById(R.id.tdt_leechers);
                tdtLeechers.setText(tdt_leechers + " Leechers");

                tdtNote = (TextView) findViewById(R.id.tdt_note);
                tdtNote.setText(tdt_note.split("/")[0]);

                tdtVotes = (TextView) findViewById(R.id.tdt_votes);
                tdtVotes.setText(tdt_votes);

                tdtComplets = (TextView) findViewById(R.id.tdt_complets);
                tdtComplets.setText(tdt_complets + " Complets");

                tdtTaille = (TextView) findViewById(R.id.tdt_taille);
                tdtTaille.setText(tdt_taille);


                tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_00));

                if (note > 0) {
                    tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_05));
                }
                if (note > 0.7)
                {   tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_10)); }
                if (note > 1)
                {   tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_15)); }
                if (note > 1.7)
                {   tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_20)); }
                if (note > 2)
                {   tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_25)); }
                if (note > 2.7)
                {   tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_30)); }
                if (note > 3)
                {   tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_35)); }
                if (note > 3.7)
                {   tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_40)); }
                if (note > 4)
                {tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_45)); }
                if (note > 4.7)
                {tdtNote.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_note_50)); }

            } catch (Exception e) {
                //details_www.loadDataWithBaseURL("fake://seeJavaDocForExplanation/", "<meta name=\"viewport\" content=\"width=320; user-scalable=no\" />" + doc.select(".block").first().text(), mimeType, encoding, "");
                details_www.loadDataWithBaseURL("fake://seeJavaDocForExplanation/", "<meta name=\"viewport\" content=\"width=320; user-scalable=no\" />" + e.getMessage(), mimeType, encoding, "");
            }

            dialog.dismiss();
        }
    }

}
