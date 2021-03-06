package com.example.drinkernote;







import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Coment_And_Note extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;
    int NoteKey;
    String NoteMaker;
    RecyclerView rv_coment;
    LinearLayoutManager layoutManager;
    MyAdapter myAdapter;
    EditText et_coment;

    LinearLayout coment_linearLayout;
    Button btn_submit;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.

    ArrayList<MyData> myData = new ArrayList<>();
    int focusComentID = -1;
    int focusReplyID = -1;
    int page = 0, limit = 10;
    int minpage =0; //위로 올라갈때의 페이지값


    TextView tv_MakerID,tv_price;
    ConstraintLayout cl_in_scroll;
    TextView tv_Notename,tv_contents,tv_proof,tv_likeNote;
    ImageView iv_profileimg;
    Button iv_like;
    LinearLayout layoutIndicator;
    ViewPager2 sliderViewPager;
//    ScrollView scroll_view;
    NestedScrollView scroll_view;
    TextView text_view_11111111;

    MyData focusComent;

    @Override
    protected void onStart() {
        super.onStart();

        if(page == 0) {
            page++;
            if (requestQueue == null) {
                //RequestQueue 객체 생성하기
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }


            getcoment(NoteKey, page, limit,focusComentID);

        }else{
            myAdapter.setItems(myData);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coment_and_note);
        Intent mIntent = getIntent();
        NoteKey = mIntent.getIntExtra("NoteKey", 0);
        NoteMaker = mIntent.getStringExtra("NoteMaker");
        focusComentID = mIntent.getIntExtra("focusComentID", -1);
        focusReplyID = mIntent.getIntExtra("focusReplyID",-1);
//        Log.e("댓글 노트키 ", String.valueOf(NoteKey));
//        Log.e("댓글 노트작성자 ", NoteMaker);
        setToolbar();

        layoutIndicator = findViewById(R.id.layoutIndicators);
        sliderViewPager = findViewById(R.id.IV_imgPager);


        tv_price = findViewById(R.id.tv_price);
        tv_MakerID = findViewById(R.id.tv_MakerID);
        tv_Notename = findViewById(R.id.tv_Notename);
        tv_contents = findViewById(R.id.tv_contents);
        tv_proof = findViewById(R.id.tv_proof);
        iv_profileimg = findViewById(R.id.iv_feed_profileimg);
        tv_likeNote = findViewById(R.id.tv_likeNote);
        cl_in_scroll = findViewById(R.id.cl_in_scroll);
        iv_like = findViewById(R.id.iv_coment_like);
//        tv_nocoment = findViewById(R.id.tv_nocoment);
        et_coment = findViewById(R.id.et_search);
        btn_submit = findViewById(R.id.btn_cancel);
        rv_coment = findViewById(R.id.rv_coment);
        scroll_view = findViewById(R.id.scroll_view);
        text_view_11111111 = findViewById(R.id.text_view_11111111);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        rv_coment.setLayoutManager(layoutManager);
        coment_linearLayout = findViewById(R.id.coment_linearLayout);
//        nestedScrollView = findViewById(R.id.scroll_view);


        scroll_view.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {

                    page++;
//                    Log.e("페이지", String.valueOf(page));
                    getcoment(NoteKey, page, limit,focusComentID);
                }
            }
        });


        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
            }

            @Override
            public void onprofileClicked(MyData model) {
                Intent mIntent = new Intent(getApplicationContext(), Profile.class);
                mIntent.putExtra("Maker", model.getID());
                startActivity(mIntent);
            }

            @Override
            public void onLikeClicked(MyData model, MyAdapter.MyViewHolder viewHolder) {
                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기

                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                String userId = AutoLogin.getUserId(getApplicationContext());
                LikeComent(userId, model.getComentKey(), viewHolder);
            }

            @Override
            public void onUnLikeClicked(MyData model, MyAdapter.MyViewHolder viewHolder) {
                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기

                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                String userId = AutoLogin.getUserId(getApplicationContext());
                unLikeComent(userId, model.getComentKey(), viewHolder);
            }

            @Override
            public void onLikeListClicked(MyData model) {
                Intent mIntent = new Intent(getApplicationContext(), LikeList.class);
                mIntent.putExtra("NoteKey", model.getComentKey());
                mIntent.putExtra("from", "coment");
                startActivity(mIntent);

            }

            @Override
            public void onReplyClicked(MyData model, int position) {
                Intent mIntent = new Intent(getApplicationContext(), Reply.class);
                mIntent.putExtra("comentKey", model.getComentKey());
                mIntent.putExtra("position", position);
                mIntent.putExtra("NoteKey", NoteKey);
//                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(mIntent, 11);
            }
        },getApplicationContext());

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contents = et_coment.getText().toString();
                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                // TODO : 댓글을 전송해서 데이터베이스에 저장하고 새로고침해야함
                String userId = AutoLogin.getUserId(getApplicationContext());
                InsertComent(NoteKey, userId, contents);


            }
        });

        rv_coment.setAdapter(myAdapter);
        rv_coment.setHasFixedSize(true);

        MySwipeHelper_a swipeHelper = new MySwipeHelper_a(getApplicationContext(), rv_coment, 300) {
            @Override
            public void instantiatrMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buffer) {

                MyData mdata = myAdapter.getItem(viewHolder.getAdapterPosition());
                String userId = AutoLogin.getUserId(getApplicationContext());
                if (mdata.getID().equals(userId)) {
                    buffer.add(new MyButton(getApplicationContext(),
                            "Delete",
                            30,
                            R.drawable.ic_delete,
                            Color.parseColor("#B13333"),
                            new MyButtonClickListener_a() {
                                @Override
                                public void onClick(int pos) {
//                                    Log.d("TAG", viewHolder.getAdapterPosition() + "");
                                    if (requestQueue == null) {
                                        //RequestQueue 객체 생성하기
                                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                                    }
                                    DeleteComent(mdata.getComentKey(), pos);

                                }
                            }));
                    buffer.add(new MyButton(getApplicationContext(),
                            "Update",
                            30,
                            R.drawable.ic_update,
                            Color.parseColor("#A8A8A8"),
                            new MyButtonClickListener_a() {
                                @Override
                                public void onClick(int pos) {
                                    //TODO: 편집할 코드
                                    if (requestQueue == null) {
                                        //RequestQueue 객체 생성하기
                                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                                    }

                                    //TODO :여기에 댓글수정 넣어야함
                                    Intent mIntent = new Intent(getApplicationContext(), comentUpdate_pop.class);
                                    mIntent.putExtra("contents", mdata.getContents());
                                    mIntent.putExtra("comentkey", mdata.getComentKey());
                                    mIntent.putExtra("position", viewHolder.getAdapterPosition());
                                    startActivityForResult(mIntent, 10);

                                }
                            }));
                } else {
                    buffer.add(new MyButton(getApplicationContext(),
                            "reply",
                            30,
                            R.drawable.ic_reply,
                            Color.parseColor("#A8A8A8"),
                            new MyButtonClickListener_a() {
                                @Override
                                public void onClick(int pos) {
                                    //TODO: 편집할 코드
                                    Intent mIntent = new Intent(getApplicationContext(), Reply.class);
                                    mIntent.putExtra("comentKey", mdata.getComentKey());
                                    mIntent.putExtra("position", viewHolder.getAdapterPosition());
                                    mIntent.putExtra("NoteKey", NoteKey);
                                    startActivityForResult(mIntent, 11);
                                }
                            }));
                }
            }
        };// swipeHelper

        coment_linearLayout.bringToFront();


        if (requestQueue == null) {
            //RequestQueue 객체 생성하기
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        String userId = AutoLogin.getUserId(this);
        getNoteInfo(NoteKey,userId);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10){
            //댓글수정으로부터 오는곳
            if(resultCode == RESULT_OK) {

                String contents = data.getStringExtra("contents");
                int comentkey = data.getIntExtra("comentkey", 0);
                int position = data.getIntExtra("position",-1);
                //댓글을 업데이트 해야함
                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기

                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                UpdateComent(comentkey,contents,position);
            }
        }
        if(requestCode == 11){
            //대댓글로부터 오는곳
            //여기에 갯수를 넣어야함
            int position,Reply_num;
            Reply_num = data.getIntExtra("Reply_num",-1);
            position = data.getIntExtra("position",-1);
            myData.get(position).setReplynum(Reply_num);
            myAdapter.setItems(myData);
        }
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



    public void getNoteInfo(int NoteKey,String myID){
        String url = "http://13.209.19.188/SelectNoteInfo.php?NoteKey="+NoteKey+"&myID="+myID;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            NoteMaker = jsonObject.getString("NoteMaker");

                            String NoteDate = jsonObject.getString("NoteDate");
                            String Profile_img = jsonObject.getString("Profile_img");
                            JSONArray NoteImg = jsonObject.getJSONArray("NoteImg");
                            String NoteObj = jsonObject.getString("NoteObj");
                            boolean amilike = jsonObject.getBoolean("amilike");
                            int Like_num = jsonObject.getInt("Like_num");
                            int coment_num = jsonObject.getInt("coment_num");
                            String userId = AutoLogin.getUserId(getApplicationContext());
                            if(!userId.equals(NoteMaker)){
                                toolbar.getMenu().clear();
                            }

                            ArrayList NoteImgList = new ArrayList();
                            for (int i = 0; i < NoteImg.length(); i++){
                                NoteImgList.add(NoteImg.getString(i));
                            }

                            //노트데이터를 객채화
                            Gson gson = new Gson();
                            WhiskyData whiskyData = gson.fromJson(NoteObj,WhiskyData.class);

                            setNote(whiskyData,NoteMaker,NoteDate,Profile_img,NoteImgList,Like_num,amilike,coment_num);

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

    void setNote(WhiskyData whiskyData,String NoteMaker,String NoteDate,String Profile_img, ArrayList NoteImgList,int Like_num,boolean amilike,int coment_num) {

        Log.e("이미지 갯수", String.valueOf(NoteImgList.size()));

        tv_likeNote.setText(Like_num + " 명이 좋아합니다.");
        tv_likeNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getApplicationContext(), LikeList.class);

                mIntent.putExtra("NoteKey",NoteKey);
                mIntent.putExtra("from","note");
                startActivity(mIntent);
            }
        });


        iv_like.setSelected(amilike);
        iv_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = AutoLogin.getUserId(getApplicationContext());
                if (view.isSelected()) { //좋아요 취소
                    iv_like.setSelected(false);
                    if (requestQueue == null) {
                        //RequestQueue 객체 생성하기
                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                    }
//                    unLike(NoteKey,userId);

                } else { //좋아요
                    iv_like.setSelected(true);
                    if (requestQueue == null) {
                        //RequestQueue 객체 생성하기
                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                    }

                    if(!userId.equals(NoteMaker)) { //좋아요누른사람이 같은사람이 아니라면
                        Newspeed_item likenews = new Newspeed_item(1, NoteKey, userId, NoteMaker);
                        Gson gson = new Gson();
                        String jsonMSG = gson.toJson(likenews);
//                        Like(NoteKey,userId,NoteMaker,jsonMSG);
                        sendNewsToChatServer(likenews);


                    }else{
//                        Like(NoteKey,userId,NoteMaker,"nonews");
                    }
                }
            }
        });

        sliderViewPager.setOffscreenPageLimit(1);
        sliderViewPager.setAdapter(new ImageSliderAdapter(this, NoteImgList));
        sliderViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
            }
        });


        setupIndicators(NoteImgList.size());

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
        View.OnClickListener profileclick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getApplicationContext(), Profile.class);
                mIntent.putExtra("Maker",NoteMaker);
                startActivity(mIntent);
            }
        } ;

        iv_profileimg.setOnClickListener(profileclick);
        tv_MakerID.setOnClickListener(profileclick);

        tv_MakerID.setText(NoteMaker); //작성자 아이디
        String name = whiskyData.getWhisky_name()+" "+whiskyData.getWhisky_label()+" "+whiskyData.getWhisky_cask();
        name = name.replaceAll("\\R", "");
        tv_Notename.setText(name);
        tv_contents.setText(whiskyData.getContents());
        tv_proof.setText(whiskyData.getWhisky_proof()+" %");
        tv_price.setText(whiskyData.getWhisky_price()+" 원");

    }


    private void setupIndicators(int count) {
        layoutIndicator.removeAllViews();
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(16, 8, 16, 8);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.bg_indicator_inactive));
            indicators[i].setLayoutParams(params);
            layoutIndicator.addView(indicators[i]);
        }
        setCurrentIndicator(0);
    }

    private void setCurrentIndicator(int position) {
        int childCount = layoutIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicator.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.bg_indicator_active
                ));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.bg_indicator_inactive
                ));
            }
        }
    }



    public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.MyViewHolder> {
        private Context context;
        private ArrayList<String> sliderImage;

        public ImageSliderAdapter(Context context, ArrayList<String> sliderImage) {
            this.context = context;
            this.sliderImage = sliderImage;
        }

        @NonNull
        @Override
        public ImageSliderAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_slider, parent, false);
            return new ImageSliderAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageSliderAdapter.MyViewHolder holder, int position) {

            holder.bindSliderImage(sliderImage.get(position));
        }

        @Override
        public int getItemCount() {
            return sliderImage.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private ImageView mImageView;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                mImageView = itemView.findViewById(R.id.imageSlider);
            }

            public void bindSliderImage(String imageURL) {
                Log.e("imgurl", "durldhsl");
                if(imageURL.equals("Nothing")){
                    Glide.with(context)
                            .load(R.drawable.logo)
                            .fitCenter()
                            .into(mImageView);
                }else {
                    Glide.with(context)
                            .load("http://13.209.19.188/" + imageURL)
                            .fitCenter()
                            .into(mImageView);
                }
            }
        }
    }



    //해당 게시글의 댓글들을 불러오는 메서드
    public void getcoment(int NoteKey,int pagee,int limit,int focusComentIDin) {
        String userId = AutoLogin.getUserId(this);
        String url = "http://13.209.19.188/GetComent.php?myID="+userId+"&notekey="+NoteKey+"&page="+pagee+"&limit="+limit+"&focusComentID="+focusComentIDin;


        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

                        Log.e("게시글댓글 불러오기 ", "onResponse: 응답 : " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(focusComentIDin != -1){
                                if(focusReplyID == -1) { //대댓글알림이 아니라면
                                    focusComentID = -1;
                                    focusComent = new MyData(jsonObject.getString("focusComent_profileImg"),
                                            jsonObject.getString("focusComent_ID"),
                                            jsonObject.getString("focusComent_contents"),
                                            makeDate.formatTimeString(jsonObject.getString("focusComent_date")),
                                            jsonObject.getInt("focusComent_likenum"),
                                            jsonObject.getBoolean("focusComent_amilike"),
                                            jsonObject.getInt("focusComentKey"),
                                            jsonObject.getInt("focusComent_replynum"),
                                            true
                                    );
                                    myData.add(focusComent);
                                }else{
                                    focusComentID = -1;
                                    focusComent = new MyData(jsonObject.getString("focusComent_profileImg"),
                                            jsonObject.getString("focusComent_ID"),
                                            jsonObject.getString("focusComent_contents"),
                                            makeDate.formatTimeString(jsonObject.getString("focusComent_date")),
                                            jsonObject.getInt("focusComent_likenum"),
                                            jsonObject.getBoolean("focusComent_amilike"),
                                            jsonObject.getInt("focusComentKey"),
                                            jsonObject.getInt("focusComent_replynum"),
                                            false
                                    );
                                    focusComent.setFocusreply(true);
                                    focusComent.setFocusreplyKey(focusReplyID);
                                    myData.add(focusComent);
                                }
                            }

                            int allcoment_num = jsonObject.getInt("allcoment_num");
                            if(allcoment_num > 0){ //댓글이 있을경우
                                rv_coment.setVisibility(View.VISIBLE);
//                                tv_nocoment.setVisibility(View.GONE);
                                String nocoment = jsonObject.getString("nocoment");
                                if(nocoment.equals("yes")) {
                                    JSONArray comentID_List = jsonObject.getJSONArray("comentID_List");
                                    JSONArray coment_contents = jsonObject.getJSONArray("coment_contents");
                                    JSONArray coment_date = jsonObject.getJSONArray("coment_date");
                                    JSONArray coment_profileImg = jsonObject.getJSONArray("coment_profileImg");
                                    JSONArray coment_likenum = jsonObject.getJSONArray("coment_likenum");
                                    JSONArray amilike = jsonObject.getJSONArray("amilike");
                                    JSONArray Coment_key = jsonObject.getJSONArray("Coment_key");
                                    JSONArray Reply_num = jsonObject.getJSONArray("Reply_num");
                                    Log.i("페이징 확인 ", "페이지 : "+pagee +"\n아이디 리스트"+comentID_List);
                                    for (int i = 0; i < comentID_List.length(); i++) {
                                        String date = makeDate.formatTimeString(coment_date.getString(i));

                                        if(focusComent.getComentKey() != Coment_key.getInt(i)) {
                                            myData.add(new MyData(coment_profileImg.getString(i),
                                                    comentID_List.getString(i),
                                                    coment_contents.getString(i),
                                                    date,
                                                    coment_likenum.getInt(i),
                                                    amilike.getBoolean(i),
                                                    Coment_key.getInt(i),
                                                    Reply_num.getInt(i), false));
                                        }
                                    }


                                    // 어뎁터에 데이터 추가
                                    myAdapter.setItems(myData);


                                }else{
                                    page --;
                                }
                            }else{ //댓글이 없을경우
                                page --;
                                rv_coment.setVisibility(View.GONE);
//                                tv_nocoment.setVisibility(View.VISIBLE);
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






    //댓글을 데이터베이스로 전송하는 메서드
    public void InsertComent(int NoteKey,String comentMaker,String contents) {
        String url = "http://13.209.19.188/InsertComent.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

                        Log.e("insertcoment", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                String comentmakerImg = jsonObject.getString("comentmakerimg");
                                int comentkey = jsonObject.getInt("comentkey");
                                String comentdate = jsonObject.getString("comentdate");
                                int comentlikenum = jsonObject.getInt("comentlikenum");

                                if(!comentMaker.equals(NoteMaker)){ //댓글 작성자와 노트 작성자가 같지 않다면
                                    //노트 작성자에게 뉴스를 보내야함
                                    Newspeed_item comentnews = new Newspeed_item(0,NoteKey,comentMaker,comentkey,NoteMaker,contents);
                                    Gson gson = new Gson();
                                    String jsonMSG = gson.toJson(comentnews);
                                    InsertNews(NoteMaker,jsonMSG,comentMaker,comentkey);
                                    sendNewsToChatServer(comentnews);

                                }
                                Toast.makeText(getApplicationContext(), "댓글이 성공적으로 게시되었습니다.", Toast.LENGTH_SHORT).show();
                                et_coment.setText("");
                                InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                page =1;
//                                myData.clear();
                                String date = makeDate.formatTimeString(comentdate);
                                getcoment(NoteKey,page,limit,focusComentID);
                                myData.add(0,new MyData(comentmakerImg,comentMaker,contents,date,comentlikenum,false,comentkey,0,false));
                            } else {
                                Toast.makeText(getApplicationContext(), "댓글 게시를 실패했습니다.", Toast.LENGTH_SHORT).show();
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
        request.addStringParam("NoteKey", String.valueOf(NoteKey)); //POST파라미터 넣기
        request.addStringParam("comentMaker", comentMaker); //POST파라미터 넣기
        request.addStringParam("contents", contents); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }


    //댓글을 삭제하는 메서드
    public void DeleteComent(int comentKey,int position) {
        String url = "http://13.209.19.188/DeleteComent.php?comentKey="+comentKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

                        Log.e("deletecoment", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String res = jsonObject.getString("response");
                            if(res.equals("true")){
//                                getcoment(NoteKey);
                                myAdapter.deleteItems(position);
                                if(myAdapter.getItemsize()<=0){
                                    rv_coment.setVisibility(View.GONE);
//                                    tv_nocoment.setVisibility(View.VISIBLE);
                                }
                            }else{

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

    //댓글 좋아요 메서드
    public void LikeComent(String myID, int comentKey, MyAdapter.MyViewHolder viewHolder) {
        String url = "http://13.209.19.188/LikeComent.php?myID="+myID+"&Coment_key="+comentKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("댓글 좋아요", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "해당 댓글을 좋아합니다.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                if(Like_num == 0){
                                    viewHolder.tv_coment_likenum.setText("");
                                }else {
                                    viewHolder.tv_coment_likenum.setText("좋아요 " + Like_num + "개");
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "좋아요 실패", Toast.LENGTH_SHORT).show();
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
    public void unLikeComent(String myID, int comentKey, MyAdapter.MyViewHolder viewHolder) {
        String url = "http://13.209.19.188/unLikeComent.php?myID="+myID+"&Coment_key="+comentKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("댓글 안좋아요", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "해당 댓글을 좋아하지 않습니다.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                if(Like_num == 0){
                                    viewHolder.tv_coment_likenum.setText("");
                                }else {
                                    viewHolder.tv_coment_likenum.setText("좋아요 " + Like_num + "개");
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "좋아요 실패", Toast.LENGTH_SHORT).show();
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

    //댓글 업데이트 메서드
    public void UpdateComent(int comentKey,String contents,int position) {
        String url = "http://13.209.19.188/UpdateComent.php?Coment_key="+comentKey+"&contents="+contents;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
//                        Log.d("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "성공적으로 수정하였습니다.", Toast.LENGTH_SHORT).show();
                                myData.get(position).setContents(contents);
                                myAdapter.setItems(myData);
                            } else {
                                Toast.makeText(getApplicationContext(), "수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
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


    public void InsertNews(String userID,String newsOBJ,String fromID,int comentKey) {
        String url = "http://13.209.19.188/InsertNews.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

                        Log.e("insertnews", "onResponse: 응답 : " + response);

                    }
                },
                new Response.ErrorListener() {
                    @Override //에러시 처리할 내용
                    public void onErrorResponse(VolleyError error) {

                        Log.e("TAG", "에러-> " + error.getMessage());
                    }
                });
        request.addStringParam("userID", userID); //POST파라미터 넣기
        request.addStringParam("newsOBJ", newsOBJ); //POST파라미터 넣기
        request.addStringParam("fromID", fromID); //POST파라미터 넣기
        request.addStringParam("comentKey", String.valueOf(comentKey)); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }
    void sendNewsToChatServer(Newspeed_item newsitem){
        Gson gson = new Gson();
        String jsonMSG = gson.toJson(newsitem);
        Intent mIntent = new Intent("Sendmsg");
        mIntent.putExtra("news",jsonMSG);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
    }

    // 리사이클러뷰 아이템에 추가할 데이터 클래스
    public static class MyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트
        String img;
        String ID;
        String contents;
        String date;
        int likenum;
        boolean amilike;
        int comentKey;
        int Replynum;
        boolean focus;
        boolean focusreply =false;
        boolean replyflag=true;
        int replyminpage =0;
        int replypage = 0;
        int focusreplyKey = -1;
        ArrayList<MyreplyData> replyArray = new ArrayList<>();

        public int getFocusreplyKey() {
            return focusreplyKey;
        }

        public void setFocusreplyKey(int focusreplyKey) {
            this.focusreplyKey = focusreplyKey;
        }

        public boolean isFocusreply() {
            return focusreply;
        }

        public void setFocusreply(boolean focusreply) {
            this.focusreply = focusreply;
        }

        public int getReplyminpage() {
            return replyminpage;
        }

        public void setReplyminpage(int replyminpage) {
            this.replyminpage = replyminpage;
        }

        public int getReplypage() {
            return replypage;
        }

        public void setReplypage(int replypage) {
            this.replypage = replypage;
        }

        public ArrayList<MyreplyData> getReplyArray() {
            return replyArray;
        }

        public void setReplyArray(ArrayList<MyreplyData> replyArray) {
            this.replyArray = replyArray;
        }

        public boolean isReplyflag() {
            return replyflag;
        }

        public void setReplyflag(boolean replyflag) {
            this.replyflag = replyflag;
        }

        public boolean isFocus() {
            return focus;
        }

        public void setFocus(boolean focus) {
            this.focus = focus;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getID() {
            return ID;
        }

        public int getComentKey() {
            return comentKey;
        }

        public void setComentKey(int comentKey) {
            this.comentKey = comentKey;
        }

        public void setID(String ID) {
            this.ID = ID;
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

        public int getLikenum() {
            return likenum;
        }

        public int getReplynum() {
            return Replynum;
        }

        public void setReplynum(int replynum) {
            Replynum = replynum;
        }

        public void setLikenum(int likenum) {
            this.likenum = likenum;
        }

        public boolean isAmilike() {
            return amilike;
        }

        public void setAmilike(boolean amilike) {
            this.amilike = amilike;
        }

        public MyData(String img, String ID, String contents, String date, int likenum, boolean amilike, int comentKey, int replynum, boolean focus) {
            this.img = img;
            this.ID = ID;
            this.contents = contents;
            this.date = date;
            this.likenum = likenum;
            this.amilike = amilike;
            this.comentKey = comentKey;
            Replynum = replynum;
            this.focus = focus;
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        Context conn;
        RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.


        void checkrequestQueue() {

            if (requestQueue == null) {
                //RequestQueue 객체 생성하기

                requestQueue = Volley.newRequestQueue(conn);
            }
        }


        public void getReply(int comentkey, String myID, int pagee, int limit,int position,int replyitemtype,int focusreplyKey) {
            String url = "http://13.209.19.188/GetReply_NoteAndComent.php?comentKey=" + comentkey + "&myID=" + myID + "&page=" + pagee + "&limit=" + limit+"&focusreplyKey="+focusreplyKey;
            //타입 0 대댓글
            //타입 1 답글 n개 더보기
            //타입 2 이전답글 보기
            //타입 3 다음답글 보기
            //타입 4 답글 숨기기
            //타입 5 대댓글알림 클릭해서 들어왔을때 최초의 조회
            MyData item = getItem(position);
            ArrayList<MyreplyData> replyArray = item.getReplyArray();

            SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override //서버 응답시 처리할 내용
                        public void onResponse(String response) {

                            Log.e("대댓글 ", "onResponse: 응답 : " + response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray Reply_ID = jsonObject.getJSONArray("Reply_ID");
                                JSONArray Reply_date = jsonObject.getJSONArray("Reply_date");
                                JSONArray Reply_contents = jsonObject.getJSONArray("Reply_contents");
                                JSONArray Reply_profile = jsonObject.getJSONArray("Reply_profile");
                                String noreply = jsonObject.getString("noreply");
                                JSONArray Reply_likenum = jsonObject.getJSONArray("Reply_likenum");
                                JSONArray Reply_amilike = jsonObject.getJSONArray("Reply_amilike");
                                JSONArray Reply_key = jsonObject.getJSONArray("Reply_key");
                                int Reply_num = jsonObject.getInt("Reply_num");


                                if(!noreply.equals("Nothing")){ //조회했을때 남은 대댓글이 있다면
                                    if(replyitemtype == 1) { //댓글n개보기 클릭이라면
                                        //데이터를 넣어주고 맨아래 다음답글 보기 로 넣어준다.
                                        replyArray.clear();
                                        for (int i = 0; i < Reply_ID.length(); i++) {
                                            boolean focus = false;
                                            if(Reply_key.getInt(i) == focusreplyKey){
                                                focus = true;
                                            }
                                            replyArray.add(new MyreplyData(Reply_profile.getString(i),
                                                    Reply_ID.getString(i),
                                                    Reply_contents.getString(i),
                                                    makeDate.formatTimeString(Reply_date.getString(i)),
                                                    Reply_likenum.getInt(i),
                                                    Reply_amilike.getBoolean(i),
                                                    Reply_key.getInt(i),focus));
                                        }
                                        int replynum = getreplytype0num(replyArray);

                                        if(item.getReplypage()*limit >= Reply_num) { //현재 조회된 대댓글갯수와 모든 대댓글갯수가 같으면
                                            replyArray.add(new MyreplyData(4, "답글 숨기기"));
                                        }else {
                                            replyArray.add(new MyreplyData(3, "다음답글 보기"));
                                        }
                                        item.setReplyArray(replyArray);
                                        setItems(mItems);
                                    }else if(replyitemtype == 2){
                                        Log.e("minpage", String.valueOf(item.getReplyminpage()));
                                        replyArray.remove(0);

                                        for (int i = Reply_ID.length()-1; i >= 0; i--) {
                                            boolean focus = false;
                                            if(Reply_key.getInt(i) == focusreplyKey){
                                                focus = true;
                                            }
                                            replyArray.add(0,new MyreplyData(Reply_profile.getString(i),
                                                    Reply_ID.getString(i),
                                                    Reply_contents.getString(i),
                                                    makeDate.formatTimeString(Reply_date.getString(i)),
                                                    Reply_likenum.getInt(i),
                                                    Reply_amilike.getBoolean(i),
                                                    Reply_key.getInt(i),focus));
                                        }
                                        if(item.getReplyminpage() > 1){ //이전답글이 남아있다는 뜻
                                            replyArray.add(0,new MyreplyData(2,"이전답글 보기"));
                                        }

                                        item.setReplyArray(replyArray);
                                        setItems(mItems);
                                    }else if(replyitemtype == 3){
                                        //데이터를 넣어주고 맨아래 다음답글 보기 로 넣어준다.
                                        replyArray.remove(replyArray.size()-1);
                                        for (int i = 0; i < Reply_ID.length(); i++) {
                                            boolean focus = false;
                                            if(Reply_key.getInt(i) == focusreplyKey){
                                                focus = true;
                                            }
                                            replyArray.add(new MyreplyData(Reply_profile.getString(i),
                                                    Reply_ID.getString(i),
                                                    Reply_contents.getString(i),
                                                    makeDate.formatTimeString(Reply_date.getString(i)),
                                                    Reply_likenum.getInt(i),
                                                    Reply_amilike.getBoolean(i),
                                                    Reply_key.getInt(i),focus));
                                        }
                                        int replynum = getreplytype0num(replyArray);
                                        if(item.getReplypage()*limit >= Reply_num) { //현재 조회된 대댓글갯수와 모든 대댓글갯수가 같으면
                                            replyArray.add(new MyreplyData(4, "답글 숨기기"));
                                        }else {
                                            replyArray.add(new MyreplyData(3, "다음답글 보기"));
                                        }
                                        item.setReplyArray(replyArray);
                                        setItems(mItems);
                                    }else if(replyitemtype == 4){

                                    }else if(replyitemtype == 5){
                                        int page = jsonObject.getInt("page");
                                        if(page > 1){ //이전페이지가 있는경우임
                                            replyArray.add(new MyreplyData(2,"이전답글 보기"));
                                        }
                                        item.setReplypage(page);
                                        item.setReplyminpage(page);
                                        for (int i = 0; i < Reply_ID.length(); i++) {
                                            boolean focus = false;
                                            if(Reply_key.getInt(i) == focusreplyKey){
                                                focus = true;
                                            }
                                            replyArray.add(new MyreplyData(Reply_profile.getString(i),
                                                    Reply_ID.getString(i),
                                                    Reply_contents.getString(i),
                                                    makeDate.formatTimeString(Reply_date.getString(i)),
                                                    Reply_likenum.getInt(i),
                                                    Reply_amilike.getBoolean(i),
                                                    Reply_key.getInt(i),focus));
                                        }
                                        int replynum = getreplytype0num(replyArray);
                                        if(item.getReplypage()*limit >= Reply_num) { //현재 조회된 대댓글갯수와 모든 대댓글갯수가 같으면
                                            replyArray.add(new MyreplyData(4, "답글 숨기기"));
                                        }else {
                                            replyArray.add(new MyreplyData(3, "다음답글 보기"));
                                        }
                                        item.setReplyArray(replyArray);
                                        setItems(mItems);
                                    }
                                }else{ //조회했을때 남은 대댓글이 없다면

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

        interface OnMyClickListener {
            void onMyClicked(MyData model);

            void onprofileClicked(MyData model);

            void onLikeClicked(MyData model, MyViewHolder viewHolder);

            void onUnLikeClicked(MyData model, MyViewHolder viewHolder);

            void onLikeListClicked(MyData model);

            void onReplyClicked(MyData model, int position);
        }

        private OnMyClickListener mListener;

        private ArrayList<MyData> mItems = new ArrayList<>();

        private int getreplytype0num(ArrayList<MyreplyData> replayArray){
            int num =0;
            for (int i = 0; i < replayArray.size(); i++){
                if(replayArray.get(i).getType() == 0){
                    num++;
                }
            }
            return num;
        }



        public MyAdapter(OnMyClickListener listener, Context context) {
            mListener = listener;
            conn = context;
        }

        public void setItems(ArrayList<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }

        public void deleteItems(int position) {
            mItems.remove(position);
            notifyItemRemoved(position);
        }

        public MyData getItem(int position) {
            MyData rdata = mItems.get(position);
            return rdata;
        }

        public int getItemsize() {
            return mItems.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comentandnote, parent, false);
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
                    mListener.onprofileClicked(mItems.get(viewHolder.getAdapterPosition()));
                }
            };
            viewHolder.iv_coment_profileimg.setOnClickListener(profileclick);
            viewHolder.tv_coment_ID.setOnClickListener(profileclick);

            viewHolder.iv_coment_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view.isSelected()) {
                        view.setSelected(false);

                        // 좋아요 취소
                        mListener.onUnLikeClicked(mItems.get(viewHolder.getAdapterPosition()), viewHolder);
                    } else {
                        view.setSelected(true);
                        //좋아요
                        mListener.onLikeClicked(mItems.get(viewHolder.getAdapterPosition()), viewHolder);
                    }
                }
            });

            viewHolder.tv_coment_likenum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onLikeListClicked(mItems.get(viewHolder.getAdapterPosition()));
                }
            });

            viewHolder.tv_coment_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onReplyClicked(mItems.get(viewHolder.getAdapterPosition()), viewHolder.getAdapterPosition());
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            MyData item = mItems.get(position);
            ArrayList<MyreplyData> myreplyDatalist = item.getReplyArray();

            LinearLayoutManager layoutManager;
            layoutManager = new LinearLayoutManager(holder.rv_inreply.getContext());
            holder.rv_inreply.setLayoutManager(layoutManager);


            MyreplyAdapter MyreplyAdapter = new MyreplyAdapter(position);

            holder.rv_inreply.setAdapter(MyreplyAdapter);
            holder.rv_inreply.setHasFixedSize(true);

            MyreplyAdapter.OnMyClickListener onclick = new MyreplyAdapter.OnMyClickListener() {
                @Override
                public void onMyClicked(MyreplyData model, int comentposition) {
                    //타입 0 대댓글
                    //타입 1 답글 n개 더보기
                    //타입 2 이전답글 보기
                    //타입 3 다음답글 보기
                    //타입 4 답글 숨기기

                    //model = 대댓글 모델
                    int type = model.getType();

                    if (type == 1) {
                        checkrequestQueue();
                        String userId = AutoLogin.getUserId(conn);
                        item.setReplypage(item.getReplypage()+1);
                        getReply(item.getComentKey(),userId,item.getReplypage(),5,comentposition,type,-1);
                    } else if (type == 2) {
                        checkrequestQueue();
                        String userId = AutoLogin.getUserId(conn);
                        item.setReplyminpage(item.getReplyminpage()-1);
                        getReply(item.getComentKey(),userId,item.getReplyminpage(),5,comentposition,type,-1);
                    } else if (type == 3) {
                        checkrequestQueue();
                        String userId = AutoLogin.getUserId(conn);
                        item.setReplypage(item.getReplypage()+1);
                        getReply(item.getComentKey(),userId,item.getReplypage(),5,comentposition,type,-1);
                    } else if (type == 4) {
                        item.setReplypage(0);
                        MyreplyAdapter.settype4(item.getReplynum());
                    }

                }
            };

            MyreplyAdapter.setOnItemClickListener(onclick);

            if (item.isFocus()) {
                holder.coment_layout.setBackgroundColor(Color.DKGRAY);
            } else {
                holder.coment_layout.setBackgroundColor(Color.TRANSPARENT);
            }
            // TODO : 데이터를 뷰홀더에 표시하시오
            if (item.getImg().equals("Nothing")) {
                Glide.with(holder.itemView)
                        .load(R.drawable.myprofile)
                        .placeholder(R.drawable.dataloading)
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .into(holder.iv_coment_profileimg);
            } else {
                Glide.with(holder.itemView)
                        .load("http://13.209.19.188/" + item.getImg())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .into(holder.iv_coment_profileimg);
            }

            holder.tv_coment_ID.setText(item.getID());
            holder.tv_coment_contents.setText(item.getContents());
            holder.tv_coment_date.setText(item.getDate());

            if (item.getLikenum() == 0) {
                holder.tv_coment_likenum.setText("");
            } else {
                holder.tv_coment_likenum.setText("좋아요 " + item.getLikenum() + "개");
            }
            holder.iv_coment_like.setSelected(item.isAmilike());
            if (item.getReplynum() == 0) {
                holder.tv_coment_reply.setText("답글 달기");
            } else {
//                holder.tv_coment_reply.setText("답글 "+item.getReplynum()+"개");
                holder.tv_coment_reply.setText("답글 달기");
                if (item.isReplyflag()) {
//                    Log.e("답글n개 보기 추가", "??");
                    item.setReplyflag(false);
                    if(!item.isFocusreply()) {

                        myreplyDatalist.add(new MyreplyData(1, "답글 " + item.getReplynum() + "개 더보기"));
                    }else{
                        checkrequestQueue();
                        String userId = AutoLogin.getUserId(conn);
                        item.setFocusreply(false);
                        getReply(item.getComentKey(),userId,0,5,holder.getAdapterPosition(),5,item.getFocusreplyKey());

                    }
                }
            }


            item.setReplyArray(myreplyDatalist);
            MyreplyAdapter.setItems(myreplyDatalist);


        }

        @Override
        public int getItemCount() {

            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            CircleImageView iv_coment_profileimg;
            TextView tv_coment_ID, tv_coment_contents, tv_coment_date, tv_coment_likenum, tv_coment_reply;
            Button iv_coment_like;
            ConstraintLayout coment_layout;
            RecyclerView rv_inreply;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : 뷰홀더 코드를 입력하여 주세요
                iv_coment_profileimg = itemView.findViewById(R.id.iv_coment_profileimg);
                tv_coment_ID = itemView.findViewById(R.id.tv_coment_ID);
                tv_coment_contents = itemView.findViewById(R.id.tv_coment_contents);
                tv_coment_date = itemView.findViewById(R.id.tv_coment_date);
                tv_coment_likenum = itemView.findViewById(R.id.tv_coment_likenum);
                tv_coment_reply = itemView.findViewById(R.id.tv_coment_reply);
                iv_coment_like = itemView.findViewById(R.id.iv_coment_like);
                coment_layout = itemView.findViewById(R.id.coment_layout);
                rv_inreply = itemView.findViewById(R.id.rv_inreply);

            }
        }
    }




    // 리사이클러뷰 아이템에 추가할 데이터 클래스
    public static class MyreplyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트

        //타입 0 대댓글
        //타입 1 답글 n개 더보기
        //타입 2 이전답글 보기
        //타입 3 다음답글 보기
        //타입 4 답글 숨기기

        int type=0;
        String img = "";
        String ID = "";
        String contents = "";
        String date = "";
        int likenum = 0;
        boolean amilike = false;
        int replyKey = -1;
        boolean focus;

        public boolean isFocus() {
            return focus;
        }

        public void setFocus(boolean focus) {
            this.focus = focus;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }




        public MyreplyData(int type, String contents) {
            this.type = type;
            this.contents = contents;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
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

        public int getLikenum() {
            return likenum;
        }

        public void setLikenum(int likenum) {
            this.likenum = likenum;
        }

        public boolean isAmilike() {
            return amilike;
        }

        public void setAmilike(boolean amilike) {
            this.amilike = amilike;
        }

        public int getReplyKey() {
            return replyKey;
        }

        public void setReplyKey(int replyKey) {
            this.replyKey = replyKey;
        }

        public MyreplyData(String img, String ID, String contents, String date, int likenum, boolean amilike, int replyKey,boolean focus) {
            this.img = img;
            this.ID = ID;
            this.contents = contents;
            this.date = date;
            this.likenum = likenum;
            this.amilike = amilike;
            this.replyKey = replyKey;
            this.focus = focus;
        }
    }
    private static class MyreplyAdapter extends RecyclerView.Adapter<MyreplyAdapter.MyViewHolder> {
        int comentposition;

        interface OnMyClickListener {
            void onMyClicked(MyreplyData model,int position);
        }

        private OnMyClickListener mListener;

        private List<MyreplyData> mItems = new ArrayList<>();

        public void setOnItemClickListener(OnMyClickListener listener) {
            this.mListener = listener;
        }

        public MyreplyAdapter(int comentposition) {
            this.comentposition = comentposition;
        }

        public int getComentposition() {
            return comentposition;
        }

        public void setComentposition(int comentposition) {
            this.comentposition = comentposition;
        }

        public void setItems(List<MyreplyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }

        public void settype4(int replynum) {
            mItems.clear();
            mItems.add(new MyreplyData(1,"답글 "+replynum+"개 보기"));
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reply, parent, false);
            final MyViewHolder viewHolder = new MyViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        final MyreplyData item = mItems.get(viewHolder.getAdapterPosition());

                        mListener.onMyClicked(item,getComentposition());
                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            MyreplyData item = mItems.get(position);

            if(item.getType() ==0) {
                holder.tv_type1.setVisibility(View.GONE);

                // TODO : 데이터를 뷰홀더에 표시하시오
                if (item.getImg().equals("Nothing")) {
                    Glide.with(holder.itemView)
                            .load(R.drawable.myprofile)
                            .placeholder(R.drawable.dataloading)
                            .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                            .centerCrop() //가운데를 기준으로 크기맞추기
                            .into(holder.iv_reply_profileimg);
                } else {
                    Glide.with(holder.itemView)
                            .load("http://13.209.19.188/" + item.getImg())
                            .centerCrop() //가운데를 기준으로 크기맞추기
                            .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                            .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                            .into(holder.iv_reply_profileimg);
                }

                holder.tv_reply_ID.setText(item.getID());
                holder.tv_reply_contents.setText(item.getContents());
                holder.tv_reply_date.setText(item.getDate());

                if (item.getLikenum() == 0) {
                    holder.tv_reply_likenum.setText("");
                } else {
                    holder.tv_reply_likenum.setText("좋아요 " + item.getLikenum() + "개");
                }
                holder.iv_reply_like.setSelected(item.isAmilike());
            }else{
                holder.tv_type1.setVisibility(View.VISIBLE);
                holder.tv_type1.setText(item.getContents());
                holder.iv_reply_profileimg.setVisibility(View.GONE);
                holder.tv_reply_ID.setVisibility(View.GONE);
                holder.tv_reply_date.setVisibility(View.GONE);
                holder.tv_reply_contents.setVisibility(View.GONE);
                holder.tv_reply_likenum.setVisibility(View.GONE);
                holder.iv_reply_like.setVisibility(View.GONE);
            }

            if (item.isFocus()) {
                holder.reply_layout.setBackgroundColor(Color.DKGRAY);
            } else {
                holder.reply_layout.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            CircleImageView iv_reply_profileimg;
            TextView tv_reply_ID,tv_reply_date,tv_reply_contents,tv_reply_likenum,tv_type1;
            Button iv_reply_like;
            ConstraintLayout reply_layout;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : 뷰홀더 코드를 입력하여 주세요
                iv_reply_profileimg = itemView.findViewById(R.id.iv_reply_profileimg);
                tv_reply_ID = itemView.findViewById(R.id.tv_reply_ID);
                tv_reply_date = itemView.findViewById(R.id.tv_reply_date);
                tv_reply_contents = itemView.findViewById(R.id.tv_reply_contents);
                tv_reply_likenum = itemView.findViewById(R.id.tv_reply_likenum);
                iv_reply_like = itemView.findViewById(R.id.iv_reply_like);
                tv_type1 = itemView.findViewById(R.id.tv_type1);
                reply_layout = itemView.findViewById(R.id.reply_layout);
            }
        }
    }
}