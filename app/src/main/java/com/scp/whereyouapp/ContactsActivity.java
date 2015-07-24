package com.scp.whereyouapp;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ContactsActivity extends AppCompatActivity {
    private ListView contactListView;
    private ArrayList<String> contactList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        contactListView = (ListView) findViewById(R.id.contactListView);
        readContacts();
        updateContacts();
    }

    private void readContacts() {
        // TODO Auto-generated method stub

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,null, null);

        while (cur.moveToNext()) {
            String name =cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactList.add(name + " - " + phoneNumber);
        }
    }

    private void updateContacts() {
        ArrayAdapter<String> friendArrayAdapter = new ArrayAdapter<String>(ContactsActivity.this, android.R.layout.simple_list_item_1, contactList);
        contactListView.setAdapter(friendArrayAdapter);
    }
}

