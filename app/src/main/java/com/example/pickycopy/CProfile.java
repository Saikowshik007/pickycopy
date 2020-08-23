package com.example.pickycopy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CProfile extends AppCompatActivity {

    String pname,pnum,email,group,age,userId,location,password;
    TextView a, b,c,d,e;
    ImageButton back;
    Dialog mdialog;
    EditText emailf,passwordf;
    FirebaseUser user;
    FirebaseAuth fa;
    CUser cuser;
    FloatingActionButton floatingActionButton,delete;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_profile);
        fa=FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        a = findViewById(R.id.my_name);
        b = findViewById(R.id.my_phone);
        c = findViewById(R.id.my_emailid);
        d = findViewById(R.id.my_id);
        password="saikowshik@1";
        floatingActionButton=findViewById(R.id.floatingActionButton);
        delete=findViewById(R.id.floatingActionButton2);
        e = findViewById(R.id.my_phone);
        setData();
        back = findViewById(R.id.imageButton5);
        mdialog=new Dialog(CProfile.this);
        mdialog.setContentView(R.layout.passwordfrag);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CProfile.this, CMainActivity.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CProfile.this,CEditProfile.class));
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdialog.show();
                Button confirm,close;
                emailf=mdialog.findViewById(R.id.email);
                emailf.setText(c.getText());
                passwordf=mdialog.findViewById(R.id.password);
                confirm=mdialog.findViewById(R.id.button4);
                close=mdialog.findViewById(R.id.button3);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { mdialog.cancel(); }});
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        email=emailf.getText().toString();
                        password=passwordf.getText().toString();
                        reauthenticate();}});
            }});
    }
    public void setData(){
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users").document(fa.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                       a.setText(document.getData().get("name").toString());
                       b.setText(document.getData().get("phone").toString());
                       c.setText(document.getData().get("email").toString());
                    } else {
                        Toast.makeText(CProfile.this, "No such document",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CProfile.this, "get failed with: "+task.getException(),Toast.LENGTH_SHORT).show();
                }
            }


        });
    }
    void deletedata() {
        FirebaseFirestore.getInstance().collection("Users").document(fa.getCurrentUser().getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CProfile.this, "deleted data", Toast.LENGTH_SHORT).show();
                    deleteuser();
                } else {
                    Toast.makeText(CProfile.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    public void deleteuser(){
        FirebaseAuth.getInstance().getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CProfile.this,"deleted",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CProfile.this,Login.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CProfile.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void reauthenticate(){
        if(!email.isEmpty()&&!password.isEmpty()) {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(email, password);
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                deletedata();
                            } else {
                                mdialog.cancel();
                                emailf.setText("");passwordf.setText("");emailf.clearFocus();
                                Toast.makeText(CProfile.this, "deleted" + task.getException(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        else{emailf.setError("This field cannot be empty.");emailf.requestFocus();}
    }
    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(CProfile.this, CMainActivity.class));
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        super.onBackPressed();
    }

}

