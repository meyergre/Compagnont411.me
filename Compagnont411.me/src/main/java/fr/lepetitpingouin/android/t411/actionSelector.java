package fr.lepetitpingouin.android.t411;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class actionSelector extends Activity {

    private LinearLayout btnOpen;
    private LinearLayout btnUpdate;
    private LinearLayout btnInbox;
    private LinearLayout btnWeb;
    private LinearLayout back;

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);

        back = (LinearLayout) findViewById(R.id.popupBg);
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnOpen = (LinearLayout) findViewById(R.id.selectorOpen);
        btnOpen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(actionSelector.this,
                        MainActivity2.class);
                actionSelector.this.startActivity(myIntent);
                finish();
            }
        });

        btnUpdate = (LinearLayout) findViewById(R.id.selectorUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    stopService(new Intent(actionSelector.this,
                            t411UpdateService.class));
                } catch (Exception ex) {

                }
                startService(new Intent(actionSelector.this, t411UpdateService.class));
                finish();
            }
        });

        btnInbox = (LinearLayout) findViewById(R.id.selectorInbox);
        btnInbox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(actionSelector.this,
                        messagesActivity.class);
                actionSelector.this.startActivity(myIntent);
            }
        });

        btnWeb = (LinearLayout) findViewById(R.id.selectorWeb);
        btnWeb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = Default.URL_INDEX;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                finish();
            }
        });
    }
}
