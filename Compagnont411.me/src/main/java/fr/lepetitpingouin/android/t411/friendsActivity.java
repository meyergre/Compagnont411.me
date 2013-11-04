package fr.lepetitpingouin.android.t411;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;

public class friendsActivity extends ActionBarActivity {
    static final String CONNECTURL = Default.URL_FRIENDS;
    public ProgressDialog dialog;

    SharedPreferences prefs;
    Editor edit;

    friendsFetcher mF;

    HashMap<String, String> map;

    GridView maListViewPerso;
    ArrayList<HashMap<String, String>> listItem;

    @Override
    public void onDestroy() {
        mF = null;
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendslist);

        getSupportActionBar().setIcon(R.drawable.ic_friends);
        getSupportActionBar().setTitle(getResources().getString(R.string.my_friends));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            Intent i = new Intent(Default.Appwidget_update);
            i.putExtra("LED_T411", true);
            i.putExtra("LED_Net", false);
            sendBroadcast(i);

            Toast.makeText(getApplicationContext(), getString(R.string.noConError), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            maListViewPerso = (GridView) findViewById(R.id.malistviewperso);

            listItem = new ArrayList<HashMap<String, String>>();

            update();
        }
    }

    public void update() {
        dialog = ProgressDialog.show(friendsActivity.this, this.getString(R.string.my_friends), this.getString(R.string.pleasewait), true, true);
        try {
            mF = new friendsFetcher();
            mF.execute();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mF.cancel(true);
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class friendsFetcher extends AsyncTask<Void, String[], Void> {

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

        String username = prefs.getString("login", ""), password = prefs
                .getString("password", "");

        @Override
        protected Void doInBackground(Void... arg0) {
            listItem.clear();


            Log.v("Credentials :", username + "/" + password);

            String url = CONNECTURL;
            if (prefs.getBoolean("useHTTPS", false))
                url = CONNECTURL.replace("http://", "https://");

            try {
                res = Jsoup
                        .connect(url)
                        .data("login", username, "password", password)
                        .method(Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .cookies(Jsoup
                                .connect(Default.URL_LOGIN)
                                .data("login", prefs.getString("login", ""), "password", prefs.getString("password", ""))
                                .method(Connection.Method.POST)
                                .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                                .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                                .execute().cookies())
                        .ignoreContentType(true).execute();
                doc = res.parse();

            } catch (Exception e) {
                Log.e("erreur", e.toString());
                Toast.makeText(getApplicationContext(),
                        "Erreur lors de la récupération des amis...",
                        Toast.LENGTH_SHORT).show();
            }

            try {
                int unread = 0;
                for (Element friend : doc.select(".profile .block")) {
                    map = new HashMap<String, String>();
                    map.put("username", friend.select(".avatar").first().attr("alt"));
                    map.put("status", (friend.select("h2 > div").first().attr("class").equals("online") ? String.valueOf(R.drawable.led_green) : String.valueOf(R.drawable.led_red)));
                    map.put("date", friend.select("dd").get(2).text());
                    map.put("class", friend.select("dd").get(1).text());
                    map.put("userID", friend.select(".pm").first().attr("href").substring(friend.select(".pm").first().attr("href").lastIndexOf("=") + 1));

                    map.put("up", "n/c");
                    map.put("down", "n/c");
                    map.put("ratio", "n/c");

                    map.put("smiley", String.valueOf(R.drawable.smiley_unknown));

                    try {
                        Document profile = Jsoup
                                .connect("http://www.t411.me/users/profile/?id=" + friend.select(".pm").first().attr("href").substring(friend.select(".pm").first().attr("href").lastIndexOf("=") + 1))
                                .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                                .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                                .cookies(Jsoup
                                        .connect(Default.URL_LOGIN)
                                        .data("login", prefs.getString("login", ""), "password", prefs.getString("password", ""))
                                        .method(Connection.Method.POST)
                                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
.maxBodySize(0).followRedirects(true).ignoreContentType(true)
                                        .execute().cookies())
                                .ignoreContentType(true).get();

                        map.put("up", new BSize(profile.select(".itemWrapperHalf").last().select(".block dd").get(0).text()).convert());
                        map.put("down", new BSize(profile.select(".itemWrapperHalf").last().select(".block dd").get(1).text()).convert());
                        String ratio = profile.select(".itemWrapperHalf").last().select(".block dd .alignleft").html().replaceAll("&nbsp;", "").trim();
                        map.put("ratio", String.format("%.2f", Double.valueOf(ratio)));

                        map.put("smiley", String.valueOf(new Ratio(getApplicationContext()).getSmiley(Double.valueOf(ratio))));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    listItem.add(map);
                }
                edit = prefs.edit();
                edit.putString("mails", String.valueOf(unread));
                edit.commit();

            } catch (Exception ex) {
                Log.e("Erreur test TD", ex.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {


            SimpleAdapter mSchedule = new SimpleAdapter(
                    friendsActivity.this.getBaseContext(), listItem,
                    R.layout.item_user, new String[]{"username", "status",
                    "date", "class", "up", "down", "ratio", "smiley"}, new int[]{R.id.friend_name,
                    R.id.friend_online, R.id.friend_time, R.id.friend_class, R.id.friend_up, R.id.friend_down, R.id.friend_ratio, R.id.friend_smiley});

            try {
                maListViewPerso.setAdapter(mSchedule);
            } catch (Exception ex) {
            }

            maListViewPerso.setOnItemClickListener(new OnItemClickListener() {
                @Override
                @SuppressWarnings("unchecked")
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    HashMap<String, String> map = (HashMap<String, String>) maListViewPerso.getItemAtPosition(position);

                    Intent i = new Intent();
                    i.setClass(getApplicationContext(), ComposeMessageActivity.class);
                    i.putExtra("to", map.get("username"));
                    startActivity(i);
                }
            });

            dialog.dismiss();
        }
    }
}
