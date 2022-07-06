package com.example.drinkernote;
import android.content.Context;
import android.content.Intent;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import static android.widget.LinearLayout.VERTICAL;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;


import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class frag_home extends Fragment {
    private View view;
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    NestedScrollView nestedScrollView;
    ProgressBar progressBar;
    String userID;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    TextView tv_NoPost;
    ArrayList<MyData> myData = new ArrayList<>();
    int page = 0, limit = 5;

    private String TAG = "홈 프래그";


    @Override
    public void onStart() {
        super.onStart();


        if(page == 0) {
            page++;
            if (requestQueue == null) {
                //RequestQueue 객체 생성하기
                requestQueue = Volley.newRequestQueue(getContext());
            }
            getFeed(userID, page, limit);
        }else{
            myAdapter.setItems(myData);
            progressBar.setVisibility(View.GONE);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_home, container, false);
        recyclerView = view.findViewById(R.id.rv_home);
        tv_NoPost = view.findViewById(R.id.tv_NoPost);
        nestedScrollView = view.findViewById(R.id.scroll_view);
        progressBar = view.findViewById(R.id.progress_bar);
        userID = AutoLogin.getUserId(getContext());


        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
                Intent mIntent = new Intent(getContext(), NoteInfo.class);
                mIntent.putExtra("NoteKey", model.getNoteKey());
                startActivity(mIntent);
            }

            @Override
            public void onProfileClicked(MyData model) {
                Intent mIntent = new Intent(getContext(), Profile.class);
                mIntent.putExtra("Maker", model.getMaker());
                startActivity(mIntent);
            }

            @Override
            public void likeClicked(MyData model, MyAdapter.MyViewHolder holder) {
                Log.e("aaa", "인터페이스 like 클릭" + model.getNoteKey());
                String userId = AutoLogin.getUserId(getContext());
                if(!userId.equals(model.getMaker())) {
                    Newspeed_item likenews = new Newspeed_item(1, model.getNoteKey(), userId, model.getMaker());
                    Gson gson = new Gson();
                    String jsonMSG = gson.toJson(likenews);
                    Like(model.getNoteKey(), userId, holder, model,model.getMaker(),jsonMSG );
                    sendNewsToChatServer(likenews);
                }else{
                    Like(model.getNoteKey(), userId, holder, model,model.getMaker(),"nonews" );
                }
            }

            @Override
            public void unlikeClicked(MyData model, MyAdapter.MyViewHolder holder) {
                String userId = AutoLogin.getUserId(getContext());
                unLike(model.getNoteKey(), userId, holder, model);
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(myAdapter);
        recyclerView.setHasFixedSize(true);




        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    progressBar.setVisibility(View.VISIBLE);
                    page++;
                    Log.e("페이지", String.valueOf(page));
                    getFeed(userID, page, limit);
                }
            }
        });
        // TODO : 리싸이클러뷰에 추가될 데이터

        return view;
    }
    public void setNoPost(){
        recyclerView.setVisibility(View.GONE);
        tv_NoPost.setVisibility(View.VISIBLE);
    }
    public void setYesPost(){
        recyclerView.setVisibility(View.VISIBLE);
        tv_NoPost.setVisibility(View.GONE);
    }

    public void getFeed(String myID,int pagee, int limit) {
        String url = "http://13.209.19.188/GetFeed.php?myID="+myID+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {

                            //응답받은데이터를 마이데이터에 넣는다
                            JSONObject jsonObject = new JSONObject(response);
                            String check = jsonObject.getString("nofollow");

                            if(check.equals("true")) {

                                setYesPost();


                                JSONArray Note_keyList = jsonObject.getJSONArray("Note_keyList");
                                if(Note_keyList.getInt(0) == 0){
                                    progressBar.setVisibility(View.GONE);
                                    page --;
                                }else {

                                    JSONArray Note_objList = jsonObject.getJSONArray("Note_objList");
                                    JSONArray Note_makerList = jsonObject.getJSONArray("Note_makerList");
                                    JSONArray Note_makerprofile_img = jsonObject.getJSONArray("Note_makerprofile_img");
                                    JSONArray Note_ImgList = jsonObject.getJSONArray("Note_ImgList");
                                    JSONArray Note_Date = jsonObject.getJSONArray("Note_Date");
                                    JSONArray amilike = jsonObject.getJSONArray("amilike");
                                    JSONArray LikeNum = jsonObject.getJSONArray("Like_num");
//                                    Log.i("페이징 확인 ", "페이지 : "+pagee +"\n노트키 리스트"+Note_keyList);
                                    for (int i = 0; i < Note_keyList.length(); i++) {

                                        int NoteKey = Note_keyList.getInt(i);
                                        String maker = Note_makerList.getString(i);
                                        String makerImg = Note_makerprofile_img.getString(i);
                                        String NoteImg = Note_ImgList.getString(i);
                                        String NoteObj = Note_objList.getString(i);
                                        String NoteDate = Note_Date.getString(i);
                                        int likenum = LikeNum.getInt(i);
                                        boolean Amilike = amilike.getBoolean(i);


                                        Gson gson = new Gson();
                                        WhiskyData whiskyData = gson.fromJson(NoteObj, WhiskyData.class);

                                        String date = makeDate.formatTimeString(NoteDate);
                                        myData.add(new MyData(makerImg,
                                                maker,
                                                whiskyData.getWhisky_name() + " " + whiskyData.getWhisky_label() + " " + whiskyData.getWhisky_cask(),
                                                NoteImg,
                                                whiskyData.getBody(),
                                                whiskyData.getSweet(),
                                                whiskyData.Spice,
                                                whiskyData.getMalty(),
                                                NoteKey,
                                                date, Amilike, likenum
                                        ));


                                    }
                                }
//                                Collections.reverse(myData);
                                myAdapter.setItems(myData);
                                progressBar.setVisibility(View.GONE);
                            }else{
                                setNoPost();
                            }
                        } catch (JSONException e) {
                            Log.e("제이선 에러  ", String.valueOf(e));
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

    public void Like(int NoteKey, String myID, MyAdapter.MyViewHolder holder,MyData myData,String Maker,String newsjson) {
//        Log.e("aaa", "볼리 like 호출 " + NoteKey);
        String url = "http://13.209.19.188/LikeNote.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {


//                        Log.e("좋아요", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getContext(), "해당 노트를 좋아합니다.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                holder.tv_likenum.setText(String.valueOf(Like_num));
                                myData.setAmilike(true);
                                myData.setLikeNum(Like_num);

                            } else {

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
        request.addStringParam("Note_key", String.valueOf(NoteKey)); //POST파라미터 넣기
        request.addStringParam("myID", myID); //POST파라미터 넣기
        request.addStringParam("myID", myID); //POST파라미터 넣기
        request.addStringParam("Maker", Maker); //POST파라미터 넣기
        request.addStringParam("NewsOBJ", newsjson); //POST파라미터 넣기


        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }
    public void unLike(int NoteKey, String myID, MyAdapter.MyViewHolder holder,MyData myData) {
        String url = "http://13.209.19.188/unLikeNote.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("좋아요", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getContext(), "해당 노트를 더이상 좋아하지 않습니다.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                holder.tv_likenum.setText(String.valueOf(Like_num));
                                myData.setAmilike(false);
                                myData.setLikeNum(Like_num);
                            } else {

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
        request.addStringParam("Note_key", String.valueOf(NoteKey)); //POST파라미터 넣기
        request.addStringParam("myID", myID); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

    void sendNewsToChatServer(Newspeed_item newsitem){
        Gson gson = new Gson();
        String jsonMSG = gson.toJson(newsitem);
        Intent mIntent = new Intent("Sendmsg");
        mIntent.putExtra("news",jsonMSG);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(mIntent);
    }

    // 리사이클러뷰 아이템에 추가할 데이터 클래스
    public static class MyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트
        String profileImg,maker,tittle,NoteImg;
        float body,sweet,spice,malty;
        int NoteKey,LikeNum;
        String NoteDate;
        boolean amilike;


        public String getNoteDate() {
            return NoteDate;
        }

        public void setNoteDate(String noteDate) {
            NoteDate = noteDate;
        }

        public boolean isAmilike() {
            return amilike;
        }

        public void setAmilike(boolean amilike) {
            this.amilike = amilike;
        }

        public int getLikeNum() {
            return LikeNum;
        }

        public void setLikeNum(int likeNum) {
            LikeNum = likeNum;
        }

        public MyData(String profileImg, String maker, String tittle, String noteImg, float body, float sweet, float spice, float malty, int noteKey, String noteDate, boolean amilike, int LikeNum) {
            this.profileImg = profileImg;
            this.maker = maker;
            this.tittle = tittle;
            NoteImg = noteImg;
            this.body = body;
            this.sweet = sweet;
            this.spice = spice;
            this.malty = malty;
            NoteKey = noteKey;
            NoteDate = noteDate;
            this.amilike = amilike;
            this.LikeNum = LikeNum;
        }

        public int getNoteKey() {
            return NoteKey;
        }

        public void setNoteKey(int noteKey) {
            NoteKey = noteKey;
        }

        public String getProfileImg() {
            return profileImg;
        }

        public void setProfileImg(String profileImg) {
            this.profileImg = profileImg;
        }

        public String getMaker() {
            return maker;
        }

        public void setMaker(String maker) {
            this.maker = maker;
        }

        public String getTittle() {
            return tittle;
        }

        public void setTittle(String tittle) {
            this.tittle = tittle;
        }

        public String getNoteImg() {
            return NoteImg;
        }

        public void setNoteImg(String noteImg) {
            NoteImg = noteImg;
        }

        public float getBody() {
            return body;
        }

        public void setBody(float body) {
            this.body = body;
        }

        public float getSweet() {
            return sweet;
        }

        public void setSweet(float sweet) {
            this.sweet = sweet;
        }

        public float getSpice() {
            return spice;
        }

        public void setSpice(float spice) {
            this.spice = spice;
        }

        public float getMalty() {
            return malty;
        }

        public void setMalty(float malty) {
            this.malty = malty;
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
            void onProfileClicked(MyData model);
            void likeClicked(MyData model,MyViewHolder holder);
            void unlikeClicked(MyData model,MyViewHolder holder);
        }

        private OnMyClickListener mListener;


        private List<MyData> mItems = new ArrayList<>();

        public MyAdapter(OnMyClickListener listener) {
            mListener = listener;

        }

        public void setItems(List<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_home, parent, false);
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


            View.OnClickListener profileclick = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        final MyData item = mItems.get(viewHolder.getAdapterPosition());
                        mListener.onProfileClicked(item);
                    }
                }
            };

            viewHolder.iv_feed_profileimg.setOnClickListener(profileclick);
            viewHolder.tv_feed_ID.setOnClickListener(profileclick);



            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MyData item = mItems.get(position);
            holder.iv_feed_like.setSelected(item.isAmilike());
            holder.iv_feed_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean a = view.isSelected();
                    Log.e("하트클릭", String.valueOf(a));
                    if(view.isSelected()){ //좋아요 취소
                        holder.iv_feed_like.setSelected(false);
                        mListener.unlikeClicked(item,holder);

                    }else{ //좋아요
                        holder.iv_feed_like.setSelected(true);
                        Log.e("aaa", "바인드 뷰홀더 클릭 " + item.getNoteKey());
                        mListener.likeClicked(item,holder);

                    }
                }
            });

            holder.tv_likenum.setText(String.valueOf(item.getLikeNum()));

            // TODO : 데이터를 뷰홀더에 표시하시오
            if(item.getProfileImg().equals("Nothing")){
                Glide.with(holder.iv_feed_profileimg)
                        .load(R.drawable.myprofile)
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .into(holder.iv_feed_profileimg);
            }else {
                Glide.with(holder.iv_feed_profileimg)
                        .load("http://13.209.19.188/" + item.getProfileImg())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .into(holder.iv_feed_profileimg);
            }
            holder.tv_feed_ID.setText(item.getMaker());


            if(item.getNoteImg().equals("null")){
                Glide.with(holder.iv_feed_noteimg)
                        .load(R.drawable.logo)

                        .fallback(R.drawable.logo) //아무것도 없을때 그림표시
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .into(holder.iv_feed_noteimg);
            }else {
                Glide.with(holder.iv_feed_noteimg)
                        .load("http://13.209.19.188/" + item.getNoteImg())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.logo) //아무것도 없을때 그림표시
                        .into(holder.iv_feed_noteimg);
            }
            holder.tv_feed_tittle.setText(item.getTittle());
            holder.rb_feed_BODY.setRating(item.getBody());
            holder.rb_feed_SWEET.setRating(item.getSweet());
            holder.rb_feed_SPICE.setRating(item.getSpice());
            holder.rb_feed_MALTY.setRating(item.getMalty());
            holder.tv_Date.setText(item.getNoteDate());


        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            CircleImageView iv_feed_profileimg;
            ImageView iv_feed_noteimg;
            TextView tv_feed_ID,tv_feed_tittle,tv_Date,tv_likenum;
            RatingBar rb_feed_BODY,rb_feed_SWEET,rb_feed_SPICE,rb_feed_MALTY;
            Button iv_feed_like;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : 뷰홀더 코드를 입력하여 주세요
                iv_feed_profileimg = itemView.findViewById(R.id.iv_feed_profileimg);
                tv_feed_ID = itemView.findViewById(R.id.tv_feed_ID);
                iv_feed_noteimg = itemView.findViewById(R.id.iv_feed_noteimg);
                tv_feed_tittle = itemView.findViewById(R.id.tv_feed_tittle);
                rb_feed_BODY = itemView.findViewById(R.id.rb_feed_BODY);
                rb_feed_SWEET = itemView.findViewById(R.id.rb_feed_SWEET);
                rb_feed_SPICE = itemView.findViewById(R.id.rb_feed_SPICE);
                rb_feed_MALTY = itemView.findViewById(R.id.rb_feed_MALTY);
                iv_feed_like = itemView.findViewById(R.id.iv_feed_like);
                tv_likenum = itemView.findViewById(R.id.tv_likenum);
                tv_Date = itemView.findViewById(R.id.tv_Date);





            }
        }



    }


}


