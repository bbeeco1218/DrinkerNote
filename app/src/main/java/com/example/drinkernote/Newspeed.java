package com.example.drinkernote;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Newspeed extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;
    MyAdapter myAdapter;
    LinearLayoutManager layoutManager;
    RecyclerView rv_NewsList;
    NestedScrollView nestedScrollView;
    String myID;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    ArrayList<MyData> myData = new ArrayList<>();

        int page = 0;
        int limit = 10;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            myData.clear();
            CheckRequestQueue();
            GetNewsList(myID);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newspeed);
        setToolbar();
        myID = AutoLogin.getUserId(this);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1); // cancel(알림 특정 id)

        rv_NewsList = findViewById(R.id.rv_NewsList);
        nestedScrollView = findViewById(R.id.scroll_view);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        rv_NewsList.setLayoutManager(layoutManager);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,new IntentFilter("updatenews"));

        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
                int itemtype = model.getNews().getType();
                int newspeed_key = model.getNews_key();
                model.setNews_read(0);
                myAdapter.updateitem();
                CheckRequestQueue();
                updateNewsread(newspeed_key);

                if(itemtype == 0){ //댓글뉴스일때
                    Intent mIntent = new Intent(getApplicationContext(), Coment_And_Note.class);
                    Newspeed_item news = model.getNews();
                    mIntent.putExtra("NoteKey",news.getNoteKey());
                    mIntent.putExtra("NoteMaker",news.getToID());
                    mIntent.putExtra("focusComentID",news.getComentKey());
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(mIntent);

                }else if(itemtype == 1){ //좋아요 뉴스일때
                    Intent mIntent = new Intent(getApplicationContext(), NoteInfo.class);
                    mIntent.putExtra("NoteKey",model.getNews().getNoteKey());
                    startActivity(mIntent);
                }else if(itemtype == 2){ //팔로우 뉴스일때
                    Intent mIntent = new Intent(getApplicationContext(), Follow.class);
                    mIntent.putExtra("From","Follower");
                    mIntent.putExtra("Who",myID);
                    startActivity(mIntent);
                }else{ //대댓글 뉴스일때
                    Intent mIntent = new Intent(getApplicationContext(), Coment_And_Note.class);
                    Newspeed_item news = model.getNews();
                    mIntent.putExtra("NoteKey",news.getNoteKey());
                    mIntent.putExtra("NoteMaker",news.getToID());
                    mIntent.putExtra("focusComentID",news.getComentKey());
                    mIntent.putExtra("focusReplyID",news.getReplyKey());
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(mIntent);
                }
            }

            @Override
            public void onFollowclicked(String who, String follow, String whattodo) {
                CheckRequestQueue();
                if(whattodo.equals("unfollow")) {
                    unFollow(who, follow);
                }
                else{
                    Newspeed_item follownews = new Newspeed_item(2,follow,who,follow);
                    Gson gson = new Gson();
                    String jsonMSG = gson.toJson(follownews);
                    follow(who,follow,jsonMSG);
                    sendNewsToChatServer(follownews);
                }
            }
        });


        rv_NewsList.setAdapter(myAdapter);
        rv_NewsList.setHasFixedSize(true);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
                    GetNewsList(myID);
                }
            }
        });

        CheckRequestQueue();
        page ++;
        GetNewsList(myID);

    }




    void CheckRequestQueue(){
        if (requestQueue == null) {
            //RequestQueue 객체 생성하기
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    void sendNewsToChatServer(Newspeed_item newsitem){
        Gson gson = new Gson();
        String jsonMSG = gson.toJson(newsitem);
        Intent mIntent = new Intent("Sendmsg");
        mIntent.putExtra("news",jsonMSG);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
    }

    public void updateNewsread(int newskey) {
        String url = "http://13.209.19.188/updateNewsread.php?newskey="+newskey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("updatenewsread", "onResponse: 응답 : " + response);

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

    public void GetNewsList(String myID) {
        String url = "http://13.209.19.188/GetNewsList.php?myID="+myID+"&page="+page+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("getnewslist!", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int NewsListnum = jsonObject.getInt("NewsListnum");
                            if(NewsListnum > 0){
                                JSONArray NewsOBJ = jsonObject.getJSONArray("NewsOBJ");
                                JSONArray News_date = jsonObject.getJSONArray("News_date");
                                JSONArray profileImgList = jsonObject.getJSONArray("profileImgList");
                                JSONArray News_read = jsonObject.getJSONArray("News_read");
                                JSONArray Newspeed_key = jsonObject.getJSONArray("Newpeed_key");
                                JSONArray isfollownews = jsonObject.getJSONArray("isfollownews");
                                JSONArray Img_or_followcheck = jsonObject.getJSONArray("Img_or_followcheck");
                                Log.i("페이징 확인 ", "페이지 : "+page +"\n뉴스 키 "+Newspeed_key);
                                for (int i = 0; i < NewsListnum; i++){
                                    String Newsjson = NewsOBJ.getString(i);
                                    String Newsdate = News_date.getString(i);
                                    String porfileIMG = profileImgList.getString(i);
                                    int Newsread = News_read.getInt(i);
                                    int Newspeedkey = Newspeed_key.getInt(i);
                                    Gson gson = new Gson();
                                    Newspeed_item News = gson.fromJson(Newsjson, Newspeed_item.class);
                                    NewsTypeString NewsString = new NewsTypeString(News);
                                    if(isfollownews.getBoolean(i)){ //팔로우뉴스라면
                                        myData.add(new MyData(porfileIMG,NewsString.getnotiString(),makeDate.formatTimeString(Newsdate),News,Newsread,Newspeedkey,News.getType(),Img_or_followcheck.getBoolean(i)));
                                    }else{

                                        myData.add(new MyData(porfileIMG,NewsString.getnotiString(),makeDate.formatTimeString(Newsdate),News,Newsread,Newspeedkey,News.getType(),Img_or_followcheck.getString(i)));
                                    }

                                }
                                myAdapter.setItems(myData);
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


    // 리사이클러뷰 아이템에 추가할 데이터 클래스
    public static class MyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트
        String profileIMG;
        String contents;
        String date;
        Newspeed_item News;
        int News_read;
        int News_key;
        int type;
        String Noteimg;
        Boolean followcheck;

        public String getNoteimg() {
            return Noteimg;
        }

        public void setNoteimg(String noteimg) {
            Noteimg = noteimg;
        }

        public Boolean getFollowcheck() {
            return followcheck;
        }

        public void setFollowcheck(Boolean followcheck) {
            this.followcheck = followcheck;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getNews_key() {
            return News_key;
        }

        public void setNews_key(int news_key) {
            News_key = news_key;
        }

        public String getProfileIMG() {
            return profileIMG;
        }

        public Newspeed_item getNews() {
            return News;
        }

        public void setNews(Newspeed_item news) {
            News = news;
        }

        public void setProfileIMG(String profileIMG) {
            this.profileIMG = profileIMG;
        }

        public String getContents() {
            return contents;
        }

        public void setContents(String contents) {
            this.contents = contents;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }



        public int getNews_read() {
            return News_read;
        }

        public void setNews_read(int news_read) {
            News_read = news_read;
        }

        public MyData(String profileIMG, String contents, String date, Newspeed_item news, int news_read, int news_key, int type, String noteimg) {
            this.profileIMG = profileIMG;
            this.contents = contents;
            this.date = date;
            News = news;
            News_read = news_read;
            News_key = news_key;
            this.type = type;
            Noteimg = noteimg;
        }

        public MyData(String profileIMG, String contents, String date, Newspeed_item news, int news_read, int news_key, int type, Boolean followcheck) {
            this.profileIMG = profileIMG;
            this.contents = contents;
            this.date = date;
            News = news;
            News_read = news_read;
            News_key = news_key;
            this.type = type;
            this.followcheck = followcheck;
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
            void onFollowclicked(String me,String following,String whattodo);

        }

        private OnMyClickListener mListener;

        private List<MyData> mItems = new ArrayList<>();

        public MyAdapter() {}

        public MyAdapter(OnMyClickListener listener) {
            mListener = listener;
        }

        public void setItems(List<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }
        public void updateitem() {

            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_newslist, parent, false);
            final MyViewHolder viewHolder = new MyViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        final MyData item = mItems.get(viewHolder.getAdapterPosition());
                        mListener.onMyClicked(item);
                    }
                }
            });

            //팔로우 버튼 눌림 - 팔로우버튼이 팔로잉 버튼으로 바뀌고 디비 삭제해야함
            viewHolder.btn_follow_c.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(view.getContext(), "팔로우가 취소되었습니다.", Toast.LENGTH_SHORT).show();

                    viewHolder.btn_follow.setVisibility(View.VISIBLE);
                    viewHolder.btn_follow_c.setVisibility(View.GONE);
                    mItems.get(viewHolder.getAdapterPosition()).setFollowcheck(false);

                    String itemID = mItems.get(viewHolder.getAdapterPosition()).getNews().getFollowerID();


                    if(mListener != null){
                        String userId = AutoLogin.getUserId(view.getContext());
//                    Log.e("팔로우버튼 ", userId+"가 " + itemID+"를 언팔로우");
//                    Log.e("버튼값", String.valueOf(mItems.get(viewHolder.getAdapterPosition()).getFollowcheck()));
                        mListener.onFollowclicked(userId,itemID,"unfollow");
                    }
                }
            });

            //팔로잉 버튼 눌림 - 팔로잉버튼이 눌리면 팔로우버튼으로 바뀌고 디비 추가해야함
            viewHolder.btn_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String itemID = mItems.get(viewHolder.getAdapterPosition()).getNews().getFollowerID();
                    Toast.makeText(view.getContext(), itemID+"님을 팔로우 합니다.", Toast.LENGTH_SHORT).show();
                    viewHolder.btn_follow_c.setVisibility(View.VISIBLE);
                    viewHolder.btn_follow.setVisibility(View.GONE);
                    mItems.get(viewHolder.getAdapterPosition()).setFollowcheck(true);
