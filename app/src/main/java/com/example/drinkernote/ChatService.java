package com.example.drinkernote;
import android.os.Handler;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;



import androidx.annotation.Nullable;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ChatService extends Service {
    final String TAG = "ChatService";
    int thisRoom = -1;
    NotificationHelper mNotificationhelper;
    public static Intent serviceIntent = null;
    SocketThread socketThread= new SocketThread();
    public HashMap<String, Boolean> newsdelay = new HashMap<String, Boolean>();


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            thisRoom = intent.getIntExtra("thisRoom",-1);
            String read = intent.getStringExtra("read");
//            Log.e("readmsg", read);
            if(read!=null) { //읽음메세지가 있을경우
                socketThread.sendmsg(read,"read");
            }else{ //일반메세지라면
                String news = intent.getStringExtra("news");

                if(news!=null){ //읽음메세지가 아니고 뉴스메세지라면
                    Log.e("챗서비스", "뉴스 메세지 :"+news);
                    Gson gson = new Gson();
                    Newspeed_item newsOBJ = gson.fromJson(news,Newspeed_item.class);
                    if(newsOBJ.getType() == 1){ //좋아요 뉴스라면
                        //해시맵확인후 쓰레드를 돌린다.
//                        Log.e("좋아요 뉴스", String.valueOf(newsdelay.get(newsOBJ.getLikeID()+newsOBJ.getNoteKey())));
                        if(newsdelay.get(newsOBJ.getLikeID()+newsOBJ.getNoteKey()) != null){ //이미 한번 보냈던 뉴스라면
//                            Log.e("좋아요 뉴스", "이미 보낸뉴스임");
                        }else { //보낸 뉴스가 아니라면
                            //해시맵에 해당 뉴스를 넣고 쓰레드를 돌린다
                            newsdelay.put(newsOBJ.getLikeID() + newsOBJ.getNoteKey(), true);
//                            Log.e("딜레이해쉬 사이즈", String.valueOf(newsdelay.size()));
                            newsdelayThread delay = new newsdelayThread(newsOBJ.getLikeID()+newsOBJ.getNoteKey());
                            delay.start();
                            socketThread.sendmsg(news, "news");
                        }

                    }else { //좋아요 뉴스가 아니면 바로 뉴스메세지를 보낸다
                        socketThread.sendmsg(news, "news");
                    }
                }else {
                    String msg = intent.getStringExtra("msg");
                    Log.e("챗서비스", "일반메세지 : " + msg);
                    socketThread.sendmsg(msg, "msg");
                }
            }

        }
    };

    private BroadcastReceiver roomReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            thisRoom  = intent.getIntExtra("thisRoom",-1);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,new IntentFilter("Sendmsg"));
        LocalBroadcastManager.getInstance(this).registerReceiver(roomReceiver,new IntentFilter("thisRoom"));


        mNotificationhelper = new NotificationHelper(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");

        if (socketThread != null) {

            socketThread.closeServer();
            socketThread.interrupt();
            socketThread = null;
        }

        serviceIntent = null;
//        setAlarmTimer();
        Thread.currentThread().interrupt();

        //서비스가 종료될때 소켓쓰레드의 소켓도 닫아준다

//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        //소켓 쓰레드 시작
        serviceIntent = intent;
        socketThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");

        return null;

    }

    public void sendOnChannel1(String tittle,String msg,int RoomNum,boolean restartService){
        optionset optionSet = AutoLogin.getUseroptionset(getApplicationContext());
        if(optionSet.isMsgAlam()) {
            NotificationCompat.Builder nb = mNotificationhelper.getChannel1Notification(tittle, msg, RoomNum, restartService);
            mNotificationhelper.getManager().notify(RoomNum, nb.build());
        }
    }
    public void sendOnChannel1News(String tittle,String msg,boolean restartService,Newspeed_item News){
        Log.e("isoptionCheck", "type = "+ News.getType()+" " +isoptionCheck(News.getType()));
        if(isoptionCheck(News.getType())) {
            NotificationCompat.Builder nb = mNotificationhelper.getChannel1NewsNotification(tittle, msg, restartService, News);
            mNotificationhelper.getManager().notify(1, nb.build());
        }

    }

    public boolean isoptionCheck(int type){
        optionset optionSet = AutoLogin.getUseroptionset(getApplicationContext());
        boolean res = false;
        //0 댓글뉴스 1좋아요뉴스 2팔로우뉴스 3대댓글뉴스
        if(type == 0 ){
            res = optionSet.isComentAlam();
        }else if(type ==1){
            res = optionSet.isNotelikeAlam();
        }else if(type ==2){
            res = optionSet.isFollowAlam();
        }else if(type ==3){
            res = optionSet.isReplyAlam();
        }


        return res;
    }


    protected void setAlarmTimer() {

        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, (int) 0);
        Intent intent = new Intent(this, AlarmReceiver.class);

        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }


    class newsdelayThread extends Thread{
        String hashid;

        public newsdelayThread(String hashid) {
            this.hashid = hashid;
        }

        @Override
        public void run() {
//            Log.e("뉴스 딜레이쓰레드 ", "실행");
            try {
                Thread.sleep(60000);
                newsdelay.remove(hashid);
//                Log.e("뉴스 딜레이쓰레드 ", "핸들러 삭제 :"+hashid);

            } catch (InterruptedException e) {
//                Log.e("뉴스 딜레이스레드", "에러 : "+e);
                e.printStackTrace();
            }

        }
    }


    class SocketThread extends Thread {

        Socket socket = null;            //Server와 통신하기 위한 Socket
        DataInputStream in = null;        //Server로부터 데이터를 읽어들이기 위한 입력스트림

        DataOutputStream out = null;    //서버로 내보내기위한 스트림


        public void closeServer() {
            try {
                if(socket != null) {
                    socket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendmsg(String msg,String type) {
            Thread th = new Thread(new Send(out, msg,type));
            th.start();
        }




        @Override
        public void run() {
            Log.e(TAG, "소켓스레드 시작");

            try {
//                Log.e("소켓 접속 시도 ","");

                socket = new Socket("10.0.2.2",12345);    //서버로 접속
                if(socket != null){
                    Log.e(TAG, "소켓접속");
                }


                in = new DataInputStream(socket.getInputStream());            //서버로부터 데이터 읽어들이기 위한 스트림 생성

                out = new DataOutputStream(socket.getOutputStream());        //채팅 내용을 서버로 전송하기 위한 출력 스트림

                //채팅에 사용 할 닉네임을 입력받음
                String userId = AutoLogin.getUserId(getApplicationContext());
                //서버로 닉네임을 전송
                out.writeUTF(userId);
//
//                //채팅방을 입력받는다
//                String room = "1";
//                //서버로 방번호를 전송
//                out.writeUTF(room);

            }catch(IOException e) {
                Log.e("소켓 접속에러 ", String.valueOf(e));
            }
            try {
                //클라이언트의 메인 쓰레드는 서버로부터 데이터 읽어들이는 것만 반복.
                while(true)
                {
                    //서버로부터 오는 메세지를 읽는다
                    String str2 = in.readUTF();
                    Log.e("서버에서 오는 메세지 : ", str2);
                    if(str2.equals("read")) { //읽음메세지라면
                        int Roomnum = Integer.parseInt(in.readUTF()); //방번호를 받아서 해당방에 있다면 1을 없애야함
                        Intent mIntent = new Intent("Getmsg");
                        mIntent.putExtra("read", Roomnum);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
                    }
                    else if(str2.equals("news")){ //뉴스메세지라면
                        String newsJson = in.readUTF();
                        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.RunningTaskInfo> info = manager.getRunningTasks(1);
                        Gson gson = new Gson();
                        Newspeed_item newsOBJ = gson.fromJson(newsJson,Newspeed_item.class);
                        if(info.size()> 0) { //무언가 실행중이라면
                            ComponentName componentName = info.get(0).topActivity;
                            String ActivityName = componentName.getShortClassName().substring(1);
                            Log.e("현재실행중 : ", ActivityName);
                            Log.e("뉴스메세지 : ", newsJson);
                            sendOnChannel1News("DrinkerNote",new NewsTypeString(newsOBJ).getnotiString(), false,newsOBJ);
                            if (!ActivityName.equals("Newspeed")) { //뉴스피드가 아니라면
                                //MainUI 라면 종을 빨간점으로 바꿔준다.
                                if(ActivityName.equals("MainUI")){
                                    Intent mIntent = new Intent("news");
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
                                }
                            }else if(ActivityName.equals("Newspeed")){ //뉴스피드액티비티가 실행중이라면
                                //아이템을 업데이트 시켜줘야함

                                Intent mIntent = new Intent("updatenews");
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
                            }
                        }
                    }
                    else{ //일반 메세지라면
                        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.RunningTaskInfo> info = manager.getRunningTasks(1);
                        Gson gson = new Gson();
                        Chat.MSG MSG = gson.fromJson(str2, Chat.MSG.class);
                        if(info.size()> 0) { //무언가 실행중이라면
                            ComponentName componentName = info.get(0).topActivity;
                            String ActivityName = componentName.getShortClassName().substring(1);
                            Log.e("현재실행중 : ",  ActivityName);
                            if (!ActivityName.equals("Chat")) { //채팅방이 아니라면
                                //notification알림 한다
                                if(MSG.isCheckimg()) {
                                    sendOnChannel1(MSG.getMyID(), "사진을 " +MSG.getMsgs().size()+"장 보냈습니다.", MSG.getRoomNum(), false);
                                }else{
                                    sendOnChannel1(MSG.getMyID(), MSG.getMsgs().get(0), MSG.getRoomNum(), false);
                                }

                                if (ActivityName.equals("Chating")) { //채팅방 목록이 띄워져있다면
                                    //들어온 메세지를 채팅방목록에 업데이트 해야함
                                    Intent mIntent = new Intent("Newmsg");
                                    mIntent.putExtra("msg", str2);
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
                                }
                            } else if (ActivityName.equals("Chat")) { //현재 실행중인 액티비티가 채팅창이라면
//                                Log.e("챗서비스", "현재 실행중인 chat 방번호 : " + thisRoom);
                                if (MSG.getRoomNum() != thisRoom) {//받은 메세지가 현재 열려있는 채팅창의 방과 같은지 확인한다
                                    //현재열려있는 채팅창의 방이 들어온메세지의 방과 다르다면 알림한다.
                                    if(MSG.isCheckimg()) {
                                        sendOnChannel1(MSG.getMyID(), "사진을 " +MSG.getMsgs().size()+"장 보냈습니다.", MSG.getRoomNum(), false);
                                    }else{
                                        sendOnChannel1(MSG.getMyID(), MSG.getMsgs().get(0), MSG.getRoomNum(), false);
                                    }
                                } else { //현재 실행중인 채팅창이 같은 방번호라면
                                    Chat.read read = new Chat.read(MSG.getRoomNum(),MSG.getMyID());
                                    Gson gsson = new Gson();
                                    String jsonobj = gsson.toJson(read);
                                    socketThread.sendmsg(jsonobj,"read"); //읽음메세지를 보낸다
                                    Intent mIntent = new Intent("Getmsg");
                                    mIntent.putExtra("msg", str2); //받은메세지를 챗액티비티로 보낸다
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
                                }
                            }
                        }else{
//                            Log.e("현재실행중 : ", "아무것도 안실행중");
                            if(MSG.isCheckimg()){
                                sendOnChannel1(MSG.getMyID(), "사진을 " +MSG.getMsgs().size()+"장 보냈습니다.", MSG.getRoomNum(), false);
                            }else {
                                sendOnChannel1(MSG.getMyID(), MSG.getMsgs().get(0), MSG.getRoomNum(), true);
                            }
                        }

                    }

                }
            }catch(IOException e) {

            }
        }
    }
}
