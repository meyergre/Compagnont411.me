package fr.lepetitpingouin.android.t411;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class Settings extends Activity {

    SharedPreferences prefs;

    AlarmManager alarmManager;

    Editor editor;

    CheckBox ratioAlert, mailAlert, autoUpdate, wifiOnly, hadopi, nw,
            rtcMode, useHTTPS, menuRechercher, menuTop100, menuInbox, menuFriends, menuStats, menuCalc, menuNews, menuForum, menuBookmarks;
    RadioButton dlModeDirect, dlModeRedirect, lpDownload, lpShare, lpFuture;
    EditText minimum, frequency, targetR, login, password, timeout, UA;
    LinearLayout btnWidgetAction, btnChangePath;
    TextView savePath;

    @Override
    public void onPause() {
        update();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void update() {
        editor.putString("login", login.getText().toString());
        editor.putString("password", password.getText().toString());

        editor.putBoolean("autoUpdate", autoUpdate.isChecked());
        editor.putBoolean("ratioAlert", ratioAlert.isChecked());
        editor.putString("ratioMinimum", minimum.getText().toString());
        editor.putString("ratioCible", targetR.getText().toString());
        editor.putBoolean("mailAlert", mailAlert.isChecked());
        editor.putString("updateFreq", frequency.getText().toString());
        //editor.putBoolean("updAlert", notifAlert.isChecked());
        editor.putBoolean("wifiOnly", wifiOnly.isChecked());
        editor.putBoolean("useHTTPS", useHTTPS.isChecked());
        editor.putBoolean("menuTop100", menuTop100.isChecked());
        editor.putBoolean("menuStats", menuStats.isChecked());
        editor.putBoolean("menuRechercher", menuRechercher.isChecked());
        editor.putBoolean("menuNews", menuNews.isChecked());
        editor.putBoolean("menuCalc", menuCalc.isChecked());
        editor.putBoolean("menuFriends", menuFriends.isChecked());
        editor.putBoolean("menuForum", menuForum.isChecked());
        editor.putBoolean("notificationWidget", nw.isChecked());
        editor.putBoolean("menuInbox", menuInbox.isChecked());
        editor.putBoolean("dlModeDirect", dlModeDirect.isChecked());
        editor.putBoolean("dlModeRedirect", dlModeRedirect.isChecked());
        editor.putBoolean("lpShare", lpShare.isChecked());
        editor.putBoolean("lpDownload", lpDownload.isChecked());
        editor.putBoolean("firstSettings", true);
        editor.putString("savePath", savePath.getText().toString());
        editor.putBoolean("dlLater", menuBookmarks.isChecked());
        editor.putBoolean("lpFuture", lpFuture.isChecked());
        editor.putString("timeoutValue", timeout.getText().toString());
        editor.putBoolean("rtcMode", rtcMode.isChecked());
        editor.putBoolean("hadopi", hadopi.isChecked());
        editor.putString("User-Agent", UA.getText().toString());

        editor.commit();

        if (!prefs.getBoolean("autoUpdate", false)) {
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(PendingIntent.getService(Settings.this, 0,
                    new Intent(Settings.this, t411UpdateService.class), 0));
        }
        new NotificationWidget(getApplicationContext()).updateNotificationWidget();

    }

    public synchronized void onActivityResult(final int requestCode,
                                              int resultCode, final Intent data) {

        if (resultCode == Activity.RESULT_OK) {

            String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
            savePath.setText(filePath);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!prefs.getBoolean("firstLogin", false)) {
            Intent myIntent = new Intent(this, FirstLoginActivity.class);
            startActivity(myIntent);
        }

        editor = prefs.edit();

        LinearLayout UserAgentExtra = (LinearLayout) findViewById(R.id.settings_UserAgentBlock);
        UserAgentExtra.setVisibility(getIntent().getBooleanExtra("advanced", false) ? View.VISIBLE : View.GONE);

        LinearLayout TimeoutExtra = (LinearLayout) findViewById(R.id.settings_timeoutBlock);
        TimeoutExtra.setVisibility(getIntent().getBooleanExtra("advanced", false) ? View.VISIBLE : View.GONE);

        LinearLayout hideMenusExtra = (LinearLayout) findViewById(R.id.settings_mainmenuBlock);
        hideMenusExtra.setVisibility(getIntent().getBooleanExtra("advanced", false) ? View.VISIBLE : View.GONE);

        TextView title = (TextView) findViewById(R.id.titleView);
        title.setText(getIntent().getBooleanExtra("advanced", false) ? R.string.service_settings_advanced : R.string.service_settings);

        UA = (EditText) findViewById(R.id.txtUA);
        UA.setText(prefs.getString("User-Agent", Default.USER_AGENT));

        LinearLayout btnResetUA = (LinearLayout) findViewById(R.id.btnResetUA);
        btnResetUA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UA.setText(Default.USER_AGENT);
            }
        });

        savePath = (TextView) findViewById(R.id.settings_save_path);
        savePath.setText(prefs.getString("savePath", Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS));

        btnChangePath = (LinearLayout) findViewById(R.id.settings_path);
        btnChangePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), FileDialog.class);
                intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());

                //can user select directories or not
                intent.putExtra(FileDialog.CAN_SELECT_DIR, true);

                intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

                //alternatively you can set file filter
                intent.putExtra(FileDialog.FORMAT_FILTER, new String[]{"@file_not_allowed"});

                startActivityForResult(intent, SelectionMode.MODE_OPEN);
            }
        });


        login = (EditText) findViewById(R.id.settings_login);
        login.setText(prefs.getString("login", ""));

        password = (EditText) findViewById(R.id.settings_password);
        password.setText(prefs.getString("password", ""));

        final CharSequence[] items = {this.getString(R.string.open_app),
                this.getString(R.string.update_now),
                this.getString(R.string.read_mails),
                this.getString(R.string.goto_t411),
                this.getString(R.string.askWhatToDo)};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.ChooseAction));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int item) {
                Toast.makeText(getApplicationContext(),
                        getApplicationContext().getString(R.string.savedData),
                        Toast.LENGTH_SHORT).show();
                editor.putInt("widgetAction", item);
                editor.commit();
                startService(new Intent(Settings.this, t411UpdateService.class));
            }
        });

        btnWidgetAction = (LinearLayout) findViewById(R.id.btnWidgetAction);
        btnWidgetAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform action on click
                builder.create().show();
            }
        });

        nw = (CheckBox) findViewById(R.id.chk_notificationWidget);
        nw.setChecked(prefs.getBoolean("notificationWidget", false));

        timeout = (EditText) findViewById(R.id.field_timeout);
        timeout.setText(prefs.getString("timeoutValue", Default.timeout));

        menuCalc = (CheckBox) findViewById(R.id.chkMenuCalc);
        menuCalc.setChecked(prefs.getBoolean("menuCalc", false));

        menuForum = (CheckBox) findViewById(R.id.chkMenuForum);
        menuForum.setChecked(prefs.getBoolean("menuForum", false));

        menuInbox = (CheckBox) findViewById(R.id.chkMenuInbox);
        menuInbox.setChecked(prefs.getBoolean("menuInbox", false));

        menuBookmarks = (CheckBox) findViewById(R.id.menuBookmarks);
        menuBookmarks.setChecked(prefs.getBoolean("dlLater", false));

        menuFriends = (CheckBox) findViewById(R.id.chkMenuFriends);
        menuFriends.setChecked(prefs.getBoolean("menuFriends", false));

        menuNews = (CheckBox) findViewById(R.id.chkMenuNews);
        menuNews.setChecked(prefs.getBoolean("menuNews", false));

        menuRechercher = (CheckBox) findViewById(R.id.chkMenuSearch);
        menuRechercher.setChecked(prefs.getBoolean("menuRechercher", false));

        menuStats = (CheckBox) findViewById(R.id.chkMenuStats);
        menuStats.setChecked(prefs.getBoolean("menuStats", false));

        menuTop100 = (CheckBox) findViewById(R.id.chkMenuTop100);
        menuTop100.setChecked(prefs.getBoolean("menuTop100", false));

        dlModeDirect = (RadioButton) findViewById(R.id.chkDirectDL);
        dlModeDirect.setChecked(prefs.getBoolean("dlModeDirect", true));

        dlModeRedirect = (RadioButton) findViewById(R.id.chkRedirectDL);
        dlModeRedirect.setChecked(prefs.getBoolean("dlModeRedirect", false));

        lpDownload = (RadioButton) findViewById(R.id.longclicDownload);
        lpDownload.setChecked(prefs.getBoolean("lpDownload", false));

        lpShare = (RadioButton) findViewById(R.id.longclickShare);
        lpShare.setChecked(prefs.getBoolean("lpShare", true));

        lpFuture = (RadioButton) findViewById(R.id.longClicFuture);
        lpFuture.setChecked(prefs.getBoolean("lpFuture", false));

        autoUpdate = (CheckBox) findViewById(R.id.checkBox0);
        autoUpdate.setChecked(prefs.getBoolean("autoUpdate", false));

        rtcMode = (CheckBox) findViewById(R.id.chk_rtc);
        rtcMode.setChecked(prefs.getBoolean("rtcMode", false));

        ratioAlert = (CheckBox) findViewById(R.id.checkBox1);
        ratioAlert.setChecked(prefs.getBoolean("ratioAlert", false));

        minimum = (EditText) findViewById(R.id.editText1);
        minimum.setText(prefs.getString("ratioMinimum", "1"));

        targetR = (EditText) findViewById(R.id.EditText01);
        targetR.setText(prefs.getString("ratioCible", "1"));

        mailAlert = (CheckBox) findViewById(R.id.checkBox2);
        mailAlert.setChecked(prefs.getBoolean("mailAlert", false));

        frequency = (EditText) findViewById(R.id.field_frequency);
        frequency.setText(prefs.getString("updateFreq", Default.UpdateFreq));

        /*notifAlert = (CheckBox) findViewById(R.id.checkBox3);
        notifAlert.setChecked(prefs.getBoolean("updAlert", false));*/

        /*
        noAds = (CheckBox) findViewById(R.id.chk_removeAds);
		noAds.setChecked(prefs.getBoolean("removeAds", false));
        */

        hadopi = (CheckBox) findViewById(R.id.hadopi);
        hadopi.setChecked(prefs.getBoolean("hadopi", false));

        wifiOnly = (CheckBox) findViewById(R.id.chk_wifiOnly);
        wifiOnly.setChecked(prefs.getBoolean("wifiOnly", false));

        /*devPreview = (CheckBox) findViewById(R.id.CheckBoxPreview);
        devPreview.setChecked(prefs.getBoolean("devPreview", false));*/

        useHTTPS = (CheckBox) findViewById(R.id.chk_HTTPS);
        useHTTPS.setChecked(prefs.getBoolean("useHTTPS", false));
    }
}