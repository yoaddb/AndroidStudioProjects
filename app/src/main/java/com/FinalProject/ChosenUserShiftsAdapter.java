package com.FinalProject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ChosenUserShiftsAdapter extends BaseAdapter {
    private Activity context;
    private List<mShift> shiftList;

    public ChosenUserShiftsAdapter(Activity context, List<mShift> shiftList) {
        this.context = context;
        this.shiftList = shiftList;
    }

    @Override
    public int getCount() {
        return shiftList.size();
    }

    @Override
    public Object getItem(int position) {
        return shiftList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mShift shift = (mShift) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.shift_list_item, null);
        }

        TextView shiftHours = convertView.findViewById(R.id.shiftHours);
        shiftHours.setTextSize(18);
        String hrs = "Hours: " + shift.getStartTime() + " ~ " + shift.getEndTime() + "\nDate: " + shift.getDate();
        shiftHours.setText(hrs);

        ImageView edit = convertView.findViewById(R.id.editShift);
        edit.setVisibility(View.GONE);
        edit.setOnClickListener(null);

        ImageView next = convertView.findViewById(R.id.next);
        next.setVisibility(View.GONE);
        next.setOnClickListener(null);
        return convertView;
    }
}
