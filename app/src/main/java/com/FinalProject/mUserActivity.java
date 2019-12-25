package com.FinalProject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class mUserActivity extends AppCompatActivity {
    private final DateTimeFormatter dtf = DateTimeFormatter.BASIC_ISO_DATE; //yyyymmdd
    private EditText firstName, lastName, id, email, password, phone, wage;
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private SmsManager smsManager = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // bind views
        ImageView btnHome = findViewById(R.id.btnHome).findViewById(R.id.home);
        Button btnSave = findViewById(R.id.btnSaveUser);
        Button btnDelete = findViewById(R.id.btnDeleteUser);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        id = findViewById(R.id.id);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phone);
        wage = findViewById(R.id.userwage);
        ImageView image = findViewById(R.id.img);
        TextView title = findViewById(R.id.userTitle).findViewById(R.id.titleTxt);
        String msg = "User Info";
        title.setText(msg);

        //move to Calendar Activity
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mUserActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        // get extra data from intent
        mUser user = (new Gson())
                .fromJson(getIntent()
                        .getStringExtra("edit_user"), mUser.class);
        if (user == null) {
            // no extra data is passed
            // new user is to be created
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "User must be created first", Toast.LENGTH_SHORT).show();
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // check for client side validation
                    if (validateData()) {
                        mUser _user = constructUserObj();
                        create(_user);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            // extra data is present
            // user is to be edited

            // populate text fields in GUI with current User's data
            populateFields(user);
            id.setInputType(InputType.TYPE_NULL);
            id.setFocusable(false);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mUserActivity.this, R.style.AlertDialogCustom);
                    builder.setMessage("Delete User?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int i) {
                                    delete(user);
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
                    // check for client side validation
                    if (validateData()) {
                        mUser _user = constructUserObj();
                        update(_user);
                    }
                }
            });
        }
    }

    private void delete(mUser _user) {
        String today = LocalDateTime.now().toLocalDate().format(dtf);

        mDatabase.child("user-assign").child(_user.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Map<String, Object> pathsToDelete = new HashMap<>();
                        pathsToDelete.put("users/" + _user.getId(), null);

                        if(!dataSnapshot.exists()) {
                            handleSimultaneousOp(pathsToDelete, "User was deleted successfully",
                                    "Could not delete in database");
                            return;
                        }
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            mShift shift = ds.getValue(mShift.class);
                            // delete only shifts assigned to user
                            // that are ahead of today
                            // leave past shifts intact
                            if (shift != null && shift.getDate().compareTo(today) >= 0) {
                                pathsToDelete.put("user-assign/" + _user.getId() + "/" + shift.getKey(), null);
                                pathsToDelete.put("shift-assign/" + shift.getKey() + "/" + _user.getId(), null);
                            }
                        }
                        handleSimultaneousOp(pathsToDelete,
                                "User was deleted successfully",
                                "Could not delete in database",
                                _user.getId(),
                                "Your account has been deleted" );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void update(mUser _user) {
        mDatabase.child("user-assign").child(_user.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Map<String, Object> pathsToUpdate = new HashMap<>();
                        pathsToUpdate.put("users/" + _user.getId(), _user);

                        if(dataSnapshot.exists()){
                            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                pathsToUpdate.put("shift-assign/" + ds.getKey()+"/"+_user.getId(), _user);
                            }
                        }
                        handleSimultaneousOp(pathsToUpdate,
                                "User was updated successfully",
                                "Could not update in database",
                                _user.getId(),
                                "Your info has been updated" );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
    }

    private void create(mUser _user) {
        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // user does not exist in database
                if(!dataSnapshot.hasChild(_user.getId())){
                    Map<String, Object> pathsToCreate = new HashMap<>();
                    pathsToCreate.put("users/" + _user.getId(), _user);
                    handleSimultaneousOp(pathsToCreate,
                            "User was created successfully",
                            "Could not push to database",
                            _user.getId(),
                            "Your account has been created");
                }
                else{
                    Toast.makeText(getApplicationContext(), "User with same ID exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void sendSMS(String userId, String message) {
        mDatabase.child("users").child(userId).child("phone")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getKey() != null) {
                            String phone = dataSnapshot.getValue(String.class);
                            smsManager.sendTextMessage("+972" + phone.substring(1), null, message, null, null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private mUser constructUserObj(){
        mUser user = new mUser();
        user.setId(id.getText().toString());
        user.setEmail(email.getText().toString());
        user.setFirstName(firstName.getText().toString());
        user.setLastName(lastName.getText().toString());
        user.setPassword(password.getText().toString());
        user.setPhone(phone.getText().toString());
        user.setWage(Float.parseFloat(wage.getText().toString()));
        //TODO add image in mUser model
        return user;
    }

    private void populateFields(mUser _user) {
        firstName.setText(_user.getFirstName());
        lastName.setText(_user.getLastName());
        id.setText(_user.getId());
        email.setText(_user.getEmail());
        password.setText(_user.getPassword());
        phone.setText(_user.getPhone());
        wage.setText(_user.getWage().toString());
    }

    // Client-Side validation
    private boolean validateData() {
        if (firstName.getText().toString().isEmpty()) {
            firstName.setError("First Name is required");
            return false;
        }else if (lastName.getText().toString().isEmpty()) {
            lastName.setError("Last Name is required");
            return false;
        } else if (id.getText().toString().isEmpty()) {
            id.setError("ID is required");
            return false;
        } else if (id.getText().toString().length() != 9) {
            id.setError("ID must be 9 digits long");
            return false;
        } else if (password.getText().toString().isEmpty()) {
            password.setError("Password is required");
            return false;
        } else if (phone.getText().toString().isEmpty()) {
            phone.setError("Phone is required");
            return false;
        } else if (!phone.getText().toString().startsWith("05")
                    || phone.getText().toString().length() != 10) {
            phone.setError("Phone must be valid (05xxxxxxxx)");
            return false;
        } else if (Float.parseFloat(wage.getText().toString())<=24) {
            wage.setError("Minimum wage is required - 24₪");
            return false;
        } else if (email.getText().toString().isEmpty()) {
            email.setError("Email is required");
            // TODO add validation for email (regex)
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(mUserActivity.this, ListUsersActivity.class);
        startActivity(intent);
    }

    private void handleSimultaneousOp(Map<String, Object> paths, String successMsg, String errMsg){
        mDatabase.updateChildren(paths, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(getApplicationContext(), successMsg, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mUserActivity.this, ListUsersActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleSimultaneousOp(Map<String, Object> paths, String successMsg, String errMsg, String id, String sms){
        mDatabase.updateChildren(paths, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(getApplicationContext(), successMsg, Toast.LENGTH_SHORT).show();
                    sendSMS(id, sms);
                    Intent intent = new Intent(mUserActivity.this, ListUsersActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
