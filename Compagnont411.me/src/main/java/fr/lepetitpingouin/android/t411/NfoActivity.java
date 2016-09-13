package fr.lepetitpingouin.android.t411;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class NfoActivity extends AppCompatActivity {

    private TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfo);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("NFO");
        getSupportActionBar().setIcon(R.drawable.ic_nfo_file);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv = (TextView) findViewById(R.id.nfo_textview);
        tv.setText(getIntent().getStringExtra("nfo"));
        tv.setMovementMethod(new ScrollingMovementMethod());
    }
}
