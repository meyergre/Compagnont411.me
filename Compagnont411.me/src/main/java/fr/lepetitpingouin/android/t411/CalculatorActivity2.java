package fr.lepetitpingouin.android.t411;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by gregory on 28/01/2017.
 */

public class CalculatorActivity2 extends AppCompatActivity {

    SharedPreferences prefs;
    FloatingActionButton fab_refresh;
    TextView ratio, upload, download, ratioMin, ratioMax, upSpeedResult;

    Float ratioValue;
    Double uploadValue, downloadValue;

    LinearLayout upView, dlView;

    Spinner uniteDl, uniteUp, uniteConn;

    EditText simuUp, simuDl, simuConn;

    Integer upMultiplier = 1, dlMultiplier = 1, connspeedMultiplier = 1;

    View circleRatio;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator2);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        getSupportActionBar().setTitle(getResources().getString(R.string.calculator));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        circleRatio = findViewById(R.id.circle_ratio);

        ratio = (TextView)findViewById(R.id.tv_ratio);
        download = (TextView)findViewById(R.id.tv_download);
        upload = (TextView)findViewById(R.id.tv_upload);
        fab_refresh = (FloatingActionButton)findViewById(R.id.fab_refresh);

        upView = (LinearLayout)findViewById(R.id.layout_upload);
        dlView = (LinearLayout)findViewById(R.id.layout_download);

        uniteUp = (Spinner)findViewById(R.id.uniteup);
        uniteDl = (Spinner)findViewById(R.id.unitedl);
        uniteConn = (Spinner)findViewById(R.id.uniteconn);

        simuUp = (EditText)findViewById(R.id.et_qtup);
        simuDl = (EditText)findViewById(R.id.et_qtdl);
        simuConn = (EditText)findViewById(R.id.et_qtconn);

        ratioMin = (TextView)findViewById(R.id.ratioMin);
        ratioMax = (TextView)findViewById(R.id.ratioMax);

        upSpeedResult = (TextView)findViewById(R.id.upspeedResult);

        ratioMin.setText(String.format("%.2f", Float.valueOf(prefs.getString("ratioMinimum", "0.00").replace(",","."))));
        ratioMax.setText(String.format("%.2f", Float.valueOf(prefs.getString("ratioCible", "0.00").replace(",","."))));

        simuConn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateSimu();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        simuUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(s.length() != 0) {
                    calculateData();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        simuDl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(s.length() != 0) {
                    calculateData();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        uniteUp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(((TextView)view).getText().equals("MB")) {
                    upMultiplier = 1;
                }
                if(((TextView)view).getText().equals("GB")) {
                    upMultiplier = 1024;
                }
                if(((TextView)view).getText().equals("TB")) {
                    upMultiplier = 1048576;
                }
                calculateData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                upMultiplier = 1;
                calculateData();
            }
        });
        uniteDl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(((TextView)view).getText().equals("MB")) {
                    dlMultiplier = 1;
                }
                if(((TextView)view).getText().equals("GB")) {
                    dlMultiplier = 1024;
                }
                if(((TextView)view).getText().equals("TB")) {
                    dlMultiplier = 1048576;
                }
                calculateData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                dlMultiplier = 1;
                calculateData();
            }
        });

        uniteConn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(((TextView)view).getText().equals("KB/s")) {
                    connspeedMultiplier = 1;
                }
                if(((TextView)view).getText().equals("MB/s")) {
                    connspeedMultiplier = 1024;
                }
                if(((TextView)view).getText().equals("GB/s")) {
                    connspeedMultiplier = 1048576;
                }
                updateSimu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                connspeedMultiplier = 1;
                updateSimu();
            }
        });


        fab_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshData();
            }
        });

        refreshData();
    }

    private void updateSimu() {
        try {

            int iBis = (Integer.parseInt(simuConn.getText().toString()) * connspeedMultiplier);
            double bytesToUp = new BSize(prefs.getString("UpLeft", "0 B")).getInKB();

            if (iBis == 0) {
                upSpeedResult.setText("" + iBis);
                return;
            }

            int sec = (int) bytesToUp / iBis;
            int min = sec / 60;
            sec = sec - (min * 60);
            int hrs = min / 60;
            min = min - (hrs * 60);
            int jrs = hrs / 24;
            hrs = hrs - (jrs * 24);
            int sem = jrs / 7;
            jrs = jrs - (sem * 7);
            int moi = Double.valueOf(sem / 4.33).intValue();
            sem = sem - (Double.valueOf(moi * 4.33).intValue());
            int ann = moi / 12;
            moi = moi - (ann * 12);

            String result = "~";
            if (ann > 0)
                result += " " + ann + " " + getApplicationContext().getString(R.string.year) + ((ann > 1) ? "s" : "");
            if (moi > 0)
                result += " " + moi + " " + getApplicationContext().getString(R.string.month) + ((moi > 1 && !getApplicationContext().getString(R.string.month).endsWith("s")) ? "s" : "");
            if (sem > 0)
                result += " " + sem + " " + getApplicationContext().getString(R.string.week) + ((sem > 1) ? "s" : "");
            if (jrs > 0)
                result += " " + jrs + " " + getApplicationContext().getString(R.string.day) + ((jrs > 1) ? "s" : "");
            if (hrs > 0)
                result += " " + hrs + " " + getApplicationContext().getString(R.string.hour) + ((hrs > 1) ? "s" : "");
            if (min > 0)
                result += " " + min + " " + getApplicationContext().getString(R.string.minute) + ((min > 1) ? "s" : "");

            if (result.equals("~")) upSpeedResult.setText("");
            else
                upSpeedResult.setText(getString(R.string.UpLeftString).replace("%s", result).replace("%d", String.format("%.2f", Float.valueOf(prefs.getString("ratioCible", "0.00").replace(",", ".")))));
        } catch(Exception e) {

        }
    }

    private void refreshData() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ratioValue = Float.valueOf(prefs.getString("lastRatio", "0").replace(",","."));
        downloadValue = new BSize(prefs.getString("lastDownload", "0 MB")).getInMB();
        uploadValue = new BSize(prefs.getString("lastUpload", "0 MB")).getInMB();

        simuUp.setText("0");
        simuDl.setText("0");

        ratio.setText(String.format("%.2f", (ratioValue>0?ratioValue:"∞")));
        download.setText(prefs.getString("lastDownload", "0 GB"));
        upload.setText(prefs.getString("lastUpload", "0 GB"));

        animateRatioChange();
    }

    private void calculateData() {

        try {
            downloadValue = new BSize(prefs.getString("lastDownload", "0 GB")).getInMB();
            uploadValue = new BSize(prefs.getString("lastUpload", "0 GB")).getInMB();

            downloadValue += (Integer.parseInt(simuDl.getText().toString()) * dlMultiplier);
            uploadValue += (Integer.parseInt(simuUp.getText().toString()) * upMultiplier);

            download.setText(new BSize(String.valueOf(downloadValue) + " MB").getInAuto());
            upload.setText(new BSize(String.valueOf(uploadValue) + " MB").getInAuto());

            ratio.setText(String.format("%.2f", uploadValue / downloadValue));

            animateRatioChange();
        } catch(Exception e) {

        }
    }

    private void animateRatioChange() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                circleRatio.getBackground().setTint(getResources().getColor(R.color.t411_action_blue));

                // Si < limite basse, rouge
                if(uploadValue/downloadValue < 1 || uploadValue/downloadValue < Float.valueOf(prefs.getString("ratioMinimum", "1.00")))
                    circleRatio.getBackground().setTint(getResources().getColor(R.color.t411_red));

            }

            int range = 50;

            upView.animate()
                    .translationY(0)
                    .setDuration(1000)
                    .start();


            dlView.animate()
                    .translationY(0)
                    .setDuration(1000)
                    .start();

            upView.animate()
                .translationY(uploadValue>downloadValue?-range:range)
                .setDuration(1000)
                .start();


            dlView.animate()
                .translationY((uploadValue>downloadValue?range:-range))
                .setDuration(1000)
                .start();
        }
    }


}
