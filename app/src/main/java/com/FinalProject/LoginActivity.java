package com.FinalProject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;


public class LoginActivity extends AppCompatActivity {

    private Button login;
    private ProgressBar progressBar;
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private int backButtonCount =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // bind views
        EditText id = findViewById(R.id.txtId);
        EditText password = findViewById(R.id.txtPassword);
        progressBar = findViewById(R.id.loadingProgress);
        login = findViewById(R.id.btnLogin);
        String btnText = "Login";
        login.setText(btnText);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set hidden visibility
                v.setVisibility(View.GONE);
                // show progress bar to simulate work in background
                progressBar.setVisibility(View.VISIBLE);

                mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //user id and password match
                            if (dataSnapshot.hasChild(id.getText().toString())) {
                                mUser user = dataSnapshot.child(id.getText().toString()).getValue(mUser.class);
                                if (user != null && password.getText().toString().equals(user.getPassword())) {
                                    Intent intent = new Intent(LoginActivity.this, CalendarActivity.class);
                                    SharedPreferences sp = getSharedPreferences("sp", 0);
                                    SharedPreferences.Editor editor = sp.edit();
                                    String json = (new Gson()).toJson(user);
                                    editor.putString("userObject", json);
                                    editor.apply();
                                    startActivity(intent);
                                    Toast.makeText(getApplicationContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    login.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), "Incorrect ID / Password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                                login.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "Incorrect ID / Password", Toast.LENGTH_SHORT).show();
                            }
                        }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        });
    }

    //when user logs off via 'back' button
    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        login.setVisibility(View.VISIBLE);
    }

    // prevent relogging using 'back' button
    //@Override
    //public void onBackPressed() { }

    @Override
    public void onBackPressed()
    {
        if(backButtonCount >= 1)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }
}

