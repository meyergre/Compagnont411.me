package fr.lepetitpingouin.android.t411;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent i) {

        try {
            Intent intent = new Intent(ctx, t411UpdateService.class);
            ctx.startService(intent);
        } finally {
            new NotificationWidget(ctx).updateNotificationWidget();

        }
    }
}