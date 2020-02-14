package com.example.remotevisualassistant;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    private Button b_req, b_device, b_signout, b_edit;
    private CheckBox rv;
    private TextView dev_status, ocr, contacts, logs, staus_heading, dev_hd, settings;
    private EditText dev_ip;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private double curr_lat,curr_lon;
    private boolean loc_set;

    private boolean edit_flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        setup_UI_components();

        loc_set=false;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        b_req.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String my_id = FirebaseAuth.getInstance().getUid();
                DatabaseReference mydbr = FirebaseDatabase.getInstance().getReference("userdetails");
                mydbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final UserDetails ud = dataSnapshot.getValue(UserDetails.class);
                        if(!ud.isSet()){
                            build_an_alert("Request failed", "Please configure the device IP in the section below", "okay");
                        }
                        else{
//                            Toast.makeText(UserActivity.this,"Call started",Toast.LENGTH_SHORT).show();
                            if(loc_set){
                                progressDialog = new ProgressDialog(UserActivity.this);
                                progressDialog.setMessage("Calling...");
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();

                                DatabaseReference locdbr = FirebaseDatabase.getInstance().getReference("last_location");
                                UserLocation ul = new UserLocation(my_id,curr_lat,curr_lon);
                                locdbr.child(my_id).setValue(ul);

                                final String s1 = ud.getName();
                                final String s2 = ud.getNumber();
                                final String vid_url = "http://" + ud.getDevice_ip() + ":8081";
                                if(rv.isChecked()){
                                    //randomly choose a volunteer
                                    final String id_to = "OafXvcb3goRQRZJPL2dQV6AiU0M2";

                                    final DatabaseReference usrdbr = FirebaseDatabase.getInstance().getReference("userdetails");
                                    usrdbr.child(id_to).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            final UserDetails to_ud = dataSnapshot.getValue(UserDetails.class);
                                            make_a_call(my_id, id_to, s1, s2, vid_url, to_ud.getName(),to_ud.getNumber());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                                else{
                                    //cycle through frequent volunteers until someone accepts or timer runs out
                                    DatabaseReference cdbr = FirebaseDatabase.getInstance().getReference("usercontacts");
                                    cdbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            UserContacts uc = dataSnapshot.getValue(UserContacts.class);
                                            final List<String> conlist = uc.getClist();

                                            if (conlist.size() > 0) {
                                                progressDialog.dismiss();
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                                                builder.setTitle("Choose a contact");

                                                String[] dummy = uc.getContactNames().split(";");
                                                final String[] my_contacts = new String[dummy.length-1];
                                                for(int i=0;i<my_contacts.length;i++){
                                                    my_contacts[i] = dummy[i+1];
                                                }

                                                builder.setItems(my_contacts, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        progressDialog = new ProgressDialog(UserActivity.this);
                                                        progressDialog.setMessage("Calling..."+my_contacts[which]);
                                                        progressDialog.setCanceledOnTouchOutside(false);
                                                        progressDialog.show();// user checked an item
                                                        final String id_to = conlist.get(which+1);
                                                        final DatabaseReference usrdbr = FirebaseDatabase.getInstance().getReference("userdetails");
                                                        usrdbr.child(id_to).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                final UserDetails to_ud = dataSnapshot.getValue(UserDetails.class);
                                                                make_a_call(my_id, id_to, s1, s2, vid_url, to_ud.getName(),to_ud.getNumber());
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }
                                                });

                                                builder.setNegativeButton("Cancel", null);

                                                AlertDialog dialog = builder.create();
                                                dialog.show();

                                            } else {
                                                build_an_alert("Request failed", "You have no contacts. Either add contacts to address book or check random volunteer box", "okay");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                            else{
                                android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                                builder.setTitle("Warning");
                                builder.setMessage("Couldnt fetch location. Check GPS on/off or continue without location sharing");
                                builder.setCancelable(false);
                                builder.setPositiveButton(
                                        "continue",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //continue with call
                                                progressDialog = new ProgressDialog(UserActivity.this);
                                                progressDialog.setMessage("Calling...");
                                                progressDialog.setCanceledOnTouchOutside(false);
                                                progressDialog.show();

                                                final String s1 = ud.getName();
                                                final String s2 = ud.getNumber();
                                                final String vid_url = "http://" + ud.getDevice_ip() + ":8081";
                                                if(rv.isChecked()){
                                                    final String id_to = "OafXvcb3goRQRZJPL2dQV6AiU0M2";

                                                    final DatabaseReference usrdbr = FirebaseDatabase.getInstance().getReference("userdetails");
                                                    usrdbr.child(id_to).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            final UserDetails to_ud = dataSnapshot.getValue(UserDetails.class);
                                                            make_a_call(my_id, id_to, s1, s2, vid_url, to_ud.getName(),to_ud.getNumber());
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                                else{
                                                    //cycle through frequent volunteers until someone accepts or timer runs out
                                                    DatabaseReference cdbr = FirebaseDatabase.getInstance().getReference("usercontacts");
                                                    cdbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            UserContacts uc = dataSnapshot.getValue(UserContacts.class);
                                                            final List<String> conlist = uc.getClist();

                                                            if (conlist.size() > 0) {
                                                                progressDialog.dismiss();
                                                                final AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                                                                builder.setTitle("Choose a contact");

                                                                String[] dummy = uc.getContactNames().split(";");
                                                                final String[] my_contacts = new String[dummy.length-1];
                                                                for(int i=0;i<my_contacts.length;i++){
                                                                    my_contacts[i] = dummy[i+1];
                                                                }

                                                                builder.setItems(my_contacts, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        progressDialog = new ProgressDialog(UserActivity.this);
                                                                        progressDialog.setMessage("Calling..."+my_contacts[which]);
                                                                        progressDialog.setCanceledOnTouchOutside(false);
                                                                        progressDialog.show();// user checked an item
                                                                        final String id_to = conlist.get(which+1);
                                                                        final DatabaseReference usrdbr = FirebaseDatabase.getInstance().getReference("userdetails");
                                                                        usrdbr.child(id_to).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                final UserDetails to_ud = dataSnapshot.getValue(UserDetails.class);
                                                                                make_a_call(my_id, id_to, s1, s2, vid_url, to_ud.getName(),to_ud.getNumber());
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                                builder.setNegativeButton("Cancel", null);

                                                                AlertDialog dialog = builder.create();
                                                                dialog.show();

                                                            } else {
                                                                build_an_alert("Request failed", "You have no contacts. Either add contacts to address book or check random volunteer box", "okay");
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            }
                                        }
                                );
                                builder.setNegativeButton(
                                        "cancel",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //call cancelled
                                                dialog.cancel();
                                            }
                                        }
                                );
                                AlertDialog alert1 = builder.create();
                                alert1.show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        b_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String s = dev_ip.getText().toString().trim();
                if (!validate_device(s)) {
                    String[] x = s.split("\\.");
                    Toast.makeText(UserActivity.this, "Invalid IP format: " + x.length, Toast.LENGTH_SHORT).show();
                } else {
                    final String id = FirebaseAuth.getInstance().getUid();
                    final DatabaseReference mydbr = FirebaseDatabase.getInstance().getReference("userdetails");
                    mydbr.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserDetails my_ud = dataSnapshot.getValue(UserDetails.class);
                            my_ud.setDevice_ip(s);
                            my_ud.setSet(true);
                            mydbr.child(id).setValue(my_ud).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        b_edit.setBackgroundColor(getResources().getColor(R.color.my_bright_green));
                                        b_edit.setText("Edit");
                                        edit_flag = false;
                                        update_device_status();
                                        build_an_alert("Success", "Device IP updated to: " + s,"okay");
                                    } else {
                                        build_an_alert("Error", "Unable to update device details","okay");
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(UserActivity.this, "Error: Device not registered", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        b_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edit_flag) {
                    dev_status.setVisibility(View.INVISIBLE);
                    staus_heading.setVisibility(View.INVISIBLE);
                    dev_ip.setVisibility(View.VISIBLE);
                    b_device.setVisibility(View.VISIBLE);
                    b_edit.setBackgroundColor(getResources().getColor(R.color.my_bright_red));
                    b_edit.setText("CANCEL");
                    edit_flag = true;
                } else {
                    dev_status.setVisibility(View.VISIBLE);
                    staus_heading.setVisibility(View.VISIBLE);
                    dev_ip.setVisibility(View.INVISIBLE);
                    b_device.setVisibility(View.INVISIBLE);
                    b_edit.setBackgroundColor(getResources().getColor(R.color.my_bright_green));
                    b_edit.setText("Edit");
                    edit_flag = false;
                }
            }
        });

        ocr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent my_intent = new Intent(UserActivity.this, OCRActivity.class);
                startActivity(my_intent);
            }
        });

        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent my_intent = new Intent(UserActivity.this, ContactsActivity.class);
                startActivity(my_intent);
            }
        });

        logs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent my_intent = new Intent(UserActivity.this, CallLogsActivity.class);
                startActivity(my_intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent my_intent = new Intent(UserActivity.this, ProfileSettingsActivity.class);
                startActivity(my_intent);
            }
        });

        //on clicking signout
        b_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                finish();
                Intent my_intent = new Intent(UserActivity.this, MainActivity.class);
                startActivity(my_intent);
            }
        });
    }

    private void cycle_calls(final String my_id, final String my_name, final String my_number, final String vid_url, List<String> clist){
        int max_calls = 4;
        if(clist.size()<4){
            max_calls = clist.size();
        }
        for(int i=0;i<max_calls;i++){
            final String id_to = clist.get(i);
            DatabaseReference usrdbr = FirebaseDatabase.getInstance().getReference("userdetails");
            usrdbr.child(id_to).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserDetails ud_to = dataSnapshot.getValue(UserDetails.class);
                    make_a_call(my_id,id_to,my_name,my_number,vid_url,ud_to.getName(),ud_to.getNumber());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void make_a_call(final String my_id, final String id_to, String s1, String s2, String vid_url, final String name_to, String number_to) {


//        Toast.makeText(UserActivity.this, "Creating Communications", Toast.LENGTH_SHORT).show();
        create_communication_out(my_id, id_to, s1,name_to,number_to);
        create_communication_in(my_id, id_to, s1, s2, vid_url);

//        long start_time = System.currentTimeMillis();
//        long curr_time = start_time;
//        while((curr_time-start_time)/1000<15){
//            if((int)((curr_time-start_time)/1000)%5==3){
//
//            }
//            curr_time=System.currentTimeMillis();
//        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
                DatabaseReference codbr = FirebaseDatabase.getInstance().getReference("out_comms");
                codbr.child(my_id).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        //CommunicationOut c_out = dataSnapshot.getValue(boolean);
                        int resp = dataSnapshot.getValue(int.class);
                        if (resp == 1) {
                            progressDialog.dismiss();
                            Intent my_intent = new Intent(UserActivity.this, UserCallActivity.class);
                            startActivity(my_intent);
                        } else if (resp == 2) {
                            progressDialog.dismiss();
                            Toast.makeText(UserActivity.this, "Call was rejected", Toast.LENGTH_SHORT).show();
//                            delete_communication_in(id_to);
//                            delete_communication_out(my_id);
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                progressDialog.dismiss();
                android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                builder.setTitle("Call failed");
                builder.setMessage(name_to+" was not responding");
                builder.setCancelable(false);
                builder.setPositiveButton(
                        "okay",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                delete_communication_in(id_to);
                                delete_communication_out(my_id);
                                dialog.cancel();
                            }
                        }
                );
                AlertDialog alert1 = builder.create();
                alert1.show();
            }
        }, 15000);
    }

    private void setup_UI_components() {
        b_signout = (Button) findViewById(R.id.button_signout);
        b_req = (Button) findViewById(R.id.button_req);
        b_device = (Button) findViewById(R.id.button_connect);
        contacts = (TextView) findViewById(R.id.contacts_manager);
        dev_ip = (EditText) findViewById(R.id.editText_ip);
        dev_status = (TextView) findViewById(R.id.text_status);
        rv = (CheckBox) findViewById(R.id.checkBox);
        logs = (TextView) findViewById(R.id.check_logs_user);
        b_edit = (Button) findViewById(R.id.edit_device_button);
        staus_heading = (TextView) findViewById(R.id.textView9);
        dev_hd = (TextView) findViewById(R.id.textViewdd);
        settings = (TextView) findViewById(R.id.ps_user);
        ocr = (TextView) findViewById(R.id.textView11);

        edit_flag = false;
        update_device_status();
    }

    private void update_device_status() {
        String id = FirebaseAuth.getInstance().getUid();
        DatabaseReference mydbr = FirebaseDatabase.getInstance().getReference("userdetails");
        mydbr.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserDetails ud = dataSnapshot.getValue(UserDetails.class);
                if (ud.isSet()) {
                    dev_hd.setText("Device details");
                    dev_hd.setTextColor(getResources().getColor(R.color.my_bright_green));
                    dev_ip.setVisibility(View.INVISIBLE);
                    b_device.setVisibility(View.INVISIBLE);
                    dev_status.setVisibility(View.VISIBLE);
                    staus_heading.setVisibility(View.VISIBLE);
                    dev_status.setText("Connected to IP: " + ud.getDevice_ip());
                    dev_status.setTextColor(getResources().getColor(R.color.my_bright_green));
                } else {
                    dev_hd.setText("Device not paired");
                    dev_hd.setTextColor(getResources().getColor(R.color.my_bright_red));
                    dev_status.setVisibility(View.INVISIBLE);
                    staus_heading.setVisibility(View.INVISIBLE);
                    dev_ip.setVisibility(View.VISIBLE);
                    b_device.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserActivity.this, "Unable to retrieve device", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validate_device(String s) {
        String[] x = s.split("\\.");
        if (x.length == 4) {
            return true;
        } else {
            return false;
        }
    }

    private void build_an_alert(String t, String m) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setTitle(t);
        builder.setMessage(m);
        builder.setCancelable(true);
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    private void build_an_alert(String t, String m, String b) {
        android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
        builder.setTitle(t);
        builder.setMessage(m);
        builder.setCancelable(false);
        builder.setPositiveButton(
                b,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );
        AlertDialog alert1 = builder.create();
        alert1.show();
    }


    private void create_communication_out(String id_from, String id_to, String name_from, String name_to, String number_to) {
        DatabaseReference codbr = FirebaseDatabase.getInstance().getReference("out_comms");
        CommunicationOut co = new CommunicationOut(id_from, id_to, name_from, name_to, number_to, 0);
        codbr.child(id_from).setValue(co).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(UserActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void create_communication_in(String id_from, String id_to, String name_from, String number_from, String vid_url) {
        DatabaseReference cidbr = FirebaseDatabase.getInstance().getReference("in_comms");
        CommunicationIn ci = new CommunicationIn(id_from, id_to, name_from, number_from, vid_url);
        cidbr.child(id_to).setValue(ci).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(UserActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void delete_communication_in(String id) {
        DatabaseReference cidbr = FirebaseDatabase.getInstance().getReference("in_comms");
        cidbr.child(id).removeValue();
    }

    private void delete_communication_out(String myid) {
        DatabaseReference codbr = FirebaseDatabase.getInstance().getReference("out_comms");
        codbr.child(myid).removeValue();
    }

    boolean[] time_track = new boolean[15];

    private void reset_time_track() {
        for (int i = 0; i < 15; i++) {
            time_track[i] = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            //Toast.makeText(this, " NOT WORKING ", Toast.LENGTH_LONG).show();


        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
//            Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
            curr_lat = currentLatitude;
            curr_lon = currentLongitude;
            loc_set=true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
//        currentLatitude = location.getLatitude();
//        currentLongitude = location.getLongitude();
//
//        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }
}
