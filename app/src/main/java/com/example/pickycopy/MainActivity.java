package com.example.pickycopy;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;



public class MainActivity extends AppCompatActivity {
    FirebaseAuth fa;
    String displayname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fa = FirebaseAuth.getInstance();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
        }

        if (fa.getCurrentUser() == null || fa.getCurrentUser().getDisplayName()==null) {
            Intent i = new Intent(MainActivity.this, Login.class);
            startActivity(i);
            finish();
        }
        else if(fa.getCurrentUser().getDisplayName().contains("student")){
        startActivity(new Intent(MainActivity.this,CMainActivity.class));finish();}
        else{startActivity(new Intent(MainActivity.this,PMainActivity.class));finish();}

    }
}

