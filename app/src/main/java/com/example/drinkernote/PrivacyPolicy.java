package com.example.drinkernote;


import android.view.View;
import android.widget.Toast;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;




import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;


import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PrivacyPolicy extends AppCompatActivity {
    String finEmail, finID, finPW;
    CheckBox checkBox_All, checkBox_1, checkBox_2, checkBox_3;
    Button btn_joinfin, btn_SeePolicy;

    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        Intent mIntent = getIntent();
        finEmail = mIntent.getStringExtra("Email");
        finID = mIntent.getStringExtra("ID");
        finPW = mIntent.getStringExtra("PW");
        GetViewModel();


        checkBox_All.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkBox_All.isChecked()) { //전체동의가 체크되어있다면
                    checkBox_1.setChecked(true);
                    checkBox_2.setChecked(true);
                    checkBox_3.setChecked(true);
                } else {
                    checkBox_1.setChecked(false);
                    checkBox_2.setChecked(false);
                    checkBox_3.setChecked(false);
                }
            }
        });
        checkBox_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox_1.isChecked()) {
                    if (checkBox_2.isChecked() && checkBox_3.isChecked()) {
                        checkBox_All.setChecked(true);
                    } else {
                        checkBox_All.setChecked(false);
                    }
                } else {
                    checkBox_All.setChecked(false);
                }
            }
        });
        checkBox_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox_2.isChecked()) {
                    if (checkBox_1.isChecked() && checkBox_3.isChecked()) {
                        checkBox_All.setChecked(true);
                    } else {
                        checkBox_All.setChecked(false);
                    }
                } else {
                    checkBox_All.setChecked(false);
                }
            }
        });
        checkBox_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox_3.isChecked()) {
                    if (checkBox_1.isChecked() && checkBox_2.isChecked()) {
                        checkBox_All.setChecked(true);
                    } else {
                        checkBox_All.setChecked(false);
                    }
                } else {
                    checkBox_All.setChecked(false);
                }
            }
        });

        btn_joinfin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox_1.isChecked() && checkBox_2.isChecked() && checkBox_3.isChecked()) {
                    if (requestQueue == null) {
                        //RequestQueue 객체 생성하기
                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                    }
                    InsertUserToDb(finEmail, finID, finPW);
                } else {
                    Toast.makeText(getApplicationContext(), "양식을 모두 체크해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    void GetViewModel() {
        checkBox_All = findViewById(R.id.checkBox_All);
        checkBox_1 = findViewById(R.id.checkBox_1);
        checkBox_2 = findViewById(R.id.checkBox_2);
        checkBox_3 = findViewById(R.id.checkBox_3);
        btn_joinfin = findViewById(R.id.btn_joinfin);
        btn_SeePolicy = findViewById(R.id.btn_SeePolicy);

    }

    public void InsertUserToDb(String Email, String ID, String PW) {
        String url = "http://13.209.19.188/InsertUser.php";
        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
//                        Log.e("리스폰스 ", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {

                                //회원가입 완료
                                Intent mIntent = new Intent(getApplicationContext(), MainUI.class);
                                mIntent.putExtra("ID", ID);
                                AutoLogin.setUserId(getApplicationContext(), ID);
                                Log.e("오토로그인", "아이디 저장함");
                                Toast.makeText(getApplicationContext(), "회원가입에 성공 했습니다.", Toast.LENGTH_SHORT).show();
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                            } else {
                                //회원가입 실패
                                Toast.makeText(getApplicationContext(), "회원가입에 실패 했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
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
        request.addStringParam("Email", Email);
        request.addStringParam("ID", ID);
        request.addStringParam("PW", PW);

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }
}