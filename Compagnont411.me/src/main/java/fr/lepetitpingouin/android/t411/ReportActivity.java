package fr.lepetitpingouin.android.t411;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class ReportActivity extends AppCompatActivity {

    TextView brand, model, osVersion, osCodename, packageName, appVersion, username, https, ipT411, dossierDl, frequence, lastupdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        PackageManager manager = getApplicationContext().getPackageManager();
        PackageInfo monAppli = new PackageInfo();
        try {
            monAppli = manager.getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {}

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        brand = (TextView)findViewById(R.id.report_brand);
        brand.setText(Build.MANUFACTURER);

        model = (TextView)findViewById(R.id.report_model);
        model.setText(Build.MODEL);

        osVersion = (TextView)findViewById(R.id.report_osversion);
        osVersion.setText(Build.VERSION.RELEASE);

        osCodename = (TextView)findViewById(R.id.report_oscodename);
        osCodename.setText(Build.VERSION.CODENAME);

        packageName = (TextView)findViewById(R.id.report_package);
        packageName.setText(monAppli.packageName);

        appVersion = (TextView)findViewById(R.id.report_version);
        appVersion.setText(monAppli.versionName);

        username = (TextView)findViewById(R.id.report_username);
        username.setText(prefs.getString("lastUsername", "NOT FOUND").toUpperCase());

        https = (TextView)findViewById(R.id.report_https);
        https.setText((prefs.getBoolean("useHTTPS", false)?"ENABLED":"DISABLED"));

        ipT411 = (TextView)findViewById(R.id.report_addresst411);
        ipT411.setText(prefs.getString("SiteIP", "NOT FOUND"));

        dossierDl = (TextView)findViewById(R.id.report_downloadpath);
        dossierDl.setText(prefs.getString("savePath", "NOT FOUND"));

        frequence = (TextView)findViewById(R.id.report_frequeny);
        frequence.setText(prefs.getString("updateFreq", "NOT FOUND"));

        lastupdate = (TextView)findViewById(R.id.report_lastupdate);
        lastupdate.setText(prefs.getString("lastUpdate", "NOT FOUND"));
    }
}
