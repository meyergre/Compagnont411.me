package fr.lepetitpingouin.android.t411;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class Widget_huge extends AppWidgetProvider {

    private String ratio;
    private String upload;
    private String download;
    private String mails;
    private String username;
    private String origusername;
    private String _ratio;
    private String _upload;
    private String _download;
    private String _mails;
    private String _username;
    private Date date = new Date();
    private Intent myIntent = new Intent();
    private PendingIntent pIntent;
    private SharedPreferences prefs;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        AlarmManager alarmManager;
        Intent intent = new Intent(Default.Appwidget_clock_update);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, 0);

        alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.SECOND, 59);
        alarmManager
                .set(AlarmManager.RTC, cal.getTimeInMillis(), pendingIntent);

        //final int N = appWidgetIds.length;

        username = context.getString(R.string.waiting_for_update);
        origusername = username;

        // loop through all app widgets the user has enabled
        for (int widgetId : appWidgetIds) {
            // get our view so we can edit the time
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_huge);
            date = new Date();

            try {


                ratio = (ratio == null) ? prefs.getString("lastRatio", "0.00")
                        : ratio;
                upload = (upload == null) ? prefs.getString("lastUpload",
                        "  ???.?? MB") : upload;
                download = (download == null) ? prefs.getString("lastDownload",
                        "  ???.?? MB") : download;
                mails = (mails == null) ? String.valueOf(prefs.getInt(
                        "lastMails", 0)) : mails;
                username = (username == null || username.equals(origusername)) ? prefs
                        .getString("lastUsername", origusername) : username;
                username = (username == null) ? String.valueOf(prefs.getInt(
                        "lastUsername", 0)) : username;

                String[] choices = context.getResources().getStringArray(R.array.widget_actions);

                if (prefs.getString("widgetAction", "").equals(choices[0])) {
                    myIntent.setClassName("fr.lepetitpingouin.android.t411", "fr.lepetitpingouin.android.t411.MainActivity");
                    pIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

                } else if (prefs.getString("widgetAction", "").equals(choices[1])) {
                    myIntent.setClassName("fr.lepetitpingouin.android.t411", "fr.lepetitpingouin.android.t411.t411UpdateService");
                    pIntent = PendingIntent.getService(context, 0, myIntent, 0);

                } else if (prefs.getString("widgetAction", "").equals(choices[2])) {
                    myIntent.setClass(context.getApplicationContext(), messagesActivity.class);
                    pIntent = PendingIntent.getActivity(context, 0, myIntent, 0);
                } else if (prefs.getString("widgetAction", "").equals(choices[3])) {
                    String url = "http://www.t411.me";
                    myIntent = new Intent(Intent.ACTION_VIEW);
                    myIntent.setData(Uri.parse(url));
                    pIntent = PendingIntent.getActivity(context, 0, myIntent, 0);
                } else {
                    myIntent.setClassName("fr.lepetitpingouin.android.t411",
                            "fr.lepetitpingouin.android.t411.actionSelector");
                    pIntent = PendingIntent.getActivity(context, 0, myIntent, 0);
                }

                views.setOnClickPendingIntent(R.id.topLogo, pIntent);
            } catch (Exception ex) {

            }

            try {
                PackageManager packageManager = context.getPackageManager();
                Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER);

                // Verify clock implementation
                String clockImpls[][] = {
                        {"HTC Alarm Clock", "com.htc.android.worldclock",
                                "com.htc.android.worldclock.WorldClockTabControl"},
                        {"Standar Alarm Clock", "com.android.deskclock",
                                "com.android.deskclock.AlarmClock"},
                        {"Froyo Nexus Alarm Clock",
                                "com.google.android.deskclock",
                                "com.android.deskclock.DeskClock"},
                        {"Moto Blur Alarm Clock",
                                "com.motorola.blur.alarmclock",
                                "com.motorola.blur.alarmclock.AlarmClock"},
                        {"Samsung Galaxy Clock",
                                "com.sec.android.app.clockpackage",
                                "com.sec.android.app.clockpackage.ClockPackage"}};

                boolean foundClockImpl = false;

                for (String[] clockImpl : clockImpls) {
                    String vendor = clockImpl[0];
                    String packageName = clockImpl[1];
                    String className = clockImpl[2];
                    try {
                        ComponentName cn = new ComponentName(packageName, className);
                        //ActivityInfo aInfo = packageManager.getActivityInfo(cn,PackageManager.GET_META_DATA);
                        alarmClockIntent.setComponent(cn);

                        foundClockImpl = true;
                    } catch (Exception e) {

                    }
                }

                if (foundClockImpl) {
                    PendingIntent clockFoundPIntent = PendingIntent
                            .getActivity(context, 0, alarmClockIntent, 0);
                    // add pending intent to your component
                    views.setOnClickPendingIntent(R.id.wMinutes,
                            clockFoundPIntent);
                }
            } catch (Exception ex) {

            }

            views.setOnClickPendingIntent(R.id.wHour, PendingIntent
                    .getBroadcast(context, 0, new Intent(
                            Default.Appwidget_clock_update), 0));


            views.setTextViewText(R.id.updatedTime,
                    prefs.getString("lastDate", "?????"));
            views.setTextViewText(R.id.wUpload, upload);
            views.setTextViewText(R.id.wDownload, download);
            views.setTextViewText(R.id.wMails, mails);
            views.setTextViewText(R.id.wRatio, String.format("%.2f", Double.valueOf(ratio)).replace(",","."));
            views.setTextViewText(R.id.wUsername, username);

            // updating time
            views.setTextViewText(
                    R.id.wHour,
                    String.valueOf(Calendar.getInstance().get(
                            Calendar.HOUR_OF_DAY)));

            int minutes = Calendar.getInstance().get(Calendar.MINUTE);

            String sMinutes = (minutes < 10) ? "0" + minutes : String
                    .valueOf(minutes);
            views.setTextViewText(R.id.wMinutes, ":" + sMinutes);

            String sDate = DateFormat.getDateInstance(DateFormat.FULL).format(
                    date);

            views.setTextViewText(R.id.wDate, sDate.toUpperCase());


            views.setImageViewResource(R.id.wSmiley, new Ratio(context).getSmiley());


            // update the widget
            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            username = context.getString(R.string.waiting_for_update);
            origusername = username;
        } catch (Exception e) {
        }

        try {
            _ratio = intent.getStringExtra("ratio");
            ratio = (_ratio != null) ? _ratio : ratio;
            _upload = intent.getStringExtra("upload");
            upload = (_upload != null) ? _upload : upload;
            _download = intent.getStringExtra("download");
            download = (_download != null) ? _download : download;
            _mails = intent.getStringExtra("mails");
            mails = (_mails != null) ? _mails : mails;
            _username = intent.getStringExtra("username");
            username = (_username != null) ? _username : username;
        } catch (Exception ex) {

        }

        AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(context);
        ComponentName thisAppWidget = new ComponentName(
                context.getPackageName(), Widget_huge.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
