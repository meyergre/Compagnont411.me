package fr.lepetitpingouin.android.t411;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;

public class Widget_Full extends AppWidgetProvider {

    Date date = new Date();
    Intent myIntent = new Intent();
    PendingIntent pIntent;
    SharedPreferences prefs;

    int LED_T411 = R.drawable.led_off, LED_Net = R.drawable.led_off;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {


        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_full);
            if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.DECEMBER
                    && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > 22
                    && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) < 27)
                views.setImageViewResource(R.id.topLogo, R.drawable.ic_xmas);

            if (Calendar.getInstance().get(Calendar.MONTH) == Calendar.DECEMBER
                    && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > 0
                    && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) < 23)
                views.setImageViewResource(R.id.topLogo, R.drawable.ic_xmastree);

            views.setImageViewResource(R.id.hLED_T411, LED_T411);
            views.setImageViewResource(R.id.hLED_NETWORK, LED_Net);

            date = new Date();

            try {
                Log.v("widget t411", "D�finition de l'Intent");

                prefs = PreferenceManager.getDefaultSharedPreferences(context);

                switch (prefs.getInt("widgetAction", 5)) {
                    case 0:
                        myIntent.setClassName("fr.lepetitpingouin.android.t411",
                                "fr.lepetitpingouin.android.t411.MainActivity");
                        pIntent = PendingIntent
                                .getActivity(context, 0, myIntent, 0);
                        break;
                    case 1:
                        myIntent.setClassName("fr.lepetitpingouin.android.t411",
                                "fr.lepetitpingouin.android.t411.t411UpdateService");
                        pIntent = PendingIntent.getService(context, 0, myIntent, 0);
                        break;
                    case 2:
                        break;
                    case 3:
                        String url = "http://www.t411.me";
                        myIntent = new Intent(Intent.ACTION_VIEW);
                        myIntent.setData(Uri.parse(url));
                        pIntent = PendingIntent
                                .getActivity(context, 0, myIntent, 0);
                        break;
                    default:
                        myIntent.setClassName("fr.lepetitpingouin.android.t411",
                                "fr.lepetitpingouin.android.t411.actionSelector");
                        pIntent = PendingIntent
                                .getActivity(context, 0, myIntent, 0);
                        break;
                }
                views.setOnClickPendingIntent(R.id.topLogo, pIntent);
            } catch (Exception ex) {
                Log.e("widget t411 - lancement de l'Intent", ex.toString());
            }

            Log.v("widget t411", "mise � jour des valeurs");
            views.setTextViewText(R.id.updatedTime, prefs.getString("lastDate", "?????"));
            views.setTextViewText(R.id.wUpload, new BSize(prefs.getString("lastUpload", "0.00")).convert());
            views.setTextViewText(R.id.wDownload, new BSize(prefs.getString("lastDownload", "0.00")).convert());
            views.setTextViewText(R.id.wMails, String.valueOf(prefs.getInt("lastMails", 0)));
            views.setTextViewText(R.id.wRatio, String.format("%.2f", Double.valueOf(prefs.getString("lastRatio", "0.00"))));
            views.setTextViewText(R.id.wUsername, prefs.getString("lastUsername", "Anonymous"));

            views.setTextColor(R.id.wUsername, new Ratio(context).getTitleColor());

            views.setImageViewResource(R.id.topLogo, R.drawable.t411_search_icon);

            if (prefs.getBoolean("useHTTPS", false)) {
                views.setImageViewResource(R.id.topLogo, R.drawable.ic_padlock);
            }

            views.setImageViewResource(R.id.wSmiley, new Ratio(context).getSmiley());
            appWidgetManager.updateAppWidget(widgetId, views);

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), Widget_Full.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        if (intent.getAction().equals(Default.Appwidget_flag_updating)) {
            for (int widgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(),
                        R.layout.widget_full);
                views.setImageViewResource(R.id.hLED_T411, R.drawable.led_blue);
                views.setImageViewResource(R.id.hLED_NETWORK,
                        R.drawable.led_blue);
                appWidgetManager.updateAppWidget(widgetId, views);
            }
        } else {
            try {
                this.LED_T411 = (intent.getBooleanExtra("LED_T411", false)) ? R.drawable.led_off : R.drawable.led_orange;
                this.LED_Net = (intent.getBooleanExtra("LED_Net", false)) ? R.drawable.led_off : R.drawable.led_orange;
            } catch (Exception ex) {
            }
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }
}
