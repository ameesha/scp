package com.scp.whereyouapp;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class TripActivity extends AppCompatActivity {
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private DrawerLayout mDrawerLayout;
    private final String[] osArray = { "Home", "Friends", "Trips", "Location Log", "Settings" };
    private Firebase firebaseRef;
    private String friend;
    private TextView friendLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        firebaseRef = new Firebase("https://whereyouapp.firebaseio.com/");

        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        addDrawerItems();
        friendLoc = (TextView) findViewById(R.id.friendLoc);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onPingFriend(View view) {
        friend = ((EditText) findViewById(R.id.friend)).getText().toString().trim().toLowerCase();
        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                DataSnapshot friends = snapshot.child("trips").child(friend).child("allowedFriends");
                List<String> ret = (List<String>) friends.getValue();
                Map<String, String> map = new HashMap<String, String>();

                //check if you're in allowedFriends
                if(ret.contains(Globals.getUsername())) {
                    Double latitude = Double.parseDouble(snapshot.child("trips").child(friend).child("currentLat").getValue().toString());
                    Double longitude = Double.parseDouble(snapshot.child("trips").child(friend).child("currentLong").getValue().toString());
                    friendLoc.setText("Location: " + getAddress(latitude, longitude));

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ssa");
                    String strDate = sdf.format(c.getTime());
                    map.put(Globals.getUsername(), strDate);
                    firebaseRef.child("trips").child(friend).child("pingLog").push().setValue(map);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
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

    private void addDrawerItems() {
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = osArray[(int) id];
                if (value == "Settings"){
                    Intent intent = new Intent(TripActivity.this, SettingsActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else if (value == "Location Log"){
                    Intent intent = new Intent(TripActivity.this, LocationLogActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                } else if (value == "Trips") {
                    mDrawerLayout.closeDrawers();
                }
                else if (value == "Friends"){
                    Intent intent = new Intent(TripActivity.this, FriendsActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else if (value == "Home"){
                    Intent intent = new Intent(TripActivity.this, MainActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else{
                    Toast.makeText(TripActivity.this, osArray[(int) id], Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
