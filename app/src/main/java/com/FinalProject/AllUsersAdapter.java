package com.FinalProject;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

public class AllUsersAdapter extends BaseAdapter {
    private Activity context;
    private List<mUser> usersList;

    public AllUsersAdapter(Activity context, List<mUser> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public Object getItem(int position) {
        return usersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        mUser user = (mUser) getItem(position);

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.user_list_item, null);
        }

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, mUserActivity.class);
                String json = (new Gson()).toJson(user);
                intent.putExtra("edit_user", json);
                context.startActivity(intent);
            }
        });

        String nameTxt = user.getFirstName() + ' ' + user.getLastName();
        ((TextView) convertView.findViewById(R.id.name)).setText(nameTxt);
        ((TextView) convertView.findViewById(R.id.phone)).setText(user.getPhone());

        return convertView;
    }
}
