package com.FinalProject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibotta.android.support.pickerdialogs.SupportedDatePickerDialog;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SalaryActivity extends AppCompatActivity {

    //define format of YYYYMMDD
    private final DateTimeFormatter dtf = DateTimeFormatter.BASIC_ISO_DATE;
    private CustomSpinnerAdapter spinnerAdapter;
    private List<mUser> userList = new ArrayList<>();
    private List<mShift> shiftList = new ArrayList<>();
    private TextView textView;
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private TextView displayedSalary;
    private ChosenUserShiftsAdapter shiftsAdapter;
    private mUser chosenUser;
    private String queryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);

        // bind views
        TextView title = findViewById(R.id.salTitle).findViewById(R.id.titleTxt);
        String titleMsg = "View Salaries and Shifts";
        title.setText(titleMsg);

        Spinner spinner = findViewById(R.id.spinner);
        spinnerAdapter = new CustomSpinnerAdapter(SalaryActivity.this, userList);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                chosenUser = userList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        displayedSalary = findViewById(R.id.userSalary);

        ListView lvShifts = findViewById(R.id.lvShifts);
        shiftsAdapter = new ChosenUserShiftsAdapter(SalaryActivity.this, shiftList);
        lvShifts.setAdapter(shiftsAdapter);

        // move to CalendarActivity
        ImageView btnHome = findViewById(R.id.btnHome).findViewById(R.id.home);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SalaryActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        populateUsersDropdown();

        textView = findViewById(R.id.txtMonthYear);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                SupportedDatePickerDialog dpd = new SupportedDatePickerDialog(SalaryActivity.this, R.style.SpinnerDatePickerDialogTheme, new SupportedDatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(@NonNull DatePicker datePicker, int year, int month, int day) {
                        String formatted = LocalDate.of(year, month + 1, day).format(dtf);
                        queryDate = formatted.substring(0, 6);
                        String txt = queryDate.substring(4, 6) + "/" + queryDate.substring(0, 4);
                        textView.setText(txt);
                    }
                }, mYear, mMonth, mDay);
                // hide day in datepicker
                dpd.getDatePicker().findViewById(getResources().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
                dpd.show();

            }
        });

        Button btn = findViewById(R.id.btnView);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (queryDate == null) {
                    Toast.makeText(getApplicationContext(), "Please choose a date", Toast.LENGTH_SHORT).show();
                    return;
                }
                displayedSalary.setVisibility(View.GONE);
                shiftList.clear();
                shiftsAdapter.notifyDataSetChanged();
                displayInfo();
            }
        });
    }

    private void displayInfo() {
        if(chosenUser == null || queryDate == null) {
            displayedSalary.setText("");
            return;
        }
        mDatabase.child("user-assign").child(chosenUser.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int totalWorkingMinutes = 0;
                            if(dataSnapshot.exists()){
                                List<mShift> shifts = new ArrayList<>();
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    mShift shift = ds.getValue(mShift.class);
                                    if(shift != null && queryDate != null &&
                                            shift.getDate().contains(queryDate)){
                                        shifts.add(shift);

                                        LocalTime start = LocalTime.of(
                                                Integer.parseInt(shift.getStartTime().substring(0,2)),
                                                Integer.parseInt(shift.getStartTime().substring(3)));

                                        LocalTime end = LocalTime.of(
                                                Integer.parseInt(shift.getEndTime().substring(0,2)),
                                                Integer.parseInt(shift.getEndTime().substring(3)));

                                        // returns minutes between start and end
                                        int minutes = (int) ChronoUnit.MINUTES.between(start, end);
                                        totalWorkingMinutes += minutes;
                                    }
                                }
                                shiftList.clear();
                                shiftList.addAll(shifts);
                                shiftsAdapter.notifyDataSetChanged();

                                float totalWorkingHours = totalWorkingMinutes / 60.0f;
                                float totalSalaryInMonth = totalWorkingHours * chosenUser.getWage();
                                String sal = totalSalaryInMonth + "₪";
                                displayedSalary.setVisibility(View.VISIBLE);
                                displayedSalary.setText(sal);
                            }
                        }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }


    private void populateUsersDropdown() {
        mDatabase.child("users")
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                    mUser user = dataSnapshot.getValue(mUser.class);
                    if (user != null && !user.getId().equals("99999")) {
                        userList.add(user);
                        spinnerAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                            userList.removeIf(u -> u.getId().equals(dataSnapshot.getKey()));
                            spinnerAdapter.notifyDataSetChanged();
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SalaryActivity.this, ConfigActivity.class);
        startActivity(intent);
    }
}
