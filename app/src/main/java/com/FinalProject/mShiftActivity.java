package com.FinalProject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class mShiftActivity extends AppCompatActivity {

    private TimeEditText startTime, endTime;
    private String date;
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private SmsManager smsManager = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shift);

        // bind views
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        Button btnSave = findViewById(R.id.btnSaveShift);
        Button btnDelete = findViewById(R.id.btnDeleteShift);

        ImageView btnHome = findViewById(R.id.btnHome).findViewById(R.id.home);
        TextView title = findViewById(R.id.shiftTitle).findViewById(R.id.titleTxt);

        SharedPreferences sp = getSharedPreferences("sp", 0);
        date = sp.getString("date", "");
        if (date.equals("")) return;

        String msgTitle = "Shift Info - " +
                date.substring(6) + "/" +
                date.substring(4, 6) + "/" +
                date.substring(0, 4);
        title.setText(msgTitle);

        // get extra data from intent
        mShift shift = (new Gson())
                        .fromJson(getIntent()
                        .getStringExtra("edit_shift"), mShift.class);
        if (shift == null) {
            // no extra data is passed
            // new shift is to be created
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "Shift must be created first", Toast.LENGTH_SHORT).show();
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        mShift _shift = constructShiftObj();
                        create(_shift);
                }
            });
        }
        else{
            // extra data is present
            // shift is to be edited

            // populate text fields in GUI with current shift's data
            populateFields(shift);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mShiftActivity.this, R.style.AlertDialogCustom);
                    builder.setMessage("Delete Shift?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int i) {
                                    delete(shift);
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
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        mShift _shift = constructShiftObj();
                        _shift.setKey(shift.getKey());
                        update(_shift);
                }
            });
        }

        //move to Calendar Activity
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mShiftActivity.this, CalendarActivity.class);
                mShiftActivity.this.startActivity(intent);
            }
        });
    }

    private void update(mShift _shift) {
        mDatabase.child("shift-assign").child(_shift.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        HashMap<String, Object> pathsToUpdate = new HashMap<>();
                        pathsToUpdate.put("shifts/"+ _shift.getKey(), _shift);

                        if(dataSnapshot.exists()){
                            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                pathsToUpdate.put("user-assign/" + ds.getKey()+"/"+_shift.getKey(), _shift);
                                sendSMS(_shift);
                            }
                        }
                        handleSimultaneousOp(pathsToUpdate, "Shift was updated successfully","Could not update in database" );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void create(mShift _shift) {
        mDatabase.child("shifts").push().setValue(_shift, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError == null){
                    Toast.makeText(getApplicationContext(), "Shift was created successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mShiftActivity.this, DateActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Could not push to database", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private mShift constructShiftObj(){
        mShift shift = new mShift();
        shift.setStartTime(startTime.getText().toString());
        shift.setEndTime(endTime.getText().toString());
        shift.setDate(date);
        return shift;
    }

    private void populateFields(mShift _shift) {
        startTime.setText(_shift.getStartTime());
        endTime.setText(_shift.getEndTime());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(mShiftActivity.this, DateActivity.class);
        startActivity(intent);
    }

    private void sendSMS(mShift _shift){
        mDatabase.child("shift-assign").child(_shift.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.getKey()!=null){
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                if(ds.exists() && ds.getKey()!=null){
                                    String phone = ds.child("phone").getValue(String.class);
                                    if(phone != null)
                                        smsManager.sendTextMessage("+972" + phone.substring(1),
                                                null,
                                                "Shift in date:" +
                                                        _shift.getDate().substring(6)+"/"+
                                                        _shift.getDate().substring(4,6)+"/"+
                                                        _shift.getDate().substring(0,4)+" has changed",
                                                null, null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void delete(mShift _shift){
        mDatabase.child("shift-assign").child(_shift.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChildren()) {
                            mDatabase.child("shifts").child(_shift.getKey()).setValue(null, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if(databaseError == null){
                                        Toast.makeText(getApplicationContext(), "Shift deleted successfully", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(mShiftActivity.this, DateActivity.class);
                                        startActivity(intent);
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Could not delete in database", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Cannot delete an occupied shift", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void handleSimultaneousOp(Map<String, Object> paths, String successMsg, String errMsg){
        mDatabase.updateChildren(paths, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(getApplicationContext(), successMsg, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mShiftActivity.this, ListUsersActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}



