package com.scp.whereyouapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christine on 7/24/2015.
 */
public class FriendsAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<String> list = new ArrayList<String>();
    private List<String> requestList = new ArrayList<String>();
    private Context context;
    private Firebase firebaseRef;


    public FriendsAdapter(ArrayList<String> list, Context context, List<String> requestList) {
        this.list = list;
        this.context = context;
        firebaseRef = new Firebase("https://whereyouapp.firebaseio.com/");
        this.requestList = requestList;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
//        return list.get(pos).getId();
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.friends_item, null);
        }

        //Handle TextView and display string from your list
        TextView item = (TextView)view.findViewById(R.id.list_item_string);
        item.setText(list.get(position));
        Log.e("friends", item.getText().toString());
        Log.e("friends", String.valueOf(requestList.contains(item.getText().toString())));
        if (requestList.contains(list.get(position))) {
            item.setTextColor(Color.RED);
        }

        //Handle buttons and add onClickListeners
        final Button revokeBtn = (Button)view.findViewById(R.id.revoke_btn);
        final Button addBtn = (Button)view.findViewById(R.id.add_btn);

        revokeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addBtn.setVisibility(View.INVISIBLE);
            }
        });

        return view;
    }
}