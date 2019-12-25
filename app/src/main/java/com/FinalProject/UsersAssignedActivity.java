package com.FinalProject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersAssignedActivity extends AppCompatActivity {

    private final DateTimeFormatter dtf = DateTimeFormatter.BASIC_ISO_DATE; //yyyymmdd
    private List<mUser> userList = new ArrayList<>();
    private UsersAssignedAdapter adapter;
    private String date;
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private mUser user;
    private mShift shift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_assigned);

        SharedPreferences sp = getSharedPreferences("sp", 0);
        date = sp.getString("date", "");
        if(date.equals("")) return;

        String json = sp.getString("userObject", "");
        if(json.equals("")) return;
        user = (new Gson()).fromJson(json, mUser.class);

        String json2 = sp.getString("shift", "");
        if(json2.equals("")) return;
        shift = (new Gson()).fromJson(json2, mShift.class);


        TextView dateView = findViewById(R.id.dateTitle).findViewById(R.id.titleTxt);
        String msgTitle = date.substring(6) + "/" +
                date.substring(4, 6) + "/" +
                date.substring(0, 4);
        dateView.setText(msgTitle);

        TextView shiftInfo = findViewById(R.id.shiftInfo);
        String info = "Hours: " + shift.getStartTime() + " ~ " + shift.getEndTime();
        shiftInfo.setText(info);

        ImageView btnHome = findViewById(R.id.btnHome).findViewById(R.id.home);
        //move to Calendar Activity
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UsersAssignedActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton fabAdd = findViewById(R.id.fab);
        FloatingActionButton fabDelete = findViewById(R.id.fab2);

        ListView lv = findViewById(R.id.users);
        adapter = new UsersAssignedAdapter(UsersAssignedActivity.this, userList);
        lv.setAdapter(adapter);

        fabAdd.setImageResource(R.drawable.add);
        fabDelete.setImageResource(R.drawable.delete);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assign();
            }

        });

        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remove();
            }
        });

        ListenForUserChanges();
    }

    private void remove() {
        if (!userList.contains(user)) {
            Toast.makeText(UsersAssignedActivity.this, "You are not assigned to this shift.", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(UsersAssignedActivity.this, R.style.AlertDialogCustom);
        builder.setMessage("Do you wish to remove yourself from this shift?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        Map<String, Object> pathsToDelete = new HashMap<>();
                        pathsToDelete.put("shift-assign/" + shift.getKey() + "/" + user.getId(), null);
                        pathsToDelete.put("user-assign/" + user.getId() + "/" + shift.getKey(), null);
                        mDatabase.updateChildren(pathsToDelete);
                    }
                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void assign() {
        // user cannot be assigned to shifts whose date < today
        if (date.compareTo(LocalDateTime.now().toLocalDate().format(dtf)) < 0) {
            Toast.makeText(getApplicationContext(), "Cannot assign to prior dates", Toast.LENGTH_SHORT).show();
            return;
        }

        // user already assigned to this shift
        if (user != null && userList.contains(user)) {
            Toast.makeText(getApplicationContext(), "You are already assigned to this shift", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> pathsToUpdate = new HashMap<>();
        pathsToUpdate.put("shift-assign/" + shift.getKey() + "/" + user.getId(), user);
        pathsToUpdate.put("user-assign/" + user.getId() + "/" + shift.getKey(), shift );
        mDatabase.updateChildren(pathsToUpdate);
    }

    private void ListenForUserChanges() {
        mDatabase.child("shift-assign").child(shift.getKey())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                            mUser userToGUI =dataSnapshot.getValue(mUser.class);
                            if(userToGUI!=null) AddUserToGUI(userToGUI);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                            mUser userToGUI = dataSnapshot.getValue(mUser.class);
                            if (userToGUI != null) RemoveUserFromGUI(userToGUI); ;
                        }
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void RemoveUserFromGUI(mUser _user) {
        userList.remove(_user);
        adapter.notifyDataSetChanged();
        if (!userList.contains(_user) && _user.equals(user))
            Toast.makeText(UsersAssignedActivity.this, "You have been removed from this shift.", Toast.LENGTH_SHORT).show();
    }

    private void AddUserToGUI(mUser _user) {
        userList.add(_user);
        adapter.notifyDataSetChanged();
        if (userList.contains(_user) && _user.equals(user))
            Toast.makeText(getApplicationContext(), "Successfully assigned to shift", Toast.LENGTH_SHORT).show();
    }

    public void onBackPressed() {
        Intent intent = new Intent(UsersAssignedActivity.this, DateActivity.class);
        startActivity(intent);
    }

}

