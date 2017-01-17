package fr.lepetitpingouin.android.t411;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.helper.StringUtil;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
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
    private Context ctx;

    private HttpsURLConnection conn;
    public String errorMessage, errorCode;

    public APIBrowser(Context context) {

        this.ctx = context;

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
        try {
            this.bodyParts.add(URLEncoder.encode(key, "utf-8") + "=" + URLEncoder.encode(value, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public APIBrowser connect(String url) {
        this.url = url;
        return this;
    }

    public String load() {

        URL mUrl;
        String response = "";
        try {
            mUrl = new URL(this.url.replace(Default.IP_T411, prefs.getString("custom_domain", Default.IP_T411)));
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

            NotificationManager mNotificationManager = (NotificationManager) this.ctx.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(12345);

            try {
                JSONObject o = new JSONObject(response);
                if(o.has("error")) {

                    String code = o.get("code").toString();
                    String error = o.get("error").toString();

                    this.errorCode = code;
                    this.errorMessage = error;
                    if(code.startsWith("1") || code.startsWith("2")) { // erreur token ou utilisateur
                        error += " - " + this.ctx.getResources().getString(R.string.notif_apierror_reconnect);
                    }

                    NotificationCompat.Builder n = new NotificationCompat.Builder(this.ctx.getApplicationContext());
                    n.setContentTitle(this.ctx.getResources().getString(R.string.notif_apierror) + " ["+code+"]");
                    n.setSmallIcon(R.drawable.ic_notif_torrent_failure);
                    n.setContentText(error);
                    n.setDefaults(0);
                    n.setSound(null);
                    n.setOnlyAlertOnce(true);

                    mNotificationManager.notify(12345, n.build());
                }
            } catch(Exception ex){
                ex.printStackTrace();
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

    public byte[] loadFile() {
        try {
            return this.load().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
