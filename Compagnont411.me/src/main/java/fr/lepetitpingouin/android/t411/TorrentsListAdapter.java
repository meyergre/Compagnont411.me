package fr.lepetitpingouin.android.t411;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Collections;

/**
 * Created by gregory on 23/05/2016.
 */
class TorrentsListAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Context context;

    private JSONArray json;


    public TorrentsListAdapter(Context context) {
        this.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.json = new JSONArray();

        try {
            JSONArray _json = new JSONArray(prefs.getString("jsonTorrentList", "[]"));
            for (int i = 0; i <  _json.length(); i++) {
                JSONObject o = (JSONObject) _json.get(i);
                Torrent t = new Torrent(this.context, o.get("title").toString(), o.get("id").toString(), o.get("size").toString(), o.get("uploader").toString(), o.get("category").toString());
                if( new File(t.getTorrentPath(), t.getTorrentName()).exists()) {
                    this.json.put(o);
                } else {
                    t.delete();
                }

            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return this.json.length();
    }

    @Override
    public Torrent getItem(int position) {
        JSONObject o = new JSONObject();
        Torrent t;
        try {
            o = (JSONObject) this.json.get(position);
            t = new Torrent(this.context, o.get("title").toString(), o.get("id").toString(), o.get("size").toString(), o.get("uploader").toString(), o.get("category").toString());
            if(o.has("download_date")) t.download_date = (Long)o.get("download_date");
            return t;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
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
            Torrent t = this.getItem(position);

            TextView title = (TextView) vi.findViewById(R.id.torrent_title);
            title.setText(t.name);

            TextView uploader = (TextView) vi.findViewById(R.id.torrent_uploader);
            uploader.setText(t.uploader);

            TextView size = (TextView) vi.findViewById(R.id.torrent_size);
            size.setText(t.size);

            ImageView img = (ImageView) vi.findViewById(R.id.imageViewCatIcon);
            img.setImageResource(new CategoryIcon(t.category).getIcon());

            TextView recent = (TextView) vi.findViewById(R.id.torrent_recent);
            if(System.currentTimeMillis() - t.download_date < 1000*60*60*24)recent.setVisibility(View.VISIBLE);

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return vi;
    }
}

