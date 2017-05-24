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

import org.json.JSONArray;
import org.jsoup.Jsoup;

public class Widget_news extends AppWidgetProvider {

    private SharedPreferences prefs;
    private RemoteViews views;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //final int N = appWidgetIds.length;

        // loop through all app widgets the user has enabled
        for (int widgetId : appWidgetIds) {
            // get our view so we can edit the time
            views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_news);

            prefs = PreferenceManager.getDefaultSharedPreferences(context);

            JSONArray jarr = new JSONArray();
            String title1 = "", article1 = "", link1="", title2 = "", article2 = "", link2 = "", title3 = "" ,article3 = "", link3 = "";
            try {
                jarr = new JSONArray(prefs.getString("news", "[]"));
                if(jarr.length() > 0) {
                    title1 = jarr.getJSONObject(0).getString("title");
                    article1 = jarr.getJSONObject(0).getString("content");
                    link1 = jarr.getJSONObject(0).getString("link");
                }
                if(jarr.length() > 1) {
                    title2 = jarr.getJSONObject(1).getString("title");
                    article2 = jarr.getJSONObject(1).getString("content");
                    link2 = jarr.getJSONObject(1).getString("link");
                }
                if(jarr.length() > 2) {
                    title3 = jarr.getJSONObject(2).getString("title");
                    article3 = jarr.getJSONObject(2).getString("content");
                    link3 = jarr.getJSONObject(2).getString("link");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            views.setTextViewText(R.id.title1, title1);
            views.setTextViewText(R.id.article1, article1);
            views.setTextViewText(R.id.title2, title2);
            views.setTextViewText(R.id.article2, article2);
            views.setTextViewText(R.id.title3, title3);
            views.setTextViewText(R.id.article3, article3);



            views.setOnClickPendingIntent(R.id.article1, PendingIntent.getActivity(context, 0, new Intent(context, newsActivity.class).putExtra("url", link1), 0));
            views.setOnClickPendingIntent(R.id.article2, PendingIntent.getActivity(context, 0, new Intent(context, newsActivity.class).putExtra("url", link2), 0));
            views.setOnClickPendingIntent(R.id.article3, PendingIntent.getActivity(context, 0, new Intent(context, newsActivity.class).putExtra("url", link3), 0));

            views.setOnClickPendingIntent(R.id.newspaper, PendingIntent
                    .getActivity(context, 0, new Intent(
                            Default.Intent_Update_News), 0));
            // update the widget
            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager
                .getInstance(context);
        ComponentName thisAppWidget = new ComponentName(
                context.getPackageName(), Widget_news.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
