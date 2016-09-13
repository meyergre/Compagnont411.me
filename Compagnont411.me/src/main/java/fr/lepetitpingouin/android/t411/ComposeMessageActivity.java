package fr.lepetitpingouin.android.t411;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ComposeMessageActivity extends AppCompatActivity {

    private Button Bold;
    private Button Italic;
    private Button Underline;
    private ImageView list;
    private ImageView spoil;
    private ImageView quote;
    private ImageView link;
    private ImageView img;
    private ImageView send;
    private EditText destinataire;
    private EditText objet;
    private EditText _message;
    private SharedPreferences prefs;
    private mailSender mS;
    private ProgressDialog dialog;

    private SuperT411HttpBrowser browser;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_compose);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.messages_compose));

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        _message = (EditText) findViewById(R.id.editTextMessage);
        destinataire = (EditText) findViewById(R.id.composeTo);
        objet = (EditText) findViewById(R.id.composeSubject);

        destinataire.setText(getIntent().getStringExtra("to"));
        objet.setText(getIntent().getStringExtra("subject"));

        if (getIntent().getStringExtra("msg") != null)
            _message.setText("\n\n----- Message d'origine -----\n\n" + getIntent().getStringExtra("msg"));

        Bold = (Button) findViewById(R.id.txtctrl_bold);
        Bold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                append("[b]");
                Toast.makeText(ComposeMessageActivity.this, getApplicationContext().getString(R.string.longpress_closetag), Toast.LENGTH_SHORT).show();
            }
        });
        Bold.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                append("[/b]");
                return true;
            }
        });

        Italic = (Button) findViewById(R.id.txtctrl_italic);
        Italic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                append("[i]");
                Toast.makeText(ComposeMessageActivity.this, getApplicationContext().getString(R.string.longpress_closetag), Toast.LENGTH_SHORT).show();
            }
        });
        Italic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                append("[/i]");
                return true;
            }
        });

        Underline = (Button) findViewById(R.id.txtctrl_underlined);
        Underline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                append("[u]");
                Toast.makeText(ComposeMessageActivity.this, getApplicationContext().getString(R.string.longpress_closetag), Toast.LENGTH_SHORT).show();
            }
        });
        Underline.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                append("[/u]");
                return true;
            }
        });

        list = (ImageView) findViewById(R.id.txtctrl_list);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                append("\n" + "[list]\n" + "[*]\n" + "[*]\n" + "[/list]\n");
            }
        });

        spoil = (ImageView) findViewById(R.id.txtctrl_spoiler);
        spoil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                append("[hide]");
                Toast.makeText(ComposeMessageActivity.this, getApplicationContext().getString(R.string.longpress_closetag), Toast.LENGTH_SHORT).show();
            }
        });
        spoil.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                append("[/hide]");
                return true;
            }
        });

        link = (ImageView) findViewById(R.id.txtctrl_link);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                append("[url]");
                Toast.makeText(ComposeMessageActivity.this, getApplicationContext().getString(R.string.longpress_closetag), Toast.LENGTH_SHORT).show();
            }
        });
        link.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                append("[/url]");
                return true;
            }
        });

        img = (ImageView) findViewById(R.id.txtctrl_img);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                append("[img]");
                Toast.makeText(ComposeMessageActivity.this, getApplicationContext().getString(R.string.longpress_closetag), Toast.LENGTH_SHORT).show();
            }
        });
        img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                append("[/img]");
                return true;
            }
        });

        quote = (ImageView) findViewById(R.id.txtctrl_quote);
        quote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                append("[quote]");
                Toast.makeText(ComposeMessageActivity.this, getApplicationContext().getString(R.string.longpress_closetag), Toast.LENGTH_SHORT).show();
            }
        });
        quote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                append("[/quote]");
                return true;
            }
        });

        send = (ImageView) findViewById(R.id.btn_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (destinataire.getText().length() > 0) {
                    mS = new mailSender();
                    dialog = ProgressDialog.show(ComposeMessageActivity.this,
                            ComposeMessageActivity.this.getString(R.string.sentMessage),
                            ComposeMessageActivity.this.getString(R.string.pleasewait), true, false);
                    try {
                        mS.execute();
                    } catch (Exception e) {
                        mS = null;
                    }
                } else
                    Toast.makeText(ComposeMessageActivity.this, "Destinataire non renseigné", Toast.LENGTH_SHORT).show();
            }
        });

        _message.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
    }

    private void append(String string) {
        int start = _message.getSelectionStart();
        int end = _message.getSelectionEnd();
        _message.getText().replace(Math.min(start, end), Math.max(start, end),
                string, 0, string.length());
    }

    private String htmlEncode(String value) {
        value = value.replaceAll("\n", "#BR#");
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN)
            value = Html.escapeHtml(value);
        value = value.replaceAll("#BR#", "\n");

        return value;
    }

    class mailSender extends AsyncTask<Void, Void, Void> {

        String to, subject, message, value;

        @Override
        protected void onPreExecute() {
            to = destinataire.getText().toString();

            subject = (objet.getText().toString());
            message = htmlEncode(_message.getText().toString());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String username = prefs.getString("login", ""), password = prefs
                    .getString("password", "");

            Connection.Response res = null;
            Document doc = null;

            try {
                /* res = Jsoup
                        .connect(Default.URL_LOGIN)
                        .data("login", username, "password", password)
                        .method(Connection.Method.POST)
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .execute();

                Map<String, String> Cookies = res.cookies();

                res = Jsoup
                        .connect(Default.URL_SENDMAIL)
                        .cookies(Cookies)
                        .data("receiverName", to, "subject", subject, "msg", message, "save", "1", "id", "", "receiver", "")
                        .timeout(Integer.valueOf(prefs.getString("timeoutValue", Default.timeout)) * 1000)
                        .maxBodySize(0).followRedirects(true).ignoreContentType(true)
                        .userAgent(prefs.getString("User-Agent", Default.USER_AGENT))
                        .method(Connection.Method.POST)
                        .execute();

                doc = res.parse(); */

                browser = new SuperT411HttpBrowser(getApplicationContext());

                doc = Jsoup.parse(browser.login(username, password)
                        .connect(Default.URL_SENDMAIL)
                        .addData("receiverName", to)
                        .addData("subject", subject)
                        .addData("msg", message)
                        .addData("save", "1")
                        .addData("id", "")
                        .addData("receiver", "")
                        .executeInAsyncTask());

                value = doc.select("#messages").first().text();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            if (!browser.getErrorMessage().equals(""))
                Toast.makeText(getApplicationContext(), browser.getErrorMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
