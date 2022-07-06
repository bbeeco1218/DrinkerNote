package com.example.drinkernote;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class frag_following extends Fragment {
    private View view;
    private String TAG = "frag 팔로잉";
    String whoID;
    RecyclerView rv_following;
    LinearLayoutManager layoutManager;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    MyAdapter myAdapter;
    NestedScrollView nestedScrollView;
    ProgressBar progressBar;
    ArrayList<MyData> myData = new ArrayList<MyData>();
    int page = 0, limit = 10;
    public frag_following(String whoID) {
        this.whoID = whoID;
    }


    @Override
    public void onStart() {
        super.onStart();
        page = 0;
        myData.clear();
        if(page == 0) {
            page++;
            if (requestQueue == null) {
                //RequestQueue 객체 생성하기
                requestQueue = Volley.newRequestQueue(getContext());
            }

            getFollowList(whoID,page,limit);
        }else{
            myAdapter.setItems(myData);
            progressBar.setVisibility(View.GONE);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_following, container, false);
        rv_following = view.findViewById(R.id.rv_Following);
        layoutManager = new LinearLayoutManager(getContext());
        rv_following.setLayoutManager(layoutManager);
        nestedScrollView = view.findViewById(R.id.scroll_view);
        progressBar = view.findViewById(R.id.progress_bar);

        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
                String id = model.getId();
                Intent mIntent = new Intent(getContext(), Profile.class);
                mIntent.putExtra("Maker",id);
                startActivity(mIntent);
            }
        }, new MyAdapter.BtnClick() {
            @Override
            public void onBtnClicked(String who,String follow,String whattodo) {
                // TODO : 팔로우 팔로잉 버튼클릭시
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
        },whoID,getContext());

        rv_following.setAdapter(myAdapter);
        rv_following.setHasFixedSize(true);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    progressBar.setVisibility(View.VISIBLE);
                    page++;
//                    Log.e("페이지", String.valueOf(page));
                    getFollowList(whoID, page, limit);
                }
            }
        });



        return view;
    }


    void sendNewsToChatServer(Newspeed_item newsitem){
        Gson gson = new Gson();
        String jsonMSG = gson.toJson(newsitem);
        Intent mIntent = new Intent("Sendmsg");
        mIntent.putExtra("news",jsonMSG);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(mIntent);
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
        }
        interface BtnClick{
            void onBtnClicked(String me,String following,String whattodo);
        }


        private MyAdapter.OnMyClickListener mListener;
        private MyAdapter.BtnClick mBtnclick;

        private List<MyData> mItems = new ArrayList<>();
        private String whoID;
        private Context mContext;


        public MyAdapter(MyAdapter.OnMyClickListener listener, MyAdapter.BtnClick btnclick, String whoID_,Context mContext) {
            mListener = listener;
            mBtnclick = btnclick;
            whoID = whoID_;
            this.mContext = mContext;
        }

        public void setItems(List<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.e("팔로우", "온 크리에이트 뷰홀더");
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.follow_item, parent, false);
            final MyAdapter.MyViewHolder viewHolder = new MyAdapter.MyViewHolder(view);
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
                    if(mBtnclick != null){
                        String userId = AutoLogin.getUserId(mContext);
                        mBtnclick.onBtnClicked(userId,itemID,"unfollow");
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

                    if(mBtnclick != null){
                        String userId = AutoLogin.getUserId(mContext);
                        mBtnclick.onBtnClicked(userId,itemID,"follow");
                    }
                }
            });

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
            Log.e("팔로우", "온 바인드 뷰홀더");
            MyData item = mItems.get(position);

            // TODO : 데이터를 뷰홀더에 표시하시오
            //아이디 표시
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
            if(userId.equals(item.getId())){
                holder.btn_follow_c.setVisibility(View.GONE);
                holder.btn_follow.setVisibility(View.GONE);
            }
            else if(item.getCheck().equals("true")){
                holder.btn_follow_c.setVisibility(View.GONE);
                holder.btn_follow.setVisibility(View.VISIBLE);
            }else{
                holder.btn_follow_c.setVisibility(View.VISIBLE);
                holder.btn_follow.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
//            Log.e("팔로우", "겟아이템카운트"+mItems.size());
            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            CircleImageView iv_follow_profileimg;
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

    public static class MyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트
        String id;
        String img;
        String check;


        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getCheck() {
            return check;
        }

        public void setCheck(String check) {
            this.check = check;
        }

        public MyData(String id, String img, String check) {
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
    }


    public void getFollowList(String id,int pagee,int limit) {
        String userId = AutoLogin.getUserId(getContext());
        String url = "http://13.209.19.188/GetFollowerList.php?ID="+id+"&myID="+userId+"&from=following"+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray JS_follower_IDlist = jsonObject.getJSONArray("following_IDlist"); //팔로워 리스트
                            if(JS_follower_IDlist.getString(0).equals("Nothing")) {
                                progressBar.setVisibility(View.GONE);
                                page --;
                            }else {


                                JSONArray JS_follower_profileImg = jsonObject.getJSONArray("following_profileImg"); //팔로워 프로필이미지
                                JSONArray JS_follower_check = jsonObject.getJSONArray("following_check"); //팔로워 맞팔 여부
                                Log.i("페이징 확인 ", "페이지 : "+pagee +"\n아이디 리스트"+JS_follower_IDlist);
                                for (int i = 0; i < JS_follower_IDlist.length(); i++) {
                                    myData.add(new MyData(JS_follower_IDlist.getString(i),
                                            JS_follower_profileImg.getString(i),
                                            JS_follower_check.getString(i)
                                    ));

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

}
