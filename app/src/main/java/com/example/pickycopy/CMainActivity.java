package com.example.pickycopy;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.view.View.VISIBLE;

public class CMainActivity  extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Button upload;
    TextView tv;
    Uri uri;
    String downloadUrl;
    Date currentTime;
    FirebaseAuth fa;
    FirebaseFirestore fs;
    StorageTask task;
    Cursor returnCursor;
    int nameIndex, sizeIndex;
    String filename, token;
    ProgressBar pb;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    JSONArray regArray = new JSONArray();
    String emailf, userid;
    ArrayList<String> messages;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    final String TAG = "NOTIFICATION TAG";
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    OkHttpClient mClient = new OkHttpClient();
    private List<CUser> list = new ArrayList<CUser>() {
    };
    private RecyclerView userRecycler;
    public static CListAdapter listAdapter;
    private StorageReference mStorageRef;
    @Override
    protected void onStart() {

        super.onStart();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cmainactivity);
        fa = FirebaseAuth.getInstance();
        fs = FirebaseFirestore.getInstance();
        emailf = fa.getCurrentUser().getEmail();
        userid = fa.getCurrentUser().getUid();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );
        userRecycler = findViewById(R.id.user_recycle);
        userRecycler.setLayoutManager(new LinearLayoutManager(this));
        upload = findViewById(R.id.button);
        tv = findViewById(R.id.textView);
        pb = findViewById(R.id.progressBar4);
        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(CMainActivity.this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        listAdapter = new CListAdapter(CMainActivity.this, list);
        userRecycler.setAdapter(listAdapter);
        ((LinearLayoutManager) userRecycler.getLayoutManager()).setStackFromEnd(true);
        getToken();
        messages=new ArrayList<>();
        getdbData();
    }

    public void pick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    10001);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 10001:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    uri = data.getData();
                    currentTime = Calendar.getInstance().getTime();
                    returnCursor =
                            getContentResolver().query(uri, null, null, null, null);
                    nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    filename = returnCursor.getString(nameIndex);
                    upload();

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void upload() {
        if (uri != null && returnCursor.getLong(sizeIndex) <= 26214400) {
            pb.setVisibility(VISIBLE);
            mStorageRef = FirebaseStorage.getInstance().getReference("" + fa.getCurrentUser().getUid() + "/" + currentTime.toString().substring(0, 11) + "/" + filename);
            task = mStorageRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CMainActivity.this, "Upload success!!", Toast.LENGTH_SHORT).show();
                            String downloadUrl = taskSnapshot.getUploadSessionUri().toString();
                            //Toast.makeText(CMainActivity.this, downloadUrl, Toast.LENGTH_SHORT).show();
                            sendMessage(regArray,"New message!","hi","Download link:",userid);
                            pb.setVisibility(View.INVISIBLE);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(CMainActivity.this, "failed" + exception, Toast.LENGTH_SHORT).show();
                            pb.setVisibility(View.INVISIBLE);
                        }
                    });
        } else if (returnCursor.getLong(sizeIndex) > 26214400) {
            Toast.makeText(CMainActivity.this, "Please select a file less than 25 mb", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(CMainActivity.this, "Please select a file", Toast.LENGTH_SHORT).show();
        }
    }

    public void getdbData() {
        list.clear();
        fs.collection("Users").whereEqualTo("type", "owner").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        if(!doc.getDocument().getId().contains(fa.getCurrentUser().getUid())){
                        list.add(new CUser(name, phone, email, userId,token,address));
                        userRecycler.scrollToPosition(0);}
                    }
                }

            }
        });

    }

    public void uploader(JSONArray reg) {
        if (task != null && task.isInProgress()) {
            Toast.makeText(CMainActivity.this, "File upload in progress", Toast.LENGTH_SHORT).show();
        } else {
            pick();
            regArray=reg;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                startActivity(new Intent(CMainActivity.this, CProfile.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
                break;
            case R.id.about_app:
                startActivity(new Intent(CMainActivity.this, About.class));
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
        Intent i = new Intent(CMainActivity.this, Login.class);
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

                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        //Toast.makeText(CMainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        updatetoken();
                    }
                });
    }

    public void sendMessage(final JSONArray recipients, final String title, final String body, final String message,final String sendername) {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);

                    JSONObject data = new JSONObject();
                    data.put("message", message);
                    data.put("userId",userid);
                    data.put("downloadUrl",downloadUrl);
                    data.put("senderName",sendername);
                    data.put("sentTime", Calendar.getInstance().getTime());
                    data.put("recieverId",fa.getCurrentUser().getUid());
                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("registration_ids", recipients);

                    String result = postToFCM(root.toString());
                    Log.d(TAG, "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    //Toast.makeText(CMainActivity.this, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(CMainActivity.this, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    String postToFCM(String bodyString) throws IOException {
        RequestBody body = RequestBody.create(JSON, bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + "AAAAWdilDfk:APA91bH6CzUyoEIidn36CyyvIsOOZ5Ls4TAXvr2FmD_EmP48-tT_G7WlwWp5IHRLFPry5kqxu4BS_cnPXIx85Tbu79oykZD7HPcWMPwtRaC_VJef77TSAdQ0DE2nC-OEIUSygAXFnfZ9")
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
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
            downloadUrl=intent.getExtras().getString("message");
            messages.add(downloadUrl);
            //Toast.makeText(CMainActivity.this,messages.get(0),Toast.LENGTH_SHORT).show();
            listAdapter.notifyDataSetChanged();



        }
    };
}


