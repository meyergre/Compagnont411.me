package fr.lepetitpingouin.android.t411;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class Widget_search extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        //final int N = appWidgetIds.length;

        // loop through all app widgets the user has enabled
        for (int widgetId : appWidgetIds) {
            // get our view so we can edit the time
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_search);

            Intent myIntent = new Intent();
            PendingIntent pIntent;

            myIntent.setClassName("fr.lepetitpingouin.android.t411",
                    "fr.lepetitpingouin.android.t411.SearchActivity");
            pIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

            views.setOnClickPendingIntent(R.id.searchWidget, pIntent);
            appWidgetManager.updateAppWidget(widgetId, views);

        }
    }
}
