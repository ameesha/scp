package com.scp.whereyouapp;

import android.location.Location;

/**
 * Created by Christine on 7/15/2015.
 */
public class Globals {
    private static String uid;
    private static String username;
    private static Location location;

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
}
