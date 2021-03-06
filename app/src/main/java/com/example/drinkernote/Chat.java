package com.example.drinkernote;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;


import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.Date;


public class Chat extends AppCompatActivity {

    Toolbar toolbar;
    ActionBar actionBar;
    MyAdapter myAdapter;
    LinearLayoutManager layoutManager;
    RecyclerView rv_MesegeList;

    CircleImageView iv_chat_profileimg;
    TextView tv_chatID;
    EditText et_chat_contents;
    Button btn_chat_submit;
    ArrayList<MyData> myData;
    String profileIMG;
    ImageButton btn_chat_img;
//    NestedScrollView nestedScrollView;

    int RoomNum;
    String targetID;
    String myID;
    String centerdate = "";
    String pagedate = "";
    boolean lastdateflag = false;
    RequestQueue requestQueue;//?????? ????????? ??????????????? ??????????????????.

    boolean position_flag=true;
    int page = 0;
    int limit = 10;

    private static final int PICK_FROM_CAMERA = 0;

    private static final int PICK_FROM_ALBUM = 1;


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //???????????????????????? ??? ????????? ????????? ???????????? ???

            int readRoomnum = intent.getIntExtra("read",-1);
//            Log.e("?????????????????? ????????????", "?????? ?????? : "+ String.valueOf(readRoomnum));
            if(readRoomnum == -1) { //read??? ???????????? ????????? ??????????????? ?????????
                String msg = intent.getStringExtra("msg");
//                Log.e("??????????????? ??????????????? ", "?????? ?????????? "+msg);
                Gson gson = new Gson();
                MSG MSG = gson.fromJson(msg, MSG.class);
//                Log.e("?????? ?????????", msg);
                if (MSG.getRoomNum() == RoomNum) { //?????? ????????? ??????????????? ???????????? ????????? ????????? ???????????? ????????????.
                    //????????? ??????????????? ???????????? ???????????? ?????? ????????????????????? ?????? ?????????????????? ????????????
//                    readmsg(RoomNum, MSG.getMyID());
                    //?????????????????? ????????? ???????????? ????????? ???????????? ?????????????????? ???????????????
                    if (!centerdate.equals(makeDate.dateformat(getnow()))) { //?????? ???????????? ????????? ????????????
                        centerdate = makeDate.dateformat(getnow()); //????????? ????????????
                        myData.add(new MyData(2, centerdate));
                    }
                    if (MSG.isCheckimg()) {
                        for (int i = 0; i < MSG.getMsgs().size(); i++){
                            myData.add(new MyData(MSG.getMyID(), MSG.getMsgs().get(i), 3, makeDate.customdateformat(getnow(), "a hh:mm"), 0));
                        }

                    } else {
                        myData.add(new MyData(MSG.getMyID(), MSG.getMsgs().get(0), 0, makeDate.customdateformat(getnow(), "a hh:mm"), 0));
                    }
                    myAdapter.setItems(myData);
                    rv_MesegeList.scrollToPosition(myData.size() - 1);
//            nestedScrollView.fullScroll(View.FOCUS_DOWN);
                    et_chat_contents.requestFocus();
                    onThisRoom(RoomNum);
                }
            }
            else{ //?????? ???????????????
                if(readRoomnum == RoomNum){ //?????? ??????????????? ?????? ????????? ??????????????? ?????? ???????????? ?????????
//                    ?????? ?????????????????? ?????? 1??? ???????????? ?????????

                    for (int i = 0; i < myData.size(); i++){
                        if(myData.get(i).getViewType() != 2 ) {
                            if (myData.get(i).getId().equals(myID)) { //?????? ?????? ???????????????
                                myData.get(i).setReadnum(0); //???????????? ????????? ???????????????
                            }
                        }
                    }
                    myAdapter.setItems(myData); //????????? ???????????? ??????????????????.
                }
            }


        }
    };


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //?????? ???????????? ????????????????????? ????????? ???????????? ??????????????? ???????????????????????? ????????? ???????????? ?????????????????? ????????? ?????? ??????????????????.
        Log.e("ChatClass", "onNewIntent");

