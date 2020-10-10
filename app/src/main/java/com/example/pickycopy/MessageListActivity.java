package com.example.pickycopy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.List;

import javax.annotation.concurrent.Immutable;

public class MessageListActivity extends AppCompatActivity {
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    Button send;
    TextView text;
    FirebaseAuth fa;
    List<BaseMessage>list;
    DatabaseHelper mDatabaseHelper;
    OkHttpClient mClient = new OkHttpClient();
    JSONArray regArray = new JSONArray();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    String message,sendername,recievername,sentTime,recievedTime,senderId,sentMessage,userId;
    final String TAG = "NOTIFICATION TAG";
    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    protected void onStart() {
        fa = FirebaseAuth.getInstance();

        super.onStart();
    }
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        Intent i=getIntent();
        userId=i.getStringExtra("userId");
        regArray.put(i.getStringExtra("user"));
        fa = FirebaseAuth.getInstance();
        mDatabaseHelper = new DatabaseHelper(this);
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );
       list = new ArrayList<BaseMessage>() {
        };
        text=findViewById(R.id.edittext_chatbox);
        send=findViewById(R.id.button_chatbox_send);
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this, list);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);
        Log.d("user",""+userId);
        Cursor data=mDatabaseHelper.getData(userId);
        while(data.moveToNext()){
            list.add(new BaseMessage("","",data.getString(0),data.getString(2),data.getString(1),data.getString(3),data.getString(5),data.getString(6),data.getString(7)));
        }
        mMessageAdapter.notifyDataSetChanged();
        mMessageRecycler.scrollToPosition(0);
        //Toast.makeText(MessageListActivity.this,""+userId,Toast.LENGTH_LONG).show();
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users").document(fa.getCurrentUser().getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        sendername = document.getData().get("name").toString();
                    }
                }
            }});
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!text.getText().toString().isEmpty()){
                    Log.d("Rec2",""+userId);
                    list.add(new BaseMessage(sendername, "", Calendar.getInstance().getTime().toString(), "", text.getText().toString().trim(), "", fa.getCurrentUser().getUid(), userId,""));
                    mMessageAdapter.notifyDataSetChanged();
                    sentMessage=text.getText().toString();
                    sendMessage(regArray, "File uploaded!!", "hi", text.getText().toString());
                    text.setText("");
                    mDatabaseHelper.addData(new BaseMessage("","",Calendar.getInstance().getTime().toString().substring(0,19),message,sentMessage,"",fa.getCurrentUser().getUid(),userId,""));
                    mMessageAdapter.notifyDataSetChanged();
                }
                else{Toast.makeText(MessageListActivity.this,"Message cannot be empty",Toast.LENGTH_LONG).show();}
            }
        });
    }
    public void sendMessage(final JSONArray recipients, final String title, final String body, final String message) {
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
                    data.put("userId",fa.getCurrentUser().getUid());
                    data.put("senderName",sendername);
                    data.put("sentTime", Calendar.getInstance().getTime());
                    Log.d("sent :",""+userId);
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

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MessageListActivity.this, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
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
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            message=intent.getExtras().getString("message");
            String sender=intent.getExtras().getString("senderName");
            sentTime=intent.getExtras().getString("sentTime");
            senderId=intent.getExtras().getString("userId");
            String recId=intent.getExtras().getString("recieverId");
            String downloadUrl=intent.getExtras().getString("downloadUrl");
            //Toast.makeText(MessageListActivity.this,"added at mlist",Toast.LENGTH_SHORT).show();
            //mDatabaseHelper.addData(new BaseMessage("",sender,"",Calendar.getInstance().getTime().toString().substring(0,19),"",message,"",recId,downloadUrl));
            list.add(new BaseMessage("You",sender,sentTime,Calendar.getInstance().getTime().toString().substring(0,19),"",message,senderId,recId,downloadUrl));
            mMessageAdapter.notifyDataSetChanged();


        }
    };

    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}