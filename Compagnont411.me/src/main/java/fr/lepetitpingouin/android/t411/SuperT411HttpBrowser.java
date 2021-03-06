package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SuperT411HttpBrowser {

    private boolean customProxy;
    private static CookieStore cookieStore;

    private SharedPreferences prefs;
    private static DefaultHttpClient httpclient;

    private String encoding = "utf-8";

    private int retry = 0;

    private String username;
    private String password;
    private String url;
    private String cookies;
    private String errorMessage = "";
    private String fadeMessage = "";

    private List<NameValuePair> data = new ArrayList<NameValuePair>(9);

    private Context ctx;

    private String qpA;
    private String qpT;
    private String qpQ;

    private Boolean proxy = false;

    private Boolean skipLogin = false;
    private Boolean forcedLogin = false;

    private HttpHost httpproxy;
    private byte[] byteResponse;

    public SuperT411HttpBrowser(Context context) {
        ctx = context;
        this.retry = 0;

        this.cookieStore = new BasicCookieStore();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.proxy = prefs.getBoolean("usePaidProxy", false);
        this.customProxy = prefs.getBoolean("userProxy", false);
        this.cookies = prefs.getString("cookies", "");

        /* Si on a un cookie enregistré, on passe la connexion */
        if(!this.cookies.equals("")) {
            this.skipLogin();
        }

        if (this.proxy) {
            this.httpproxy = new HttpHost(Private.URL_PROXY, 411);
            new T411Logger(this.ctx).writeLine("Utilisation du proxy dédié");
        } else if (this.customProxy) {
            this.httpproxy = new HttpHost(prefs.getString("customProxy", ""), 411);

            final String pLogin = prefs.getString("proxy_login", "");
            final String pPassword = prefs.getString("proxy_pasword", "");

            new T411Logger(this.ctx).writeLine("Utilisation du proxy : " + prefs.getString("customProxy", "non spécifié"));

            if (!pLogin.equals("")) {

                new T411Logger(this.ctx).writeLine("Le proxy nécessite une authentification");

                Authenticator authenticator = new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication(pLogin, pPassword.toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
            }
        }

        doHackTrustedCerts();
    }

    private void doHackTrustedCerts() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return hostname.contains(".");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public SuperT411HttpBrowser skipLogin() {
        this.skipLogin = true;
        new T411Logger(this.ctx).writeLine("Cette connexion ne nécessite pas de se connecter au profil t411");
        return this;
    }

    public SuperT411HttpBrowser forceLogin() {
        this.forcedLogin = true;
        new T411Logger(this.ctx).writeLine("Login forcé");
        return this;
    }

    public boolean download(File file) {
        this.executeInAsyncTask();
        try {
            FileOutputStream writer = new FileOutputStream(file);

            writer.write(this.byteResponse);
            writer.flush();

            writer.close();
            return true;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public SuperT411HttpBrowser connect(String mUrl) {
        this.url = mUrl.replace(Default.API_T411, prefs.getString("custom_domain", Default.IP_T411));
        if (prefs.getBoolean("useHTTPS", false) ) {
            new T411Logger(this.ctx).writeLine("Connexion HTTPS activée");
            this.url = this.url.replace("http://", "https://");
        }
        new T411Logger(this.ctx).writeLine("Connexion à l'adresse " + this.url);
        return this;
    }

    private boolean resolveCaptcha(String token, String captcha) {

        String[] elements = captcha.split("\\s");
        String operator = elements[1];
        int result = 0;

        if(operator.equals("+")) {
            result = Integer.valueOf(elements[0]) + Integer.valueOf(elements[2]);
        }
        if(operator.equals("-")) {
            result = Integer.valueOf(elements[0]) - Integer.valueOf(elements[2]);
        }

        this.qpA = String.valueOf(result);
        this.qpQ = captcha;
        this.qpT = token;

        new T411Logger(this.ctx).writeLine("Résolution du captcha : " + captcha + " " + result);
        return result != 0;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getFadeMessage() {
        return this.fadeMessage;
    }

    public SuperT411HttpBrowser login(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public SuperT411HttpBrowser setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String execute() {

        String value = "";
        try {
            value = new LoginTask(this.username, this.password).execute(this.url).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

    public SuperT411HttpBrowser addData(String key, String value) {
        new T411Logger(this.ctx).writeLine("Ajout de données: {"+key+": '"+value+"'}");
        this.data.add(new BasicNameValuePair(key, value));
        return this;
    }

    private void initHttpClient() {
        httpclient = new DefaultHttpClient();

        if (this.proxy || this.customProxy) {
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, this.httpproxy);
            if (this.customProxy && !this.prefs.getString("proxy_login", "").equals("")) {
                new T411Logger(this.ctx).writeLine("Utilisation du proxy avec le login " + this.prefs.getString("proxy_login", ""));
                httpclient.getCredentialsProvider().setCredentials(
                        new AuthScope(this.prefs.getString("customProxy", ""), 411),
                        new UsernamePasswordCredentials(
                                this.prefs.getString("proxy_login", ""), this.prefs.getString("proxy_pasword", "")));
            }
        }
        //HttpClientParams.setRedirecting(httpclient.getParams(), true);
        //HttpClientParams.setRedirecting(httpclient.getParams(), false);
        httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, Default.USER_AGENT);
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                CookiePolicy.BROWSER_COMPATIBILITY);

        httpclient.setCookieStore(cookieStore);

    }

    public String executeInAsyncTask() {

        doHackTrustedCerts();

        initHttpClient();

        String loginUrl = Default.URL_LOGIN;
        String referer = Default.URL_LOGIN;
        String origin = Default.IP_T411;

        if (prefs.getBoolean("useHTTPS", false) ) {
            loginUrl = loginUrl.replace("http://", "https://");
            referer = referer.replace("http://", "https://");
            origin = origin.replace("http://", "https://");
        }

        HttpPost httppost = new HttpPost(loginUrl);

        httppost.addHeader("Host", "www."+Default.IP_T411);
        httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse response;
        String responseString = null;

        List<NameValuePair> logindata = new ArrayList<>(9);

        logindata.add(new BasicNameValuePair("login", this.username));
        logindata.add(new BasicNameValuePair("password", this.password));
        if(qpT != null && qpQ != null && qpA != null) {
            logindata.add(new BasicNameValuePair("captchaToken", qpT));
            logindata.add(new BasicNameValuePair("captchaQuery", qpQ));
            logindata.add(new BasicNameValuePair("captchaAnswer", qpA));
        }

        if(!this.skipLogin || this.forcedLogin)
        try {
            new T411Logger(this.ctx).writeLine("Connexion de l'utilisateur " + this.username);
            new T411Logger(this.ctx).writeLine("Initiation de la connexion vers " + Default.URL_LOGIN);

            httppost.setEntity(new UrlEncodedFormEntity(logindata));//, this.encoding));

            response = httpclient.execute(httppost);

            String tmpCookies = "";
            for(Cookie c : httpclient.getCookieStore().getCookies()) {
                tmpCookies += c.getName() + "=" + c.getValue() + ";";
            }

            if(tmpCookies.contains("auth") && !tmpCookies.contains("null")) {
                prefs.edit().putString("cookies", tmpCookies).commit();
                new T411Logger(this.ctx).writeLine("Cookies enregistrés : " + (prefs.getBoolean("appLogs_sensitive", true)?tmpCookies:"???"));
            } else {
                prefs.edit().remove("cookies").commit();
            }

            StatusLine statusLine = response.getStatusLine();
            new T411Logger(this.ctx).writeLine("La connexion a répondu : " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());

            responseString = EntityUtils.toString(response.getEntity(), this.encoding);
            initHttpClient();

            if(Jsoup.parse(responseString).select("div.fade").first() != null) {
                //Log.e("ERRORMESSAGE", Jsoup.parse(responseString).select("div.fade").first().text());
                try {
                    String conError = Jsoup.parse(responseString).select("div.fade").first().text();
                    if (!conError.equals(null) && !conError.equals("") && !conError.contains("identifié")) {
                        errorMessage = conError;

                        new T411Logger(this.ctx).writeLine("Le site a répondu : " + errorMessage, T411Logger.WARN);

                        if (retry < 3) {
                            retry++;
                            new T411Logger(this.ctx).writeLine("Essai " + retry + "/3");

                            try {
                                Element doc = Jsoup.parse(responseString).select(".loginForm").first();
                                boolean captcha = resolveCaptcha(doc.select("input[name=captchaToken]").first().val(), doc.select("input[name=captchaQuery]").first().val());

                                if( captcha ) executeInAsyncTask();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            new T411Logger(this.ctx).writeLine("Abandon après 3 essais " + Default.URL_LOGIN);
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        httppost = new HttpPost(this.url);

        new T411Logger(this.ctx).writeLine("Initiation de la connexion vers " + this.url);
        try {

            httppost.setEntity(new UrlEncodedFormEntity(this.data));

            prefs = PreferenceManager.getDefaultSharedPreferences(this.ctx);
            this.cookies = prefs.getString("cookies", "");
            /* Si on a un cookie sauvegardé, on l'utilise */
            if(this.skipLogin && !this.cookies.equals("")) {
                new T411Logger(this.ctx).writeLine("Connexion par les cookies enregistrés : " + (prefs.getBoolean("appLogs_sensitive", true)?this.cookies:"???"));
                httppost.addHeader("Cookie", this.cookies);
            }
            //Log.e("cookies", this.cookies);

            response = httpclient.execute(httppost);
            this.byteResponse = EntityUtils.toByteArray(response.getEntity());
            //responseString = EntityUtils.toString(response.getEntity(), this.encoding);
            responseString = new String(this.byteResponse, this.encoding);

            StatusLine statusLine = response.getStatusLine();
            new T411Logger(this.ctx).writeLine("La connexion a répondu : " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());

            try {
                if(Jsoup.parse(responseString).select("div.fade").first()!=null) {
                    String conError = Jsoup.parse(responseString).select("div.fade").first().text();
                    if (!conError.equals("")) {
                        fadeMessage = conError;
                        new T411Logger(this.ctx).writeLine("Le site a répondu : " + conError, T411Logger.WARN);
                    }
                }
            } catch (Exception ex) {
                new T411Logger(ctx).writeLine(ex.getMessage(), T411Logger.INFO);
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String retValue = "";

        try {
                retValue = new String(responseString.getBytes(this.encoding));
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return retValue;
    }

    class LoginTask extends AsyncTask<String, String, String> {

        String username, password, url;


        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(String... uri) {
            this.url = uri[0];
            return executeInAsyncTask();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }


}
