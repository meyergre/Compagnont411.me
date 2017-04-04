package fr.lepetitpingouin.android.t411;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gregory on 10/09/2016.
 */
public class WelcomeActivity extends AppCompatActivity {

    ViewFlipper vf;
    ProgressDialog dialog;
    EditText login, passwd;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    PackageManager pkgMgr;
    ComponentName cn;

    CheckBox launcher_messages, launcher_downloads;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_welcome);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit = prefs.edit();

        pkgMgr = getPackageManager();


        launcher_messages = (CheckBox)findViewById(R.id.checkBoxMessages);
        launcher_messages.setChecked(pkgMgr.getComponentEnabledSetting(new ComponentName(getPackageName(), getPackageName()+".launcherMessages"))!= PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

        launcher_downloads = (CheckBox)findViewById(R.id.checkBoxDownloads);
        launcher_downloads.setChecked(pkgMgr.getComponentEnabledSetting(new ComponentName(getPackageName(), getPackageName()+".launcherDownloads"))!= PackageManager.COMPONENT_ENABLED_STATE_DISABLED);


        vf = (ViewFlipper)findViewById(R.id.viewFlipper);

        findViewById(R.id.btn_skip).setVisibility(prefs.getString("login", "").equals("") ? View.INVISIBLE : View.VISIBLE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.BLACK);
            getWindow().setStatusBarColor(getResources().getColor(R.color.t411_action_blue_darker));
        }

        login = (EditText) findViewById(R.id.login_username);
        passwd = (EditText) findViewById(R.id.login_password);

        login.setText(prefs.getString("login", ""));
        passwd.setText(prefs.getString("password", ""));

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
        mAdView.loadAd(adRequest);
    }

    public void onNext(View v) {
        vf.showNext();
    }

    public void onFinish(View v) {
        finish();
    }

    public void onLogin(View v) {
        new AsyncConnector().execute();
    }

    public void onRateOnPlaystore(View v) {
        /* This code assumes you are inside an activity */
        final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

        if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0)
        {
            startActivity(rateAppIntent);
        }
        else
        {
            /* handle your error case: the device has no way to handle market urls */
        }
    }

    public void onLauncherIcons(View v) {
        pkgMgr.setComponentEnabledSetting(new ComponentName(getPackageName(), getPackageName()+".launcherMessages"), !launcher_messages.isChecked()?PackageManager.COMPONENT_ENABLED_STATE_DISABLED:PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        pkgMgr.setComponentEnabledSetting(new ComponentName(getPackageName(), getPackageName()+".launcherDownloads"), !launcher_downloads.isChecked()?PackageManager.COMPONENT_ENABLED_STATE_DISABLED:PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        vf.showNext();
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
            if(value.has("error")) {
                Snackbar snk = null;
                try {
                    snk = Snackbar.make(vf, value.getString("error"), Snackbar.LENGTH_LONG);
                    View snkView = snk.getView();
                    ((TextView)snkView.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
                    snkView.setBackgroundColor(Color.RED);
                    snk.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                    try {
                        edit.putString("uid", value.getString("uid"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                edit.putString("login", this.mLogin);
                edit.putString("password", this.mPassword);
                edit.putBoolean("firstLogin", true);
                edit.putBoolean("autoUpdate", ((SwitchCompat)findViewById(R.id.autoupdate)).isChecked());
                edit.commit();
                startService(new Intent(getApplicationContext(), t411UpdateService.class));
                vf.showNext();
            }

        }
    }
}
