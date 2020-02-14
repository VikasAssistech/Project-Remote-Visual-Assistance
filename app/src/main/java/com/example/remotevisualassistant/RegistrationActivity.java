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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private EditText ed1,ed2,ed3,ed4,ed5;
    private RadioButton rb1, rb2;
    private CheckBox cb;
    private Button signup;
    private FirebaseAuth mAuth;
    private String verificationCode;
    private int verificationComplete;
    ProgressDialog pd;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setup_UI_components();
        mAuth = FirebaseAuth.getInstance();
        start_mobile_verification_firebase();

        rb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb.setVisibility(View.INVISIBLE);
                cb.setChecked(false);
                ed5.setVisibility(View.INVISIBLE);
            }
        });

        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                cb.set
                cb.setVisibility(View.VISIBLE);
            }
        });

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
//                    ed5.setHeight(50);
                    ed5.setVisibility(View.VISIBLE);
                }
                else{
//                    ed5.setHeight(1);
                    ed5.setVisibility(View.INVISIBLE);
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int f = validate_input_formats();
                if(f==0){
                    String phone_num = "+91"+ed3.getText().toString().trim();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phone_num,20, TimeUnit.SECONDS,RegistrationActivity.this,mCallback);
                    if(verificationComplete==2){
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                        builder.setTitle("Mobile number verification");
                        final EditText otp = new EditText(RegistrationActivity.this);
                        otp.setHint("enter OTP number");
                        otp.setGravity(Gravity.CENTER);
                        otp.setInputType(InputType.TYPE_CLASS_NUMBER);
                        builder.setView(otp);
                        builder.setCancelable(false);
                        builder.setPositiveButton(
                                "verify",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String my_otp = otp.getText().toString().trim();
                                        if(validate_otp(my_otp)){
                                            continue_registration();
                                        }
                                        else{
                                            build_an_alert("Error","OTP does not match","okay");
                                        }
                                    }
                                }
                        );
                        builder.setNegativeButton(
                                "cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        signup.setText("Register");
                                        dialog.cancel();
                                    }
                                }
                        );
                        builder.setNeutralButton(
                                "resend",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        signup.setText("Register");
                                        dialog.cancel();
                                    }
                                }
                        );
                        AlertDialog alert1 = builder.create();
                        alert1.show();
                    }
                    else if(verificationComplete==1){
                        Toast.makeText(RegistrationActivity.this,"Please wait for a few seconds",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(RegistrationActivity.this,"Please wait for the sms",Toast.LENGTH_SHORT).show();
                        //build_an_alert("Connection Error", "Not connected to internet");
                    }
                }
                else if(f==1){
                    build_an_alert("Invalid input", "Name must not contain special characters","okay");
                }
                else if(f==2){
                    build_an_alert("Invalid input", "Invalid email format","okay");
                }
                else if(f==3){
                    build_an_alert("Invalid input", "Invalid phone number ","okay");
                }
                else if(f==4){
                    build_an_alert("Invalid Input","password cant start with any special characters","okay");
                }
                else{
                    build_an_alert("Invalid input", "Do not leave any field blank","okay");
                }
            }
        });
    }

    private boolean validate_otp(String s){
        return true;
    }

    private void continue_registration(){
        final String input_name = ed1.getText().toString().trim();
        final String input_email = ed2.getText().toString().trim();
        final String input_number = ed3.getText().toString().trim();
        String input_password = ed4.getText().toString().trim();

        pd = new ProgressDialog(RegistrationActivity.this);
        pd.setMessage("Signing up ...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        mAuth.createUserWithEmailAndPassword(input_email,input_password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    DatabaseReference mydbr = FirebaseDatabase.getInstance().getReference("userdetails");
                    final String id = mAuth.getCurrentUser().getUid().toString();
                    if(rb1.isChecked()){
                        UserDetails ud = new UserDetails(id,input_name,input_email,input_number,"User");
                        mydbr.child(id).setValue(ud).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    DatabaseReference cdbr = FirebaseDatabase.getInstance().getReference("usercontacts");
                                    List<String> cl = new ArrayList<String>();
                                    cl.add("dummy");
                                    UserContacts uc = new UserContacts(id,cl,"dummy;","dummy;");
                                    cdbr.child(id).setValue(uc);

                                    DatabaseReference logdbr = FirebaseDatabase.getInstance().getReference("call_logs");
                                    CallLog clog = new CallLog(id,"dummy","dummy","dummy","dummy","dummy");
                                    AllLogs loglist = new AllLogs(clog);
                                    logdbr.child(id).setValue(loglist);

                                    DatabaseReference locdbr = FirebaseDatabase.getInstance().getReference("last_location");
                                    UserLocation ul = new UserLocation(id,0.0,0.0);
                                    locdbr.child(id).setValue(ul);

                                    pd.dismiss();
                                    //Toast.makeText(RegistrationActivity.this, "Data Succesfully Recorded",Toast.LENGTH_SHORT).show();
                                    build_an_alert_changeactivity("Success","User's account created !", "continue","User");
                                }
                                else{
                                    pd.dismiss();
                                    build_an_alert("Critical Error", "Account created, data not recorded","okay");
                                    //Toast.makeText(RegistrationActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else{
                        UserDetails ud = new UserDetails(id,input_name,input_email,input_number,"Volunteer");
                        mydbr.child(id).setValue(ud).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    if(cb.isChecked()){
                                        DatabaseReference nssdbr = FirebaseDatabase.getInstance().getReference("nssvolunteers");
                                        NssDetails nd = new NssDetails(id,ed5.getText().toString().trim(),0.0,true);
                                        nssdbr.child(id).setValue(nd);
                                    }
                                    else{
                                        DatabaseReference nssdbr = FirebaseDatabase.getInstance().getReference("nssvolunteers");
                                        NssDetails nd = new NssDetails(id,"",0.0,false);
                                        nssdbr.child(id).setValue(nd);
                                    }

                                    DatabaseReference logdbr = FirebaseDatabase.getInstance().getReference("call_logs");
                                    CallLog clog = new CallLog("dummy",id,"dummy","dummy","dummy","dummy");
                                    AllLogs loglist = new AllLogs(clog);
                                    logdbr.child(id).setValue(loglist);

                                    pd.dismiss();
//                                                Toast.makeText(RegistrationActivity.this, "Data Succesfully Recorded",Toast.LENGTH_SHORT).show();
                                    build_an_alert_changeactivity("Success","Volunteer's account created !", "continue","Volunteer");
                                }
                                else{
                                    pd.dismiss();
                                    build_an_alert("Critical Error", "Account created, data not recorded","okay");
                                    //Toast.makeText(RegistrationActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
                else {
                    pd.dismiss();
                    if(task.getException().getMessage().startsWith("A network error")){
                        build_an_alert("Error", "No Internet Connectivity","okay");
                    }
                    else if(task.getException().getMessage().startsWith("The email address")){
                        build_an_alert("Error", "Email id already in use","okay");
                    }
                    else{
                        build_an_alert("Fatal Error",task.getException().getMessage(),"okay");
                    }
                    //Toast.makeText(RegistrationActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void start_mobile_verification_firebase(){
        mAuth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                verificationComplete = 2;
                signup.setText("Enter otp");
                //                Toast.makeText(RegistrationActivity.this,"verification completed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                verificationComplete = 0;
                Toast.makeText(RegistrationActivity.this,"verification failed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationComplete = 1;
                verificationCode = s;
//                Toast.makeText(RegistrationActivity.this,"Code sent",Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void setup_UI_components(){
        ed1 = (EditText)findViewById(R.id.ed_name);
        ed2 = (EditText)findViewById(R.id.ed_email);
        ed3 = (EditText)findViewById(R.id.ed_number);
        ed4 = (EditText)findViewById(R.id.ed_password);
        ed5 = (EditText)findViewById(R.id.ed_entryno);
        cb = (CheckBox)findViewById(R.id.checkBox_nssv);
        rb1 = (RadioButton)findViewById(R.id.radioButton1_user);
        rb2 = (RadioButton)findViewById(R.id.radioButton2_voluteer);
        signup = (Button)findViewById(R.id.button_sign_up);
    }

    private boolean isValidName(String Name){
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(Name);
        if(!m.find()){
            return true;
        }
        else{
            return false;
        }
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
        return true;
    }

    public boolean isValidnumber( String number){
        if(number.length() == 10){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isValidPassword( String Password){
        Pattern p = Pattern.compile("[^A-Za-z0-9]");
        Matcher m = p.matcher(String.valueOf(Password.charAt(0)));
//        boolean b1 = m.find();
        if(!m.find()){
            return true;
        }
        else{
            return false;
        }
    }

    private int validate_input_formats(){
        final String input_name = ed1.getText().toString().trim();
        final String input_email = ed2.getText().toString().trim();
        final String input_number = ed3.getText().toString().trim();
        String input_password = ed4.getText().toString().trim();

        if(input_email.length()==0||input_name.length()==0||input_number.equals("")||input_password.equals("")){
            return 5;
        }
        else{
            if(isValidName(input_name)){
                if(check_email_format(input_email)){
                    if(isValidnumber(input_number)){
                        if(isValidPassword(input_password)){
                            return 0;
                        }
                        else{
                            return 4;
                        }
                    }
                    else{
                        return 3;
                    }
                }
                else{
                    return 2;
                }
            }
            else{
                return 1;
            }
        }
    }

    private void build_an_alert_changeactivity(String t, String m, String b, final String tp){
        android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
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
                            Intent myintent = new Intent(RegistrationActivity.this,UserActivity.class);
                            startActivity(myintent);
                            finish();
                        }
                        else{
                            Intent myintent = new Intent(RegistrationActivity.this,VolunteerActivity.class);
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
        android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
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
