package fr.lepetitpingouin.android.t411;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class FirstLoginActivity extends Activity {

    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    EditText login, passwd;

    TextView infoError, txtLogin;

    LinearLayout loginBar;

    ProgressDialog dialog;

    AsyncConnector asc;

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

        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstlogin);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit = prefs.edit();

        loginBar = (LinearLayout) findViewById(R.id.loginBar);
        txtLogin = (TextView) findViewById(R.id.txtloginplease);
        login = (EditText) findViewById(R.id.loginLogin);
        passwd = (EditText) findViewById(R.id.loginPassword);

        loginBar.setVisibility(View.GONE);
        txtLogin.setVisibility(View.INVISIBLE);
        login.setVisibility(View.INVISIBLE);
        passwd.setVisibility(View.INVISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                loginBar.setVisibility(View.VISIBLE);
                txtLogin.setVisibility(View.VISIBLE);
                login.setVisibility(View.VISIBLE);
                passwd.setVisibility(View.VISIBLE);
            }
        }, 1500);

        infoError = (TextView) findViewById(R.id.txtInfoError);
        infoError.setVisibility(View.GONE);

        dialog = new ProgressDialog(this);
        dialog.setIcon(R.drawable.t411_search_icon);
        dialog.setTitle("Connexion...");
        dialog.setMessage("Veuillez patienter.");
        dialog.setCancelable(true);

        dialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                asc.cancel(true);
                asc = null;
                //finish();
            }
        });

        Button createAccount = (Button) findViewById(R.id.btnCreateAccount);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Default.URL_CREATE_ACCOUNT));
                startActivity(i);
            }
        });

        Button connect = (Button) findViewById(R.id.loginConnect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    asc = new AsyncConnector();
                    asc.execute();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Une erreur est survenue. Veuillez réessayer.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class AsyncConnector extends AsyncTask<Void, String[], Void> {

        String message;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            //Connection.Response res = null;
            Document doc;
            try {
                /*res = Jsoup
                        .connect(Default.URL_LOGIN)
                        .data("login", login.getText().toString(), "password", passwd.getText().toString())
                        .method(Connection.Method.POST)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                        .ignoreContentType(true).execute();

                doc = res.parse();*/

                String html = new SuperT411HttpBrowser(getApplicationContext())
                                .login(login.getText().toString(), passwd.getText().toString())
                                .connect(Default.URL_LOGIN)
                                .executeLoginForMessage();

                if(!html.equals("OK")) {
                    doc = Jsoup.parse(html);
                    message = doc.select("#messages > p").first().text();
                } else message = "Connexion réussie !";

            } catch (Exception e) {
                Log.e("Erreur connect :", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            try {
                if (message.contains("incorrect")) {
                    dialog.dismiss();
                    infoError.setText(message);
                    infoError.setVisibility(View.VISIBLE);
                    edit.putBoolean("firstLogin", false);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    edit.commit();
                } else {
                    infoError.setVisibility(View.GONE);
                    edit.putString("login", login.getText().toString());
                    edit.putString("password", passwd.getText().toString());
                    edit.putBoolean("firstLogin", true);
                    edit.commit();

                    try {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        startService(new Intent(getApplicationContext(), t411UpdateService.class));
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
