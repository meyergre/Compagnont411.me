package fr.lepetitpingouin.android.t411;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.io.File;

/**
 * Created by gregory on 23/10/2013.
 */
public class UserPrefsActivity extends PreferenceActivity {

    private Preference filePicker;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);


        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        try {

            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            filePicker = findPreference("savePath");
            assert filePicker != null;
            filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if(shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        }
                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                    else{

                        Intent intent = new Intent(getBaseContext(), FileDialog.class);
                        intent.putExtra(FileDialog.START_PATH, prefs.getString("filePicker", Environment.getExternalStorageDirectory().getPath()));

                        intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
                        intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
                        intent.putExtra(FileDialog.FORMAT_FILTER, new String[]{"@file_not_allowed"});

                        startActivityForResult(intent, SelectionMode.MODE_OPEN);
                    }
                    return true;
                }
            });

            //filePicker.setSummary(prefs.getString("filePicker", "Aucun chemin choisi"));
        } catch (ClassCastException cce) {
            Toast.makeText(this, "Erreur d'ICC détectée. Veuillez suivre les instructions données dans les notes de version du Play Store.", Toast.LENGTH_LONG).show();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Preference customDomain = findPreference("custom_domain");
        customDomain.setSummary(prefs.getString("custom_domain", Default.IP_T411));
        customDomain.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if(o.toString().isEmpty()) {
                    prefs.edit().remove("custom_domain").apply();
                }
                prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                preference.setSummary(prefs.getString("custom_domain", Default.IP_T411 + " (default)"));
                return true;
            }
        });


        Preference openProxyStatus = findPreference("option_proxy");
        openProxyStatus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getApplicationContext(), ProxyActivity.class);
                startActivity(i);
                return false;
            }
        });

        Preference clearHistory = findPreference("clearSearchHistory");
        clearHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                prefs.edit().remove("searchHistory").apply();
                Toast.makeText(getApplicationContext(), "Historique vidé.", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        Preference sendLog = findPreference("sendLogs");
        final String logFile = new T411Logger(getApplicationContext()).logFilePath();

        final File log = new File(logFile);
        sendLog.setEnabled(log.exists());

        sendLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:meyergre@gmail.com"));
                //i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + logFile));
                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(log));
                i.putExtra(Intent.EXTRA_SUBJECT, "[Compagnon t411] envoi de logs");
                i.putExtra(Intent.EXTRA_TEXT, "Description du problème rencontré : \n\n");
                startActivity(Intent.createChooser(i, ""));
                return false;
            }
        });

        Preference openLogs = findPreference("openLogs");
        openLogs.setEnabled(log.exists());
        openLogs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(log), "text/plain");
                startActivity(Intent.createChooser(i, ""));
                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //get the new value from Intent data
        SharedPreferences.Editor editor = prefs.edit();

        if (resultCode == Activity.RESULT_OK) {
            editor.putString("filePicker", data.getStringExtra(FileDialog.RESULT_PATH));
            editor.commit();
            filePicker.setSummary(data.getStringExtra(FileDialog.RESULT_PATH));
        }
    }
}
