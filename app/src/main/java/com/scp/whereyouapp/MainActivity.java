package com.scp.whereyouapp;

import android.app.Activity;
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
import android.widget.ListView;
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
import com.google.android.gms.maps.model.MarkerOptions;

import com.facebook.FacebookSdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.DateFormat;
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
    private TextView current_location;
    private final String[] osArray = { "Home", "Friends", "Trips", "Location Log", "Settings" };
    private Firebase firebaseRef;
    private ArrayList<String> numbers;

    private CallbackManager callbackManager;

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
        numbers.add("2269781724");
        numbers.add("2269893193");
        numbers.add("5197298639");
        numbers.add("6479750458");

        final MyTrip myTrip = new MyTrip(null, home, "User", null, numbers, 1, this);//Example trip created for testing purposes

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
                myTrip.updateLocation(cur_loc);
                String addr;

                if((addr = getAddress(cur_loc.latitude, cur_loc.longitude)) != "") {
                    current_location.setText("Current location: " + addr);
                }
                map.clear();
                map.addMarker(new MarkerOptions().position(cur_loc).title("Current location").snippet(addr));
                map.addMarker(new MarkerOptions().position(home).title("Home").snippet(getAddress(home.latitude, home.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                map.addMarker(new MarkerOptions().position(work).title("Work").snippet(getAddress(work.latitude, work.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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
                }
                else if (value == "Friends"){
                    Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else{
                    Toast.makeText(MainActivity.this, osArray[(int) id], Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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
