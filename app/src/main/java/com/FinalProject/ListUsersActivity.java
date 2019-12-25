package com.FinalProject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

public class ListUsersActivity extends AppCompatActivity {

    private AllUsersAdapter adapter;
    private List<mUser> userList = new ArrayList<>();
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);

        //bind views
        ImageView btnHome = findViewById(R.id.btnHome).findViewById(R.id.home);

        Button btnAddUser = findViewById(R.id.btnAddUser);
        String msgBtn = "Add New User";
        btnAddUser.setText(msgBtn);

        TextView usersTitle = findViewById(R.id.usersTitle);
        String msgTitle = "Users";
        usersTitle.setText(msgTitle);

        ListView userLv = findViewById(R.id.userLv);
        adapter = new AllUsersAdapter(ListUsersActivity.this, userList);
        userLv.setAdapter(adapter);

        //move to Calendar Activity
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListUsersActivity.this, CalendarActivity.class);
                ListUsersActivity.this.startActivity(intent);
            }
        });

        //move to User Activity
        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListUsersActivity.this, mUserActivity.class);
                ListUsersActivity.this.startActivity(intent);
            }
        });

        ListenForUsersChanges();
    }

    private void ListenForUsersChanges() {
        mDatabase.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                    mUser user = dataSnapshot.getValue(mUser.class);
                    if (user != null && !user.getId().equals("99999")) {
                        userList.add(user);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                    mUser user = dataSnapshot.getValue(mUser.class);
                    if(user!=null && userList.contains(user)) {
                        int index = userList.indexOf(user);
                        userList.set(index, user);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                    mUser user = dataSnapshot.getValue(mUser.class);
                    if (user != null){
                        userList.remove(user);
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
        Intent intent = new Intent(ListUsersActivity.this, ConfigActivity.class);
        startActivity(intent);
    }
}
