package fr.lepetitpingouin.android.t411;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

public class TorrentsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrents_list);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView list = (ListView)findViewById(R.id.listView);
        TorrentsListAdapter adapter = new TorrentsListAdapter(getApplicationContext());

        list.setAdapter(adapter);
    }
}
