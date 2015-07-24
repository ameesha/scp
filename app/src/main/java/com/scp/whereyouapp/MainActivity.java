package com.scp.whereyouapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.facebook.FacebookSdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {
    public boolean text = true;
    public Context context =  this;
    private Toolbar toolbar;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private DrawerLayout mDrawerLayout;
    private GoogleMap map;
    private Marker destinationMarker;
    private Marker currentLocationMarker;
    private TextView current_location;
    private final String[] osArray = { "Home", "Friends", "Trips", "Location Log", "Settings" };
    private Firebase firebaseRef;
    private ArrayList<String> numbers;
    private HashMap<String, LatLng> favouriteLocations;
    private MyTrip myTrip;
    private LatLng myLocation;

    private CallbackManager callbackManager;

    private ListView contactList;
    private RelativeLayout tripFields;
    private EditText destinationText;
    private EditText delayText;
    private EditText contactText;
    private Button submitButton;
    private Button contactListButton;
    private Button addButton;
    private Button cancelButton;
    private Button favlocButton;

    NfcAdapter mNfcAdapter;
    public static final String MIME_TEXT_PLAIN = "text/plain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( getIntent().getBooleanExtra("Exit me", false)){
            finish();
            return; // add this to prevent from doing unnecessary stuffs
        }
        Firebase.setAndroidContext(this);
        firebaseRef = new Firebase("https://whereyouapp.firebaseio.com/");

        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.facebook);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("SUCCESS!", "SUCCESS!");
                //check if this user already exists in Firebase
                enterApp(loginResult);
            }

            @Override
            public void onCancel() {
                Log.e("Canceled", "canc");
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("ERROR", exception.getMessage());
                // App code
            }
        });

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
        //LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, LocationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000,
                pendingIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void enterApp(LoginResult loginResult) {
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        destinationMarker = null;
        currentLocationMarker = null;
        myTrip = null;
        setSupportActionBar(toolbar);

        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        addDrawerItems();



        firebaseRef.authWithOAuthToken("facebook", loginResult.getAccessToken().getToken(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(final AuthData authData) {
                Globals.setUid(authData.getUid());

                firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        //new user (no username for this fb login)
                        if (!snapshot.child("users").hasChild(Globals.getUid()) || !snapshot.child("users").child(Globals.getUid()).hasChild("username")) {
                            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                            MainActivity.this.startActivity(loginIntent);
                        }
                        else {
                            Globals.setUsername(snapshot.child("users").child(Globals.getUid()).child("username").getValue().toString());
                            if (!snapshot.child("users").hasChild(Globals.getUid()) || !snapshot.child("users").child(Globals.getUid()).hasChild("enablePing")){
                                Map<String, Object> usersMap = new HashMap<String, Object>();
                                usersMap.put("enablePing", true);
                                firebaseRef.child("users").child(Globals.getUid()).updateChildren(usersMap);
                            }
                            if (!snapshot.child("users").hasChild(Globals.getUid()) || !snapshot.child("users").child(Globals.getUid()).hasChild("enablePush")){
                                Map<String, Object> usersMap = new HashMap<String, Object>();
                                usersMap.put("enablePush", true);
                                firebaseRef.child("users").child(Globals.getUid()).updateChildren(usersMap);
                            }
                            Globals.setEnablePing((Boolean) snapshot.child("users").child(Globals.getUid()).child("enablePing").getValue());
                            Globals.setEnablePush((Boolean) snapshot.child("users").child(Globals.getUid()).child("enablePush").getValue());
                        }
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("provider", authData.getProvider());
                if (authData.getProviderData().containsKey("id")) {
                    map.put("provider_id", authData.getProviderData().get("id").toString());
                }
                if (authData.getProviderData().containsKey("displayName")) {
                    map.put("name", authData.getProviderData().get("displayName").toString());
                }
                if (authData.getProviderData().containsKey("email")) {
                    map.put("email", authData.getProviderData().get("email").toString());
                }
                if (authData.getProviderData().containsKey("profileImageURL")) {
                    map.put("profileImageURL", authData.getProviderData().get("profileImageURL").toString());
                }
                firebaseRef.child("users").child(Globals.getUid()).updateChildren(map);
            }

            @Override
            public void onAuthenticationError(FirebaseError error) {
                Log.e("Firebase auth", error.getMessage());
            }
        });

        current_location = (TextView) findViewById(R.id.current_location);
        final LocationTracker tracker = new FallbackLocationTracker(this,ProviderLocationTracker.ProviderType.GPS);
        final double testLat = 43.472672;
        final double testLong = -80.542216;
        final LatLng home = new LatLng(43.477794,-80.537274);
        final LatLng work = new LatLng(43.473929,-80.546237);

        numbers = new ArrayList<String>();
        favouriteLocations = new HashMap<String, LatLng>();
        favouriteLocations.put("Work", work);
        favouriteLocations.put("Home", home);

        contactList = (ListView)findViewById(R.id.contact_list);
        tripFields = (RelativeLayout)findViewById(R.id.trip_fields);
        destinationText = (EditText)findViewById(R.id.destination_text);
        delayText = (EditText)findViewById(R.id.time_text);
        contactText = (EditText)findViewById(R.id.contact_text);

        submitButton = (Button)findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = destinationText.getText().toString();
                addTargetMarker(address);

                TextView displayDestination = (TextView) findViewById(R.id.display_destination_text);
                TextView displayDelay = (TextView) findViewById(R.id.display_time_text);
                TextView displayContacts = (TextView) findViewById(R.id.display_contact_text);


                LatLng dest = getLocationFromAddress(destinationText.getText().toString());
                displayDestination.setText(getAddress(dest.latitude, dest.longitude));

                String delay = delayText.getText().toString();

                if(delay.isEmpty()) {
                    displayDelay.setText("No reminder");
                    delay = "-1";
                } else {
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.MINUTE, Integer.parseInt(delay));
                    int minute = c.get(Calendar.MINUTE);
                    String minutes;
                    if(minute < 10) {
                        minutes = "0" + minute;
                    } else {
                        minutes = String.valueOf(minute);
                    }
                    String ampm;
                    if(c.get(Calendar.AM_PM) == 1) {
                        ampm = "pm";
                    } else {
                        ampm = "am";
                    }

                    displayDelay.setText(c.get(Calendar.HOUR) + ":" + minutes + ampm);
                }

                StringBuilder stringContacts = new StringBuilder();
                for(int i = 0; i < numbers.size(); i++) {
                    stringContacts.append(numbers.get(i) + "\n");
                }
                displayContacts.setText(stringContacts.toString());

                findViewById(R.id.create_layout).setVisibility(View.GONE);
                findViewById(R.id.display_layout).setVisibility(View.VISIBLE);


                myTrip = new MyTrip(myLocation, dest, "User", null, numbers, Long.parseLong(delay), context);//Example trip created for testing purposes
                updateActiveTrips(myLocation);
            }
        });

        contactListButton = (Button)findViewById(R.id.contact_list_button);
        contactListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripFields.setVisibility(View.GONE);
                contactList.setVisibility(View.VISIBLE);
            }
        });

        addButton = (Button)findViewById(R.id.add_contact);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numbers.add(contactText.getText().toString());
                contactText.setText("");
                contactText.setHint("Add Another");
            }
        });

        cancelButton = (Button)findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinationText.setText("");
                delayText.setText("");
                contactText.setText("");
                contactText.setHint("Phone Number");

                findViewById(R.id.display_layout).setVisibility(View.GONE);
                findViewById(R.id.create_layout).setVisibility(View.VISIBLE);
                myTrip.cancel();
                myTrip = null;
                destinationMarker.remove();
                destinationMarker = null;
                numbers.clear();
            }
        });

        favlocButton = (Button)findViewById(R.id.add_favloc);
        favlocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });



        LocationTracker.LocationUpdateListener listener = new LocationTracker.LocationUpdateListener() {
            @Override
            public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime) {
                if (!tracker.hasLocation()) {
                    current_location.setText("No location found");
                }
                if (tracker.hasPossiblyStaleLocation()) {
                    current_location.setText("Last known location: " + tracker.getPossiblyStaleLocation().getLatitude() + " " + tracker.getPossiblyStaleLocation().getLongitude());
                    Globals.setLocation(tracker.getPossiblyStaleLocation());
                }
                LatLng cur_loc = new LatLng(newLoc.getLatitude(), newLoc.getLongitude());
                myLocation = cur_loc;
                updateActiveTrips(cur_loc);
                String addr;

                if((addr = getAddress(cur_loc.latitude, cur_loc.longitude)) != "") {
                    current_location.setText("Current location: " + addr);
                    Globals.setLocation(newLoc);
                }
                if(currentLocationMarker != null) {
                    currentLocationMarker.remove();
                }
                currentLocationMarker = map.addMarker(new MarkerOptions().position(cur_loc).title("Current location").snippet(addr));
                markFavouriteLocations();
//                map.addMarker(new MarkerOptions().position(home).title("Home").snippet(getAddress(home.latitude, home.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//                map.addMarker(new MarkerOptions().position(work).title("Work").snippet(getAddress(work.latitude, work.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                map.setMyLocationEnabled(true);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(cur_loc, 13));
//
//                if(Math.abs(cur_loc.latitude - testLat) < 0.001 && Math.abs(cur_loc.longitude - testLong) < 0.001 && text) {
//                    text = false;
//                    SmsManager smsManager = SmsManager.getDefault();
//                    String[] texted_numbers = {"2269781724", "2269893193", "5197298639", "6479750458"};
//                    for (int i = 0; i < texted_numbers.length; i++){
//                        smsManager.sendTextMessage(texted_numbers[i], null, "@DC: " + cur_loc.latitude + ", " + cur_loc.longitude, null, null);
//                    }
//                    /*smsManager.sendTextMessage("2269781724", null, "@DC: " + cur_loc.latitude + ", " + cur_loc.longitude, null, null);
//                    smsManager.sendTextMessage("2269893193", null, "@DC: " + cur_loc.latitude + ", " + cur_loc.longitude, null, null);
//                    smsManager.sendTextMessage("5197298639", null, "@DC: " + cur_loc.latitude + ", " + cur_loc.longitude, null, null);
//                    smsManager.sendTextMessage("6479750458", null, "@DC: " + cur_loc.latitude + ", " + cur_loc.longitude, null, null);*/
//
//                    NotificationCompat.Builder mBuilder =
//                            new NotificationCompat.Builder(context)
//                                    .setSmallIcon(R.mipmap.ic_launcher)
//                                    .setContentTitle("Where You App")
//                                    .setContentText("Location sent to friends");
//                    NotificationManager mNotificationManager =
//                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                    mNotificationManager.notify(0, mBuilder.build());
//
//                    saveTextedNumbers(texted_numbers);
//                }
            }
        };
        tracker.start(listener);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            // Register callback
            Log.e("NFC", "IS THERE");
            mNfcAdapter.setNdefPushMessageCallback(this, this);
        }
        else
            Log.e("NFC", "FAILED");

    }

    private void saveTextedNumbers(String[] numbers){
        SharedPreferences sp = getSharedPreferences("notificationLog", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        boolean exists = sp.contains("texted_numbers");
        String current_numbers = null;
        if (exists){
            current_numbers = sp.getString("texted_numbers", current_numbers);
        }
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        for (int i = 0; i < numbers.length; i++){
            if (current_numbers == null){
                current_numbers = "Number: " + numbers[i] + " " + currentDateTimeString;
            }
            else{
                current_numbers = current_numbers + " Number: "  + numbers[i] + " " + currentDateTimeString;
            }
        }
        editor.putString("texted_numbers", current_numbers);
        editor.commit();
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

    private LatLng getLocationFromAddress(String strAddress){

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(),location.getLongitude());

            return p1;
        }
        catch(Exception e) {
            return null;
        }
    }

    private void addTargetMarker(String strAdress) {
        Log.e("ADDRESS", "adding Target");
        LatLng address = getLocationFromAddress(strAdress);
        destinationMarker = map.addMarker(new MarkerOptions().position(address).title("Target Location").snippet(strAdress).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(address, 13));
    }

    private void markFavouriteLocations() {

        Object[] locations = favouriteLocations.keySet().toArray();

        for(int i = 0; i < favouriteLocations.size(); i++) {
            LatLng loc = favouriteLocations.get(locations[i]);

            map.addMarker(new MarkerOptions().position(loc).title((String)locations[i]).snippet(getAddress(loc.latitude, loc.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(favouriteLocations.containsKey(marker.getTitle())) {
                    destinationText.setText(marker.getSnippet());
                }
                marker.showInfoWindow();
                return false;
            }
        });
    }

    private void updateActiveTrips(LatLng location) {
        if(myTrip != null) {
            if(myTrip.updateLocation(location)) {
                Log.e("Trip", "Completed");
                cancelButton.callOnClick();
            }
        }
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
                String value = osArray[(int) id];
                if (value == "Settings") {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                } else if (value == "Location Log") {
                    Intent intent = new Intent(MainActivity.this, LocationLogActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                } else if (value == "Home") {
                    mDrawerLayout.closeDrawers();
                }
                else if (value == "Friends"){
                    Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else if (value == "Trips"){
                    Intent intent = new Intent(MainActivity.this, TripActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else{
                    Toast.makeText(MainActivity.this, osArray[(int) id], Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = ("UserID" + "k2sandhu");
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { NdefRecord.createMime(
                        "application/com.scp.whereyouapp.MainActivity", text.getBytes())
                });
        return msg;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        processIntent(intent);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        super.onPause();
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if presen
        Log.e("NFC Tag:", new String(msg.getRecords()[0].getPayload()));
        Log.e("NFCCCC", "WAS HIT");
    }
}
