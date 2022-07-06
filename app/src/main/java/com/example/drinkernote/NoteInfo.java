package com.example.drinkernote;

import android.content.DialogInterface;
import android.widget.Button;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NoteInfo extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;

    TextView tv_OpenClose,tv_MakerID,tv_price;
    ConstraintLayout constraint_OPENCLOSE;
    ConstraintLayout cl_in_scroll;
    TextView tv_coment,tv_Notename,tv_contents,tv_proof,tv_likeNote;
    ImageView iv_profileimg;
    RatingBar rb_BODY,rb_SWEET,rb_SPICE,rb_MALTY,rb_FRUIT,rb_TANNIC,rb_FLORAL;
    Button iv_like;
    LinearLayout layoutIndicator;
    ViewPager2 sliderViewPager;
    String NoteMaker;
    int NoteKey;

    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.

    @Override
    protected void onStart() {
        if (requestQueue == null) {
            //RequestQueue 객체 생성하기

            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }


        String userId = AutoLogin.getUserId(this);
        getNoteInfo(NoteKey,userId);
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_info);

        layoutIndicator = findViewById(R.id.layoutIndicators);
        sliderViewPager = findViewById(R.id.IV_imgPager);

        rb_BODY = findViewById(R.id.rb_feed_SWEET);
        rb_SWEET = findViewById(R.id.rb_SWEET);
        rb_SPICE = findViewById(R.id.rb_SPICE);
        rb_MALTY = findViewById(R.id.rb_MALTY);
        rb_FRUIT = findViewById(R.id.rb_FRUIT);
        rb_TANNIC = findViewById(R.id.rb_TANNIC);
        rb_FLORAL = findViewById(R.id.rb_FLORAL);


        tv_coment = findViewById(R.id.tv_coment);
        tv_price = findViewById(R.id.tv_price);
        tv_MakerID = findViewById(R.id.tv_MakerID);
        tv_Notename = findViewById(R.id.tv_Notename);
        tv_contents = findViewById(R.id.tv_contents);
        tv_proof = findViewById(R.id.tv_proof);
        iv_profileimg = findViewById(R.id.iv_feed_profileimg);
        tv_likeNote = findViewById(R.id.tv_likeNote);
        cl_in_scroll = findViewById(R.id.cl_in_scroll);
        constraint_OPENCLOSE = findViewById(R.id.constraint_OPENCLOSE);
        iv_like = findViewById(R.id.iv_coment_like);
        tv_OpenClose = findViewById(R.id.tv_OpenClose);
        Intent mIntent = getIntent();
        NoteKey = mIntent.getIntExtra("NoteKey", 0);
        Log.e("노트인포", String.valueOf(NoteKey));


        tv_coment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getApplicationContext(), Coment.class);
                mIntent.putExtra("NoteKey",NoteKey);
                mIntent.putExtra("NoteMaker",NoteMaker);
                startActivity(mIntent);
            }
        });


        tv_OpenClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_OpenClose.getText().toString().equals("접기")) {
                    tv_OpenClose.setText("펼치기");
                    constraint_OPENCLOSE.setVisibility(View.GONE);
                    //컨스트레인트 레이아웃에서 뷰를 지정
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) tv_coment.getLayoutParams();
                    //변경할 마진 선택
                    layoutParams.topMargin = 110;
                    //뷰안에 넣기
                    tv_coment.setLayoutParams(layoutParams);


                } else {
                    tv_OpenClose.setText("접기");
                    constraint_OPENCLOSE.setVisibility(View.VISIBLE);
                    //컨스트레인트 레이아웃에서 뷰를 지정
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) tv_coment.getLayoutParams();
                    //변경할 마진 선택
                    layoutParams.topMargin = 680;
                    //뷰안에 넣기
                    tv_coment.setLayoutParams(layoutParams);

                }
            }
        });

        setToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.noteinfo_menu, menu) ;

        return true;

    }

    //액션바 아이템 클릭시
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
            case R.id.UpdateNote :{
                Log.e("수정버튼", ">??>");
                Intent mIntent = new Intent(getApplicationContext(), NewNote.class);
                mIntent.putExtra("from","Update");
                mIntent.putExtra("NoteKey",NoteKey);
                startActivity(mIntent);
                return true;
            }
            case R.id.DeleteNote : {
                // 다이얼로그 바디
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
                // 메세지
                alert_confirm.setMessage("게시글을 삭제 하시겠습니까?");
                // 확인 버튼 리스너
                alert_confirm.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (requestQueue == null) {
                            //RequestQueue 객체 생성하
                            requestQueue = Volley.newRequestQueue(getApplicationContext());
                        }
                        deleteNote(NoteKey);
                    }
                });
                alert_confirm.setNegativeButton("아니오",null);
                // 다이얼로그 생성
                AlertDialog alert = alert_confirm.create();
                // 다이얼로그 보기
                alert.show();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
                    unLike(NoteKey,userId);

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
                        Like(NoteKey,userId,NoteMaker,jsonMSG);
                        sendNewsToChatServer(likenews);


                    }else{
                        Like(NoteKey,userId,NoteMaker,"nonews");
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
        rb_BODY.setRating(whiskyData.getBody());
        rb_SWEET.setRating(whiskyData.getSweet());
        rb_SPICE.setRating(whiskyData.getSpice());
        rb_MALTY.setRating(whiskyData.getMalty());
        rb_FRUIT.setRating(whiskyData.getFruit());
        rb_TANNIC.setRating(whiskyData.getTannic());
        rb_FLORAL.setRating(whiskyData.getFloral());
        if(coment_num > 0){
            tv_coment.setText("댓글 "+coment_num+"개 모두보기");
        }else{
            tv_coment.setText("댓글이 없습니다. 댓글쓰기");
        }



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

    public void deleteNote(int NoteKey) {
        String url = "http://13.209.19.188/DeleteNote.php?NoteKey="+NoteKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("노트 삭제", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "노트를 성공적으로 삭제했습니다.", Toast.LENGTH_SHORT).show();
                                Intent mIntent = new Intent(getApplicationContext(), MainUI.class);
                                mIntent.putExtra("from","NewNote");
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);

                            } else {
                                Toast.makeText(getApplicationContext(), "노트를 삭제하는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                Intent mIntent = new Intent(getApplicationContext(), MainUI.class);
                                mIntent.putExtra("from","NewNote");
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
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

    public void Like(int NoteKey,String myID,String Maker,String newsjson) {
        String url = "http://13.209.19.188/LikeNote.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {


                        Log.e("좋아요", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "해당 노트를 좋아합니다.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                tv_likeNote.setText(Like_num + " 명이 좋아합니다.");
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
        request.addStringParam("Note_key", String.valueOf(NoteKey)); //POST파라미터 넣기
        request.addStringParam("myID", myID); //POST파라미터 넣기
        request.addStringParam("Maker", Maker); //POST파라미터 넣기
        request.addStringParam("NewsOBJ", newsjson); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }
    public void unLike(int NoteKey,String myID) {
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
                                Toast.makeText(getApplicationContext(), "해당 노트를 더이상 좋아하지 않습니다.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                tv_likeNote.setText(Like_num + " 명이 좋아합니다.");
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
        request.addStringParam("Note_key", String.valueOf(NoteKey)); //POST파라미터 넣기
        request.addStringParam("myID", myID); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

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
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_slider, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

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
}