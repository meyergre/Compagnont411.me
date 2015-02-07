package fr.lepetitpingouin.android.t411;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class CalculatorActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        getSupportActionBar().setIcon(R.drawable.ic_calculator);
        getSupportActionBar().setTitle(getResources().getString(R.string.calculator));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        final SeekBar upSeek = (SeekBar) findViewById(R.id.seekBar_uploadSimu);
        final SeekBar dlSeek = (SeekBar) findViewById(R.id.seekBar_downloadSimu);
        final SeekBar upspeedSeek = (SeekBar) findViewById(R.id.seekBarUpSpeed);
        final TextView upSpeedResult = (TextView) findViewById(R.id.upSpeedResult);
        Button RaZ_simu = (Button) findViewById(R.id.calc_simu_raz);
        Button RaZ_upspeed = (Button) findViewById(R.id.simulation_raz_seedduration);
        final TextView upSeekText = (TextView) findViewById(R.id.Text_uploadSimu);
        final TextView dlSeekText = (TextView) findViewById(R.id.Text_downloadSimu);
        final TextView upspeedText = (TextView) findViewById(R.id.upspeedsimu);
        TextView txtDLLeft = (TextView) findViewById(R.id.calc_dlleft);
        TextView txtUPLeft = (TextView) findViewById(R.id.calc_upleft);
        TextView txtRatio = (TextView) findViewById(R.id.calc_ratio);
        TextView txtTargetRatio = (TextView) findViewById(R.id.calc_targetratio);
        TextView txtLowRatio = (TextView) findViewById(R.id.calc_lowratio);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final TextView ratioOriginal = (TextView) findViewById(R.id.calc_ratio1);
        final TextView ratioEstimated = (TextView) findViewById(R.id.calc_ratio2);

        txtDLLeft.setText(prefs.getString("GoLeft", "??"));
        txtUPLeft.setText(prefs.getString("UpLeft", "??"));
        ratioOriginal.setText(String.format("%.2f", Float.valueOf(prefs.getString("lastRatio", "00"))));
        ratioEstimated.setText(ratioOriginal.getText());

        txtRatio.setText(String.format("%.2f", Float.valueOf(prefs.getString("lastRatio", "0.00"))));

        txtLowRatio.setText(String.format("%.2f", Float.valueOf(prefs.getString("ratioMinimum", "0.00"))));
        txtTargetRatio.setText(String.format("%.2f", Float.valueOf(prefs.getString("ratioCible", "0.00"))));

        RaZ_simu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upSeek.setProgress(0);
                upSeekText.setText(new BSize("0 MB").convert());
                dlSeek.setProgress(0);
                dlSeekText.setText(new BSize("0 MB").convert());
                ratioEstimated.setText(ratioOriginal.getText());
            }

        });

        RaZ_upspeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upspeedSeek.setProgress(0);
                upspeedText.setText(new BSize("1 KB").convert() + "/s");
                upSpeedResult.setText("");
            }

        });

        upSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                upSeekText.setText(i < 1024 ? i + " MB" : (int) Math.ceil((i - 1024) / 10) + 1 + " GB");
                double esti = (new BSize(prefs.getString("lastUpload", "? 0.00 GB")).getInBytes() + new BSize(upSeekText.getText().toString()).getInBytes()) / (new BSize(prefs.getString("lastDownload", "? 0.00 GB")).getInBytes() + new BSize(dlSeekText.getText().toString()).getInBytes());
                //esti = esti > Float.valueOf(ratioOriginal.getText().toString().replace(",","."))?Float.valueOf(ratioOriginal.getText().toString().replace(",",".")):esti;
                ratioEstimated.setText(String.format("%.2f", esti));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        upspeedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                int iBis = i + 1;
                double bytesToUp = new BSize(prefs.getString("UpLeft", "0 B")).getInKB();

                upspeedText.setText(iBis + " KB/s");

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
                result += ".";

                upSpeedResult.setText(result);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        dlSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                dlSeekText.setText(i < 1024 ? i + " MB" : (int) Math.ceil((i - 1024) / 10) + 1 + " GB");
                double esti = (new BSize(prefs.getString("lastUpload", "? 0.00 GB")).getInBytes() + new BSize(upSeekText.getText().toString()).getInBytes()) / (new BSize(prefs.getString("lastDownload", "? 0.00 GB")).getInBytes() + new BSize(dlSeekText.getText().toString()).getInBytes()) - 0.0051;
                //esti = esti > Float.valueOf(ratioOriginal.getText().toString().replace(",","."))?Float.valueOf(ratioOriginal.getText().toString().replace(",",".")):esti;
                ratioEstimated.setText(String.format("%.2f", esti));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
