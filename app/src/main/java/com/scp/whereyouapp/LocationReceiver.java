package com.scp.whereyouapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.provider.Settings;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.HashMap;
import java.util.Map;

public class LocationReceiver extends BroadcastReceiver {
    private Firebase firebaseRef;
    private Location loc;

    @Override
    public void onReceive(Context context, Intent intent) {
        firebaseRef = new Firebase("https://whereyouapp.firebaseio.com/");
        if((loc = Globals.getLocation()) != null && Globals.getUid() != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("location", loc.getLatitude() + "," + loc.getLongitude());
            firebaseRef.child("users").child(Globals.getUid()).updateChildren(map);
        }
    }
}