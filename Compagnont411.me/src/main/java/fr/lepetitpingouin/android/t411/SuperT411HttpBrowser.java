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
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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


    boolean customProxy;
    CookieStore cookieStore;

    SharedPreferences prefs;

    String encoding = "utf-8";

    int retry = 0;

    String username, password, url, errorMessage = "", fadeMessage = "";

    List<NameValuePair> data = new ArrayList<NameValuePair>(9);

    Context ctx;

    String qpA, qpT, qpQ;

    Boolean proxy = false;

    Boolean skipLogin = false;

    HttpHost httpproxy;
    //Proxy httpproxy;

    byte[] mresponseStream;

    public SuperT411HttpBrowser(Context context) {
        ctx = context;

        this.retry = 0;

        cookieStore = new BasicCookieStore();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.proxy = prefs.getBoolean("usePaidProxy", false);
        this.customProxy = prefs.getBoolean("userProxy", false);

        if (this.proxy) {
            this.httpproxy = new HttpHost(Private.URL_PROXY, 411);
            //httpproxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Private.URL_PROXY, 411));
            new T411Logger(this.ctx).writeLine("Utilisation du proxy dédié");
        } else if (this.customProxy) {
            this.httpproxy = new HttpHost(prefs.getString("customProxy", ""), 411);
            //httpproxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(prefs.getString("customProxy", ""), 411));

            final String pLogin = prefs.getString("proxy_login", "");
            final String pPassword = prefs.getString("proxy_password", "");

            new T411Logger(this.ctx).writeLine("Utilisation du proxy : " + prefs.getString("customProxy", "non spécifié"));

            if (!pLogin.equals("")) {

                new T411Logger(this.ctx).writeLine("Le proxy nécessite une authentification");

                Authenticator authenticator = new Authenticator() {
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
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
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

    public byte[] getByteResponse() {
        return this.mresponseStream;
    }

    public SuperT411HttpBrowser connect(String mUrl) {

        this.url = mUrl;//.replace("www.t411.in", prefs.getString("SiteIP", Default.IP_T411));
        /*/TEST TRUE PROXY/*if(proxy){
            this.url = Private.URL_PROXY + this.url.replace("http://", "");
        }*/

        //this.url = this.url.replace("t411.io", "t411.in");

        if (prefs.getBoolean("useHTTPS", false) && !proxy) {
            new T411Logger(this.ctx).writeLine("Connexion HTTPS activée");
            this.url = this.url.replace("http://", "https://");
        }

        new T411Logger(this.ctx).writeLine("Connexion à l'adresse " + this.url);

        Log.e("t411Browser Connect", this.url);
        return this;
    }

    public SuperT411HttpBrowser resolveCaptcha(String token, String captcha) {

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

    public String executeLoginForMessage() {

        HttpContext clientcontext;
        clientcontext = new BasicHttpContext();
        clientcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        DefaultHttpClient httpclient = new DefaultHttpClient();


        //httpclient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslsf, 443));


        if (proxy || customProxy) {
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, this.httpproxy);
            if (customProxy && !prefs.getString("proxy_login", "").equals("")) {
                httpclient.getCredentialsProvider().setCredentials(
                        new AuthScope(prefs.getString("customProxy", ""), 411),
                        new UsernamePasswordCredentials(
                                prefs.getString("proxy_login", ""), prefs.getString("proxy_password", "")));
            }
        }
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);
        HttpConnectionParams.setSoTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);
        HttpClientParams.setRedirecting(httpclient.getParams(), true);

        String mUrl = Default.URL_LOGIN;
        //if(proxy) mUrl = Private.URL_PROXY + mUrl;
        HttpPost httppost = new HttpPost(mUrl);

        HttpResponse response;
        String responseString = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
        nameValuePairs.add(new BasicNameValuePair("remember", "0"));
        nameValuePairs.add(new BasicNameValuePair("login", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        // captcha
        nameValuePairs.add(new BasicNameValuePair("captchaToken", qpT));
        nameValuePairs.add(new BasicNameValuePair("captchaQuery", qpQ));
        nameValuePairs.add(new BasicNameValuePair("captchaAnswer", qpA));


        HttpEntity e = null;

        try {
            e = new UrlEncodedFormEntity(nameValuePairs);

            httppost.setEntity(e);

            response = httpclient.execute(httppost, clientcontext);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception ex) {
            //TODO Handle problems..
        }
        //httpclient.close();
        if (responseString == null)
            responseString = "OK";


        try {
            String conError = Jsoup.parse(responseString).select("div.fade").first().text();
            if (!conError.equals("OK") && !conError.contains("identifié")) {

                if (retry < 3) {
                    retry++;

                    try {
                        Element doc = Jsoup.parse(responseString).select(".loginForm").first();
                        resolveCaptcha(doc.select("input[name=captchaToken]").first().val(), doc.select("input[name=captchaQuery]").first().val());

                        executeLoginForMessage();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Log.e("CAPTCHA", "Abandon après 3 essais");
                    }
                }

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return responseString;
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
                                prefs.getString("proxy_login", ""), prefs.getString("proxy_password", "")));
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
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse response;
        String responseString = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(8);
        nameValuePairs.add(new BasicNameValuePair("remember", "1"));
        nameValuePairs.add(new BasicNameValuePair("login", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        // captcha
        nameValuePairs.add(new BasicNameValuePair("captchaToken", qpT));
        nameValuePairs.add(new BasicNameValuePair("captchaQuery", qpQ));
        nameValuePairs.add(new BasicNameValuePair("captchaAnswer", qpA));

        HttpEntity e = null;

        if(!this.skipLogin)
        try {
            e = new UrlEncodedFormEntity(nameValuePairs);

            httppost.setEntity(e);

            response = httpclient.execute(httppost, clientcontext);
            StatusLine statusLine = response.getStatusLine();


            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                this.mresponseStream = out.toByteArray();
                new T411Logger(this.ctx).writeLine("La connexion a répondu : " + "200 OK");
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                new T411Logger(this.ctx).writeLine("La connexion a répondu : " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase(), T411Logger.ERROR);
                throw new IOException(statusLine.getReasonPhrase());
            }

            try {
                String conError = Jsoup.parse(responseString).select("div.fade").first().text();
                if (!conError.equals("") && !conError.contains("identifié")) {
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

        } catch (Exception ex) {
            //TODO Handle problems..
            ex.printStackTrace();
        }
        //return responseString;

        httppost = new HttpPost(this.url);

        new T411Logger(this.ctx).writeLine("Initiation de la connexion HTTP vers " + mUrl);

        try {

            e = new UrlEncodedFormEntity(data);

            httppost.setEntity(e);

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
                responseString = new String(this.mresponseStream, encoding);

                new T411Logger(this.ctx).writeLine("La connexion a répondu : 200 OK ");
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                new T411Logger(this.ctx).writeLine("La connexion a répondu : " + statusLine.getStatusCode() + " " + statusLine.getReasonPhrase(), T411Logger.ERROR);
                throw new IOException(statusLine.getReasonPhrase());
            }

            try {
                String conError = Jsoup.parse(responseString).select("div.fade").first().text();
                if (!conError.equals("")) {
                    fadeMessage = conError;
                    new T411Logger(this.ctx).writeLine("Le site a répondu : " + mUrl, T411Logger.WARN);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            //TODO Handle problems..
            ex.printStackTrace();
        }

        //httpclient.close();
        String retValue = "";

        try {
            retValue = new String(responseString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

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
