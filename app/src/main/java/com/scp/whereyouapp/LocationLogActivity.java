package com.scp.whereyouapp;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class LocationLogActivity extends ActionBarActivity {
    private int[] colors = new int[] { 0x30FF0000, 0x300000FF };
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private final String[] osArray = { "Home", "Friends", "Trips", "Location Log", "Settings" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_log);
        getWindow().getDecorView().setBackgroundColor(Color.LTGRAY);

        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        addDrawerItems();

        ArrayList<String> numList = getListedLog();
        //String[] examples = {"a", "Dsadsa" ,"DAfs", "trgfdgfhgfhfgjsdf23f43g54g45b45454g5"};
        ArrayAdapter<String> exampleArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, numList);
        ListView exampleList = (ListView)findViewById(R.id.logListView);
        exampleList.setAdapter(exampleArrayAdapter);
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent){
//        View view = super.getView(position, convertView, parent);
//        int colorPos = position % colors.length;
//        view.setBackgroundColor(colors[colorPos]);
//        return view;
//    }

    private ArrayList<String> getListedLog(){
        ArrayList<String> ret = new ArrayList<String>();
        SharedPreferences sp = getSharedPreferences("notificationLog", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        boolean exists = sp.contains("texted_numbers");
        String current_numbers = null;
        if (exists){
            current_numbers = sp.getString("texted_numbers", current_numbers);
        }
        else{
            return ret;
        }
        for (String s: current_numbers.split("Number: ")){
            String numAndTime = null;
            if (s.length() == 0){
                continue;
            }
            for (String n: s.split("Time: ")){
                if (numAndTime == null){
                    numAndTime = n;
                }
                else{
                    numAndTime = numAndTime + " at " + n;
                }
            }
            ret.add(numAndTime);
        }
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location_log, menu);
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

    public void addDrawerItems() {
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = osArray[(int) id];
                if (value == "Settings"){
                    Intent intent = new Intent(LocationLogActivity.this, SettingsActivity.class);
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else if (value == "Home"){
                    Intent intent = new Intent(LocationLogActivity.this, MainActivity.class);
                    //Context packageContext
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else if (value == "Location Log"){
                    mDrawerLayout.closeDrawers();
                }
                else if (value == "Friends"){
                    Intent intent = new Intent(LocationLogActivity.this, FriendsActivity.class);
                    //Context packageContext
                    mDrawerLayout.closeDrawers();
                    startActivity(intent);
                }
                else{
                    Toast.makeText(LocationLogActivity.this, osArray[(int) id], Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}