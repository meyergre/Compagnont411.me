package fr.lepetitpingouin.android.t411;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class aboutActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        String version = "????";
        PackageInfo pInfo;

        getSupportActionBar().setIcon(R.drawable.ic_about);
        getSupportActionBar().setTitle(getResources().getString(R.string.about));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        LinearLayout paypal = (LinearLayout) findViewById(R.id.paypal);
        paypal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DonateActivity.class));
            }
        });

        Button OtherAppsBtn = (Button) findViewById(R.id.btnOtherApps);
        OtherAppsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(Default.URL_OTHERAPPS));
                startActivity(i);
            }
        });

        Button donateToT411 = (Button) findViewById(R.id.about_t411);
        donateToT411.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i;
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Default.URL_DONATE));
                startActivity(i);
            }
        });

        TextView appVersion = (TextView) findViewById(R.id.appVersion);
        appVersion.setText("Version " + version);

        Button changelog = (Button) findViewById(R.id.btnChgLog);
        changelog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String chlog = "";
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("Changelog.txt"), "UTF-8"));
                    String mLine = reader.readLine();
                    while (mLine != null) {
                        chlog += mLine + "\n";
                        mLine = reader.readLine();
                    }
                    reader.close();
                } catch (IOException e) {
                }


                AlertDialog alertDialog = alertDialog = new AlertDialog.Builder(aboutActivity.this).setTitle("Changelog").setMessage(chlog).setIcon(R.drawable.file).show();
                TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
                textView.setTextSize(10);
                textView.setTypeface(Typeface.MONOSPACE);
            }
        });
    }
}
