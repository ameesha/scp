package com.scp.whereyouapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {
    private Firebase firebaseRef;
    private EditText username;
    private String uid;
    private TextView loginWarning;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseRef = new Firebase("https://whereyouapp.firebaseio.com/");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uid = extras.getString("uid");
        }
    }

    public void onLoginClick(View view){
        username = (EditText) findViewById(R.id.username);
        loginWarning = (TextView) findViewById(R.id.loginWarning);
        Map<String, Object> map = new HashMap<String, Object>();

        if(username.getText().toString().trim().length() == 0) {
            loginWarning.setVisibility(View.VISIBLE);
        }
        else {
            loginWarning.setVisibility(View.INVISIBLE);
            map.put("username", username.getText().toString().trim());
            SharedPreferences sp = getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("username", username.getText().toString().trim());
            editor.putBoolean("allow_friends_to_ping", true);
            editor.putBoolean("receive_push_notifications", true);
            editor.commit();
            firebaseRef.child("users").child(uid).updateChildren(map);
            finish();
        }
    }


}
