package com.example.pickycopy;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity{
    Button register;
    CountryCodePicker countryCodePicker;
    EditText namef, phonef, emailf, passwordf;
    Spinner typef;
    TextView login;
    FirebaseAuth fa;
    ProgressBar pb;
    String userId, email, password, phone, name, type,token;
    FirebaseFirestore fs;
    String[] types = {"Please select","Student","Shop-keeper"};
    private String mVerificationId;
    public boolean validate(String name, String email, String password, String phone) {
        if (TextUtils.isEmpty(name)) {
            namef.setError("Enter a valid name");
            namef.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(email) || !email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
            emailf.setError("Invalid Email");
            emailf.requestFocus();
            return false;
        }
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            passwordf.setError("password must contain special symbols,numbers and letters combination");
            passwordf.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            phonef.setError("Enter a valid Phone");
            return false;
        } else {
            return true;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        countryCodePicker = findViewById(R.id.ccp);
        login = findViewById(R.id.link_login);
        namef = findViewById(R.id.editText3);
        emailf = findViewById(R.id.editText4);
        phonef = findViewById(R.id.editText6);
        typef = findViewById(R.id.spinner);
        fa = FirebaseAuth.getInstance();
        pb = findViewById(R.id.progressBar2);
        passwordf = findViewById(R.id.editText5);
        register = findViewById(R.id.button2);
        fs = FirebaseFirestore.getInstance();
        ArrayAdapter<String> typea = new ArrayAdapter<String>(Register.this, R.layout.support_simple_spinner_dropdown_item, types);
        typef.setAdapter(typea);
        typef.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                switch (position) {
                    case 0:
                        Toast.makeText(Register.this,"Please select type of user",Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        type = "student";
                        break;
                    case 2:
                        type = "owner";
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {


            }
        });

        if (fa.getCurrentUser() != null) {
            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
        }
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = namef.getText().toString().trim();
                email = emailf.getText().toString().trim();
                password = passwordf.getText().toString().trim();
                phone = "+"+countryCodePicker.getSelectedCountryCode() + phonef.getText().toString().trim();
                v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                v.invalidate();

                if (validate(name, email, password, phone)) {
                    sendVerificationCode(phone);
                    pb.setVisibility(View.VISIBLE);

                }
            }


        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
            }
        });

    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                Toast.makeText(Register.this,""+code,Toast.LENGTH_LONG);
                verify(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
            Toast.makeText(Register.this,"Auto-Fetching OTP",Toast.LENGTH_LONG).show();
            // mResendToken = forceResendingToken;
        }
    };
    private void verify(String code){
        PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(mVerificationId,code);
        createaccount();
    }
    public void createaccount(){
        fa.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    pb.setVisibility(View.INVISIBLE);
                    userId = fa.getCurrentUser().getUid();
                    Map<String, Object> user1 = new HashMap<>();
                    user1.put("name",name);
                    user1.put("email",email);
                    user1.put("phone",phone);
                    user1.put("type",type);
                    user1.put("userId",userId);
                    user1.put("token",token);
                    Toast.makeText(Register.this, "Authentication passed.", Toast.LENGTH_SHORT).show();
                    DocumentReference cf = fs.collection("Users").document(fa.getCurrentUser().getUid());
                    cf.set(user1);
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(type).build();
                    fa.getCurrentUser().updateProfile(profileUpdates);
                    finish();

                } else {

                    Toast.makeText(Register.this, "Authentication failed."+task.getException(), Toast.LENGTH_LONG).show();
                }
                startActivity(new Intent(Register.this, MainActivity.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
            }
        });

    }

}

