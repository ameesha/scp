package com.scp.whereyouapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebViewFragment;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    public boolean text = true;
    public Context context =  this;
    private Toolbar toolbar;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private DrawerLayout mDrawerLayout;
    private GoogleMap map;
    private Marker cur_loc_marker;
    private TextView current_location;
    private int selectedPosition;
    private final String[] osArray = { "Home", "Users", "Trips", "Location Log", "Settings" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        setSupportActionBar(toolbar);


        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        addDrawerItems();

        current_location = (TextView) findViewById(R.id.current_location);
        final LocationTracker tracker = new FallbackLocationTracker(this,ProviderLocationTracker.ProviderType.GPS);
        final double testLat = 43.472672;
        final double testLong = -80.542216;
        final LatLng home = new LatLng(43.477794,-80.537274);
        final LatLng work = new LatLng(43.473929,-80.546237);


        LocationTracker.LocationUpdateListener listener = new LocationTracker.LocationUpdateListener() {
            @Override
            public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime) {
                if (!tracker.hasLocation()) {
                    current_location.setText("No location found");
                }
                if (tracker.hasPossiblyStaleLocation()) {
                    current_location.setText("Last known location: " + tracker.getPossiblyStaleLocation().getLatitude() + " " + tracker.getPossiblyStaleLocation().getLongitude());
                }
                LatLng cur_loc = new LatLng(newLoc.getLatitude(), newLoc.getLongitude());
                String addr;

                if((addr = getAddress(cur_loc.latitude, cur_loc.longitude)) != "") {
                    current_location.setText("Current location: " + addr);
                }

                map.addMarker(new MarkerOptions().position(cur_loc).title("Current location").snippet(addr));
                map.addMarker(new MarkerOptions().position(home).title("Home").snippet(getAddress(home.latitude, home.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                map.addMarker(new MarkerOptions().position(work).title("Work").snippet(getAddress(work.latitude, work.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                map.setMyLocationEnabled(true);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(cur_loc, 13));

                if(Math.abs(cur_loc.latitude - testLat) < 0.001 && Math.abs(cur_loc.longitude - testLong) < 0.001 && text) {
                    text = false;
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("2269781724", null, "@DC: " + cur_loc.latitude + ", " + cur_loc.longitude, null, null);
                    smsManager.sendTextMessage("2269893193", null, "@DC: " + cur_loc.latitude + ", " + cur_loc.longitude, null, null);
                    smsManager.sendTextMessage("5197298639", null, "@DC: " + cur_loc.latitude + ", " + cur_loc.longitude, null, null);
                    smsManager.sendTextMessage("6479750458", null, "@DC: " + cur_loc.latitude + ", " + cur_loc.longitude, null, null);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("Where You App")
                                    .setContentText("Location sent to friends");
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(0, mBuilder.build());
                }
            }
        };
        tracker.start(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getAddress(double lat, double lon) {
        Geocoder geocoder= new Geocoder(this, Locale.ENGLISH);
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

    private void sendSMS(String phoneNo, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, message, null, null);
    }

    private void notification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void addDrawerItems() {
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, osArray[(int) id], Toast.LENGTH_SHORT).show();
            }
        });
    }
}
