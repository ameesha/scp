package com.scp.whereyouapp;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class MyTrip extends BaseTrip {
    protected boolean text = true;
    private Firebase firebaseRef;

    ArrayList<String> allowedFriends;
    ArrayList<String> numbersToText;
    ArrayList<Map.Entry<String, String>> pingLog;

    long reminderTime; //time in minutes before app sends a reminder to the user
    Handler handler;
    SmsManager smsManager = SmsManager.getDefault();

    public MyTrip(LatLng cur_loc, LatLng dest, String user, ArrayList<String> friends, ArrayList<String> numbers, long time, Context ctxt) {
        super(cur_loc, dest, user, ctxt);

        firebaseRef = new Firebase("https://whereyouapp.firebaseio.com/");
        pingLog = new ArrayList<Map.Entry<String, String>>();
        allowedFriends = friends;
        numbersToText = numbers;
        reminderTime = time;

        handler = new Handler();

        if(reminderTime > 0) {
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


                    for (int i = 0; i < numbersToText.size(); i++) {
                        smsManager.sendTextMessage(numbersToText.get(i), null, Globals.getUsername() + " has not yet arrived at their destination. Would you like to send them a text?", null, null);
                    }
                }
            }, reminderTime * 60 * 1000);
        }

        Log.e("RUNTIME", "TRIP CREATED: " + reminderTime);
        if(Globals.getUid() != null && Globals.getUsername() != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("currentLat", cur_loc.latitude);
            map.put("currentLong", cur_loc.longitude);
            map.put("destLat", dest.latitude);
            map.put("destLong", dest.longitude);
//            map.put("allowedFriends", friends);

            //test data
            List<String> friendsList = new ArrayList<String>();
            friendsList.add("ameesha");
            friendsList.add("ckatigbak");
            friendsList.add("selinakyle");

            map.put("allowedFriends", friendsList);
            firebaseRef.child("trips").child(Globals.getUsername()).updateChildren(map);
        }
        //updateLocation(cur_loc);
    }

    @Override
    public boolean updateLocation(LatLng cur_loc) {
        currentLocation = cur_loc;

        if(Math.abs(cur_loc.latitude - destination.latitude) < 0.001 && Math.abs(cur_loc.longitude - destination.longitude) < 0.001 && text) {
            text = false;
            for (int i = 0; i < numbersToText.size(); i++) {
                smsManager.sendTextMessage(numbersToText.get(i), null, Globals.getUsername() + " has arrived at " + getAddress(cur_loc.latitude, cur_loc.longitude), null, null);
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
            return true;
        }

        if(Globals.getUid() != null && Globals.getUsername() != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("currentLat", cur_loc.latitude);
            map.put("currentLong", cur_loc.longitude);
            firebaseRef.child("trips").child(Globals.getUsername()).updateChildren(map);
        }
        updatePingLog();
        return false;
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

    public void cancel() {
        handler.removeCallbacksAndMessages(null);
        allowedFriends = null;
        numbersToText = null;
        reminderTime = -1;
        destination = null;

        if(Globals.getUid() != null && Globals.getUsername() != null) {
            firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(snapshot.child("trips").child(Globals.getUsername()).hasChild("pingLog")) {
                        DataSnapshot log = snapshot.child("trips").child(Globals.getUsername()).child("pingLog");

                        Map<String, Object> ret = (Map<String, Object>) log.getValue();

                        for (Map.Entry<String, Object> entry : ret.entrySet()) {
                            Map.Entry<String, String> logEntry = (Map.Entry<String, String>) ((Map<String, String>) entry.getValue()).entrySet().toArray()[0];
                            Log.e("PingLog", logEntry.toString());
                            pingLog.add(logEntry);
                        }
                        //TODO: store pingLog information in Location Log
                        firebaseRef.child("trips").child(Globals.getUsername()).removeValue();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }
    }

    private void updatePingLog() {
        if(Globals.getUid() != null && Globals.getUsername() != null) {
            firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(snapshot.child("trips").child(Globals.getUsername()).hasChild("pingLog")) {
                        DataSnapshot log = snapshot.child("trips").child(Globals.getUsername()).child("pingLog");

                        Map<String, Object> ret = (Map<String, Object>) log.getValue();

                        for (Map.Entry<String, Object> entry : ret.entrySet()) {
                            Map.Entry<String, String> logEntry = (Map.Entry<String, String>) ((Map<String, String>) entry.getValue()).entrySet().toArray()[0];
                            Log.e("PingLog", logEntry.toString());
                            pingLog.add(logEntry);
                        }
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }
        //TODO: store pingLog information in Location Log
    }

    private String getAddress(double lat, double lon) {
        Geocoder geocoder= new Geocoder(context, Locale.ENGLISH);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if(addresses != null) {
                Address fetchedAddress = addresses.get(0);
                StringBuilder strAddress = new StringBuilder();
                for(int i=0; i<fetchedAddress.getMaxAddressLineIndex(); i++) {
                    strAddress.append(fetchedAddress.getAddressLine(i)).append("\n");
                }
                return strAddress.toString();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
