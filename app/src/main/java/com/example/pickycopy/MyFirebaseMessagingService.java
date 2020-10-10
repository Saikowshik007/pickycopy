package com.example.pickycopy;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String ADMIN_CHANNEL_ID ="admin_channel";
    private LocalBroadcastManager broadcaster;
    DatabaseHelper mdatabaseHelper;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        mdatabaseHelper=new DatabaseHelper(this);
        final Intent intent = new Intent(this, MainActivity.class);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels(notificationManager);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this , 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.notify_icon);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.notify_icon)
                .setLargeIcon(largeIcon)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("message").contains("")?"File Url received":remoteMessage.getData().get("message"))
                .setAutoCancel(true)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        if(isForeground("com.example.pickycopy.MessageListActivity")){}
        else{
            notificationManager.notify(notificationID, notificationBuilder.build());
            Log.d("sercive:","added at service");
        }
        mdatabaseHelper.addData(new BaseMessage(remoteMessage.getData().get("senderName"),"",remoteMessage.getData().get("sentTime"), Calendar.getInstance().getTime().toString().substring(0,19),"",remoteMessage.getData().get("message"),remoteMessage.getData().get("userId"),remoteMessage.getData().get("recieverId"),remoteMessage.getData().get("downloadUrl")));
        sendMessageActivity(remoteMessage);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager){
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device to device notification";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }


    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }


public void sendMessageActivity(RemoteMessage remoteMessage){
        Intent intent = new Intent("MyData");
    intent.putExtra("title", remoteMessage.getData().get("title"));
    intent.putExtra("message", remoteMessage.getData().get("message"));
    intent.putExtra("downloadUrl",remoteMessage.getData().get("downloadUrl"));
    intent.putExtra("userId",remoteMessage.getData().get("userId"));
    intent.putExtra("sentTime",remoteMessage.getData().get("sentTime"));
    intent.putExtra("recievedTime",remoteMessage.getData().get("recievedTime"));
    intent.putExtra("senderName",remoteMessage.getData().get("senderName"));
    intent.putExtra("recieverId",remoteMessage.getData().get("recieverId"));
    intent.putExtra("downloadUrl",remoteMessage.getData().get("downloadUrl"));
    broadcaster.sendBroadcast(intent);

}
    public boolean isForeground(String myactivity) {
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        componentInfo.getPackageName();
        return taskInfo.get(0).topActivity.getClassName().contains(myactivity);

    }
}


