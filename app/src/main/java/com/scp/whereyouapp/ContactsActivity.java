package com.scp.whereyouapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContactsActivity extends AppCompatActivity {
    private ListView contactListView;
    private ArrayList<String> contactList = new ArrayList<String>();
    private Map<String, String> selectedContactList = new HashMap<String,String>();
    private int selPos = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        contactListView = (ListView) findViewById(R.id.contactListView);
        readContacts();
        updateContacts();

        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String value = contactList.get(position);
                selPos = position;
                selectedContactList.put(value.substring(value.indexOf(" - ") + 3, value.length()), value.substring(0, value.indexOf(" - ")));
                updateContacts();
            }
        });
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
        ArrayAdapter<String> friendArrayAdapter = new ArrayAdapter<String>(ContactsActivity.this, android.R.layout.simple_list_item_1, contactList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView item = (TextView) view.findViewById(android.R.id.text1);
                String value = item.getText().toString();
                Log.e("contacts", value.substring(0, value.indexOf(" - ")));
                Log.e("contacts", String.valueOf(selectedContactList.containsValue(value.substring(0, value.indexOf(" - ")))));
                if (selectedContactList.containsValue(value.substring(0, value.indexOf(" - ")))) {
                    Log.e("Contacting: ", value.substring(0, value.indexOf(" - ")));
                    item.setTextColor(Color.RED);
                }
/*                if (position == selPos) {

                    // set your color

                }*/
                contactListView.smoothScrollToPosition(selPos);

                return view;
            }
        };
        contactListView.setAdapter(friendArrayAdapter);
    }

    public void finishContacts(View view) {
        Intent intent = new Intent(ContactsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("contactNumbers", Arrays.copyOf(selectedContactList.values().toArray(), selectedContactList.size(), String[].class));
        startActivity(intent);
    }
}

