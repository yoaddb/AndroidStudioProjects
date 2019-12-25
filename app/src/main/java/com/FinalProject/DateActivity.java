package com.FinalProject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class DateActivity extends AppCompatActivity {

    private ShiftListAdapter adapter;
    private List<mShift> shiftList = new ArrayList<>();
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private String date;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);

        sp = getSharedPreferences("sp", 0);
        String json = sp.getString("userObject", "");
        if(json.equals("")) return;
        mUser user = (new Gson()).fromJson(json, mUser.class);

        date = sp.getString("date", "");
        if (date.equals("")) return;

        //bind views
        Button btnAddShift = findViewById(R.id.btnAddShift);
        String btn = "Add New Shift";
        btnAddShift.setText(btn);

        // hide button if not Admin
        if (!user.getId().equals("99999")) {
            btnAddShift.setVisibility(View.INVISIBLE);
            btnAddShift.setOnClickListener(null);
        }
        else {
            btnAddShift.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // move to mShiftActivity
                    Intent intent = new Intent(DateActivity.this, mShiftActivity.class);
                    DateActivity.this.startActivity(intent);
                }
            });
        }

        ImageView btnHome = findViewById(R.id.btnHome).findViewById(R.id.home);

        TextView title = findViewById(R.id.dateTitle).findViewById(R.id.titleTxt);
        String msgTitle = "Shifts for date - " +
                date.substring(6) + "/" +
                date.substring(4, 6) + "/" +
                date.substring(0, 4);
        title.setText(msgTitle);

        //move to Calendar Activity
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DateActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = findViewById(R.id.shiftList);
        adapter = new ShiftListAdapter(DateActivity.this, shiftList, user.getId());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long i) {
                Intent intent = new Intent(DateActivity.this, UsersAssignedActivity.class);

                SharedPreferences.Editor editor = sp.edit();
                mShift shift = shiftList.get(position);
                String json = (new Gson()).toJson(shift);
                editor.putString("shift", json);
                editor.apply();
                startActivity(intent);
            }
        });

        ListenForShiftChanges();

    }

    private void ListenForShiftChanges() {
        mDatabase.child("shifts")
                .orderByChild("date")
                .equalTo(date)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                            mShift shift = dataSnapshot.getValue(mShift.class);
                            if (shift != null) {
                                shift.setKey(dataSnapshot.getKey());
                                shiftList.add(shift);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String prevChild) {
                        if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                            mShift shift = dataSnapshot.getValue(mShift.class);
                            if(shift != null) {
                                shift.setKey(dataSnapshot.getKey());
                                if( shiftList.contains(shift)){
                                    int index = shiftList.indexOf(shift);
                                    shiftList.set(index, shift);
                                }
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                            mShift shift = dataSnapshot.getValue(mShift.class);
                            if(shift != null) {
                                shift.setKey(dataSnapshot.getKey());
                                shiftList.remove(shift);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DateActivity.this, CalendarActivity.class);
        startActivity(intent);
    }
}


