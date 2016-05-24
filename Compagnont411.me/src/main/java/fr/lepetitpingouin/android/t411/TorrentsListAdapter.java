package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by gregory on 23/05/2016.
 */
public class TorrentsListAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Context context;

    private JSONArray json;


    public TorrentsListAdapter(Context context) {
        this.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            this.json = new JSONArray(prefs.getString("jsonTorrentList", "[]"));
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
            vi = inflater.inflate(R.layout.item_torrents_list_torrent, null);

        try {
            JSONObject o = this.getItem(position);

            TextView title = (TextView) vi.findViewById(R.id.torrent_title); // title
            title.setText(o.get("title").toString());

            TextView uploader = (TextView) vi.findViewById(R.id.torrent_uploader);
            uploader.setText(o.get("uploader").toString());

            TextView size = (TextView) vi.findViewById(R.id.torrent_size);
            size.setText(o.get("size").toString());


        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return vi;
    }
}

