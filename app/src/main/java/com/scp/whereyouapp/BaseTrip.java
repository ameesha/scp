package com.scp.whereyouapp;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;


public class BaseTrip {
    protected LatLng currentLocation;
    protected LatLng destination;
    protected String owner;
    protected Context context;

    public BaseTrip() {

    }

    public BaseTrip(LatLng cur_loc, LatLng dest, String user, Context ctxt) {
        currentLocation = cur_loc;
        destination = dest;
        owner = user;
        context = ctxt;
    }

    public void updateLocation(LatLng location) {
        currentLocation = location;
    }

    public LatLng getLocation() {
        return currentLocation;
    }

    public LatLng getDestination() {
        return destination;
    }

    public String getOwner() {
        return owner;
    }
}
