package com.example.pickycopy;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseAuth fa;
    FirebaseFirestore fs;
    String token ,recievedToken,recievedId;
    ProgressBar pb;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    String emailf, userid;
    String myname;
    ArrayList<String> messages;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    final String TAG = "NOTIFICATION TAG";
    private List<CUser> list = new ArrayList<CUser>() {
    };
    private RecyclerView userRecycler;
    public static CListAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pmainactivity);
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );
        userRecycler = findViewById(R.id.user_recycle);
        userRecycler.setLayoutManager(new LinearLayoutManager(this));
        pb = findViewById(R.id.progressBar4);
        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(PMainActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        listAdapter = new CListAdapter(PMainActivity.this, list);
        userRecycler.setAdapter(listAdapter);
        ((LinearLayoutManager) userRecycler.getLayoutManager()).setStackFromEnd(true);
        messages=new ArrayList<>();
        getToken();
    }
    @Override
    protected void onStart() {
        fa = FirebaseAuth.getInstance();
        fs = FirebaseFirestore.getInstance();
        emailf = fa.getCurrentUser().getEmail();
        userid = fa.getCurrentUser().getUid();

        super.onStart();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                startActivity(new Intent(PMainActivity.this, CProfile.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
                break;
            case R.id.about_app:
                startActivity(new Intent(PMainActivity.this, About.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
                break;

            case R.id.logout_user:
                logout();
                break;

            case R.id.refresh:
                pb.setVisibility(View.VISIBLE);
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
                pb.setVisibility(View.INVISIBLE);
                break;

            case R.id.nav_share:
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Now get blood donations with ease at your location");
                String app_url = "https://drive.google.com/drive/folders/1FlfmK-cDQMoGbUzicx4BtYpwHR6fanr1?usp=sharing";
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, app_url);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                break;
            case R.id.rate:
                Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
                }
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout() {
        fa.signOut();
        finish();
        Intent i = new Intent(PMainActivity.this, Login.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void getToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        //Toast.makeText(CMainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        updatetoken();
                    }
                });
    }


    public void updatetoken(){
        fa=FirebaseAuth.getInstance();
        fs=FirebaseFirestore.getInstance();
        if (fa.getCurrentUser().getUid() != null) {

            DocumentReference ref=fs.collection("Users").document(fa.getCurrentUser().getUid());
            ref.update("token", token);}
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           String message=intent.getExtras().getString("message");
           //downloadUrl=intent.getExtras().getString("downloadUrl");
            //messages.add(downloadUrl);
            recievedId=intent.getExtras().getString("userId");
            Toast.makeText(PMainActivity.this,recievedId,Toast.LENGTH_LONG).show();
            getdbData();
            listAdapter.notifyDataSetChanged();}

    };
    public void getdbData() {
    list.clear();
        fs.collection("Users").whereEqualTo("userId", recievedId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                    if (doc.getType() == DocumentChange.Type.ADDED) {
                        String name = doc.getDocument().getString("name");
                        String phone = doc.getDocument().getString("phone");
                        String email = doc.getDocument().getString("email");
                        String userId = doc.getDocument().getString("userId");
                        String token = " "+doc.getDocument().getString("token");
                        String address=doc.getDocument().getString("address");
                        if(!doc.getDocument().getId().contains(fa.getCurrentUser().getUid())) {
                                list.add(new CUser(name, phone, email, userId, token, address));
                                userRecycler.scrollToPosition(0);
                        }
                        else{myname=doc.getDocument().getString("name");}
                    }
                }

            }
        });

    }
}
