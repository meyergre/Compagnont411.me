package fr.lepetitpingouin.android.t411;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

public class FirstLoginActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;

    private EditText login;
    private EditText passwd;

    private ProgressDialog dialog;

    private AsyncConnector asc;

    private View fab;

    @Override
    public void onResume() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            Intent i = new Intent(Default.Appwidget_update);
            i.putExtra("LED_T411", true);
            i.putExtra("LED_Net", false);
            sendBroadcast(i);

            Snackbar.make(fab, getString(R.string.noConError), Snackbar.LENGTH_SHORT).show();
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

        login = (EditText) findViewById(R.id.login_username);
        passwd = (EditText) findViewById(R.id.login_password);


        dialog = new ProgressDialog(this, R.style.AdTitleDialog);
        dialog.setMessage("Connexion...");
        dialog.setCancelable(true);

        AdView mAdView;
        AdRequest adRequest;
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.adtitlebar, null);
        mAdView = (AdView) view.findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice(Private.REAL_DEVICE).build();
        dialog.setCustomTitle(view);
        if(!prefs.getBoolean("stop_pub", false)) mAdView.loadAd(adRequest);

        dialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                asc.cancel(true);
                asc = null;
            }
        });
        final View login = findViewById(R.id.login_inc_loginform);
        fab = findViewById(R.id.fablogin);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    asc = new AsyncConnector();
                    asc.execute();
                } catch (Exception e) {
                    Snackbar.make(fab, "Une erreur est survenue. Veuillez réessayer.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class AsyncConnector extends AsyncTask<Void, JSONObject[], JSONObject> {

        String message, mLogin, mPassword;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.mLogin = login.getText().toString();
            this.mPassword = passwd.getText().toString();
            if (mLogin.isEmpty() || mPassword.isEmpty()) this.cancel(true);
            else dialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... arg0) {

            String apiUrl = Default.API_T411 + "/auth";
            APIBrowser api_browser = new APIBrowser(getApplicationContext());
            new T411Logger(getApplicationContext()).writeLine("Connexion à l'adresse " + apiUrl);
            return api_browser.connect(apiUrl).addPOSTParam("username", this.mLogin).addPOSTParam("password", this.mPassword).loadObject();
        }

        @Override
        protected void onPostExecute(JSONObject value) {
            dialog.dismiss();
            try {
                if(value.has("error")) {
                    Snackbar snk = null;
                    try {
                        snk = Snackbar.make(fab, value.getString("error"), Snackbar.LENGTH_LONG);
                        View snkView = snk.getView();
                        ((TextView)snkView.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
                        snkView.setBackgroundColor(Color.RED);
                        snk.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    edit.putString("login", login.getText().toString());
                    edit.putString("password", passwd.getText().toString());
                    edit.putBoolean("firstLogin", true);
                    edit.apply();

                    try {
                        startService(new Intent(getApplicationContext(), t411UpdateService.class));
                        startActivity(new Intent(getApplicationContext(), MainActivity2.class).putExtra("message", getResources().getString(R.string.initialLoad)));
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Snackbar snk = Snackbar.make(fab, "Erreur interne : " + e.getMessage(), Snackbar.LENGTH_SHORT);
                View snkView = snk.getView();
                ((TextView)snkView.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
                snkView.setBackgroundColor(Color.RED);
                snk.show();
                e.printStackTrace();
            }
        }
    }
}
