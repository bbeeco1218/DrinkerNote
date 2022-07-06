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
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.

    boolean position_flag=true;
    int page = 0;
    int limit = 10;

    private static final int PICK_FROM_CAMERA = 0;

    private static final int PICK_FROM_ALBUM = 1;


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //로컬브로드캐스트 가 무언가 보내면 받아오는 곳

            int readRoomnum = intent.getIntExtra("read",-1);
//            Log.e("브로드캐스트 메세지옴", "읽음 인가 : "+ String.valueOf(readRoomnum));
            if(readRoomnum == -1) { //read에 아무것도 없다면 일반메세지 라는뜻
                String msg = intent.getStringExtra("msg");
//                Log.e("챗액티비티 메세지받음 ", "어떤 메세지? "+msg);
                Gson gson = new Gson();
                MSG MSG = gson.fromJson(msg, MSG.class);
//                Log.e("받은 메세지", msg);
                if (MSG.getRoomNum() == RoomNum) { //지금 띄워진 액티비티의 방번호와 같다면 화면에 메세지를 표시한다.
                    //띄워진 액티비티의 방번호와 같다는건 바로 읽음이기때문에 바로 읽음메세지를 보내준다
//                    readmsg(RoomNum, MSG.getMyID());
                    //받은메세지의 날짜를 확인해서 날짜가 다르다면 날짜아이템을 추가해준다
                    if (!centerdate.equals(makeDate.dateformat(getnow()))) { //현재 메세지의 날짜가 다르다면
                        centerdate = makeDate.dateformat(getnow()); //날짜를 바꿔주고
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
            else{ //읽음 메세지라면
                if(readRoomnum == RoomNum){ //읽음 메세지인데 현재 띄워진 액티비티가 해당 방번호와 같다면
//                    내가 보낸메세지의 모든 1을 사라지게 해야함

                    for (int i = 0; i < myData.size(); i++){
                        if(myData.get(i).getViewType() != 2 ) {
                            if (myData.get(i).getId().equals(myID)) { //내가 보낸 메세지라면
                                myData.get(i).setReadnum(0); //읽었다는 표시로 셋팅해준다
                            }
                        }
                    }
                    myAdapter.setItems(myData); //어댑터 아이탬을 새로셋팅한다.
                }
            }


        }
    };


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //해당 메서드로 들어왔다는것은 인텐트 플래그에 싱글탑으로 들어온거기때문에 기존에 열려있던 액티비티이다 새롭게 뷰를 꾸며줘야한다.
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
//        notificationManager.cancel(RoomNum); // cancel(알림 특정 id)
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

        Log.e("chat oncreate 채팅방번호 : ", String.valueOf(RoomNum));
        targetID = mIntent.getStringExtra("targetID");
        Log.e("상대방아이디 : ", targetID);

        //해당액티비티가 시작되면 해당 방에 읽음을 서버로 전송해줌
        readmsg(RoomNum,targetID);


        myID = AutoLogin.getUserId(this);
        tv_chatID.setText(targetID);


        CheckRequestQueue();
        GetProfileIMG(targetID);


        if(RoomNum != -1) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.cancel(RoomNum); // cancel(알림 특정 id)
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter("Getmsg"));


        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
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
//                        Toast.makeText(getApplicationContext(), "스크롤의 최하단입니다.", Toast.LENGTH_SHORT).show();
                    } else if ((!view.canScrollVertically(-1))) {
                        page++;
                        getMessage(RoomNum, myID);

                    }
                    position_flag = false;
                }
                else position_flag = true;
            }
        });

        //채팅 전송 버튼 클릭 리스너
        btn_chat_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = et_chat_contents.getText().toString();
                if(msg.equals("")){
                    Toast.makeText(getApplicationContext(), "메세지를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else {
//                Log.e("전송버튼 클릭", "방번호 : "+ RoomNum+ " 내용 : "+msg);
                    if (RoomNum == -1) { //상대방과 만들어진 방이 없다면
                        CheckRequestQueue();
                        MakeRoom(myID, targetID, msg); //방을 만든다.
                    } else { // 방이 있다면
                        ArrayList<String> Msg = new ArrayList<>();
                        Msg.add(msg);
                        sendmsg(Msg,false);
                    }
                    //"Sendmsg" 인텐트로 브로드캐스트를 보낸다
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
                        .setTitle("업로드할 이미지")
                        .setPositiveButton("카메라", cameraListener)
                        .setNeutralButton("취소", cancelListener)
                        .setNegativeButton("앨범선택", albumListener)
                        .show();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("카메라가 실행되고 ", "현재 방번호 : "+ RoomNum);
        onThisRoom(RoomNum);
        switch(requestCode) {
            case PICK_FROM_ALBUM:
            {
                if(data == null){
                    Toast.makeText(getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
                }else{
                    ClipData clipData = data.getClipData();
                    if (clipData.getItemCount() > 5) {   // 선택한 이미지가 5장 이상인 경우
                        Toast.makeText(getApplicationContext(), "사진은 5장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
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
            //RequestQueue 객체 생성하기
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }


    public void sendImg(int Imgnum,int RoomNum,String fromID,ArrayList<String> ImgpathList) {
        String url = "http://13.209.19.188/Imgmsg.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

                        Log.e("Imgmsg", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            JSONArray imgpath = jsonObject.getJSONArray("imgpath");
                            if(res.equals("true")) {
                                //내 채팅리스트에올리고 상대방에게 해당 메세지를 보내야함
                                if (!centerdate.equals(makeDate.dateformat(getnow()))) { //현재 메세지의 날짜가 다르다면
                                    centerdate = makeDate.dateformat(getnow());
                                    myData.add(new MyData(2, centerdate));
                                }
                                //내 리스트에 넣으면서 메세지를 보낸다
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
                    @Override //에러시 처리할 내용
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "에러-> " + error.getMessage());
                    }
                });
        request.addStringParam("Imgnum", String.valueOf(Imgnum)); //POST파라미터 넣기
        request.addStringParam("RoomNum", String.valueOf(RoomNum)); //POST파라미터 넣기
        request.addStringParam("fromID", String.valueOf(fromID)); //POST파라미터 넣기
        for (int i = 0; i < ImgpathList.size(); i++){
            request.addFile("image"+i,ImgpathList.get(i));
        }
        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

    public void getMessage(int RoomNum,String myID) {
        String url = "http://13.209.19.188/GetMessage.php?RoomNum=" + RoomNum + "&myID="+myID + "&page="+page+ "&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean nomessage = jsonObject.getBoolean("nomessage");
                            if(nomessage){ //메세지가 있다면
                                JSONArray Send_UserID = jsonObject.getJSONArray("Send_UserID");
                                JSONArray message_contents = jsonObject.getJSONArray("message_contents");
                                JSONArray message_date = jsonObject.getJSONArray("message_date");
                                JSONArray readnum = jsonObject.getJSONArray("readnum");
                                JSONArray check_img = jsonObject.getJSONArray("check_img");
                                Log.i("페이징 확인 ", "페이지 : "+page +"\n메세지 "+message_contents);
                                if(page == 1){
                                    centerdate = makeDate.dateformat(message_date.getString(0));
                                    pagedate = makeDate.dateformat(message_date.getString(0));
                                }

//                                myData.add(new MyData(2,centerdate));
//                                int datecount =0;
                                for (int i = 0; i < Send_UserID.length(); i++){
                                    if(!pagedate.equals(makeDate.dateformat(message_date.getString(i)))){ //현재 메세지의 날짜가 다르다면
                                        myData.add(new MyData(2,pagedate));
                                        pagedate = makeDate.dateformat(message_date.getString(i)); //날짜를 바꿔주고

//                                        datecount++;
                                    }
                                    if(Send_UserID.getString(i).equals(myID)) { //내가보낸 메세지라면
                                        if(check_img.getInt(i) == 1){ //내가 보낸 이미지 라면
                                            myData.add(new MyData(Send_UserID.getString(i), message_contents.getString(i), 4, makeDate.customdateformat(message_date.getString(i), "a hh:mm"), readnum.getInt(i)));
                                        }else {
                                            myData.add(new MyData(Send_UserID.getString(i), message_contents.getString(i), 1, makeDate.customdateformat(message_date.getString(i), "a hh:mm"), readnum.getInt(i)));
                                        }
                                    }else{ //상대방이 보낸 메세지라면
                                        if(check_img.getInt(i) == 1){ //상대방이 보낸 이미지 라면
                                            myData.add(new MyData(Send_UserID.getString(i), message_contents.getString(i), 3, makeDate.customdateformat(message_date.getString(i), "a hh:mm"), readnum.getInt(i)));
                                        }else {
                                            myData.add(new MyData(Send_UserID.getString(i), message_contents.getString(i), 0, makeDate.customdateformat(message_date.getString(i), "a hh:mm"), readnum.getInt(i)));
                                        }
                                    }
                                }
                                if(Send_UserID.length() < 10){ //리미트인 10개보다 작다면 마지막 페이지라는 뜻
                                    lastdateflag = true;
                                    myData.add(new MyData(2,pagedate));
                                }
                                myAdapter.setItems(myData);

                                if(page == 1) {
//                                    nestedScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                    rv_MesegeList.scrollToPosition(0);

                                }


                            }else{ //메세지가 없다면
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
                    @Override //에러시 처리할 내용
                    public void onErrorResponse(VolleyError error) {

                        Log.e("TAG", "에러-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

    public void GetProfileIMG(String id) {
        String url = "http://13.209.19.188/GetProfileIMG.php?id=" + id;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.d("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            profileIMG = jsonObject.getString("profileImg");
                            if (profileIMG.equals("Nothing")) {
                                Glide.with(iv_chat_profileimg)
                                        .load(R.drawable.myprofile)
                                        .placeholder(R.drawable.dataloading)
                                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                                        .centerCrop() //가운데를 기준으로 크기맞추기
                                        .into(iv_chat_profileimg);
                            } else {
                                Glide.with(iv_chat_profileimg)
                                        .load("http://13.209.19.188/" + profileIMG)
                                        .centerCrop() //가운데를 기준으로 크기맞추기
                                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                                        .into(iv_chat_profileimg);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override //에러시 처리할 내용
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "에러-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }


    public void sendmsg(ArrayList<String> msg,boolean checkimg) {
        Intent mIntent = new Intent("Sendmsg");
        //메세지객체에 내용 보내는사람 받는사람 방번호를 넣어서 json으로 바꿔서 보낸다.
        MSG MSG = new MSG(msg, myID, targetID, RoomNum,checkimg);
        Gson gson = new Gson();
        String jsonMSG = gson.toJson(MSG);
        mIntent.putExtra("msg", jsonMSG);
//        Log.e("메세지보내기 ", jsonMSG);
        //브로드캐스트로 메세지를 전송한다.
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
        //내화면의 리사이클러뷰 에도 보낸메세지를 추가한다.
        if(!centerdate.equals(makeDate.dateformat(getnow()))) { //현재 메세지의 날짜가 다르다면
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
        //방을 입장하면 읽었다는 방번호와 읽었다는 표시를 보낸다
        onThisRoom(RoomNum);
        Intent mIntent = new Intent("Sendmsg");
        read read = new read(roomNum,id);
        Gson gson = new Gson();
        String jsonobj = gson.toJson(read);
        mIntent.putExtra("read", jsonobj);
//        Log.e("메세지보내기 ", jsonMSG);
        //브로드캐스트로 메세지를 전송한다.
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);

    }

    //채팅창이 시작되면 서비스에 현재 있는 방번호를 보내준다
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
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트

        ArrayList<String> Msgs;

        String myID; //보내는사람
        String targetID; //받는사람
        int RoomNum; //방번호
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
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

                        Log.e("방만들기", "onResponse: 응답 : " + response);
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
                        //방이 만들어지고 메세지를 보내야한다.

                    }
                },
                new Response.ErrorListener() {
                    @Override //에러시 처리할 내용
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "에러-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

    // 리사이클러뷰 아이템에 추가할 데이터 클래스
    public static class MyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트
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
            if(viewHolder instanceof CenterViewHolder){ //가운데
                ((CenterViewHolder)viewHolder).textv.setText(mItems.get(position).getDate());

            }else if(viewHolder instanceof LeftViewHolder){ //왼쪽 메세지
                ((LeftViewHolder)viewHolder).textv_nicname.setText(mItems.get(position).getId());
                ((LeftViewHolder)viewHolder).textv_msg.setText(mItems.get(position).getContents());
                ((LeftViewHolder)viewHolder).textv_time.setText(mItems.get(position).getDate());

            }else if(viewHolder instanceof LeftViewHolder_img){ //왼쪽 이미지
                ((LeftViewHolder_img)viewHolder).textv_nicname.setText(mItems.get(position).getId());
                Glide.with(((LeftViewHolder_img)viewHolder).iv_msgimg)
                        .load("http://13.209.19.188/" + mItems.get(position).getContents())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .into(((LeftViewHolder_img)viewHolder).iv_msgimg);

//                ((LeftViewHolder_img)viewHolder).iv_msgimg.setImageBitmap(mItems.get(position).getImg());
                ((LeftViewHolder_img)viewHolder).textv_time.setText(mItems.get(position).getDate());

            }else if(viewHolder instanceof RightViewHolder_img){ //오른쪽 이미지
//                ((RightViewHolder_img)viewHolder).iv_msgimg.setImageBitmap(mItems.get(position).getImg());
                Glide.with(((RightViewHolder_img)viewHolder).iv_msgimg)
                        .load("http://13.209.19.188/" + mItems.get(position).getContents())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .into(((RightViewHolder_img)viewHolder).iv_msgimg);
                ((RightViewHolder_img)viewHolder).textv_time.setText(mItems.get(position).getDate());
                if(mItems.get(position).getReadnum() == 0){
                    ((RightViewHolder_img)viewHolder).tv_readnum.setText("");
                }else{
                    ((RightViewHolder_img)viewHolder).tv_readnum.setText(String.valueOf(mItems.get(position).getReadnum()));
                }
            }else{ //오른쪽 메세지
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
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
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
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다
        actionBar.setDisplayHomeAsUpEnabled(true); //뒤로가기
    }



}