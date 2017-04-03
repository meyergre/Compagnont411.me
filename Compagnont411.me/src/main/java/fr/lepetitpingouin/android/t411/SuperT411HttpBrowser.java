package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SuperT411HttpBrowser {


    private boolean customProxy;
    private CookieStore cookieStore;

    private SharedPreferences prefs;

    private String encoding = "utf-8";

    private int retry = 0;

    private String username;
    private String password;
    private String url;
    private String errorMessage = "";
    private String fadeMessage = "";

    private List<NameValuePair> data = new ArrayList<NameValuePair>(9);

    private Context ctx;

    private String qpA;
    private String qpT;
    private String qpQ;

    private Boolean proxy = false;

    private Boolean skipLogin = false, rawResult = false;

    private HttpHost httpproxy;
    //Proxy httpproxy;

    private byte[] mresponseStream;

    public SuperT411HttpBrowser(Context context) {
        ctx = context;

        this.retry = 0;

        cookieStore = new BasicCookieStore();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.proxy = prefs.getBoolean("usePaidProxy", false);
        this.customProxy = prefs.getBoolean("userProxy", false);

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
                    return hostname.contains("t411");
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

    public SuperT411HttpBrowser rawResult() {
        this.rawResult = true;
        new T411Logger(this.ctx).writeLine("Récupération du résultat brut");
        return this;
    }

    public byte[] getByteResponse() {
        return this.mresponseStream;
    }

    public SuperT411HttpBrowser connect(String mUrl) {

        this.url = mUrl.replace(Default.API_T411, prefs.getString("custom_domain", Default.IP_T411));

        if (prefs.getBoolean("useHTTPS", false) && !proxy) {
            new T411Logger(this.ctx).writeLine("Connexion HTTPS activée");
            this.url = this.url.replace("http://", "https://");
        }

        new T411Logger(this.ctx).writeLine("Connexion à l'adresse " + this.url);

        Log.e("t411Browser Connect", this.url);
        return this;
    }

    private SuperT411HttpBrowser resolveCaptcha(String token, String captcha) {

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
        return this;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getFadeMessage() {
        return this.fadeMessage;
    }

    public SuperT411HttpBrowser login(String username, String password) {
        Log.e("t411UPDATER-URL", "login");
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
            value = new LoginTask(username, password).execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return value;
    }

    public SuperT411HttpBrowser addData(String key, String value) {
        this.data.add(new BasicNameValuePair(key, value));
        return this;
    }

    public String executeInAsyncTask() {

        doHackTrustedCerts();

        HttpContext clientcontext;
        clientcontext = new BasicHttpContext();
        clientcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        //AndroidHttpClient httpclient = AndroidHttpClient.newInstance(prefs.getString("User-Agent", Default.USER_AGENT));
        DefaultHttpClient httpclient = new DefaultHttpClient();

        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        if (proxy || customProxy) {
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, this.httpproxy);
            if (customProxy && !prefs.getString("proxy_login", "").equals("")) {
                new T411Logger(this.ctx).writeLine("Utilisation du proxy avec le login " + prefs.getString("proxy_login", ""));
                httpclient.getCredentialsProvider().setCredentials(
                        new AuthScope(prefs.getString("customProxy", ""), 411),
                        new UsernamePasswordCredentials(
                                prefs.getString("proxy_login", ""), prefs.getString("proxy_pasword", "")));
            }
        }

        //HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);
        //HttpConnectionParams.setSoTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);
        HttpClientParams.setRedirecting(httpclient.getParams(), true);

        String mUrl = Default.URL_LOGIN;
        //if(proxy) mUrl = Private.URL_PROXY + mUrl;

        new T411Logger(this.ctx).writeLine("Connexion de l'utilisateur " + username);
        new T411Logger(this.ctx).writeLine("Initiation de la connexion HTTP vers " + mUrl);

        HttpPost httppost = new HttpPost(mUrl);
        httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse response;
        String responseString = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(8);
        nameValuePairs.add(new BasicNameValuePair("remember", "1"));
        nameValuePairs.add(new BasicNameValuePair("login", username));
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair("url", "/"));
        // captcha
        nameValuePairs.add(new BasicNameValuePair("captchaToken", qpT));
        nameValuePairs.add(new BasicNameValuePair("captchaQuery", qpQ));
        nameValuePairs.add(new BasicNameValuePair("captchaAnswer", qpA));

        HttpEntity e = null;

        if(!this.skipLogin)
        try {
            e = new UrlEncodedFormEntity(nameValuePairs);

            httppost.setEntity(e);

            clientcontext = new BasicHttpContext();
            clientcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            response = httpclient.execute(httppost, clientcontext);
            StatusLine statusLine = response.getStatusLine();


            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                this.mresponseStream = out.toByteArray();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                new T411Logger(this.ctx).writeLine("La connexion a répondu : " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase(), T411Logger.ERROR);
                throw new IOException(statusLine.getReasonPhrase());
            }

            if(Jsoup.parse(responseString).select("div.fade").first() != null) {
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
                                resolveCaptcha(doc.select("input[name=captchaToken]").first().val(), doc.select("input[name=captchaQuery]").first().val());

                                executeInAsyncTask();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            new T411Logger(this.ctx).writeLine("Abandon après 3 essais" + mUrl);
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //return responseString;

        httppost = new HttpPost(this.url);

        new T411Logger(this.ctx).writeLine("Initiation de la connexion HTTP vers " + this.url);

        try {

            e = new UrlEncodedFormEntity(this.data);

            httppost.setEntity(e);

            //clientcontext = new BasicHttpContext();
            //clientcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            httppost.addHeader("Cookie", "authApi=" + prefs.getString("APIKey", "") + ";uid=" + prefs.getString("uid", "") + ";");

            response = httpclient.execute(httppost, clientcontext);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                //responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                /*ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                this.mresponseStream = out.toByteArray();*/

                HttpEntity entity = response.getEntity();

                this.mresponseStream = EntityUtils.toByteArray(entity);

                //responseString = EntityUtils.toString(entity, encoding);
                if(this.rawResult) {
                    responseString = entity.getContent().toString();
                } else {
                    responseString = new String(this.mresponseStream, encoding);
                }

                new T411Logger(this.ctx).writeLine("La connexion a répondu : 200 OK ");
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                new T411Logger(this.ctx).writeLine("La connexion a répondu : " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase(), T411Logger.ERROR);
                throw new IOException(statusLine.getReasonPhrase());
            }

            try {
                if(Jsoup.parse(responseString).select("div.fade").first()!=null) {
                    String conError = Jsoup.parse(responseString).select("div.fade").first().text();
                    if (!conError.equals("")) {
                        fadeMessage = conError;
                        new T411Logger(this.ctx).writeLine("Le site a répondu : " + mUrl, T411Logger.WARN);
                    }
                }
            } catch (Exception ex) {
                new T411Logger(ctx).writeLine(ex.getMessage(), T411Logger.INFO);
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //httpclient.close();
        String retValue = "";

        try {
            if(this.rawResult)
                retValue = responseString;
            else {
                retValue = new String(responseString.getBytes("UTF-8"));
            }
        } catch (Exception e1) {
            retValue = responseString;
            e1.printStackTrace();
        }

        Log.e("COOKIES", StringUtil.join(httpclient.getCookieStore().getCookies(), ";"));

        return retValue;
    }

    class LoginTask extends AsyncTask<String, String, String> {

        String username, password, url;

        public LoginTask() {
        }

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
