package com.scp.whereyouapp;

import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    public boolean text = true;
    public Context context =  this;
    private Toolbar toolbar;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerList = (ListView)findViewById(R.id.navList);
        addDrawerItems();

        final TextView current_location = (TextView) findViewById(R.id.current_location);
        final LocationTracker tracker = new FallbackLocationTracker(this,ProviderLocationTracker.ProviderType.GPS);
        final double testLat = 43.472672;
        final double testLong = -80.542216;

        final WebView webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);

        LocationTracker.LocationUpdateListener listener = new LocationTracker.LocationUpdateListener() {
            @Override
            public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime) {
                if (tracker.hasLocation()) {
                }
                else {
                    current_location.setText("No location found");
                }
                if (tracker.hasPossiblyStaleLocation()) {
                    current_location.setText("Last known location: " + tracker.getPossiblyStaleLocation().getLatitude() + " " + tracker.getPossiblyStaleLocation().getLongitude());
                }
                Double longitude = newLoc.getLongitude();
                Double latitude = newLoc.getLatitude();
                current_location.setText("Current location: " + latitude.toString() + ", " + longitude.toString());
                webview.loadUrl("https://maps.google.com/?q=@" + latitude.toString() + "," + longitude.toString());

                if(Math.abs(latitude - testLat) < 0.001 && Math.abs(longitude - testLong) < 0.001 && text) {
                    text = false;
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("2269781724", null, "@DC: " + latitude + ", " + longitude, null, null);
                    smsManager.sendTextMessage("2269893193", null, "@DC: " + latitude + ", " + longitude, null, null);
                    smsManager.sendTextMessage("5197298639", null, "@DC: " + latitude + ", " + longitude, null, null);
                    smsManager.sendTextMessage("6479750458", null, "@DC: " + latitude + ", " + longitude, null, null);

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
        final String[] osArray = { "Home", "Users", "Active Trips", "Location Log", "Settings" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, osArray[(int)id], Toast.LENGTH_SHORT).show();
            }
        });
    }
}
