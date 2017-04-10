package fr.lepetitpingouin.android.t411;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.helper.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
    private String downloadError;

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

    public boolean download(File file) {

        this.downloadError = "";

        final int BUFFER_SIZE = 4096;

        try {
            String host = prefs.getString("custom_domain", "");
            if(host.equals("")) host = Default.IP_T411;
            URL url = new URL(this.url.replace(Default.IP_T411, host));
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestProperty("Authorization", this.auth);
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");
                String contentType = httpConn.getContentType();
                int contentLength = httpConn.getContentLength();

                if(!contentType.equals("application/x-bittorrent")) {
                    this.downloadError = this.ctx.getString(R.string.dlErrorBadContentType, contentType);
                }

                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                    fileName = disposition.substring(index + 10, disposition.length() - 1);
                    //Log.e("FileName", fileName);
                }
            }

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(file);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

        } else {
            this.downloadError = this.ctx.getString(R.string.dlErrorNoFile, "HTTP"+responseCode);
        }
        httpConn.disconnect();

        } catch(Exception e) {
            e.printStackTrace();
            this.downloadError = "Erreur interne.";
        }

        return this.downloadError.equals("");
    }

    public String load() {

        URL mUrl;
        String response = "";
        try {
            String host = prefs.getString("custom_domain", "");
            if(host.equals("")) host = Default.IP_T411;
            mUrl = new URL(this.url.replace(Default.IP_T411, host));
            if (this.proxy || this.customProxy) {
                conn = (HttpsURLConnection) mUrl.openConnection(this.proxyServer);
            } else {
                conn = (HttpsURLConnection) mUrl.openConnection();
            }
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", this.auth);

            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(StringUtil.join(this.bodyParts, "&"));
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            response = sb.toString();

            NotificationManager mNotificationManager = (NotificationManager) this.ctx.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(12345);

            JSONObject o = new JSONObject();
            try {
                o = new JSONObject(response);
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

                this.errorCode = "000";
                this.errorMessage = ctx.getResources().getString(R.string.apiError_std);

                o.put("code", this.errorCode);
                o.put("error", this.errorMessage);

                response = o.toString();
            }

        } catch (Exception e) {
            this.errorMessage = "Serveur API injoignable, reessayez ultérieurement.";
            response = "";
            e.printStackTrace();
        }


        return response;
    }

    public JSONObject loadObject() {
        try {
            String ret = this.load();
            if(ret.isEmpty() || ret.equals("ok")) {
                this.errorMessage = "Serveur API injoignable, reessayez ultérieurement.";
                ret = "{'error': '"+this.errorMessage+"'}";
            }

            return new JSONObject(ret);
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
