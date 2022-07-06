package com.example.drinkernote;

import com.android.volley.Request;
import com.android.volley.Response;

import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class InsertPW extends AppCompatActivity {

    TextView tv_helloUser;
    EditText et_insertPW;
    EditText et_InsertPW_check;
    TextView tv_insertPW_pwtext;
    Button btn_changePW;
    String finPW;
    String finID;
    boolean PW_from_check = false;
    boolean PW_from_doublecheck = false;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_pw);
        GetViewModel();

        Intent mIntent = getIntent();
        finID = mIntent.getStringExtra("ID");

        tv_helloUser.setText(finID + " 님의 비밀번호를 새로 설정해주세요.");

        et_insertPW.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setTextview("비밀번호를 입력해주세요.\n (8~16자, 하나이상의 문자,숫자,특수문자)", Color.WHITE, true);
                finPW = "";
                PW_from_check = false;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean check = check_PW_form(charSequence.toString());
                if (check) { //비밀번호 형식이 맞다면
                    setTextview("", Color.WHITE, false);
                    PW_from_check = true;

                } else { //비밀번호 형식이 아닐때
                    PW_from_check = false;
                    finPW = "";
                    if (i == 0) {
                        //텍스트에 아무것도 없다면
                        setTextview("비밀번호를 입력해주세요.\n (8~16자, 하나이상의 문자,숫자,특수문자)", Color.WHITE, true);

                    } else {
                        //텍스트에 무언가있는데 아이디형식이 아닐경우
                        setTextview("비밀번호를 입력해주세요.\n (8~16자, 하나이상의 문자,숫자,특수문자)", Color.RED, true);

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (PW_from_check) {
                    finPW = editable.toString();
                }
            }
        });

        et_InsertPW_check.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setTextview("입력하신 비밀번호 와 똑같이 입력해주세요.", Color.WHITE, true);
                PW_from_doublecheck = false;
//                Log.e("비번더블체크", String.valueOf(PW_from_doublecheck));
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals(finPW)) { //비밀번호가 같다면
                    setTextview("", Color.WHITE, false);
                    PW_from_doublecheck = true;
//                    Log.e("비번더블체크", String.valueOf(PW_from_doublecheck));
                } else {
                    setTextview("입력하신 비밀번호와 다릅니다. 다시 확인해주세요.", Color.RED, true);
                    PW_from_doublecheck = false;
//                    Log.e("비번더블체크", String.valueOf(PW_from_doublecheck));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        btn_changePW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                makeRequest();
            }
        });
    }

    void GetViewModel() {
        tv_helloUser = findViewById(R.id.tv_helloUser);
        et_insertPW = findViewById(R.id.et_insertPW);
        et_InsertPW_check = findViewById(R.id.et_InsertPW_check);
        tv_insertPW_pwtext = findViewById(R.id.tv_insertPW_pwtext);
        btn_changePW = findViewById(R.id.btn_changePW);
    }

    void setTextview(String settxt, int color, Boolean visible) {
        tv_insertPW_pwtext.setText(settxt);
        tv_insertPW_pwtext.setTextColor(color);
        if (visible == true) {
            tv_insertPW_pwtext.setVisibility(View.VISIBLE);
        } else {
            tv_insertPW_pwtext.setVisibility(View.GONE);
        }

    }

    private boolean check_PW_form(String PW) {
        boolean check = false;
        //이메일 패턴이 맞다면
        if (Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$", PW)) {
            check = true;
        }
        return check;
    }

    public void makeRequest() {
        String url = "http://13.209.19.188/InsertPW.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.d("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                //비밀번호를 성공적으로 업데이트함
                                Intent mIntent = new Intent(getApplicationContext(), MainUI.class);
                                mIntent.putExtra("ID", finID);
                                AutoLogin.setUserId(getApplicationContext(), finID);
                                Log.e("오토로그인", "아이디 저장함");
                                Toast.makeText(getApplicationContext(), "비밀번호 변경에 성공 했습니다.", Toast.LENGTH_SHORT).show();
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                            } else {
                                //비밀번호를 업데이트 하지못함
                                Toast.makeText(getApplicationContext(), "비밀번호 변경에 실패 했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                                Intent mIntent = new Intent(getApplicationContext(), Login.class);
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
        request.addStringParam("ID", finID);
        request.addStringParam("PW", finPW);


        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }
}