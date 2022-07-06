package com.example.drinkernote;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;


import androidx.annotation.NonNull;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LikeList extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;
    MyAdapter myAdapter;
    LinearLayoutManager layoutManager;
    RecyclerView rv_LikeList;
    TextView tv_nolike;
    int NoteKey;
    NestedScrollView nestedScrollView;
    int page = 0, limit = 10;
    String from;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    ArrayList<MyData> myData = new ArrayList<>();

    @Override
    protected void onStart() {
        if(page==0) {
            page++;
            if (requestQueue == null) {
                //RequestQueue 객체 생성하기
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            getLikeList(NoteKey, from, page, limit);
        }else{
            myAdapter.setItems(myData);
        }

        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_list);

        setToolbar();


        Intent mIntent = getIntent();
        NoteKey = mIntent.getIntExtra("NoteKey",0);
        from = mIntent.getStringExtra("from");

        rv_LikeList = findViewById(R.id.rv_LikeList);
        tv_nolike = findViewById(R.id.tv_nolike);
        nestedScrollView = findViewById(R.id.scroll_view);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        rv_LikeList.setLayoutManager(layoutManager);

        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
                String id = model.getId();
                Intent mIntent = new Intent(getApplicationContext(), Profile.class);
                mIntent.putExtra("Maker",id);
                startActivity(mIntent);
            }

            @Override
            public void onBtnClicked(String who, String follow, String whattodo) {
                if(whattodo.equals("unfollow")) {
                    unFollow(who, follow);
                }
                else{
                    follow(who,follow);
                }
            }
        },getApplicationContext());

        rv_LikeList.setAdapter(myAdapter);
        rv_LikeList.setHasFixedSize(true);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
                    getLikeList(NoteKey,from,page,limit);
                }
            }
        });

    }


    //액션바 아이템 클릭시
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void setToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다
        actionBar.setDisplayHomeAsUpEnabled(true); //뒤로가기
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

    public void follow(String me,String following) {
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

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

    public void getLikeList(int NoteKey,String from,int pagee,int limit) {
        String userId = AutoLogin.getUserId(this);
        String url = "http://13.209.19.188/getLikeList.php?NoteKey="+NoteKey+"&myID="+userId+"&from="+from+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
//                        Log.e("좋아요 리스트", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray LikeList_ID = jsonObject.getJSONArray("LikeList_ID");
                            if(LikeList_ID.getString(0).equals("Nothing")){
                                page--;
                            }else {
                                JSONArray LikeList_profile = jsonObject.getJSONArray("LikeList_profile");
                                JSONArray follower_check = jsonObject.getJSONArray("follower_check");
                                if (LikeList_ID.length() <= 0) {
                                    rv_LikeList.setVisibility(View.GONE);
                                    tv_nolike.setVisibility(View.VISIBLE);

                                } else {
                                    rv_LikeList.setVisibility(View.VISIBLE);
                                    tv_nolike.setVisibility(View.GONE);
                                    Log.i("페이징 확인 ", "페이지 : "+pagee +"\n아이디 리스트"+LikeList_ID);
                                    for (int i = 0; i < LikeList_ID.length(); i++) {
                                        myData.add(new MyData(LikeList_ID.getString(i),
                                                LikeList_profile.getString(i),
                                                follower_check.getBoolean(i)));
                                    }
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
        String id;
        String img;
        boolean check;

        public MyData(String id, String img, boolean check) {
            this.id = id;
            this.img = img;
            this.check = check;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public boolean isCheck() {
            return check;
        }

        public void setCheck(boolean check) {
            this.check = check;
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
            void onBtnClicked(String me,String following,String whattodo);
        }

        private OnMyClickListener mListener;
        private Context mContext;

        private List<MyData> mItems = new ArrayList<>();

        public MyAdapter() {}

        public MyAdapter(OnMyClickListener listener,Context mContext) {
            mListener = listener;
            this.mContext = mContext;
        }

        public void setItems(List<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.follow_item, parent, false);
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
            viewHolder.btn_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Toast.makeText(view.getContext(), "팔로우가 취소되었습니다.", Toast.LENGTH_SHORT).show();
                    final MyData item = mItems.get(viewHolder.getAdapterPosition());

                    viewHolder.btn_follow.setVisibility(View.GONE);
                    viewHolder.btn_follow_c.setVisibility(View.VISIBLE);
                    String itemID = viewHolder.tv_follow_id.getText().toString();
                    if(mListener != null){
                        String userId = AutoLogin.getUserId(mContext);
                        mListener.onBtnClicked(userId,itemID,"unfollow");
                    }
                }
            });

            //팔로잉 버튼 눌림 - 팔로잉버튼이 눌리면 팔로우버튼으로 바뀌고 디비 추가해야함
            viewHolder.btn_follow_c.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String itemID = viewHolder.tv_follow_id.getText().toString();
                    Toast.makeText(view.getContext(), itemID+"님을 팔로우 합니다.", Toast.LENGTH_SHORT).show();
                    viewHolder.btn_follow_c.setVisibility(View.GONE);
                    viewHolder.btn_follow.setVisibility(View.VISIBLE);

                    if(mListener != null){
                        String userId = AutoLogin.getUserId(mContext);
                        mListener.onBtnClicked(userId,itemID,"follow");
                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MyData item = mItems.get(position);
            // TODO : 데이터를 뷰홀더에 표시하시오
            holder.tv_follow_id.setText(item.getId());

            //프로필 이미지 표시
            if(item.getImg().equals("Nothing")) {
                Glide.with(holder.itemView)
                        .load(R.drawable.myprofile)
                        .placeholder(R.drawable.dataloading)
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .into(holder.iv_follow_profileimg);
            }else {
                Glide.with(holder.itemView)
                        .load("http://13.209.19.188/"+item.getImg())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .into(holder.iv_follow_profileimg);
            }

            // 버튼 맞팔 표시
            String userId = AutoLogin.getUserId(mContext);
            if(userId.equals(item.getId())){ //아이디가 같으면 표시안함
                holder.btn_follow_c.setVisibility(View.GONE);
                holder.btn_follow.setVisibility(View.GONE);
            }
            else if(item.isCheck()==true){
                holder.btn_follow_c.setVisibility(View.GONE);
                holder.btn_follow.setVisibility(View.VISIBLE);
            }else{
                holder.btn_follow_c.setVisibility(View.VISIBLE);
                holder.btn_follow.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            ImageView iv_follow_profileimg;
            TextView tv_follow_id;
            Button btn_follow,btn_follow_c;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : 뷰홀더 코드를 입력하여 주세요
                iv_follow_profileimg = itemView.findViewById(R.id.iv_coment_profileimg);
                tv_follow_id = itemView.findViewById(R.id.tv_follow_id);
                btn_follow = itemView.findViewById(R.id.btn_follow);
                btn_follow_c = itemView.findViewById(R.id.btn_follow_c);

            }
        }
    }
}