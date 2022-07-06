package com.example.drinkernote;
import android.content.Intent;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.util.Log;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class option extends AppCompatActivity {

    Toolbar toolbar;
    ActionBar actionBar;
    Button btn_changeProfileIMG,btn_changePW,btn_optionAlam;
    CircleImageView iv_option_profileimg;
    TextView tv_profileID;

    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    String myID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        setToolbar();

        btn_changeProfileIMG = findViewById(R.id.btn_changeProfileIMG);
        btn_changePW = findViewById(R.id.btn_changePW);
        btn_optionAlam = findViewById(R.id.btn_optionAlam);
        iv_option_profileimg = findViewById(R.id.iv_option_profileimg);
        tv_profileID = findViewById(R.id.tv_profileID);
        myID = AutoLogin.getUserId(this);
        tv_profileID.setText(myID);
        checkrequestrQueue();
        getprofileImg(myID);

        btn_optionAlam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getApplicationContext(), optionalam.class);
                startActivity(mIntent);
            }
        });


    }


    void checkrequestrQueue() {
        if (requestQueue == null) {
            //RequestQueue 객체 생성하기

            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    public void getprofileImg(String targetID) {
        String url = "http://13.209.19.188/GetProfileIMG.php?id="+targetID;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.d("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String profileImg = jsonObject.getString("profileImg");
                            if(profileImg.equals("Nothing")) {
                                Glide.with(iv_option_profileimg)
                                        .load(R.drawable.myprofile)
                                        .placeholder(R.drawable.dataloading)
                                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                                        .centerCrop() //가운데를 기준으로 크기맞추기
                                        .into(iv_option_profileimg);
                            }else {
                                Glide.with(iv_option_profileimg)
                                        .load("http://13.209.19.188/"+profileImg)
                                        .centerCrop() //가운데를 기준으로 크기맞추기
                                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                                        .into(iv_option_profileimg);
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
}