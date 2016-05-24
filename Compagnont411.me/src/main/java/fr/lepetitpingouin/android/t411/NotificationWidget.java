package fr.lepetitpingouin.android.t411;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.widget.RemoteViews;

import java.io.IOException;

/**
 * Created by gregory on 16/08/13.
 */
class NotificationWidget {

    private Context context;
    private SharedPreferences prefs;

    public NotificationWidget(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean updateNotificationWidget() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_widget);
        contentView.setTextViewText(R.id.nw_username, prefs.getString("lastUsername", "???"));
        contentView.setTextViewText(R.id.nw_down, new BSize(prefs.getString("lastDownload", "0 KB")).convert());
        contentView.setTextViewText(R.id.nw_up, new BSize(prefs.getString("lastUpload", "0 KB")).convert());
        contentView.setTextViewText(R.id.nw_mails, String.valueOf(prefs.getInt("lastMails", 0)));
        contentView.setTextViewText(R.id.nw_ratio, String.format("%.2f", Double.valueOf(prefs.getString("lastRatio", "0"))));
        contentView.setTextViewText(R.id.nw_date, prefs.getString("lastDate", "???"));

        String encodedImage = prefs.getString("avatar", "");
        if (!encodedImage.equalsIgnoreCase("")) {
            try {
                byte[] b = Base64.decode(encodedImage.getBytes(), 0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                contentView.setImageViewBitmap(R.id.nw_avatar, bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        contentView.setImageViewResource(R.id.nw_smiley, new Ratio(context).getSmiley());

        builder.setContent(contentView);
        builder.setSmallIcon(new Ratio(context).getSmiley());
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity2.class), 0));

        if (!prefs.getBoolean("notificationWidget", false)) {
            mNotificationManager.cancel(1);
            return false;
        }
        mNotificationManager.notify(1, builder.build());
        return true;
    }
}
