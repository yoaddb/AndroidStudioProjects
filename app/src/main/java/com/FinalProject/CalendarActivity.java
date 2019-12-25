package com.FinalProject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    //define format of YYYYMMDD
    private final DateTimeFormatter dtf = DateTimeFormatter.BASIC_ISO_DATE;
    private CalendarView calendarView;
    private TextView displaySalary;
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private SharedPreferences sp;
    private mUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // bind views
        calendarView = findViewById(R.id.calendarView);
        ImageView logout = findViewById(R.id.logout);
        ImageView config = findViewById(R.id.config);
        TextView title = findViewById(R.id.header).findViewById(R.id.title).findViewById(R.id.titleTxt);
        displaySalary = findViewById(R.id.salary);
        TextView salaryDesc = findViewById(R.id.salaryDesc);


        // get the shared preferences
        sp = getSharedPreferences("sp", 0);
        String json = sp.getString("userObject", "");
        if(json.equals("")) return;
        user = (new Gson()).fromJson(json, mUser.class);

        //display name of user in title
        String titleMsg = "Welcome " + user.getFirstName();
        title.setText(titleMsg);

        // hide icon if not admin
        if(!user.getId().equals("99999")){
            displayInfo();
            // set hidden visibility
            config.setVisibility(View.INVISIBLE);
            //detach listener
            config.setOnClickListener(null);
            // listen fot user changes
            ListenForChanges();
        }
        else {
            salaryDesc.setText(null);
            displaySalary.setVisibility(View.GONE);
            config.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CalendarActivity.this, ConfigActivity.class);
                    startActivity(intent);
                }
            });
        }

        //move to Login Activity
        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openAlert();
            }
        });

        //move to Date Activity
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Intent intent = new Intent(CalendarActivity.this, DateActivity.class);
                SharedPreferences.Editor editor = sp.edit();

                // uses modern classes via java.time (java 8+)
                // requires API 26 and above
                //convert from Calendar to LocalDate and format to string
                String date = ((GregorianCalendar) eventDay.getCalendar()).toZonedDateTime().toLocalDate().format(dtf);
                editor.putString("date", date);
                editor.apply();
                CalendarActivity.this.startActivity(intent);
            }
        });

        //Listener for Previous months
        calendarView.setOnForwardPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                if (!user.getId().equals("99999")) {
                    displayInfo();
                }
            }
        });

        //Listener for Forward months
        calendarView.setOnPreviousPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                if (!user.getId().equals("99999")) {
                    displayInfo();
                }
            }
        });

        displayInfo();
    }

    private void ListenForChanges() {
        mDatabase.child("users").child(user.getId()).child("wage")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            user.setWage(dataSnapshot.getValue(Float.class));
                            displayInfo();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void displayInfo() {
        Calendar calendarShown = calendarView.getCurrentPageDate();
        LocalDate localDate = ((GregorianCalendar)calendarShown).toZonedDateTime().toLocalDate();
        String startOfMonth = localDate.format(dtf);
        String endOfMonth = localDate.minusMonths(-1).minusDays(1).format(dtf);

        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalWorkingMinutes = 0;
                if (dataSnapshot.exists()) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        mShift shift = ds.getValue(mShift.class);
                        if(shift != null){
                            LocalTime start = LocalTime.of(
                                    Integer.parseInt(shift.getStartTime().substring(0,2)),
                                    Integer.parseInt(shift.getStartTime().substring(3)));

                            LocalTime end = LocalTime.of(
                                    Integer.parseInt(shift.getEndTime().substring(0,2)),
                                    Integer.parseInt(shift.getEndTime().substring(3)));

                            // returns minutes between start and end
                            int minutes = (int)ChronoUnit.MINUTES.between(start, end);
                            totalWorkingMinutes += minutes;
                        }
                    }

                    float totalWorkingHours = totalWorkingMinutes / 60.0f;
                    float totalSalaryInMonth = totalWorkingHours * user.getWage();
                    String sal = totalSalaryInMonth + "₪";
                    displaySalary.setText(sal);

                }else{
                    displaySalary.setText("0₪");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        mDatabase.child("user-assign").child(user.getId())
                .orderByChild("date").startAt(startOfMonth).endAt(endOfMonth)
                .addValueEventListener(vel);



    }


    @Override
    public void onBackPressed() {
       openAlert();
    }

    private void openAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this, R.style.AlertDialogCustom);
        builder.setMessage("Do you wish to log out?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        Intent intent = new Intent(CalendarActivity.this, LoginActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "Logged off successfully", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onResume() {
        super.onResume();
        displayInfo();
    }
}
