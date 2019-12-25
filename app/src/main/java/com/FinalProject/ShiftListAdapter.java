package com.FinalProject;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

public class ShiftListAdapter extends BaseAdapter {

    private Activity context;
    private List<mShift> shiftList;
    private String userId;

    public ShiftListAdapter(Activity context, List<mShift> shiftList, String userId) {
        this.context = context;
        this.shiftList = shiftList;
        this.userId = userId;
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
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.shift_list_item, null);
        }

        TextView shiftHours = convertView.findViewById(R.id.shiftHours);
        String hrs = "Hours: " + shift.getStartTime()+ " ~ " + shift.getEndTime();
        shiftHours.setText(hrs);

        if(userId.equals("99999")){
            convertView.findViewById(R.id.editShift).setVisibility(View.VISIBLE);
            (convertView.findViewById(R.id.editShift)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, mShiftActivity.class);
                    String json = (new Gson()).toJson(shift);
                    intent.putExtra("edit_shift", json);
                    context.startActivity(intent);
                }
            });
        }else{
            convertView.findViewById(R.id.editShift).setVisibility(View.GONE);
            (convertView.findViewById(R.id.editShift)).setOnClickListener(null);
        }


        return convertView;

    }
}
