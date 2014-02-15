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

public class Widget_nano extends AppWidgetProvider {

    Intent myIntent = new Intent();
    PendingIntent pIntent;
    SharedPreferences prefs;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_mini);

            try {
                Log.v("widget t411", "D�finition de l'Intent");

                prefs = PreferenceManager.getDefaultSharedPreferences(context);

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

            Log.v("widget t411", "mise � jour des valeurs");

            views.setTextViewText(R.id.updatedTime, prefs.getString("lastDate", "?????"));
            views.setTextViewText(R.id.wMails, String.valueOf(prefs.getInt("lastMails", 0)));
            views.setTextViewText(R.id.wRatio, String.format("%.2f", Double.valueOf(prefs.getString("lastRatio", "0.00"))));

            views.setImageViewResource(R.id.wSmiley, new Ratio(context).getSmiley());

            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(context);
        ComponentName thisAppWidget = new ComponentName(
                context.getPackageName(), Widget_nano.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