//
                    if(mListener != null){
                        String userId = AutoLogin.getUserId(view.getContext());
                        mListener.onFollowclicked(userId,itemID,"follow");
                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MyData item = mItems.get(position);

            // TODO : 데이터를 뷰홀더에 표시하시오
            holder.tv_news_contents.setText(item.getContents());
            holder.tv_news_date.setText(item.getDate());
            if(item.getProfileIMG().equals("Nothing")) {
                Glide.with(holder.itemView)
                        .load(R.drawable.myprofile)
                        .placeholder(R.drawable.dataloading)
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .into(holder.iv_news_profileimg);
            }else {
                Glide.with(holder.itemView)
                        .load("http://13.209.19.188/"+item.getProfileIMG())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .into(holder.iv_news_profileimg);
            }
            if(item.getNews_read() == 0){
                holder.iv_red_dot.setVisibility(View.INVISIBLE);
            }else{
                holder.iv_red_dot.setVisibility(View.VISIBLE);
            }

            if(item.getType() == 2){ //팔로우뉴스라면
                holder.iv_newsimg.setVisibility(View.GONE);
                if(item.getFollowcheck()){ //내가 팔로우중이라면
                    holder.btn_follow.setVisibility(View.GONE);
                    holder.btn_follow_c.setVisibility(View.VISIBLE);
                }else{
                    holder.btn_follow_c.setVisibility(View.GONE);
                    holder.btn_follow.setVisibility(View.VISIBLE);
                }


            }else{
                holder.btn_follow.setVisibility(View.GONE);
                holder.btn_follow_c.setVisibility(View.GONE);
                holder.iv_newsimg.setVisibility(View.VISIBLE);

                Glide.with(holder.itemView)
                        .load("http://13.209.19.188/"+item.getNoteimg())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .into(holder.iv_newsimg);
            }

        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            TextView tv_news_contents;
            TextView tv_news_date;
            CircleImageView iv_news_profileimg;
            ImageView iv_red_dot,iv_newsimg;
            Button btn_follow,btn_follow_c;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : 뷰홀더 코드를 입력하여 주세요
                tv_news_contents = itemView.findViewById(R.id.tv_news_contents);
                tv_news_date = itemView.findViewById(R.id.tv_news_date);
                iv_news_profileimg = itemView.findViewById(R.id.iv_news_profileimg);
                iv_red_dot = itemView.findViewById(R.id.iv_red_dot);
                iv_newsimg = itemView.findViewById(R.id.iv_newsimg);
                btn_follow = itemView.findViewById(R.id.btn_follow);
                btn_follow_c = itemView.findViewById(R.id.btn_follow_c);
            }
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