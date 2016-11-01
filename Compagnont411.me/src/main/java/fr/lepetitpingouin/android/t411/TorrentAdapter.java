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

import java.util.List;

/**
 * Created by gregory on 30/10/2015.
 */
class TorrentAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private List<Torrent> torrents;
    private Context context;
    private SharedPreferences prefs;

    public TorrentAdapter(Context context, List<Torrent> torrents){
        this.torrents = torrents;
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getCount() {
        return torrents.size();
    }

    @Override
    public Torrent getItem(int position) {
        return torrents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View vi = convertView;
        inflater = LayoutInflater.from(context);
        if (convertView == null)
            vi = inflater.inflate(R.layout.item_torrent_card, null);

        try {
            Torrent t = this.getItem(position);

            TextView title = (TextView) vi.findViewById(R.id.tNom);
            title.setText(t.name);

            TextView uploader = (TextView) vi.findViewById(R.id.t_up_date);
            uploader.setText(t.uploader + ", " + t.age);

            TextView size = (TextView) vi.findViewById(R.id.tTaille);
            size.setText(BSize.quickConvert(t.size));

            TextView seed = (TextView) vi.findViewById(R.id.tSeeders);
            seed.setText(t.seeders);

            TextView leech = (TextView) vi.findViewById(R.id.tLeechers);
            leech.setText(t.leechers);

            TextView done = (TextView) vi.findViewById(R.id.tCompleted);
            done.setText(t.complets);

            ImageView img = (ImageView) vi.findViewById(R.id.tIcon);
            img.setImageResource(new CategoryIcon(t.category).getIcon());


            // Calcul du ratio
            double estimatedDl = (new BSize(prefs.getString("lastDownload", "0.00").replace(",",".")).getInMB()) + new BSize(t.size).getInMB();
            double estimatedRatio = (new BSize(prefs.getString("lastUpload", "0.00").replace(",",".")).getInMB() / estimatedDl) - 0.005;

            TextView ratioEst = (TextView) vi.findViewById(R.id.ratioTo);
            ratioEst.setText(String.format("%.2f", estimatedRatio));

            TextView ratio = (TextView) vi.findViewById(R.id.ratioFrom);
            ratio.setText(prefs.getString("lastRatio", "0"));

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return vi;
    }
}
