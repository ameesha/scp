package com.scp.whereyouapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {
    private Firebase firebaseRef;
    private String username;
    private TextView loginWarning;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseRef = new Firebase("https://whereyouapp.firebaseio.com/");
    }

    public void onLoginClick(View view){
//      A child node's key cannot be longer than 768 bytes, nor deeper than 32 levels.
//      It can include any unicode characters except for . $ # [ ] / and ASCII control characters 0-31 and 127.
        username = ((EditText) findViewById(R.id.username)).getText().toString().trim();
        loginWarning = (TextView) findViewById(R.id.loginWarning);


        if(username.length() == 0) {
            loginWarning.setText("Please enter a username.");
            loginWarning.setVisibility(View.VISIBLE);
        }
        else if(username.contains(".") || username.contains("$") || username.contains("#") || username.contains("[") || username.contains("]") || username.contains("/")) {
            loginWarning.setText("Usernames can not contain the following characters: . $ # [ ] /");
            loginWarning.setVisibility(View.VISIBLE);
        }
        else {
            loginWarning.setVisibility(View.INVISIBLE);
            firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Map<String, Object> usernameToUidMap = new HashMap<String, Object>();
                    Map<String, Object> usersMap = new HashMap<String, Object>();

                    if (!snapshot.child("usernameToUid").hasChild(username)) {
                        Globals.setUsername(username);
                        Globals.setEnablePing(true);
                        Globals.setEnablePush(true);
                        usernameToUidMap.put(Globals.getUsername(), Globals.getUid());
                        firebaseRef.child("usernameToUid").updateChildren(usernameToUidMap);

                        usersMap.put("username", Globals.getUsername());
                        usersMap.put("enablePush", Globals.getEnablePush());
                        usersMap.put("enablePing", Globals.getEnablePing());
                        firebaseRef.child("users").child(Globals.getUid()).updateChildren(usersMap);
                        finish();
                    }
                    else {
                        loginWarning.setText("This username has been taken");
                        loginWarning.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
            SharedPreferences sp = getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("username", username);
            editor.putBoolean("allow_friends_to_ping", Globals.getEnablePing());
            editor.putBoolean("receive_push_notifications", Globals.getEnablePush());
            editor.commit();
            finish();
        }
    }
    @Override
    public void onBackPressed() {
    }
}
