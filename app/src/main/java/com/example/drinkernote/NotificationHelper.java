package com.example.drinkernote;
import android.util.Log;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationHelper extends ContextWrapper {
    public static final String channel1ID="channel1ID";
    public static final String channel1Name="channel 1";

    private NotificationManager mManager;


    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        Log.e("createChannels", channel1ID);
        NotificationChannel channel1 = new NotificationChannel(channel1ID,channel1Name, NotificationManager.IMPORTANCE_HIGH);
        channel1.enableLights(true);
        channel1.enableVibration(true);
        channel1.setLightColor(R.color.design_default_color_primary);
        channel1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel1);
    }

    public NotificationManager getManager(){
        if(mManager == null){
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getChannel1Notification(String tittle,String msg,int RoomNum,boolean restartService){
        Intent mIntent = null;
        Log.e("getchannel noti", "roomNum : "+RoomNum);
        if(restartService){ //서비스가 재시작된다음 온 노티피 라면
            mIntent = new Intent( this,Login.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mIntent.putExtra("RoomNum",RoomNum);
            mIntent.putExtra("targetID",tittle);
            mIntent.putExtra("restartService",restartService);
        }else{
            mIntent = new Intent( this,Chat.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mIntent.putExtra("RoomNum",RoomNum);
            mIntent.putExtra("targetID",tittle);
            mIntent.putExtra("restartService",restartService);
        }

        Log.e("Notifi", "PendingIntent RoomNum : "+RoomNum);

        PendingIntent pIntent = PendingIntent.getActivity(this, RoomNum,mIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(),channel1ID)
                .setContentTitle(tittle)
                .setContentText(msg)
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(pIntent)
                .setAutoCancel(true);
    }

    public NotificationCompat.Builder getChannel1NewsNotification(String tittle,String msg,boolean restartService,Newspeed_item News){
        Intent mIntent = null;
        //뉴스피드 를 실행시키는 인텐트를 만들어야함
        if(restartService){ //서비스가 재시작된다음 온 노티피 라면
            //로그인부터 시작헤서 mainUI > Newspeed 까지 실행해야함

//            mIntent = new Intent( this,Login.class);
//            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            mIntent.putExtra("RoomNum",15);
//            mIntent.putExtra("targetID",tittle);
//            mIntent.putExtra("restartService",restartService);
        }else{
            if(News.getType() == 0){
                mIntent = new Intent(this, Newspeed.class);
            }else if(News.getType() == 1) {
                mIntent = new Intent(this, Newspeed.class);
//                mIntent.putExtra("NoteKey", News.getNoteKey());
//                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }else{
                mIntent = new Intent(this, Newspeed.class);
            }
        }



        PendingIntent pIntent = PendingIntent.getActivity(this, 1,mIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(),channel1ID)
                .setContentTitle(tittle)
                .setContentText(msg)
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                .setContentIntent(pIntent)
                .setAutoCancel(true);
    }


}
