package com.example.remotevisualassistant;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText email, password;
    private TextView Signup, Forgotpwd;
//    private TextView errortext;
    private Button Signin;
    private ProgressDialog pd;
    private FirebaseAuth mAuth;

//    private Switch uvswitch;
//    private TextView uvt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        set_up_UI_elements();
//        FirebaseUser user = fbAuth.getCurrentUser();
//        if(user!=null){
//            pd = new ProgressDialog(MainActivityLogin.this);
//            pd.setMessage("Signing in ...");
//            pd.setCanceledOnTouchOutside(false);
//            pd.show();
//
//            String id = fbAuth.getUid().toString();
//            DatabaseReference mydbr = FirebaseDatabase.getInstance().getReference("userdata2");
//            //String uid = mydbr.push().getKey();
//            //
//            mydbr.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    String user_type = dataSnapshot.getValue(UserDetails.class).getType();
//                    pd.dismiss();
//                    switch_activity(user_type);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//                    pd.dismiss();
//                    Toast.makeText(getApplicationContext(),"Unable to retrieve user data",Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
        //check here for returning user !

        Signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"Sign in",Toast.LENGTH_SHORT).show();
                //errortext.setText("");
                String s1 = email.getText().toString().trim();
                String s2 = password.getText().toString().trim();
                int t = validate_input(s1,s2);
                if(s1.length()==0||s2.length()==0){
                    build_an_alert("Input error","Neither Email nor password can be empty","okay");
                }
                else{
                    if(t==0){
                        authenticate_login(s1,s2);
                    }
                    else if(t==1){
                        build_an_alert("Input error","Invalid email format","okay");
                        //errortext.setText("Invalid Email Format");
                        //Toast.makeText(MainActivity.this,"Invalid Email Format",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        build_an_alert("Input error","Invalid password format","okay");
                        //errortext.setText("Invalid password format");
                        //Toast.makeText(MainActivity.this,"Password cannot be empty",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent my_intent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(my_intent);
                }
        });

        Forgotpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Forgot password ?");
                builder.setMessage("A password reset link will be sent to the following Email ID");
                final EditText email = new EditText(MainActivity.this);
                email.setHint("enter Email ID");
                email.setGravity(Gravity.CENTER);
                email.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(email);
                builder.setCancelable(false);
                mAuth = FirebaseAuth.getInstance();
                builder.setPositiveButton(
                        "Reset",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                final String in_email = email.getText().toString().trim();
                                if(check_email_format(in_email)){
                                    mAuth.sendPasswordResetEmail(in_email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                dialog.cancel();
                                                build_an_alert("Success", "Password reset link sent to "+in_email,"okay");
                                            }
                                            else{
                                                dialog.cancel();
                                                build_an_alert("Error", task.getException().getMessage(),"okay");
                                            }
                                        }
                                    });
                                }
                                else{
                                    dialog.cancel();
                                    build_an_alert("Error", "Invalid email format","okay");
                                }
                            }
                        }
                );
                builder.setNegativeButton(
                        "Cancel",
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
        });

//        uvswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                    email.setText("useralpha@gmail.com");
//                    password.setText("useralpha");
//                    uvt.setText("User");
//                }
//                else{
//                    email.setText("volunteeralpha@gmail.com");
//                    password.setText("volunteeralpha");
//                    uvt.setText("Volunteer");
//                }
//            }
//        });
    }

    private boolean check_email_format(String s){
//        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
//                "[a-zA-Z0-9_+&-]+)@" +
//                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
//                "A-Z]{2,7}$";
//
//        Pattern pat = Pattern.compile(emailRegex);
//        if(pat.matcher(s).matches()) {
//            return true;
//        }
//        else{
//            return false;
//        }
//        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
//                "[a-zA-Z0-9_+&-]+)@" +
//                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
//                "A-Z]{2,7}$";
//
//
//
//        Pattern pat = Pattern.compile(emailRegex);
//
//
//
//
//        if(pat.matcher(s).matches()){
//            return true;
//        }
//        else{
//            return false;
//        }
        return true;
    }

    private void set_up_UI_elements(){
        email = (EditText)findViewById(R.id.input_email);
        password = (EditText)findViewById(R.id.input_password);
        Signin = (Button)findViewById(R.id.button_login);
        Signup = (TextView)findViewById(R.id.text_sign_up);
        Forgotpwd = (TextView)findViewById(R.id.text_forgot_password);
//        errortext = (TextView)findViewById(R.id.et1);

//        uvswitch = (Switch)findViewById(R.id.switch1);
//        uvt = (TextView)findViewById(R.id.uv);
    }

    private int validate_input(String s1, String s2){
        if(check_email_format(s1)) {
            return 0;
        }
        else{
            return 1;
        }
    }

    private void authenticate_login(String s1,String s2){
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("Signing in ...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(s1,s2).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String id = mAuth.getUid().toString();
                    DatabaseReference mydbr = FirebaseDatabase.getInstance().getReference("userdetails");
                    mydbr.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            pd.dismiss();
                            String user_type = dataSnapshot.getValue(UserDetails.class).getType();
                            switch_activity(user_type);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            pd.dismiss();
                            //errortext.setText("Fatal Error. Unable to fetch data");
                            build_an_alert("Connection Error","Unable to fetch data","okay");
                            //Toast.makeText(getApplicationContext(),"Login failed. Unable to fetch data",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    pd.dismiss();
                    String er = task.getException().getMessage();
                    if(er.startsWith("A network error")){
                       // errortext.setText("Check Internet Connection");
                        build_an_alert("Connection Error","Please check internet connection","okay");
                    }
                    else{
                       // errortext.setText("Login Failed. Incorrect Credentials");
                        build_an_alert("Authentication Error","Invalid Credentials","okay");
                    }
                    //Toast.makeText(getApplicationContext(),"Login Failed"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void build_an_alert(String t, String m, String b){
        android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

        private void switch_activity(String s){
        if(s.equals("User")){
            Intent my_intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(my_intent);
            finish();
        }
        else{
            Intent my_intent = new Intent(MainActivity.this, VolunteerActivity.class);
            startActivity(my_intent);
            finish();
        }
    }
}
