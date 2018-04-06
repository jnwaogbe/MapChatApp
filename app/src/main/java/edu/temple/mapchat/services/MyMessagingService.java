package edu.temple.mapchat.services;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Jessica on 4/2/2018.
 */

public class MyMessagingService extends FirebaseMessagingService {
    public MyMessagingService() {

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        /*
        Intent messageIntent = new Intent("CHAT_APP_MESSAGE_ACTION");
        messageIntent.putExtra("message", remoteMessage.getData().get("message"));
*/
        String myRemoteMsg = remoteMessage.getData().get("payload");
        Log.d("Received message", "You got to this line.");

        if (myRemoteMsg != null) {
            Intent intent = new Intent("CHAT_APP_MESSAGE");
            intent.putExtra("message", myRemoteMsg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d("Received message", myRemoteMsg);
        }

    }
}
