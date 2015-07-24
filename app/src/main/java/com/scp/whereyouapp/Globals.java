package com.scp.whereyouapp;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by Christine on 7/15/2015.
 */
public class Globals {
    private static String uid;
    private static String username;
    private static String phoneNumber;
    private static Location location;
    private static HashMap<String, LatLng> favouriteLocations = new HashMap<>();

    private static boolean enablePush = true;
    private static boolean enablePing = true;


    public static String getUid() {
        return uid;
    }
    public static void setUid(String str) {
        uid = str;
    }
    public static String getUsername() {
        return username;
    }
    public static void setUsername(String str) {
        username = str;
    }
    public static String getPhoneNumber() {
        return phoneNumber;
    }
    public static void setPhoneNumber(String str) {
        phoneNumber = str;
    }
    public static Location getLocation() {
        return location;
    }
    public static void setLocation(Location loc) {
        location = loc;
    }

    public static void setEnablePush(boolean bool){
        enablePush = bool;
    }
    public static void setEnablePing(boolean bool){
        enablePing = bool;
    }
    public static boolean getEnablePush(){
        return enablePush;
    }
    public static boolean getEnablePing(){
        return enablePing;
    }

    public static void addFaveLocation(String name, LatLng latlng){
        favouriteLocations.put(name, latlng);
        return;
    }
    public static HashMap<String, LatLng> getFaveLocations(){
        return favouriteLocations;
    }
    public static void deleteFaveLocation(String name){
        LatLng value = favouriteLocations.get(name);
        if (value != null){
            favouriteLocations.remove(name);
        }
        return;
    }
}
