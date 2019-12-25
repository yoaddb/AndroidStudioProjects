package com.FinalProject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class UsersAssignedAdapter extends BaseAdapter {

    private Activity context;
    private List<mUser> userList;

    public UsersAssignedAdapter(Activity context, List<mUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
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

        String nameTxt = user.getFirstName() + ' ' + user.getLastName();
        ((TextView) convertView.findViewById(R.id.name)).setText(nameTxt);
        ((TextView) convertView.findViewById(R.id.phone)).setText(user.getPhone());

        ((ImageView) convertView.findViewById(R.id.next)).setImageDrawable(null);

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
