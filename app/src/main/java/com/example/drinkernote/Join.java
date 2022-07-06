package com.example.drinkernote;

import com.android.volley.Request;
import com.android.volley.Response;

import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;

import java.util.HashMap;
import java.util.Map;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;



public class Join extends AppCompatActivity {
    EditText et_Join_Email;


    TextView tv_emailcheck;
    Button btn_SendEmail;


    boolean email_from_check = false;

    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.


    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        GetViewModel();

        et_Join_Email.requestFocus();
        //이메일 텍스트가 바뀔때마다 확인하는 메서드
        et_Join_Email.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tv_emailcheck.setText("가입하실 Email을 입력해주세요.");
                tv_emailcheck.setTextColor(Color.WHITE);
                tv_emailcheck.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                boolean check = check_Email_form(charSequence.toString());

                if (check) { //이메일 형식이 맞다면
                    tv_emailcheck.setVisibility(View.GONE);
                    email_from_check = true;
                } else { //이메일형식이 아닐때
                    if (i == 0) {
                        //텍스트에 아무것도 없다면
                        email_from_check = false;
                        tv_emailcheck.setText("가입하실 Email을 입력해주세요.");
                        tv_emailcheck.setTextColor(Color.WHITE);
                        tv_emailcheck.setVisibility(View.VISIBLE);
                    } else {
                        email_from_check = false;
                        //텍스트에 무언가있는데 이메일형식이 아닐경우
                        tv_emailcheck.setText("이메일을 확인해 주세요. ex)id@naver.com");
                        tv_emailcheck.setTextColor(Color.RED);
                        tv_emailcheck.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        //이메일 인증버튼클릭시
        btn_SendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = et_Join_Email.getText().toString(); //이메일을 가져온다.
                if (email_from_check == false) { //이메일 형식이 아닐때
                    Toast.makeText(Join.this, "이메일형식을 다시 확인해 주세요.", Toast.LENGTH_SHORT).show();
                } else {

                    if (requestQueue == null) {
                        //RequestQueue 객체 생성하기
                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                    }
                    makeRequest(email);

                }
            }
        });


    }


    void GetViewModel() {
        et_Join_Email = findViewById(R.id.et_Join_Email);

        btn_SendEmail = findViewById(R.id.btn_SendEmail);

        tv_emailcheck = findViewById(R.id.tv_emailcheck);


    }

    private boolean check_Email_form(String Email) {
        boolean check = false;
        //이메일 패턴이 맞다면
        if (Pattern.matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$", Email)) {
            check = true;
        }
        return check;
    }


    public void makeRequest(String email) {
        String url = "http://13.209.19.188/EmailCheck.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
//                        Log.d("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int res = jsonObject.getInt("response");
                            if (res == 0) {
                                Intent mIntent = new Intent(getApplicationContext(), Join_EmailCode.class);
                                mIntent.putExtra("Email", email);
                                startActivity(mIntent);
                            } else if (res > 1 && res <= 5) {
                                AlertDialog.Builder oDialog = new AlertDialog.Builder(Join.this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                                oDialog.setMessage("해당 이메일로 가입된 계정이 있습니다. 추가로 가입하시겠습니까?")
                                        .setTitle("회원가입")
                                        .setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .setNeutralButton("예", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent mIntent = new Intent(getApplicationContext(), Join_EmailCode.class);
                                                mIntent.putExtra("Email", email);
                                                startActivity(mIntent);
                                            }
                                        })
                                        .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                                        .show();
                            } else {
                                AlertDialog.Builder oDialog = new AlertDialog.Builder(Join.this, android.R.style.Theme_DeviceDefault_Light_Dialog);
                                oDialog.setMessage("해당 이메일은 계정을 더이상 만들수 없습니다.")
                                        .setTitle("회원가입")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent mIntent = new Intent(getApplicationContext(), Login.class);
                                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(mIntent);
                                            }
                                        })
                                        .setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.
                                        .show();
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
        request.addStringParam("Email", email);


        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }


}

