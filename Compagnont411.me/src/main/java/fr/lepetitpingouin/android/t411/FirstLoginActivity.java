package fr.lepetitpingouin.android.t411;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class FirstLoginActivity extends Activity {

    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    EditText login, passwd;

    TextView infoError;
    ProgressDialog dialog;

    AsyncConnector asc;

    View fab;

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


        dialog = new ProgressDialog(this);
        dialog.setMessage("Connexion...");
        dialog.setCancelable(true);

        AdView mAdView;
        AdRequest adRequest;
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.adtitlebar, null);
        mAdView = (AdView) view.findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice(Private.REAL_DEVICE).build();
        dialog.setCustomTitle(view);
        mAdView.loadAd(adRequest);
        //dialog.show();

        dialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                asc.cancel(true);
                asc = null;
                //finish();
            }
        });

        ImageView girl = (ImageView)findViewById(R.id.login_iv_girl);
        ImageView city = (ImageView)findViewById(R.id.login_iv_city);

        final View login = findViewById(R.id.login_inc_loginform);
        fab = findViewById(R.id.fablogin);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            city.animate()
                    .setDuration(0)
                    .setStartDelay(0)
                    .alpha(0)
                    .start();

            city.animate()
                    .setDuration(3000)
                    .setStartDelay(800)
                    .alpha(1)
                    .start();
            city.animate()
                    .setStartDelay(0)
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(15000)
                    .start();


            girl.animate()
                    .setDuration(0)
                    .setStartDelay(0)
                    .translationYBy((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, getResources().getDisplayMetrics()))
                    .start();
            girl.animate()
                    .setDuration(1500)
                    .setStartDelay(1500)
                    .translationYBy(-(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, getResources().getDisplayMetrics()))
                    .start();


            login.animate()
                    .setDuration(0)
                    .setStartDelay(0)
                    .y((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -130, getResources().getDisplayMetrics()))
                    .alpha(0)
                    .start();
            login.animate()
                    .setDuration(1200)
                    .setStartDelay(1500)
                    .alpha(1)
                    .y((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()))
                    .start();

            fab.animate()
                    .setDuration(0)
                    .setStartDelay(0)
                    .scaleX(0)
                    .scaleY(0)
                    .rotation(-500)
                    .start();
            fab.animate()
                    .setDuration(600)
                    .setStartDelay(4000)
                    .scaleX(1)
                    .scaleY(1)
                    .rotation(0)
                    .start();
        }

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

    private class AsyncConnector extends AsyncTask<Void, String[], Void> {

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
        protected Void doInBackground(Void... arg0) {

            Document doc;
            try {

                String html = new SuperT411HttpBrowser(getApplicationContext())
                        .login(mLogin, mPassword)
                        .connect(Default.URL_LOGIN)
                        .executeLoginForMessage();

                if (!html.equals("OK")) {
                    doc = Jsoup.parse(html);
                    message = doc.select("#messages > p").first().text();
                } else message = "Connexion réussie !";

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            try {
                if (message != null && message.contains("incorrect")) {
                    dialog.dismiss();
                    edit.putBoolean("firstLogin", false);

                    Snackbar snk = Snackbar.make(fab, message, Snackbar.LENGTH_SHORT);
                    View snkView = snk.getView();
                    ((TextView)snkView.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
                    snkView.setBackgroundColor(Color.RED);
                    snk.show();

                    edit.commit();
                } else if (message != null && message.contains("captcha")) {
                    Snackbar snk = Snackbar.make(fab, message, Snackbar.LENGTH_SHORT);
                    View snkView = snk.getView();
                    ((TextView)snkView.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
                    snkView.setBackgroundColor(Color.RED);
                    snk.show();
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
