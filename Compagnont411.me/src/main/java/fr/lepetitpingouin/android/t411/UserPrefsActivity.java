package fr.lepetitpingouin.android.t411;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

/**
 * Created by gregory on 23/10/2013.
 */
public class UserPrefsActivity extends PreferenceActivity {

    Preference filePicker;
    SharedPreferences prefs;

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
                    Intent intent = new Intent(getBaseContext(), FileDialog.class);
                    intent.putExtra(FileDialog.START_PATH, prefs.getString("filePicker", Environment.getExternalStorageDirectory().getPath()));

                    intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
                    intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
                    intent.putExtra(FileDialog.FORMAT_FILTER, new String[]{"@file_not_allowed"});

                    startActivityForResult(intent, SelectionMode.MODE_OPEN);
                    return true;
                }
            });

            filePicker.setSummary(prefs.getString("filePicker", "Aucun chemin choisi"));
        } catch (ClassCastException cce) {
            Toast.makeText(this, "Erreur d'ICC détectée. Veuillez suivre les instructions données dans les notes de version du Play Store.", Toast.LENGTH_LONG).show();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }



        Preference openProxyStatus = findPreference("option_proxy");
        openProxyStatus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getApplicationContext(), ProxyActivity.class);
                startActivity(i);
                return false;
            }
        });

        Preference sendLog = findPreference("sendLogs");
        final String logFile = new T411Logger(getApplicationContext()).logFilePath();

        File log = new File(logFile);
        sendLog.setEnabled(!log.exists());

        sendLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:meyergre@gmail.com"));
                i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + logFile));
                i.putExtra(Intent.EXTRA_SUBJECT, "Compagnon t411 - envoi de logs");
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
