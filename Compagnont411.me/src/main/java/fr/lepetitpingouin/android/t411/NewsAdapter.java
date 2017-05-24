package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by gregory on 23/05/2016.
 */
class NewsAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Context context;

    private JSONArray json;


    public NewsAdapter(Context context) {
        this.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.json = new JSONArray();

        String jsonString = prefs.getString("news", "[]");
        try {
            this.json = new JSONArray(jsonString);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return this.json.length();
    }

    @Override
    public JSONObject getItem(int position) {
        JSONObject o = new JSONObject();
        try {
            o = (JSONObject) this.json.get(position);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return o;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        inflater = LayoutInflater.from(context);
        if (convertView == null)
            vi = inflater.inflate(R.layout.item_news, null);

        try {
            JSONObject o = this.json.getJSONObject(position);

            TextView date = (TextView) vi.findViewById(R.id.tv_date);
            TextView title = (TextView) vi.findViewById(R.id.tv_title);
            ImageView icon = (ImageView) vi.findViewById(R.id.iv_icon);
            ImageView hotmark = (ImageView) vi.findViewById(R.id.iv_hot);

            if(o.has("age") && Long.parseLong((String)o.get("age")) < 86400000) {
                hotmark.setVisibility(View.VISIBLE);
            } else {
                hotmark.setVisibility(View.INVISIBLE);
            }

            if(o.has("source") && o.get("source").equals("t411")) {
                icon.setBackgroundResource(R.drawable.ic_btn_news_t411);
            } else {
                icon.setBackgroundResource(R.drawable.ic_btn_news);
            }
            date.setText(o.get("date").toString());
            title.setText(o.get("title").toString());

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return vi;
    }
}

