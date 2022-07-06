package com.example.drinkernote;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.Response;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;


import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;


public class FindID extends AppCompatActivity {
    TextView tv_findID_check;
    EditText et_findID_email;
    Button btn_findID_sendemail;
    boolean email_from_check = false;

//    MainHandler mainHandler;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id);
        GetViewModel();

        et_findID_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean check = check_Email_form(charSequence.toString());
                if(check){
                    tv_findID_check.setVisibility(View.GONE);
                    email_from_check = true;
                }else {
                    if(i == 0){
                        //텍스트에 아무것도 없다면
                        tv_findID_check.setText("인증하신 Email을 입력해주세요.");
                        tv_findID_check.setTextColor(Color.WHITE);
                        tv_findID_check.setVisibility(View.VISIBLE);
                    }else {
                        //텍스트에 무언가있는데 이메일형식이 아닐경우
                        tv_findID_check.setText("이메일을 확인해 주세요. ex)id@naver.com");
                        tv_findID_check.setTextColor(Color.RED);
                        tv_findID_check.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        btn_findID_sendemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if (requestQueue == null){
                    //RequestQueue 객체 생성하기
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                 }
                 String email = et_findID_email.getText().toString();
                 check_email_fromDB(email);
            }
        });
    }

    void GetViewModel(){
        tv_findID_check = findViewById(R.id.tv_findID_check);
        et_findID_email = findViewById(R.id.et_findID_email);
        btn_findID_sendemail = findViewById(R.id.btn_findID_sendemail);
    }

    private boolean check_Email_form(String Email) {
        boolean check = false;
        //이메일 패턴이 맞다면
        if (Pattern.matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$", Email)) {
            check = true;
        }
        return check;
    }

    public void check_email_fromDB(String email) {
        String url = "http://13.209.19.188/FindID.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
//                            Log.e("response", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int howmany = 0;
                            howmany = jsonObject.getInt("howmany");

                            if (howmany > 0) {//이메일에 등록된 아이디가 한개라도 있다면

                                ArrayList User_ID = new ArrayList();
                                ArrayList Join_date = new ArrayList();
                                for (int i = 0; i < howmany; i++) {

                                    JSONObject obj = jsonObject.getJSONObject(String.valueOf(i));
                                    User_ID.add(obj.getString("User_ID"));
                                    Join_date.add(obj.getString("date_format(Join_date,'%Y-%m-%d')"));

                                }
                                sendMail(email, User_ID, Join_date);
                                Toast.makeText(getApplicationContext(), "입력하신 이메일로 아이디를 보냈습니다.", Toast.LENGTH_SHORT).show();
                                Intent mIntent = new Intent(getApplicationContext(), Login.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                            } else {//이메일에 등록된 아이디가 없다면
                                Intent mIntent = new Intent(getApplicationContext(), Login.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(getApplicationContext(), "이메일에 등록된 계정이 없습니다.", Toast.LENGTH_SHORT).show();
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
        request.addStringParam("Email", email);



        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

    void sendMail(String email,ArrayList UserID, ArrayList Join_date) {

        MailTread mailTread = new MailTread(email,UserID,Join_date);
        mailTread.start();
//        mainHandler = new MainHandler();
    }
    class MailTread extends Thread{
        String Email;
        ArrayList UserID;
        ArrayList Join_date;

        public MailTread(String email,ArrayList UserID, ArrayList Join_date) {
            this.Email = email;
            this.UserID = UserID;
            this.Join_date = Join_date;
        }
        public void run(){
            GMailSender gMailSender = new GMailSender("bbeeco1218@gmail.com", "2ljjos3l!!");
            //GMailSender.sendMail(제목, 본문내용, 받는사람);
            Log.e("메일 쓰레드 ", "시작");

            try {
                String body = Email+"님이 가입한계정이 "+UserID.size()+"개 있습니다.\n";
                for (int i = 0; i < UserID.size(); i++){
                    body= body + UserID.get(i) + " (가입일 : " + Join_date.get(i) + ")\n";

                }
                Log.e("오류태그", body);
                gMailSender.sendMail("DrinkerNote 아이디 찾기", body , Email);
            } catch (SendFailedException e) {

            } catch (MessagingException e) {
                System.out.println("인터넷 문제"+e);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}