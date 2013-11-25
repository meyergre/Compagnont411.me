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
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class Widget_huge extends AppWidgetProvider {

    String ratio, upload, download, mails, username, origusername;
    String _ratio, _upload, _download, _mails, _username;
    Date date = new Date();
    Intent myIntent = new Intent();
    PendingIntent pIntent;
    SharedPreferences prefs;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.v("widget t411", "onUpdate");
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
                Log.v("widget t411", "D�finition de l'Intent");

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

                if(prefs.getString("widgetAction", "").equals(choices[0])) {
                    myIntent.setClassName("fr.lepetitpingouin.android.t411","fr.lepetitpingouin.android.t411.MainActivity");
                    pIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

                } else if(prefs.getString("widgetAction", "").equals(choices[1])) {
                    myIntent.setClassName("fr.lepetitpingouin.android.t411", "fr.lepetitpingouin.android.t411.t411UpdateService");
                    pIntent = PendingIntent.getService(context, 0, myIntent, 0);

                } else if(prefs.getString("widgetAction", "").equals(choices[2])) {
                    myIntent.setClass(context.getApplicationContext(), messagesActivity.class);
                    pIntent = PendingIntent.getActivity(context, 0, myIntent, 0);
                } else if(prefs.getString("widgetAction", "").equals(choices[3])) {
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
                Log.e("widget t411 - lancement de l'Intent", ex.toString());
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
                        ComponentName cn = new ComponentName(packageName,className);
                        //ActivityInfo aInfo = packageManager.getActivityInfo(cn,PackageManager.GET_META_DATA);
                        alarmClockIntent.setComponent(cn);
                        Log.d("", "Found " + vendor + " --> " + packageName + "/" + className);
                        foundClockImpl = true;
                    } catch (Exception e) {
                        Log.d("", vendor + " does not exists");
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
                Log.e("Clock exception :", ex.toString());
            }

            views.setOnClickPendingIntent(R.id.wHour, PendingIntent
                    .getBroadcast(context, 0, new Intent(
                            Default.Appwidget_clock_update), 0));

            Log.v("widget t411", "mise � jour des valeurs");
            views.setTextViewText(R.id.updatedTime,
                    prefs.getString("lastDate", "?????"));
            views.setTextViewText(R.id.wUpload, upload);
            views.setTextViewText(R.id.wDownload, download);
            views.setTextViewText(R.id.wMails, mails);
            views.setTextViewText(R.id.wRatio, String.format("%.2f", Double.valueOf(ratio)));
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

            Log.v("widget t411", "mise à jour du smiley");

            views.setImageViewResource(R.id.wSmiley, new Ratio(context).getSmiley());

            Log.v("widget t411", "refresh du widget");
            // update the widget
            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.v("widget t411",
                "onReceive a re�u le Broadcast Intent : " + intent.getAction());

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
            Log.e("mise � jour des donn�es dapuis le service", ex.toString());
        }
        Log.v("widget t411", "mise � jour forc�e...");
        AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(context);
        ComponentName thisAppWidget = new ComponentName(
                context.getPackageName(), Widget_huge.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
