package com.FinalProject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // bind views
        Button manageUsers = findViewById(R.id.btnManageUsers);
        String btnStr1 = "Manage Users";
        manageUsers.setText(btnStr1);

        Button viewSalary = findViewById(R.id.btnViewSalary);
        String btnStr2 = "View Salaries & Shifts";
        viewSalary.setText(btnStr2);

        TextView title = findViewById(R.id.configTitle).findViewById(R.id.titleTxt);
        String titleMsg = "Configuration";
        title.setText(titleMsg);

        // move to CalendarActivity
        ImageView btnHome = findViewById(R.id.btnHome).findViewById(R.id.home);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfigActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        // move to ListUsersActivity
        manageUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfigActivity.this, ListUsersActivity.class);
                startActivity(intent);
            }
        });

        // move to SalaryActivity
        viewSalary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfigActivity.this, SalaryActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ConfigActivity.this, CalendarActivity.class);
        startActivity(intent);
    }
}
