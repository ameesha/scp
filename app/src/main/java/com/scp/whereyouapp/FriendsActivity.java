package com.scp.whereyouapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.GoogleMap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class FriendsActivity extends AppCompatActivity {
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private DrawerLayout mDrawerLayout;
    private final String[] osArray = { "Home", "Friends", "Trips", "Location Log", "Settings" };
    private Firebase firebaseRef;
    private String friend;
    private ListView friendListView;
    private ArrayList<String> friendList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        firebaseRef = new Firebase("https://whereyouapp.firebaseio.com/");

        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        addDrawerItems();

        friendListView = (ListView)findViewById(R.id.friendListView);
        updateFriends();

        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String value = friendList.get(position);
                new AlertDialog.Builder(FriendsActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Remove friend")
                        .setMessage("Are you sure you want to remove this friend?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                firebaseRef.child("users").child(Globals.getUid()).child("friend").child(value).removeValue();

                                firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        String friendUID = snapshot.child("usernameToUid").child(value).getValue().toString();
                                        firebaseRef.child("users").child(friendUID).child("friend").child(Globals.getUsername()).removeValue();
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {
                                    }
                                });
                                updateFriends();
                            }
                        })
                        .show();
            }
        });
    }

    private void updateFriends(){
        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                DataSnapshot friends;
                //if you have friends
                Log.e("friend", String.valueOf(snapshot.child("users").child(Globals.getUid()).child("friend").hasChildren()));
                if ((friends = snapshot.child("users").child(Globals.getUid()).child("friend")).hasChildren()) {
                    Map<String, Object> ret = (Map<String, Object>) friends.getValue();
                    friendList = new ArrayList<String>(ret.keySet());
                }
                else {
                    friendList = new ArrayList<String>();
                }
                ArrayAdapter<String> friendArrayAdapter = new ArrayAdapter<String>(FriendsActivity.this, android.R.layout.simple_list_item_1, friendList);
                friendListView.setAdapter(friendArrayAdapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
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

    public void onAddFriendClick(View view) {
        friend = ((EditText) findViewById(R.id.friend)).getText().toString().trim().toLowerCase();
        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child("usernameToUid").hasChild(friend)) {

                    //check that your friend isn't you
                    if(!Globals.getUsername().equalsIgnoreCase(friend)) {
                        Map<String,Object> map1 = new HashMap<String, Object>();
                        Map<String,Object> map2 = new HashMap<String, Object>();

                        map1.put(friend, "requester");
                        firebaseRef.child("users").child(Globals.getUid()).child("friend").updateChildren(map1);

                        map2.put(Globals.getUsername(), "requested");
                        firebaseRef.child("users").child(snapshot.child("usernameToUid").child(friend).getValue().toString()).child("friend").updateChildren(map2);

                        //TODO: change these toasts to textviews?
                        Toast.makeText(FriendsActivity.this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                        updateFriends();
                    }
                    else {
                        Toast.makeText(FriendsActivity.this, "As much as you want to, can't add yourself as a friend", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(FriendsActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void addDrawerItems() {
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = osArray[(int) id];
                if (value == "Settings"){
                    Intent intent = new Intent(FriendsActivity.this, SettingsActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else if (value == "Location Log"){
                    Intent intent = new Intent(FriendsActivity.this, LocationLogActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else if (value == "Home"){
                    Intent intent = new Intent(FriendsActivity.this, MainActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else{
                    Toast.makeText(FriendsActivity.this, osArray[(int) id], Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
