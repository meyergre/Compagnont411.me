package fr.lepetitpingouin.android.t411;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;

public class Widget_Plus extends AppWidgetProvider {

    private Date date = new Date();
    private Intent myIntent = new Intent();
    private PendingIntent pIntent;
    private SharedPreferences prefs;

    private int LED_T411 = R.drawable.led_off;
    private int LED_Net = R.drawable.led_off;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_plus);

            views.setImageViewResource(R.id.hLED_T411, LED_T411);
            views.setImageViewResource(R.id.hLED_NETWORK, LED_Net);

            date = new Date();

            try {


                prefs = PreferenceManager.getDefaultSharedPreferences(context);
                myIntent.setClassName("fr.lepetitpingouin.android.t411", "fr.lepetitpingouin.android.t411.MainActivity");
                pIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

                views.setOnClickPendingIntent(R.id.nw_icon, pIntent);
            } catch (Exception ex) {

            }

            views.setOnClickPendingIntent(R.id.wbtn_config, PendingIntent.getActivity(context.getApplicationContext(), widgetId, new Intent(context.getApplicationContext(), UserPrefsActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

            views.setOnClickPendingIntent(R.id.wbtn_refresh, PendingIntent.getService(context.getApplicationContext(), widgetId, new Intent(context.getApplicationContext(), t411UpdateService.class), PendingIntent.FLAG_UPDATE_CURRENT));

            views.setOnClickPendingIntent(R.id.wbtn_search, PendingIntent.getActivity(context.getApplicationContext(), widgetId, new Intent(context.getApplicationContext(), SearchActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));


            views.setTextViewText(R.id.updatedTime, prefs.getString("lastDate", "?????"));
            views.setTextViewText(R.id.wUpload, new BSize(prefs.getString("lastUpload", "0.00")).convert());
            views.setTextViewText(R.id.wDownload, new BSize(prefs.getString("lastDownload", "0.00")).convert());
            views.setTextViewText(R.id.wMails, String.valueOf(prefs.getInt("lastMails", 0)));
            views.setTextViewText(R.id.wRatio, String.format("%.2f", Double.valueOf(prefs.getString("lastRatio", "0.00").replace(",","."))));
            views.setTextViewText(R.id.wUsername, prefs.getString("lastUsername", "Anonymous"));

            views.setTextViewText(R.id.wGoLeft, prefs.getString("GoLeft", "0 GB"));
            views.setTextViewText(R.id.wUpLeft, prefs.getString("UpLeft", "0 GB"));

            views.setTextViewText(R.id.hUP24, prefs.getString("up24", "..."));
            views.setTextViewText(R.id.dl24, prefs.getString("dl24", "..."));

            String classe = prefs.getString("classe", "???");
            String titre = prefs.getString("titre", "");
            String status = " (" + classe + ((titre.length() > 1) ? ", " + titre : "") + ")";

            views.setTextViewText(R.id.wClasse, status);

            views.setImageViewResource(R.id.topLogo, R.drawable.t411_search_icon);

            views.setImageViewBitmap(R.id.topLogo, new AvatarFactory().getFromPrefs(prefs));

            if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.DECEMBER
                    && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > 0
                    && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) < 23)
                views.setImageViewResource(R.id.topLogo, R.drawable.ic_xmastree);

            if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.DECEMBER
                    && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > 22
                    && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) < 27)
                views.setImageViewResource(R.id.topLogo, R.drawable.ic_xmas);

            /*if (prefs.getBoolean("useHTTPS", false)) {
                views.setImageViewResource(R.id.topLogo, R.drawable.ic_padlock);
            }*/

            views.setTextColor(R.id.wUsername, context.getResources().getColor(R.color.t411_blue));
            views.setTextColor(R.id.wUsername, new Ratio(context).getTitleColor());
            views.setImageViewResource(R.id.wSmiley, new Ratio(context).getSmiley());

            appWidgetManager.updateAppWidget(widgetId, views);

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), Widget_Plus.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        if (intent.getAction().equals(Default.Appwidget_flag_updating)) {
            for (int widgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(),
                        R.layout.widget_plus);
                views.setImageViewResource(R.id.hLED_T411, R.drawable.led_blue);
                views.setImageViewResource(R.id.hLED_NETWORK,
                        R.drawable.led_blue);
                appWidgetManager.updateAppWidget(widgetId, views);
            }
        } else {
            try {

                this.LED_T411 = (intent.getBooleanExtra("LED_T411", true)) ? R.drawable.led_off
                        : R.drawable.led_orange;
                this.LED_Net = (intent.getBooleanExtra("LED_Net", true)) ? R.drawable.led_off
                        : R.drawable.led_orange;
            } catch (Exception ex) {
            }
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }
}
