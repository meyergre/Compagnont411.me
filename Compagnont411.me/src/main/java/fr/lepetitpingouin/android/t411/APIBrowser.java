package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.helper.StringUtil;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by gregory on 08/09/2016.
 */
public class APIBrowser {

    private String url;
    private List bodyParts;
    private String auth;

    private HttpsURLConnection conn;

    public APIBrowser(Context context) {
        this.bodyParts = new ArrayList<>();
        this.auth = PreferenceManager.getDefaultSharedPreferences(context).getString("APIToken", "");
    }

    public APIBrowser addPOSTParam(String key, String value) {
        this.bodyParts.add(key + "=" + value);
        return this;
    }

    public APIBrowser connect(String url) {
        this.url = url;
        return this;
    }

    public String load() {

        URL mUrl = null;
        String response = "";
        try {
            mUrl = new URL(this.url);
            conn = (HttpsURLConnection)mUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Authorization", this.auth);

            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(StringUtil.join(this.bodyParts, "&"));
            out.close();

            Scanner inStream = new Scanner(conn.getInputStream());
            while(inStream.hasNextLine()) {
                response+=(inStream.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return response;
    }

    public JSONObject loadObject() {
        try {
            return new JSONObject(this.load());
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public JSONArray loadArray() {
        try {
            return new JSONArray(this.load());
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

}
