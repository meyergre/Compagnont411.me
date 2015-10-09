package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.helper.StringUtil;

import java.util.ArrayList;

/**
 * Created by gregory on 07/10/2015.
 */
public class SearchHistory {

    Context context;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    ArrayList<String> values;

    public SearchHistory(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        edit = prefs.edit();
        this.values = new ArrayList<>();
        this.load();
    }

    private void load() {

        String rawValues = prefs.getString("searchHistory", "");
        String[] sValues;
        try {
            sValues = rawValues.split("");
            for (String val : sValues) {
                this.values.add(val);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<String> getValues() {

        return (ArrayList<String>) this.values;
    }

    public void save(String newValue) {
        if (!values.contains(newValue)) {
            this.values.add(newValue);
            edit.putString("searchHistory", StringUtil.join(this.values, "")).commit();
        }
    }
}
