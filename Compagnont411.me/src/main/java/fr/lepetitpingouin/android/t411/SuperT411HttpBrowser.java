package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    CookieStore cookieStore;

    SharedPreferences prefs;

    String encoding = "windows-1525";

    String username, password, url, errorMessage = "", fadeMessage = "";

    List<NameValuePair> data = new ArrayList<NameValuePair>(9);

    Context ctx;

    public SuperT411HttpBrowser(Context context) {
        ctx = context;
        Log.d("SuperT411HttpBrowser", "constructor");
        cookieStore = new BasicCookieStore();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SuperT411HttpBrowser connect(String mUrl) {
        Log.d("SuperT411HttpBrowser", "connect");
        this.url = mUrl;

        if (prefs.getBoolean("useHTTPS", false)) {
            this.url = this.url.replace("http://", "https://");
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getFadeMessage() {
        return this.fadeMessage;
    }

    public SuperT411HttpBrowser login(String username, String password) {
        Log.d("SuperT411HttpBrowser", "login");
        this.username = username;
        this.password = password;
        return this;
    }

    public SuperT411HttpBrowser setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String execute() {
        Log.d("SuperT411HttpBrowser", "execute");
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
        Log.d("SuperT411HttpBrowser", "loginForMessage");
        HttpContext clientcontext;
        clientcontext = new BasicHttpContext();
        clientcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(prefs.getString("User-Agent", Default.USER_AGENT));

        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);
        HttpConnectionParams.setSoTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);
        HttpClientParams.setRedirecting(httpclient.getParams(), true);

        HttpPost httppost = new HttpPost(Default.URL_LOGIN);

        HttpResponse response;
        String responseString = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
        nameValuePairs.add(new BasicNameValuePair("remember", "0"));
        nameValuePairs.add(new BasicNameValuePair("login", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));

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
        httpclient.close();
        if (responseString == null)
            responseString = "OK";
        return responseString;
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

    public String executeInAsyncTask() {
        Log.d("SuperT411HttpBrowser", "executeAsync");
        HttpContext clientcontext;
        clientcontext = new BasicHttpContext();
        clientcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        X509HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        SSLSocketFactory sslsf = SSLSocketFactory.getSocketFactory();
        sslsf.setHostnameVerifier(hostnameVerifier);

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(prefs.getString("User-Agent", Default.USER_AGENT));

        httpclient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslsf, 443));

        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);
        HttpConnectionParams.setSoTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);
        HttpClientParams.setRedirecting(httpclient.getParams(), true);


        HttpPost httppost = new HttpPost(Default.URL_LOGIN);
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse response;
        String responseString = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
        nameValuePairs.add(new BasicNameValuePair("remember", "0"));
        nameValuePairs.add(new BasicNameValuePair("login", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));

        HttpEntity e = null;

        try {
            e = new UrlEncodedFormEntity(nameValuePairs);

            httppost.setEntity(e);

            response = httpclient.execute(httppost, clientcontext);
            StatusLine statusLine = response.getStatusLine();
            Log.e("STATUS CODE", "" + statusLine.getStatusCode());
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }

            try {
                String conError = Jsoup.parse(responseString).select("div.fade").first().text();
                if (!conError.equals("") && !conError.contains("identifié")) {
                    errorMessage = conError;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            //TODO Handle problems..
            ex.printStackTrace();
        }
        //return responseString;
        //Log.e("html", responseString);

        httppost = new HttpPost(url);


        try {

            e = new UrlEncodedFormEntity(data);

            httppost.setEntity(e);

            response = httpclient.execute(httppost, clientcontext);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                //responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                responseString = EntityUtils.toString(response.getEntity(), encoding);
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }

            try {
                String conError = Jsoup.parse(responseString).select("div.fade").first().text();
                if (!conError.equals("")) {
                    fadeMessage = conError;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            //TODO Handle problems..
            ex.printStackTrace();
        }

        httpclient.close();
        String retValue = "";

        try {
            retValue = new String(responseString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        Log.e("url2", url);
        //Log.e("html2", retValue);
        return retValue;
    }


}
