package fr.lepetitpingouin.android.t411;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class TorrentsListActivity extends AppCompatActivity {

    private ListView list;
    private TorrentsListAdapter adapter;
    PackageManager pkgMgr;
    ComponentName cn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrents_list);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        getSupportActionBar().setTitle("Téléchargements");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        list = (ListView)findViewById(R.id.listView);
        registerForContextMenu(list);
        adapter = new TorrentsListAdapter(getApplicationContext());

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.getItem(position).open();
            }
        });

        list.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyList();
            }
        });

        pkgMgr = getPackageManager();
        cn = new ComponentName(getPackageName(), getPackageName()+".launcherDownloads");

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter = new TorrentsListAdapter(getApplicationContext());
        adapter.notifyDataSetChanged();
    }


    private void emptyList() {

        new AlertDialog.Builder(this)
                .setTitle("Vidage de la liste")
                .setMessage("Voulez-vous réellement supprimer tous les téléchargements ?")
                .setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("jsonTorrentList", "[]").apply();
                        for(int i = 0; i < adapter.getCount(); i++) {
                            adapter.getItem(i).delete();
                        }
                        adapter = new TorrentsListAdapter(getApplicationContext());
                        list.setAdapter(adapter);
                    }

                })
                .setNegativeButton(R.string.NO, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_downloads, menu);
        menu.findItem(R.id.dl_showHome).setChecked(pkgMgr.getComponentEnabledSetting(cn)!=PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.dl_showHome:
                pkgMgr.setComponentEnabledSetting(cn, item.isChecked()?PackageManager.COMPONENT_ENABLED_STATE_DISABLED:PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                item.setChecked(!item.isChecked());
                Toast.makeText(getApplicationContext(), "L'icône va "+(item.isChecked()?"apparaître":"disparaître")+" dans quelques instants...", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.downloads_menu_open_folder:
                Uri selectedUri = Uri.parse(Torrent.getTorrentPath());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(selectedUri, "resource/folder");
                if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.menu_item_downloads_open_folder)));
                } else {
                    Snackbar s = Snackbar.make(list, getResources().getString(R.string.menu_item_downloads_open_folder_error_nofm), Snackbar.LENGTH_LONG);
                    s.setAction("Play Store", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Default.URL_PLAY_FILEEXPLORER)));
                        }
                    });
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.downloads_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Torrent t = (Torrent)list.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.downloads_context_menu_open:
                t.open();
                return true;
            case R.id.downloads_context_menu_delete:
                new AlertDialog.Builder(this)
                        .setTitle("Suppression")
                        .setMessage("Voulez-vous réellement supprimer '"+t.name+"' ?")
                        .setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                t.delete();
                                adapter = new TorrentsListAdapter(getApplicationContext());
                                list.setAdapter(adapter);
                            }

                        })
                        .setNegativeButton(R.string.NO, null)
                        .show();

                return true;
            case R.id.downloads_context_menu_prez:
                t.launchUrl();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

}
