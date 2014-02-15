package fr.lepetitpingouin.android.t411;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by gregory on 23/10/2013.
 */
public class UserPrefsActivity extends PreferenceActivity {

    Preference filePicker;
    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            addPreferencesFromResource(R.xml.settings);

            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            filePicker = (Preference) findPreference("savePath");
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
