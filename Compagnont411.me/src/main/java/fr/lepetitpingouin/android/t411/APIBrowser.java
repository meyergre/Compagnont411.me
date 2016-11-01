package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpHost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.helper.StringUtil;

import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
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
    private Boolean proxy = false;
    private Boolean customProxy = false;
    private SharedPreferences prefs;
    private Proxy proxyServer;

    private HttpsURLConnection conn;

    public APIBrowser(Context context) {
        this.bodyParts = new ArrayList<>();
        this.auth = PreferenceManager.getDefaultSharedPreferences(context).getString("APIToken", "");

        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.proxy = prefs.getBoolean("usePaidProxy", false);
        this.customProxy = prefs.getBoolean("userProxy", false);

        if (this.proxy) {
            this.proxyServer = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Private.URL_PROXY, 411));
            new T411Logger(context).writeLine("Utilisation du proxy dédié");
        } else if (this.customProxy) {
            this.proxyServer = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(prefs.getString("customProxy", ""), 411));

            final String pLogin = prefs.getString("proxy_login", "");
            final String pPassword = prefs.getString("proxy_pasword", "");

            new T411Logger(context).writeLine("Utilisation du proxy : " + prefs.getString("customProxy", "non spécifié"));

            if (!pLogin.equals("")) {

                new T411Logger(context).writeLine("Le proxy nécessite une authentification");

                Authenticator authenticator = new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication(pLogin, pPassword.toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
            }
        }
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
            if (this.proxy || this.customProxy) {
                conn = (HttpsURLConnection) mUrl.openConnection(this.proxyServer);
            } else {
                conn = (HttpsURLConnection) mUrl.openConnection();
            }
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
