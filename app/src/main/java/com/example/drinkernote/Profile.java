package com.example.drinkernote;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    RecyclerView RV_myprofile_grid;
    GridLayoutManager layoutManager;
    CircleImageView iv_profileimg;
    TextView tv_NoteNum,tv_Follower,tv_Following,tv_noPost,textView21,textView23;
    MyAdapter myAdapter;
    String targetID;
    Button btn_profile_following,btn_profile_follower,btn_profile_sendmsg;
    Toolbar toolbar;
    ActionBar actionBar;
    TextView tv_tittle;
    int page=0,limit=12;
    NestedScrollView nestedScrollView;
    ArrayList<MyData> mData = new ArrayList<>();

    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.

    @Override
    protected void onStart() {
        Log.e("프로필", "온스타트");
        if (requestQueue == null) {
            //RequestQueue 객체 생성하기
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        GetMyProfile(targetID, page, limit);
        String userId = AutoLogin.getUserId(getApplicationContext());
        tv_tittle.setText(targetID);
        if(userId.equals(targetID)){
            btn_profile_following.setVisibility(View.GONE);
            btn_profile_follower.setVisibility(View.GONE);
            btn_profile_sendmsg.setVisibility(View.GONE);
        }else{
            checkFollow(userId,targetID);
        }

        if(page == 0) {
            page++;
            if (requestQueue == null) {
                //RequestQueue 객체 생성하기
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }

            GetMyNote(targetID, page, limit);

        }else{
            myAdapter.setItems(mData);
        }

        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        RV_myprofile_grid = findViewById(R.id.RV_myprofile_grid);
        layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        RV_myprofile_grid.setLayoutManager(layoutManager);

        iv_profileimg = findViewById(R.id.iv_feed_profileimg);
        tv_NoteNum = findViewById(R.id.tv_NoteNum);
        tv_Follower = findViewById(R.id.tv_Follower);
        tv_Following = findViewById(R.id.tv_Following);
        tv_noPost = findViewById(R.id.tv_noPost);
        textView21 = findViewById(R.id.textView21);
        textView23 = findViewById(R.id.textView23);
        btn_profile_following = findViewById(R.id.btn_profile_following);
        btn_profile_follower = findViewById(R.id.btn_profile_follower);
        tv_tittle = findViewById(R.id.tv_tittle);
        nestedScrollView = findViewById(R.id.scroll_view);
        btn_profile_sendmsg = findViewById(R.id.btn_profile_sendmsg);

        setToolbar();

        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData mItem) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
                Intent mIntent = new Intent(getApplicationContext(), NoteInfo.class);
                mIntent.putExtra("NoteKey",mItem.getNotekey());
                startActivity(mIntent);
            }
        });
        RV_myprofile_grid.setAdapter(myAdapter);
        Intent mIntent = getIntent();
        targetID = mIntent.getStringExtra("Maker");

        btn_profile_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "팔로우가 취소되었습니다.", Toast.LENGTH_SHORT).show();
                String userId = AutoLogin.getUserId(getApplicationContext());
                unFollow(userId,targetID);
                btn_profile_following.setVisibility(View.GONE);
                btn_profile_follower.setVisibility(View.VISIBLE);
            }
        });


        btn_profile_follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), targetID+" 님을 팔로우 합니다.", Toast.LENGTH_SHORT).show();
                String userId = AutoLogin.getUserId(getApplicationContext());
                Newspeed_item follownews = new Newspeed_item(2,targetID,userId,targetID);
                Gson gson = new Gson();
                String jsonMSG = gson.toJson(follownews);
                follow(userId,targetID,jsonMSG);
                sendNewsToChatServer(follownews);
                btn_profile_following.setVisibility(View.VISIBLE);
                btn_profile_follower.setVisibility(View.GONE);
            }
        });

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
                    GetMyNote(targetID,page,limit);
                }
            }
        });


        btn_profile_sendmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                String userId = AutoLogin.getUserId(getApplicationContext());
                CheckRoom(userId,targetID);
            }
        });

    }





    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }

            default:
                break;
        }
        return false;
    }

    void sendNewsToChatServer(Newspeed_item newsitem){
        Gson gson = new Gson();
        String jsonMSG = gson.toJson(newsitem);
        Intent mIntent = new Intent("Sendmsg");
        mIntent.putExtra("news",jsonMSG);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
    }


    void setToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다
        actionBar.setDisplayHomeAsUpEnabled(true); //뒤로가기
    }

    //프로필 이미지, 노트갯수 , 팔로워, 팔로잉 을 셋팅하는메서드
    public void setprofile(String Profile_img,int Note_num,int Follower,int Following){
        //프로필 이미지, 게시글 갯수, 팔로워, 팔로잉 을 셋팅한다
        if(Profile_img.equals("Nothing")){
            Glide.with(iv_profileimg)
                    .load(R.drawable.myprofile)
                    .centerCrop() //가운데를 기준으로 크기맞추기
                    .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                    .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                    .into(iv_profileimg);
        }else {
            Glide.with(iv_profileimg)
                    .load("http://13.209.19.188/" + Profile_img)
                    .centerCrop() //가운데를 기준으로 크기맞추기
                    .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                    .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                    .into(iv_profileimg);
        }
        tv_NoteNum.setText(String.valueOf(Note_num));
        tv_Follower.setText(String.valueOf(Follower));
        tv_Following.setText(String.valueOf(Following));
    }

    //작성한 노트가없을때 리사이클러뷰를 없애고 작성된 노트가없다는 텍스트뷰를 띄우는 메서드
    public void setNoPost(){
        RV_myprofile_grid.setVisibility(View.GONE);
        tv_noPost.setVisibility(View.VISIBLE);
    }

    //작성한 노트가 있을때 노트없다는텍스트뷰를 없애고 리사이클러뷰를 띄우는 메서드
    public void setYesPost(){
        RV_myprofile_grid.setVisibility(View.VISIBLE);
        tv_noPost.setVisibility(View.GONE);
    }
    public void checkFollow(String myID, String targetID) {
        String url = "http://13.209.19.188/CheckFollow.php?myID="+myID+"&targetID="+targetID+"&page="+page+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("프로필 체크팔로우", "onResponse: 응답 : " + response);
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");

                            if(res.equals("true")) {
                                //팔로우중이라는 뜻
                                btn_profile_following.setVisibility(View.VISIBLE);
                                btn_profile_follower.setVisibility(View.GONE);
                            }else{
                                btn_profile_following.setVisibility(View.GONE);
                                btn_profile_follower.setVisibility(View.VISIBLE);
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
    public void unFollow(String me,String following) {
        String url = "http://13.209.19.188/unFollow.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);

                    }
                },
                new Response.ErrorListener() {
                    @Override //에러시 처리할 내용
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "에러-> " + error.getMessage());
                    }
                });
        request.addStringParam("me", me); //POST파라미터 넣기
        request.addStringParam("following", following); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }
    public void follow(String me,String following,String newsjson) {
        String url = "http://13.209.19.188/Follow.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);

                    }
                },
                new Response.ErrorListener() {
                    @Override //에러시 처리할 내용
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "에러-> " + error.getMessage());
                    }
                });
        request.addStringParam("me", me); //POST파라미터 넣기
        request.addStringParam("following", following); //POST파라미터 넣기
        request.addStringParam("NewsOBJ", newsjson); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }
    public void GetMyProfile(String myID,int pagee, int limit) {
        String url = "http://13.209.19.188/SelectMyProfile.php?myID="+myID+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            String Profile_img = jsonObject.getString("Profile_img"); //프로필이미지
                            int Follower = jsonObject.getInt("follower"); //팔로워 갯수
                            int Following = jsonObject.getInt("following"); //팔로잉 갯수
                            int Note_num = jsonObject.getInt("Note_num"); //노트 갯수


                            setprofile(Profile_img, Note_num, Follower, Following); //프로필 셋팅

                            //팔로워버튼 클릭리스너
                            View.OnClickListener follower = new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent mIntent = new Intent(getApplicationContext(), Follow.class);
                                    mIntent.putExtra("Who",targetID);
                                    mIntent.putExtra("From","Follower");
                                    startActivity(mIntent);
                                }
                            };
                            tv_Follower.setOnClickListener(follower);
                            textView21.setOnClickListener(follower);

                            View.OnClickListener following = new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent mIntent = new Intent(getApplicationContext(), Follow.class);
                                    mIntent.putExtra("Who",targetID);
                                    mIntent.putExtra("From","Following");
                                    startActivity(mIntent);
                                }
                            };
                            tv_Following.setOnClickListener(following);
                            textView23.setOnClickListener(following);

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
    public void GetMyNote(String myID,int pagee, int limit) {
        String url = "http://13.209.19.188/SelectMyProfile.php?myID="+myID+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {

                            JSONObject jsonObject = new JSONObject(response);



                            int Note_num = jsonObject.getInt("Note_num"); //노트 갯수



                            if (Note_num > 0) { //노트 갯수가 있다면
                                setYesPost();
                                JSONArray Note_keyList = jsonObject.getJSONArray("Note_keyList"); //노트 키 리스트
                                if(Note_keyList.getString(0).equals("Nothing")){
                                    page--;
                                }else {
                                    JSONObject Note_ImgList = jsonObject.getJSONObject("NoteImgList"); //노트 이미지 리스트
                                    for (int i = 0; i < Note_keyList.length(); i++) {
                                        mData.add(new MyData(Note_ImgList.getString(String.valueOf(Note_keyList.get(i))), Note_keyList.getInt(i)));
                                    }
                                }
                                myAdapter.setItems(mData); // 리사이클러뷰 어뎁터에 아이템을 추가한다.

                            }else{
                                setNoPost();
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
    public void CheckRoom(String myID,String targetID) {
        //내아이디와 채팅을 하려고하는 아이디를 넘겨줘서 두명이 속한 방이 있는지 체크한다.

        String url = "http://13.209.19.188/CheckRoom.php?myID="+myID+"&targetID="+targetID;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int RoomNum = jsonObject.getInt("response");
                            Intent messageIntent = new Intent(getApplicationContext(), Chat.class);
                            messageIntent.putExtra("RoomNum",RoomNum);
                            messageIntent.putExtra("targetID",targetID);
                            startActivity(messageIntent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.e("상대방과 같은 방에 속해있는지", "onResponse: 응답 : " + response);


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

    public static class MyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트
        String img;
        int Notekey;

        public MyData(String img, int notekey) {
            this.img = img;
            Notekey = notekey;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public int getNotekey() {
            return Notekey;
        }

        public void setNotekey(int notekey) {
            Notekey = notekey;
        }
    }
    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
        }

        private MyAdapter.OnMyClickListener mListener;

        private List<MyData> mItems = new ArrayList<>();

        public MyAdapter(MyAdapter.OnMyClickListener listener) {
            mListener = listener;
        }

        public void setItems(List<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.myprofile_grid_item, parent, false);
            final MyAdapter.MyViewHolder viewHolder = new MyAdapter.MyViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = viewHolder.getAdapterPosition();
//                        int NoteKey = mItems.get(position).getNotekey();
                        mListener.onMyClicked(mItems.get(position));

                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
            MyData item = mItems.get(position);
            if(item.getImg().equals("null")){
                Glide.with(holder.itemView)
                        .load(R.drawable.logo)
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.logo) //아무것도 없을때 그림표시
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .into(holder.iv_gridimg);
            }else {
                Glide.with(holder.itemView)
                        .load("http://13.209.19.188/"+item.getImg())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.logo) //아무것도 없을때 그림표시
                        .into(holder.iv_gridimg);
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            ImageView iv_gridimg;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : 뷰홀더 코드를 입력하여 주세요
                iv_gridimg = itemView.findViewById(R.id.iv_gridimg);
            }
        }
    }

}