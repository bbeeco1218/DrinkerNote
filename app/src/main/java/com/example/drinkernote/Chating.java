package com.example.drinkernote;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.content.Intent;


import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chating extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;
    MyAdapter myAdapter;
    LinearLayoutManager layoutManager;
    RecyclerView rv_RoomList;
    TextView tv_noroom;
    NestedScrollView nestedScrollView;

    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    ArrayList<MyData> myData = new ArrayList<>();

    Context appcon;
    String userId;
    int page = 0;
    int limit = 10;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newmsg = intent.getStringExtra("msg");
            Chat.MSG MSG;

            Gson gson = new Gson();
            MSG = gson.fromJson(newmsg, Chat.MSG.class);
            boolean roomflag = false;
            for (int i = 0; i < myData.size(); i++) {
                if (myData.get(i).getRoomNum() == MSG.getRoomNum()) {
                    if (MSG.isCheckimg()) {
                        myData.get(i).setLatelymessage("사진을 보냈습니다.");
                    } else {
                        myData.get(i).setLatelymessage(MSG.getMsgs().get(0));
                    }
                    myData.get(i).setLatelymessage_date(makeDate.formatTimeString(getnow()));
                    int noread = myData.get(i).getNonreadnum();
                    noread++;
                    myData.get(i).setNonreadnum(noread);

                    MyData mdata = myData.get(i);
                    myData.remove(i);
                    myData.add(0, mdata);
                    myAdapter.setItems(myData);
                    roomflag = true;
                    break;
                }
            }
            if (!roomflag) { //반복문이 끝났는데 false라는 뜻은 메세지가 오고 최초의 방이라는뜻
                //방이 만들어지고 메세지가 저장될때까지 시간이 필요하기때문에 0.6초 정도 딜레이를 준다
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //딜레이 후 시작할 코드 작성
                        page = 1;
                        myData.clear();
                        getRoom(MSG.getTargetID());
                    }
                }, 600);// 0.6초 정도 딜레이를 준 후 시작
            }

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        page =1;
        myData.clear();
        getRoom(userId);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chating);
        Log.e("chating", "oncreate");
        appcon = getApplicationContext();
        setToolbar();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,new IntentFilter("Newmsg"));
        userId = AutoLogin.getUserId(this);
        rv_RoomList = findViewById(R.id.rv_RoomList);
        tv_noroom = findViewById(R.id.tv_noroom);
        nestedScrollView = findViewById(R.id.scroll_view);

        layoutManager = new LinearLayoutManager(getApplicationContext());
        rv_RoomList.setLayoutManager(layoutManager);
        Intent mmIntent = getIntent();
        if(mmIntent.getBooleanExtra("restartService",false)){ //다시시작된 서비스라면
            int RoomNum = mmIntent.getIntExtra("RoomNum",-1);
            String targetID = mmIntent.getStringExtra("targetID");
            Intent mIntent = new Intent(getApplicationContext(), Chat.class);
            mIntent.putExtra("RoomNum",RoomNum);
            mIntent.putExtra("targetID",targetID);
            startActivity(mIntent);
        }


        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력

                Intent mIntent = new Intent(getApplicationContext(), Chat.class);
                mIntent.putExtra("RoomNum",model.getRoomNum());
                mIntent.putExtra("targetID",model.getTargetID());



                startActivity(mIntent);
            }
        });


        rv_RoomList.setAdapter(myAdapter);
        rv_RoomList.setHasFixedSize(true);


        MySwipeHelper_a swipeHelper = new MySwipeHelper_a(getApplicationContext(),rv_RoomList,300) {
            @Override
            public void instantiatrMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper_a.MyButton> buffer) {
                MyData mdata = myAdapter.getItem(viewHolder.getAdapterPosition());
                buffer.add(new MySwipeHelper_a.MyButton(
                        getApplicationContext(),
                        "Delete",
                        30,
                        R.drawable.ic_delete,
                        Color.parseColor("#B13333"),
                        new MyButtonClickListener_a() {
                            @Override
                            public void onClick(int pos) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(Chating.this);
                                builder.setTitle("채팅방 나가기").setMessage("채팅방을 나가더라도 상대방의 채팅방은 삭제되지 않습니다.읽지않은 메세지는 읽음으로 표시됩니다.");
                                builder.setNegativeButton("네", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        int itemRoomNum = mdata.getRoomNum();
                                        String userId = AutoLogin.getUserId(getApplicationContext());
                                        if (requestQueue == null) {
                                            //RequestQueue 객체 생성하기

                                            requestQueue = Volley.newRequestQueue(getApplicationContext());
                                        }
                                        QuitRoom(itemRoomNum, userId, viewHolder.getAdapterPosition());
                                    }
                                });
                                builder.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });

                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();

                            }
                        }
                ));
            }
        };



        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
                    Log.e("page", String.valueOf(page));
                    getRoom(userId);
                }
            }
        });
        if (requestQueue == null) {
            //RequestQueue 객체 생성하기
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        myData = new ArrayList<>();
//        page ++;
//        getRoom(userId);

    }


    public void QuitRoom(int RoomNum,String myID,int position) {
        String url = "http://13.209.19.188/QuitRoom.php?myID="+myID+"&RoomNum="+RoomNum;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
//                        Log.e("QuitRoom", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean res = jsonObject.getBoolean("res");

                            if(res){
                                myAdapter.deleteItems(position);
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

    public void getRoom(String myID) {
        String url = "http://13.209.19.188/GetRoom.php?myID="+myID+"&page="+page+"&limit="+limit;
//        myData.clear();
        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String noroom = jsonObject.getString("noroom");

//                            Log.e("채팅방 응답", response);

                            if(noroom.equals("yes")){ //방이 있다면
                                JSONArray RoomNum = jsonObject.getJSONArray("RoomNum");
                                JSONArray Target_ID = jsonObject.getJSONArray("Target_ID");
                                JSONArray Target_profile = jsonObject.getJSONArray("Target_profile");
                                JSONArray latelymessage = jsonObject.getJSONArray("latelymessage");
                                JSONArray latelymessage_date = jsonObject.getJSONArray("latelymessage_date");
                                JSONArray latelymessage_num = jsonObject.getJSONArray("latelymessage_num");
                                JSONArray latelymessage_checkimg = jsonObject.getJSONArray("latelymessage_checkimg");
                                Log.i("페이징 확인 ", "페이지 : "+page +"\n채팅방 키 "+RoomNum);
                                for (int i = 0; i < RoomNum.length(); i++){
                                    if(latelymessage_checkimg.getInt(i) == 0) {
                                        myData.add(new MyData(RoomNum.getInt(i),
                                                Target_ID.getString(i),
                                                Target_profile.getString(i),
                                                latelymessage.getString(i),
                                                makeDate.formatTimeString(latelymessage_date.getString(i)),
                                                latelymessage_num.getInt(i)));
                                    }else{
                                        myData.add(new MyData(RoomNum.getInt(i),
                                                Target_ID.getString(i),
                                                Target_profile.getString(i),
                                                "사진을 보냈습니다.",
                                                makeDate.formatTimeString(latelymessage_date.getString(i)),
                                                latelymessage_num.getInt(i)));
                                    }
                                }
                                myAdapter.setItems(myData);

                            }else{
                                page--;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


//                        Log.e("TAG", "onResponse: 응답 : " + response);

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


    public String getnow(){
        SimpleDateFormat format1 = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        Date time = new Date();
        String time1 = format1.format(time);
        return time1;
    }



    // 리사이클러뷰 아이템에 추가할 데이터 클래스
    public static class MyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트
        int RoomNum;
        String targetID;
        String profileImg;
        String latelymessage,latelymessage_date;
        int nonreadnum;

        public int getRoomNum() {
            return RoomNum;
        }

        public void setRoomNum(int roomNum) {
            RoomNum = roomNum;
        }

        public String getTargetID() {
            return targetID;
        }

        public void setTargetID(String targetID) {
            this.targetID = targetID;
        }

        public String getProfileImg() {
            return profileImg;
        }

        public void setProfileImg(String profileImg) {
            this.profileImg = profileImg;
        }

        public String getLatelymessage() {
            return latelymessage;
        }

        public void setLatelymessage(String latelymessage) {
            this.latelymessage = latelymessage;
        }

        public String getLatelymessage_date() {
            return latelymessage_date;
        }

        public void setLatelymessage_date(String latelymessage_date) {
            this.latelymessage_date = latelymessage_date;
        }

        public int getNonreadnum() {
            return nonreadnum;
        }

        public void setNonreadnum(int nonreadnum) {
            this.nonreadnum = nonreadnum;
        }

        public MyData(int roomNum, String targetID, String profileImg, String latelymessage, String latelymessage_date, int nonreadnum) {
            RoomNum = roomNum;
            this.targetID = targetID;
            this.profileImg = profileImg;
            this.latelymessage = latelymessage;
            this.latelymessage_date = latelymessage_date;
            this.nonreadnum = nonreadnum;
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
        }

        private OnMyClickListener mListener;

        private List<MyData> mItems = new ArrayList<>();

        public MyAdapter() {
        }

        public MyAdapter(OnMyClickListener listener) {
            mListener = listener;
        }

        public void setItems(List<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }


        public MyData getItem(int position){
            MyData rdata = mItems.get(position);
            return rdata;
        }
        public void deleteItems(int position){
            Log.e("dkdlxpatkrwp", "");
            mItems.remove(position);
            notifyItemRemoved(position);
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_room, parent, false);
            final MyViewHolder viewHolder = new MyViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        final MyData item = mItems.get(viewHolder.getAdapterPosition());
                        item.setNonreadnum(0);
                        notifyDataSetChanged();
                        mListener.onMyClicked(item);
                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MyData item = mItems.get(position);
            // TODO : 데이터를 뷰홀더에 표시하시오
            holder.tv_room_id.setText(item.getTargetID());
            holder.tv_room_contents.setText(item.getLatelymessage());
            holder.tv_room_date.setText(item.getLatelymessage_date());
            if(item.getNonreadnum() > 0){
                holder.latelymessagenum.setVisibility(View.VISIBLE);
                holder.latelymessagenum.setText(String.valueOf(item.getNonreadnum()));
            }else{
                holder.latelymessagenum.setVisibility(View.GONE);
            }

            if(item.getProfileImg().equals("Nothing")) {
                Glide.with(holder.itemView)
                        .load(R.drawable.myprofile)
                        .placeholder(R.drawable.dataloading)
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .into(holder.iv_room_profileimg);
            }else {
                Glide.with(holder.itemView)
                        .load("http://13.209.19.188/"+item.getProfileImg())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .into(holder.iv_room_profileimg);
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            CircleImageView iv_room_profileimg;

            TextView tv_room_id, tv_room_contents, tv_room_date,latelymessagenum;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : 뷰홀더 코드를 입력하여 주세요
                iv_room_profileimg = itemView.findViewById(R.id.iv_room_profileimg);
                tv_room_id = itemView.findViewById(R.id.tv_room_id);
                tv_room_contents = itemView.findViewById(R.id.tv_room_contents);
                tv_room_date = itemView.findViewById(R.id.tv_room_date);
                latelymessagenum = itemView.findViewById(R.id.latelymessagenum);
            }
        }
    }


    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
            case R.id.toolbar_menu_searchid:{
                //검색창 눌렀을때
                Intent mIntent = new Intent(getApplicationContext(), Chating_searchid.class);
                startActivity(mIntent);
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //액션바 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chating_toolbar_menu, menu) ;

        return true;

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