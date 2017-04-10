package fr.lepetitpingouin.android.t411;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class aboutActivity extends AppCompatActivity {
    private static String version;
    private BillingProcessor bp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        version = "????";
        PackageInfo pInfo;

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(getResources().getString(R.string.about));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final LinearLayout rmAds = (LinearLayout) findViewById(R.id.removeAds);

        bp = new BillingProcessor(this, Private.IAP_API_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String s, TransactionDetails transactionDetails) {

            }

            @Override
            public void onPurchaseHistoryRestored() {

            }

            @Override
            public void onBillingError(int i, Throwable throwable) {

            }

            @Override
            public void onBillingInitialized() {
                bp.loadOwnedPurchasesFromGoogle();
                rmAds.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        bp.purchase(aboutActivity.this, Private.STOPPUB_ITEM_ID);
                    }
                });
            }
        });


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
                startActivity(new Intent(getApplicationContext(), DonateActivity.class).putExtra("version", version));
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


                AlertDialog alertDialog = new AlertDialog.Builder(aboutActivity.this).setTitle("Changelog").setMessage(chlog).setIcon(R.drawable.file).show();
                TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
                textView.setTextSize(10);
                textView.setTypeface(Typeface.MONOSPACE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