//        Log.e("OnNewIntent", "old RoomNum"+RoomNum);
//        Log.e("OnNewIntent", "new RoomNum"+intent.getIntExtra("RoomNum",-1));
        RoomNum = intent.getIntExtra("RoomNum",-1);
        targetID = intent.getStringExtra("targetID");

        myData.clear();
        myAdapter.setItems(myData);
        tv_chatID.setText(targetID);
        readmsg(RoomNum,targetID);

        CheckRequestQueue();
        GetProfileIMG(targetID);

        CheckRequestQueue();
        getMessage(RoomNum,myID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("chat onresume ", "RoomNum : "+ RoomNum);
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.cancel(RoomNum); // cancel(?????? ?????? id)
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("chat", "oncreate");
        setContentView(R.layout.activity_chat);
        myData = new ArrayList<>();
        iv_chat_profileimg = findViewById(R.id.iv_chat_profileimg);
        tv_chatID = findViewById(R.id.tv_chatID);
        et_chat_contents = findViewById(R.id.et_chat_contents);
        btn_chat_submit = findViewById(R.id.btn_chat_submit);
        rv_MesegeList = findViewById(R.id.rv_MesegeList);
        btn_chat_img = findViewById(R.id.btn_chat_img);
//        nestedScrollView = findViewById(R.id.scroll_view);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        rv_MesegeList.setLayoutManager(layoutManager);

        Intent mIntent = getIntent();
        RoomNum = mIntent.getIntExtra("RoomNum", -1);

        Log.e("chat oncreate ??????????????? : ", String.valueOf(RoomNum));
        targetID = mIntent.getStringExtra("targetID");
        Log.e("?????????????????? : ", targetID);

        //????????????????????? ???????????? ?????? ?????? ????????? ????????? ????????????
        readmsg(RoomNum,targetID);


        myID = AutoLogin.getUserId(this);
        tv_chatID.setText(targetID);


        CheckRequestQueue();
        GetProfileIMG(targetID);


        if(RoomNum != -1) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.cancel(RoomNum); // cancel(?????? ?????? id)
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("Getmsg"));


        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : ?????????????????? ?????? ????????? ?????? ??????
            }
        });


        rv_MesegeList.setAdapter(myAdapter);
        rv_MesegeList.setHasFixedSize(true);



        setToolbar();

        CheckRequestQueue();
        page ++;
        getMessage(RoomNum,myID);


        rv_MesegeList.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                if(position_flag) {
                    if ((!view.canScrollVertically(1))) {
//                        Log.d(TAG,String.valueOf(position_flag));
//                        Toast.makeText(getApplicationContext(), "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    } else if ((!view.canScrollVertically(-1))) {
                        page++;
                        getMessage(RoomNum, myID);

                    }
                    position_flag = false;
                }
                else position_flag = true;
            }
        });

        //?????? ?????? ?????? ?????? ?????????
        btn_chat_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = et_chat_contents.getText().toString();
                if(msg.equals("")){
                    Toast.makeText(getApplicationContext(), "???????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                }else {
//                Log.e("???????????? ??????", "????????? : "+ RoomNum+ " ?????? : "+msg);
                    if (RoomNum == -1) { //???????????? ???????????? ?????? ?????????
                        CheckRequestQueue();
                        MakeRoom(myID, targetID, msg); //?????? ?????????.
                    } else { // ?????? ?????????
                        ArrayList<String> Msg = new ArrayList<>();
                        Msg.add(msg);
                        sendmsg(Msg,false);
                    }
                    //"Sendmsg" ???????????? ????????????????????? ?????????
                    et_chat_contents.setText("");
                    et_chat_contents.requestFocus();
                }

            }
        });

        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PICK_FROM_CAMERA);
            }
        };

        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };


        btn_chat_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(Chat.this)
                        .setTitle("???????????? ?????????")
                        .setPositiveButton("?????????", cameraListener)
                        .setNeutralButton("??????", cancelListener)
                        .setNegativeButton("????????????", albumListener)
                        .show();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("???????????? ???????????? ", "?????? ????????? : "+ RoomNum);
        onThisRoom(RoomNum);
        switch(requestCode) {
            case PICK_FROM_ALBUM:
            {
                if(data == null){
                    Toast.makeText(getApplicationContext(), "???????????? ???????????? ???????????????.", Toast.LENGTH_LONG).show();
                }else{
                    ClipData clipData = data.getClipData();
                    if (clipData.getItemCount() > 5) {   // ????????? ???????????? 5??? ????????? ??????
                        Toast.makeText(getApplicationContext(), "????????? 5????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
                    }else {
                        ArrayList<String> imgPathList = new ArrayList<>();
                        for (int i = 0; i < clipData.getItemCount(); i++){
                            imgPathList.add(getPathFromUri(getApplicationContext(),clipData.getItemAt(i).getUri()));
                        }
                        CheckRequestQueue();
                        sendImg(clipData.getItemCount(),RoomNum,myID,imgPathList);
                    }
                }

                break;
            }

            case PICK_FROM_CAMERA:
            {
                if(data != null) {
                }

                break;
            }

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e("chat", "onstart");
//        CheckRequestQueue();
//        myData.clear();
//        getMessage(RoomNum,myID);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("chat", "onrestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        quitThisRoom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }



    public String getPathFromUri(Context context, Uri uri){

        String path;
        String[] proj = {MediaStore.Images.Media.DATA} ;

        Cursor cursor = context.getContentResolver().query(uri,proj , null,null,null);

        if(cursor == null){
            path = uri.getEncodedPath();

        }else{
            cursor.moveToNext();
            path = cursor.getString((cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
            cursor.close();
        }

        return path;
    }

    void CheckRequestQueue(){
        if (requestQueue == null) {
            //RequestQueue ?????? ????????????
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }


    public void sendImg(int Imgnum,int RoomNum,String fromID,ArrayList<String> ImgpathList) {
        String url = "http://13.209.19.188/Imgmsg.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

                        Log.e("Imgmsg", "onResponse: ?????? : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            JSONArray imgpath = jsonObject.getJSONArray("imgpath");
                            if(res.equals("true")) {
                                //??? ??????????????????????????? ??????????????? ?????? ???????????? ????????????
                                if (!centerdate.equals(makeDate.dateformat(getnow()))) { //?????? ???????????? ????????? ????????????
                                    centerdate = makeDate.dateformat(getnow());
                                    myData.add(new MyData(2, centerdate));
                                }
                                //??? ???????????? ???????????? ???????????? ?????????
                                ArrayList<String> ImgPath=new ArrayList<>();

                                for (int i = 0; i < imgpath.length(); i++){
                                    ImgPath.add(imgpath.getString(i));
                                }
                                sendmsg(ImgPath,true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "??????-> " + error.getMessage());
                    }
                });
        request.addStringParam("Imgnum", String.valueOf(Imgnum)); //POST???????????? ??????
        request.addStringParam("RoomNum", String.valueOf(RoomNum)); //POST???????????? ??????
        request.addStringParam("fromID", String.valueOf(fromID)); //POST???????????? ??????
        for (int i = 0; i < ImgpathList.size(); i++){
            request.addFile("image"+i,ImgpathList.get(i));
        }
        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }

    public void getMessage(int RoomNum,String myID) {
        String url = "http://13.209.19.188/GetMessage.php?RoomNum=" + RoomNum + "&myID="+myID + "&page="+page+ "&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: ?????? : " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean nomessage = jsonObject.getBoolean("nomessage");
                            if(nomessage){ //???????????? ?????????
                                JSONArray Send_UserID = jsonObject.getJSONArray("Send_UserID");
                                JSONArray message_contents = jsonObject.getJSONArray("message_contents");
                                JSONArray message_date = jsonObject.getJSONArray("message_date");
                                JSONArray readnum = jsonObject.getJSONArray("readnum");
                                JSONArray check_img = jsonObject.getJSONArray("check_img");
                                Log.i("????????? ?????? ", "????????? : "+page +"\n????????? "+message_contents);
                                if(page == 1){
                                    centerdate = makeDate.dateformat(message_date.getString(0));
                                    pagedate = makeDate.dateformat(message_date.getString(0));
                                }

//                                myData.add(new MyData(2,centerdate));
//                                int datecount =0;
                                for (int i = 0; i < Send_UserID.length(); i++){
                                    if(!pagedate.equals(makeDate.dateformat(message_date.getString(i)))){ //?????? ???????????? ????????? ????????????
                                        myData.add(new MyData(2,pagedate));
                                        pagedate = makeDate.dateformat(message_date.getString(i)); //????????? ????????????

//                                        datecount++;
                                    }
                                    if(Send_UserID.getString(i).equals(myID)) { //???????????? ???????????????
                                        if(check_img.getInt(i) == 1){ //?????? ?????? ????????? ??????
                                            myData.add(new MyData(Send_UserID.getString(i), message_contents.getString(i), 4, makeDate.customdateformat(message_date.getString(i), "a hh:mm"), readnum.getInt(i)));
                                        }else {
                                            myData.add(new MyData(Send_UserID.getString(i), message_contents.getString(i), 1, makeDate.customdateformat(message_date.getString(i), "a hh:mm"), readnum.getInt(i)));
                                        }
                                    }else{ //???????????? ?????? ???????????????
                                        if(check_img.getInt(i) == 1){ //???????????? ?????? ????????? ??????
                                            myData.add(new MyData(Send_UserID.getString(i), message_contents.getString(i), 3, makeDate.customdateformat(message_date.getString(i), "a hh:mm"), readnum.getInt(i)));
                                        }else {
                                            myData.add(new MyData(Send_UserID.getString(i), message_contents.getString(i), 0, makeDate.customdateformat(message_date.getString(i), "a hh:mm"), readnum.getInt(i)));
                                        }
                                    }
                                }
                                if(Send_UserID.length() < 10){ //???????????? 10????????? ????????? ????????? ??????????????? ???
                                    lastdateflag = true;
                                    myData.add(new MyData(2,pagedate));
                                }
                                myAdapter.setItems(myData);

                                if(page == 1) {
//                                    nestedScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                    rv_MesegeList.scrollToPosition(0);

                                }


                            }else{ //???????????? ?????????
                                page --;
                                if(!lastdateflag){
                                    lastdateflag = true;
                                    myData.add(new MyData(2,pagedate));
                                    myAdapter.setItems(myData);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.e("TAG", "??????-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }

    public void GetProfileIMG(String id) {
        String url = "http://13.209.19.188/GetProfileIMG.php?id=" + id;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

//                        Log.d("TAG", "onResponse: ?????? : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            profileIMG = jsonObject.getString("profileImg");
                            if (profileIMG.equals("Nothing")) {
                                Glide.with(iv_chat_profileimg)
                                        .load(R.drawable.myprofile)
                                        .placeholder(R.drawable.dataloading)
                                        .fallback(R.drawable.myprofile) //???????????? ????????? ????????????
                                        .centerCrop() //???????????? ???????????? ???????????????
                                        .into(iv_chat_profileimg);
                            } else {
                                Glide.with(iv_chat_profileimg)
                                        .load("http://13.209.19.188/" + profileIMG)
                                        .centerCrop() //???????????? ???????????? ???????????????
                                        .placeholder(R.drawable.dataloading) //??????????????? ????????????
                                        .fallback(R.drawable.myprofile) //???????????? ????????? ????????????
                                        .into(iv_chat_profileimg);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "??????-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }


    public void sendmsg(ArrayList<String> msg,boolean checkimg) {
        Intent mIntent = new Intent("Sendmsg");
        //?????????????????? ?????? ??????????????? ???????????? ???????????? ????????? json?????? ????????? ?????????.
        MSG MSG = new MSG(msg, myID, targetID, RoomNum,checkimg);
        Gson gson = new Gson();
        String jsonMSG = gson.toJson(MSG);
        mIntent.putExtra("msg", jsonMSG);
//        Log.e("?????????????????? ", jsonMSG);
        //????????????????????? ???????????? ????????????.
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
        //???????????? ?????????????????? ?????? ?????????????????? ????????????.
        if(!centerdate.equals(makeDate.dateformat(getnow()))) { //?????? ???????????? ????????? ????????????
            centerdate = makeDate.dateformat(getnow());
            myData.add(0,new MyData(2,centerdate));
        }
        int viewtype=0;
        if(checkimg){
            viewtype = 4;
            for (int i = 0; i < MSG.getMsgs().size(); i++){
                myData.add(0,new MyData(MSG.getMyID(), MSG.getMsgs().get(i),viewtype,makeDate.customdateformat(getnow(),"a hh:mm"),1));
            }
        }else{
            viewtype = 1;
            myData.add(0,new MyData(MSG.getMyID(), MSG.getMsgs().get(0),viewtype,makeDate.customdateformat(getnow(),"a hh:mm"),1));
        }

        myAdapter.setItems(myData);
        rv_MesegeList.scrollToPosition(0);
        et_chat_contents.requestFocus();
    }


    public void readmsg(int roomNum,String id) {
        //?????? ???????????? ???????????? ???????????? ???????????? ????????? ?????????
        onThisRoom(RoomNum);
        Intent mIntent = new Intent("Sendmsg");
        read read = new read(roomNum,id);
        Gson gson = new Gson();
        String jsonobj = gson.toJson(read);
        mIntent.putExtra("read", jsonobj);
//        Log.e("?????????????????? ", jsonMSG);
        //????????????????????? ???????????? ????????????.
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);

    }

    //???????????? ???????????? ???????????? ?????? ?????? ???????????? ????????????
    public void onThisRoom(int RoomNum){
        Log.e("Chat", "onThisRoom : " + RoomNum);
        Intent mIntent = new Intent("thisRoom");
        mIntent.putExtra("thisRoom",RoomNum);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
    }
    public void quitThisRoom(){
        Intent mIntent = new Intent("thisRoom");
        mIntent.putExtra("thisRoom",-1);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
    }


    public String getnow(){
        SimpleDateFormat format1 = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        Date time = new Date();
        String time1 = format1.format(time);
        return time1;
    }




    public static class MSG {
        // TODO : ?????????????????? ???????????? ????????? ?????????

        ArrayList<String> Msgs;

        String myID; //???????????????
        String targetID; //????????????
        int RoomNum; //?????????
        boolean checkimg;


        public ArrayList<String> getMsgs() {
            return Msgs;
        }

        public void setMsgs(ArrayList<String> msgs) {
            Msgs = msgs;
        }

        public MSG(ArrayList<String> msg, String myID, String targetID, int roomNum, boolean checkimg) {
            this.Msgs = msg;
            this.myID = myID;
            this.targetID = targetID;
            RoomNum = roomNum;
            this.checkimg = checkimg;
        }


        public String getMyID() {
            return myID;
        }

        public void setMyID(String myID) {
            this.myID = myID;
        }

        public String getTargetID() {
            return targetID;
        }

        public boolean isCheckimg() {
            return checkimg;
        }

        public void setCheckimg(boolean checkimg) {
            this.checkimg = checkimg;
        }

        public void setTargetID(String targetID) {
            this.targetID = targetID;
        }

        public int getRoomNum() {
            return RoomNum;
        }

        public void setRoomNum(int roomNum) {
            RoomNum = roomNum;
        }
    }

    public static class read{
        int RoomNum;
        String to;

        public int getRoomNum() {
            return RoomNum;
        }

        public void setRoomNum(int roomNum) {
            RoomNum = roomNum;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public read(int roomNum, String to) {
            RoomNum = roomNum;
            this.to = to;
        }
    }

    public void MakeRoom(String myID, String targetID, String msg) {
        String url = "http://13.209.19.188/MakeRoom.php?myID=" + myID + "&targetID=" + targetID;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

                        Log.e("????????????", "onResponse: ?????? : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int resRoomNum = jsonObject.getInt("response");
                            RoomNum = resRoomNum;
                            onThisRoom(RoomNum);
                            ArrayList<String> Msg= new ArrayList<>();
                            Msg.add(msg);
                            sendmsg(Msg,false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //?????? ??????????????? ???????????? ???????????????.

                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "??????-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }

    // ?????????????????? ???????????? ????????? ????????? ?????????
    public static class MyData {
        // TODO : ?????????????????? ???????????? ????????? ?????????
        String id, contents;
        int viewType;
        String date;
        int readnum;


        public MyData(int viewType, String date) {
            this.viewType = viewType;
            this.date = date;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getContents() {
            return contents;
        }

        public void setContents(String contents) {
            this.contents = contents;
        }

        public int getViewType() {
            return viewType;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }

        public String getDate() {
            return date;
        }


        public void setDate(String date) {
            this.date = date;
        }

        public int getReadnum() {
            return readnum;
        }

        public void setReadnum(int readnum) {
            this.readnum = readnum;
        }

        public MyData(String id, String contents, int viewType, String date, int readnum) {
            this.id = id;
            this.contents = contents;
            this.viewType = viewType;
            this.date = date;
            this.readnum = readnum;
        }

    }


    public static class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
        }

        private OnMyClickListener mListener;

        private ArrayList<MyData> mItems = new ArrayList<>();

        public MyAdapter(OnMyClickListener listener) {
            mListener = listener;
        }

        public void setItems(ArrayList<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }



        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(viewType == Code.ViewType.CENTER_CONTENT){
                view = inflater.inflate(R.layout.item_mesege_center,parent,false);
                return new CenterViewHolder(view);
            }else if(viewType == Code.ViewType.LEFT_CONTENT){
                view = inflater.inflate(R.layout.item_mesege_left,parent,false);
                return new LeftViewHolder(view);
            }else if(viewType == Code.ViewType.LEFT_CONTENT_IMG){
                view = inflater.inflate(R.layout.item_mesege_left_img,parent,false);
                return new LeftViewHolder_img(view);
            }else if(viewType == Code.ViewType.RIGHT_CONTENT_IMG){
                view = inflater.inflate(R.layout.item_mesege_right_img,parent,false);
                return new RightViewHolder_img(view);
            }
            else{
                view = inflater.inflate(R.layout.item_mesege_right,parent,false);
                return new RightViewHolder(view);
            }

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if(viewHolder instanceof CenterViewHolder){ //?????????
                ((CenterViewHolder)viewHolder).textv.setText(mItems.get(position).getDate());

            }else if(viewHolder instanceof LeftViewHolder){ //?????? ?????????
                ((LeftViewHolder)viewHolder).textv_nicname.setText(mItems.get(position).getId());
                ((LeftViewHolder)viewHolder).textv_msg.setText(mItems.get(position).getContents());
                ((LeftViewHolder)viewHolder).textv_time.setText(mItems.get(position).getDate());

            }else if(viewHolder instanceof LeftViewHolder_img){ //?????? ?????????
                ((LeftViewHolder_img)viewHolder).textv_nicname.setText(mItems.get(position).getId());
                Glide.with(((LeftViewHolder_img)viewHolder).iv_msgimg)
                        .load("http://13.209.19.188/" + mItems.get(position).getContents())
                        .centerCrop() //???????????? ???????????? ???????????????
                        .placeholder(R.drawable.dataloading) //??????????????? ????????????
                        .fallback(R.drawable.myprofile) //???????????? ????????? ????????????
                        .into(((LeftViewHolder_img)viewHolder).iv_msgimg);

//                ((LeftViewHolder_img)viewHolder).iv_msgimg.setImageBitmap(mItems.get(position).getImg());
                ((LeftViewHolder_img)viewHolder).textv_time.setText(mItems.get(position).getDate());

            }else if(viewHolder instanceof RightViewHolder_img){ //????????? ?????????
//                ((RightViewHolder_img)viewHolder).iv_msgimg.setImageBitmap(mItems.get(position).getImg());
                Glide.with(((RightViewHolder_img)viewHolder).iv_msgimg)
                        .load("http://13.209.19.188/" + mItems.get(position).getContents())
                        .centerCrop() //???????????? ???????????? ???????????????
                        .placeholder(R.drawable.dataloading) //??????????????? ????????????
                        .fallback(R.drawable.myprofile) //???????????? ????????? ????????????
                        .into(((RightViewHolder_img)viewHolder).iv_msgimg);
                ((RightViewHolder_img)viewHolder).textv_time.setText(mItems.get(position).getDate());
                if(mItems.get(position).getReadnum() == 0){
                    ((RightViewHolder_img)viewHolder).tv_readnum.setText("");
                }else{
                    ((RightViewHolder_img)viewHolder).tv_readnum.setText(String.valueOf(mItems.get(position).getReadnum()));
                }
            }else{ //????????? ?????????
                ((RightViewHolder)viewHolder).textv_msg.setText(mItems.get(position).getContents());
                ((RightViewHolder)viewHolder).textv_time.setText(mItems.get(position).getDate());
                if(mItems.get(position).getReadnum() == 0){
                    ((RightViewHolder)viewHolder).tv_readnum.setText("");
                }else{
                    ((RightViewHolder)viewHolder).tv_readnum.setText(String.valueOf(mItems.get(position).getReadnum()));
                }

            }

        }




        @Override
        public int getItemCount() {
            return mItems.size();
        }
        @Override
        public int getItemViewType(int position) {
            return mItems.get(position).getViewType();
        }


        public class CenterViewHolder extends RecyclerView.ViewHolder{
            TextView textv;

            public CenterViewHolder(@NonNull View itemView) {
                super(itemView);
                textv = (TextView)itemView.findViewById(R.id.textv);
            }
        }

        public class LeftViewHolder extends RecyclerView.ViewHolder{

            TextView textv_nicname;
            TextView textv_msg;
            TextView textv_time;

            public LeftViewHolder(@NonNull View itemView) {
                super(itemView);

                textv_nicname = (TextView)itemView.findViewById(R.id.textv_nicname);
                textv_msg = (TextView)itemView.findViewById(R.id.textv_msg);
                textv_time = (TextView)itemView.findViewById(R.id.textv_time);

            }
        }


        public class LeftViewHolder_img extends RecyclerView.ViewHolder{

            TextView textv_nicname;
            ImageView iv_msgimg;
            TextView textv_time;

            public LeftViewHolder_img(@NonNull View itemView) {
                super(itemView);

                textv_nicname = (TextView)itemView.findViewById(R.id.textv_nicname);
                iv_msgimg = itemView.findViewById(R.id.iv_msgimg);
                textv_time = (TextView)itemView.findViewById(R.id.textv_time);

            }
        }


        public class RightViewHolder extends RecyclerView.ViewHolder{
            TextView textv_msg;
            TextView textv_time;
            TextView tv_readnum;

            public RightViewHolder(@NonNull View itemView) {
                super(itemView);
                textv_msg = (TextView)itemView.findViewById(R.id.textv_msg);
                textv_time = (TextView)itemView.findViewById(R.id.textv_time);
                tv_readnum = (TextView)itemView.findViewById(R.id.tv_readnum);
            }
        }

        public class RightViewHolder_img extends RecyclerView.ViewHolder{
            ImageView iv_msgimg;
            TextView textv_time;
            TextView tv_readnum;

            public RightViewHolder_img(@NonNull View itemView) {
                super(itemView);
                iv_msgimg = itemView.findViewById(R.id.iv_msgimg);
                textv_time = (TextView)itemView.findViewById(R.id.textv_time);
                tv_readnum = (TextView)itemView.findViewById(R.id.tv_readnum);
            }
        }

    }


    public class Code{
        public class ViewType{
            public static final int LEFT_CONTENT = 0;
            public static final int RIGHT_CONTENT = 1;
            public static final int CENTER_CONTENT = 2;
            public static final int LEFT_CONTENT_IMG = 3;
            public static final int RIGHT_CONTENT_IMG = 4;
        }
    }


    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar??? back??? ????????? ??? ??????
                finish();
                return true;
            }

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//?????? ????????? ???????????????
        actionBar.setDisplayHomeAsUpEnabled(true); //????????????
    }



}