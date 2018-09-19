package yazilim.dr.sohbet;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by ramazan on 9/12/2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String bildirim_title = remoteMessage.getNotification().getTitle();
        String bildirim_mesaj = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_user_id");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(bildirim_title).setContentText(bildirim_mesaj);

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);
        PendingIntent resultpendingIntent = PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultpendingIntent);

        int dnotificationId = (int) System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(dnotificationId,builder.build());
    }
}
