package com.scp.whereyouapp;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


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
                                .setSmallIcon(R.drawable.launcher_icon)
                                .setContentTitle("Where You App")
                                .setContentText("Time's up!");
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, mBuilder.build());
            }
        }, reminderTime*60 * 1000);

        Log.e("RUNTIME", "TRIP CREATED: " + reminderTime);
        updateLocation(cur_loc);
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
                            .setSmallIcon(R.drawable.launcher_icon)
                            .setContentTitle("Where You App")
                            .setContentText("Location sent to friends");
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Globals.getEnablePush()){
                mNotificationManager.notify(0, mBuilder.build());
            }
            saveTextedNumbers(numbersToText);
        }
    }

    private void saveTextedNumbers(ArrayList<String> numbers){
        SharedPreferences sp = this.context.getSharedPreferences("notificationLog", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        boolean exists = sp.contains("texted_numbers");
        String current_numbers = null;
        if (exists){
            current_numbers = sp.getString("texted_numbers", current_numbers);
        }
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        for (int i = 0; i < numbers.size(); i++){
            if (current_numbers == null){
                current_numbers = "Number: " + numbers.get(i) + " " + currentDateTimeString;
            }
            else{
                current_numbers = current_numbers + " Number: "  + numbers.get(i) + " " + currentDateTimeString;
            }
        }
        editor.putString("texted_numbers", current_numbers);
        editor.commit();
    }
}
