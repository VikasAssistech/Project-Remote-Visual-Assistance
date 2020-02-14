package com.example.remotevisualassistant;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private Button bsearch;
    private EditText number;
    private ListView listview;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        set_UI_components();
        update_contacts_list();

        bsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input_format()){
                    Toast.makeText(ContactsActivity.this,"Searching..",Toast.LENGTH_SHORT).show();
                    final String num = number.getText().toString().trim();
                    final String my_id = FirebaseAuth.getInstance().getUid();
                    DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                    Query query = mFirebaseDatabaseReference.child("userdetails").orderByChild("number").equalTo(num);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getChildren().iterator().hasNext()){
                                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                    final UserDetails ud = postSnapshot.getValue(UserDetails.class);
                                    if(ud.getType().equals("Volunteer")){
                                        final DatabaseReference cdbr = FirebaseDatabase.getInstance().getReference("usercontacts");
                                        cdbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                UserContacts newuc = dataSnapshot.getValue(UserContacts.class);
                                                if(newuc.hasContact(ud.getId())){
//                                                    Toast.makeText(ContactsActivity.this,"Volunteer already added",Toast.LENGTH_SHORT).show();
                                                    build_an_alert("Failed","Volunteer already added","okay");
                                                }
                                                else{
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(ContactsActivity.this);
                                                    builder.setMessage("Add "+ud.getName()+" to frequent volunteers?");
                                                    builder.setCancelable(false);
                                                    builder.setPositiveButton(
                                                            "yes",
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    cdbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                            pd = new ProgressDialog(ContactsActivity.this);
                                                                            pd.setMessage("Adding contact");
                                                                            pd.setCanceledOnTouchOutside(false);
                                                                            pd.show();

                                                                            UserContacts newuc = snapshot.getValue(UserContacts.class);
                                                                            newuc.addContact(ud.id,ud.getName(),ud.getNumber());
                                                                            cdbr.child(my_id).setValue(newuc);
                                                                            update_contacts_list();
                                                                            number.setText("");
                                                                            pd.dismiss();
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                    dialog.cancel();
                                                                }
                                                            });
                                                    builder.setNegativeButton(
                                                            "no",
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
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    else{
                                        build_an_alert("Failed","Can only add Volunteers","okay");
//                                        Toast.makeText(ContactsActivity.this,"Can only add Volunteers",Toast.LENGTH_SHORT).show();
                                        number.setText("");
                                    }
                                }
                            }
                            else{
                                build_an_alert("Failed","No volunteer found","okay");
//                                Toast.makeText(ContactsActivity.this,"No volunteer found",Toast.LENGTH_SHORT).show();
                                number.setText("");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });




                }
                else{
                    build_an_alert("Failed","Phone number must be 10 digits","okay");
                    //Toast.makeText(ContactsActivity.this,"Number must be 10 digits",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void set_UI_components(){
        bsearch = (Button)findViewById(R.id.button_search_contacts);
        number = (EditText)findViewById(R.id.innum);
        listview = (ListView)findViewById(R.id.lv_contacts);
    }

    private boolean input_format(){
        String s = number.getText().toString().trim();
        if(s.length()==10){
            return true;
        }
        else{
            return false;
        }
    }

    private void update_contacts_list(){
        DatabaseReference cdbr = FirebaseDatabase.getInstance().getReference("usercontacts");
        String my_id = FirebaseAuth.getInstance().getUid();
        cdbr.child(my_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserContacts uc = dataSnapshot.getValue(UserContacts.class);
                String[] tnames = uc.getContactNames().split(";");
                String[] tnums = uc.getContactNumbers().split(";");
                List<String> tmp = new ArrayList<String>();
                for(int i=0;i<uc.getSize();i++) {
                    tmp.add(tnums[i+1] + " - " + tnames[i+1]);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(),android.R.layout.simple_list_item_1,tmp);
                listview.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void build_an_alert_changeactivity(String t, String m, String b, final String tp){
        android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(ContactsActivity.this);
        builder.setTitle(t);
        builder.setMessage(m);
        builder.setCancelable(false);
        builder.setPositiveButton(
                b,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        if(tp.equals("User")){
                            Intent myintent = new Intent(ContactsActivity.this,UserActivity.class);
                            startActivity(myintent);
                            finish();
                        }
                        else{
                            Intent myintent = new Intent(ContactsActivity.this,VolunteerActivity.class);
                            startActivity(myintent);
                            finish();
                        }
                    }
                }
        );
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    private void build_an_alert(String t, String m, String b){
        android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(ContactsActivity.this);
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

}
