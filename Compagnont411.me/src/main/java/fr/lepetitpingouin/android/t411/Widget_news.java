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
import android.widget.RemoteViews;

import org.jsoup.Jsoup;

public class Widget_news extends AppWidgetProvider {

    SharedPreferences prefs;
    RemoteViews views;

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

            views.setTextViewText(R.id.title1, prefs.getString("title1", "..."));
            views.setTextViewText(R.id.article1, Jsoup.parse(prefs.getString("article1", "...")).text());
            views.setTextViewText(R.id.title2, prefs.getString("title2", "..."));
            views.setTextViewText(R.id.article2, Jsoup.parse(prefs.getString("article2", "...")).text());
            views.setTextViewText(R.id.title3, prefs.getString("title3", "..."));
            views.setTextViewText(R.id.article3, Jsoup.parse(prefs.getString("article3", "...")).text());

            views.setOnClickPendingIntent(R.id.article1, PendingIntent
                    .getActivity(context, 0, new Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse(prefs.getString("readMore1",
                                    Default.URL_INDEX))), 0));
            views.setOnClickPendingIntent(R.id.article2, PendingIntent
                    .getActivity(context, 0, new Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse(prefs.getString("readMore2",
                                    Default.URL_INDEX))), 0));
            views.setOnClickPendingIntent(R.id.article3, PendingIntent
                    .getActivity(context, 0, new Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse(prefs.getString("readMore3",
                                    Default.URL_INDEX))), 0));
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
