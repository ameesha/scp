package com.scp.whereyouapp;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class MyTrip extends BaseTrip {
    protected boolean text = true;

    ArrayList<String> allowedFriends;
    ArrayList<String> numbersToText;
    long reminderTime; //time in minutes before app sends a reminder to the user
    Handler handler;

    public MyTrip(LatLng cur_loc, LatLng dest, String user, ArrayList<String> friends, ArrayList<String> numbers, long time, Context ctxt) {
        super(cur_loc, dest, user, ctxt);

        allowedFriends = friends;
        numbersToText = numbers;
        reminderTime = time;

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Where You App")
                                .setContentText("Time's up!");
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, mBuilder.build());
            }
        }, reminderTime*60*1000);
    }

    @Override
    public void updateLocation(LatLng cur_loc) {
        currentLocation = cur_loc;

        if(Math.abs(cur_loc.latitude - destination.latitude) < 0.001 && Math.abs(cur_loc.longitude - destination.longitude) < 0.001 && text) {
            text = false;
            SmsManager smsManager = SmsManager.getDefault();
            for (int i = 0; i < numbersToText.size(); i++) {
                smsManager.sendTextMessage(numbersToText.get(i), null, "@DC: " + cur_loc.latitude + ", " + cur_loc.longitude, null, null);
            }


            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Where You App")
                            .setContentText("Location sent to friends");
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
        }
    }
}
