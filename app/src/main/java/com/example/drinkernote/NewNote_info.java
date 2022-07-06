package com.example.drinkernote;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import android.widget.Toast;
import android.net.Uri;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class NewNote_info extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;
    EditText et_whiskyname,et_whiskylabel,et_whiskycask,et_whiskyproof,et_whiskyprice;

    DecimalFormat df = new DecimalFormat("###,###");
    String result="";
    ArrayList <Uri> img_list;
    String from;
    int NoteKey;
    WhiskyData whiskyData;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.

    //액션바 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_note_toolbar_menu, menu) ;

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
            case R.id.toolbar_menu_next:{ //toolbar의 다음 눌렀을 때 동작
                String whisky_name = et_whiskyname.getText().toString();
                if(whisky_name.equals("")){
                    et_whiskyname.requestFocus();
                    Toast.makeText(this, "이름을 입력 해주세요.", Toast.LENGTH_SHORT).show();
                }else{
                    String whisky_label,whisky_cask,whisky_proof,whisky_price;
                    whisky_label= et_whiskylabel.getText().toString();
                    whisky_cask= et_whiskycask.getText().toString();
                    whisky_proof = et_whiskyproof.getText().toString();
                    whisky_price =et_whiskyprice.getText().toString();
                    Intent mIntent = new Intent(getApplicationContext(), NewNote_taste.class);
                    mIntent.putExtra("whisky_name",whisky_name);
                    mIntent.putExtra("whisky_label",whisky_label);
                    mIntent.putExtra("whisky_cask",whisky_cask);
                    mIntent.putExtra("whisky_proof",whisky_proof);
                    mIntent.putExtra("whisky_price",whisky_price);
                    mIntent.putExtra("img_list",img_list);
                    if(from != null) {
                        if (from.equals("Update")) {
                            mIntent.putExtra("from", from);
                            mIntent.putExtra("NoteKey", NoteKey);
                            mIntent.putExtra("Notedata", whiskyData);
                        }
                    }
                    startActivity(mIntent);
                }
                return true;
            }

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note_info);
        setToolbar();
        GetViewModel();
        Intent mIntent = getIntent();
        img_list = mIntent.getParcelableArrayListExtra("img_list");
        from = mIntent.getStringExtra("from");
        if(from != null) {
            if (from.equals("Update")) {
                NoteKey = mIntent.getIntExtra("NoteKey", 0);

                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기
                    requestQueue = Volley.newRequestQueue(getApplicationContext());


                }
                GetNoteInfo(NoteKey);

            } else {
                from = "";
            }
        }

        et_whiskyprice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().equals(result)){     // StackOverflow를 막기위해,
                    if(!charSequence.toString().equals("")) {
                        result = df.format(Long.parseLong(charSequence.toString().replaceAll(",", "")));   // 에딧텍스트의 값을 변환하여, result에 저장.
                        et_whiskyprice.setText(result);    // 결과 텍스트 셋팅.
                        et_whiskyprice.setSelection(result.length());     // 커서를 제일 끝으로 보냄.
                    }else{
                        result = "";
                        et_whiskyprice.setText(result);    // 결과 텍스트 셋팅.
                        et_whiskyprice.setSelection(result.length());     // 커서를 제일 끝으로 보냄.
                    }
                }



            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }



    void setToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다
        actionBar.setDisplayHomeAsUpEnabled(true); //뒤로가기
    }

    void GetViewModel(){
        et_whiskyname = findViewById(R.id.et_whiskyname);
        et_whiskylabel = findViewById(R.id.et_whiskylabel);
        et_whiskycask = findViewById(R.id.et_whiskycask);
        et_whiskyproof = findViewById(R.id.et_whiskyproof);
        et_whiskyprice = findViewById(R.id.et_whiskyprice);

    }

    public void GetNoteInfo(int NoteKey) {
        String url = "http://13.209.19.188/GetNoteInfo.php?key="+NoteKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String NoteObj = jsonObject.getString("Note_obj");
                            Gson gson = new Gson();
                            whiskyData = gson.fromJson(NoteObj,WhiskyData.class);

                            et_whiskyname.setText(whiskyData.getWhisky_name());
                            et_whiskylabel.setText(whiskyData.getWhisky_label());
                            et_whiskycask.setText(whiskyData.getWhisky_cask());
                            et_whiskyproof.setText(whiskyData.getWhisky_proof());
                            et_whiskyprice.setText(whiskyData.getWhisky_price());


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
}