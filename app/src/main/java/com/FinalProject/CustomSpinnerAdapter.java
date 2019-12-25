package com.FinalProject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
    private List<mUser> userList;
    private Activity context;

    public CustomSpinnerAdapter(Activity context, List<mUser> userList) {
        this.userList = userList;
        this.context = context;
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
    public View getView(int position, View convertView, ViewGroup parentView) {
        mUser user = (mUser) getItem(position);

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.spinner_user, null);
        }

        TextView txtName = convertView.findViewById(R.id.txtName);
        String txt = user.getFirstName() + " " + user.getLastName();
        txtName.setText(txt);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        mUser user = (mUser) getItem(position);

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.spiner_dropdown, null);
        }

        TextView txtName = convertView.findViewById(R.id.dropdown);
        String txt = user.getFirstName() + " " + user.getLastName();
        txtName.setText(txt);

        return convertView;
    }
}
