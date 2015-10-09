package fr.lepetitpingouin.android.t411;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

/**
 * Created by gregory on 01/09/15.
 */
public class ProxyActivity extends ActionBarActivity {

    BillingProcessor bp;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proxystatus);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        edit = prefs.edit();


        new T411Logger(getApplicationContext()).writeLine("Ouverture de la page du proxy");

        final LinearLayout subscribed = (LinearLayout)findViewById(R.id.abonned);

        final Button abo = (Button)findViewById(R.id.btn_abo_proxy);
        abo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPurchase();
            }
        });

        Button vpn = (Button)findViewById(R.id.btn_vpn);
        vpn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://play.google.com/store/search?q=vpn"));
                startActivity(i);
            }
        });

        Button dns = (Button)findViewById(R.id.btn_dns);
        dns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://play.google.com/store/search?q=dns"));
                startActivity(i);
            }
        });

        bp = new BillingProcessor(this, Private.API_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String s, TransactionDetails transactionDetails) {
                subscribed.setVisibility(View.VISIBLE);
                abo.setVisibility(View.GONE);
                edit.putBoolean("showProxyAlert", true).apply();
                ((CheckBox) findViewById(R.id.checkBox_proxyAlert)).setChecked(true);
                new T411Logger(getApplicationContext()).writeLine("Abonnement souscrit");
            }

            @Override
            public void onPurchaseHistoryRestored() {
            }

            @Override
            public void onBillingError(int i, Throwable throwable) {
            }

            @Override
            public void onBillingInitialized() {
            }
        });
        if (!BillingProcessor.isIabServiceAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "Le Google Play Store doit être installé et à jour pour pouvoir vous abonner.", Toast.LENGTH_LONG).show();
            abo.setText("Play Store non conforme");
            new T411Logger(getApplicationContext()).writeLine("Le Play Store n'est pas conforme pour un achat in-app", T411Logger.ERROR);
            abo.setEnabled(false);
        }

        bp.loadOwnedPurchasesFromGoogle();

        if(bp.isSubscribed(Private.PROXY_ITEM_ID)==true) {
            subscribed.setVisibility(View.VISIBLE);
            abo.setVisibility(View.GONE);
            new T411Logger(getApplicationContext()).writeLine("Souscription effective, affichage de l'option");

        } else {
            subscribed.setVisibility(View.GONE);
            abo.setVisibility(View.VISIBLE);
            new T411Logger(getApplicationContext()).writeLine("La souscription n'est pas effective");
        }

        ((CheckBox)findViewById(R.id.checkBox_proxyAlert)).setChecked(prefs.getBoolean("showProxyAlert", true));
        ((CheckBox)findViewById(R.id.cbx_use_proxy)).setChecked(prefs.getBoolean("usePaidProxy", false));

        if(BuildConfig.DEBUG) {
            subscribed.setVisibility(View.VISIBLE);
            abo.setVisibility(View.GONE);
        }

    }

    @Override
    public void onDestroy() {
        bp.release();
        super.onDestroy();

    }

    private void doPurchase() {

        new T411Logger(getApplicationContext()).writeLine("Clic sur le bouton d'abonnement");
        bp.subscribe(this, "testing_proxy");

    }

    public void onCheckboxClick(View v) {
        edit.putBoolean("showProxyAlert", ((CheckBox)findViewById(R.id.checkBox_proxyAlert)).isChecked()).apply();
    }

    public void onUseProxyClick(View v) {
        edit.putBoolean("usePaidProxy", ((CheckBox)v).isChecked()).commit();
        new T411Logger(getApplicationContext()).writeLine("Utilisation du proxy => " + (((CheckBox)v).isChecked()?"ON":"OFF"));
    }

}
