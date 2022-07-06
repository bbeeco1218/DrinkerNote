package com.example.drinkernote;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;

import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;

import com.android.volley.toolbox.Volley;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;

public class FindPW extends AppCompatActivity {
    EditText et_Find_Email;
    Button btn_find_sendmail;
    EditText et_Find_ID;
    EditText et_Find_code;
    Button btn_find_config;
    TextView tv_find_codetime;
    TextView tv_find_idcheck;
    TextView tv_findID;
    TextView tv_find_emailcheck;

    String GmailCode;
    String finID;
    int mailSend=0;
    static int value;
    MainHandler mainHandler;

    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pw);
        GetViewModel();


        //아이디 찾기 클릭
        tv_findID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getApplicationContext(), FindID.class);
                startActivity(mIntent);
            }
        });

        //이메일 보내기 클릭
        btn_find_sendmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = et_Find_ID.getText().toString();
                String email = et_Find_Email.getText().toString();
                boolean checkemail = check_Email_form(email);
                if(checkemail){
                    tv_find_emailcheck.setText("");
                    tv_find_emailcheck.setVisibility(View.VISIBLE);
                }else{
                    tv_find_emailcheck.setTextColor(Color.RED);
                    tv_find_emailcheck.setText("이메일을 확인해 주세요. ex)id@naver.com");
                }

                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기
                    requestQueue = Volley.newRequestQueue(getApplicationContext());

                }
                Check_ID_From_db(id,email);

            }
        });


        btn_find_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("인증 코드", GmailCode);
                if (GmailCode == "time out") { //시간이 다돼서 인증코드가 바뀌었을때
                    Toast.makeText(getApplicationContext(), "인증시간이 다되었습니다. 다시 인증번호를 전송해주세요.", Toast.LENGTH_SHORT).show();
                }
                //이메일로 전송한 인증코드와 내가 입력한 인증코드가 같을 때
                else if (et_Find_code.getText().toString().equals(GmailCode)) {
                    Intent mIntent = new Intent(getApplicationContext(), InsertPW.class);
                    Toast.makeText(getApplicationContext(), "인증에 성공했습니다.", Toast.LENGTH_SHORT).show();
                    mIntent.putExtra("ID", finID);
                    startActivity(mIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "인증번호를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    void GetViewModel(){
        et_Find_Email = findViewById(R.id.et_Find_Email);
        btn_find_sendmail = findViewById(R.id.btn_find_sendmail);
        et_Find_ID = findViewById(R.id.et_Find_ID);
        et_Find_code = findViewById(R.id.et_Find_code);
        btn_find_config = findViewById(R.id.btn_find_config);
        tv_find_codetime = findViewById(R.id.tv_find_codetime);
        tv_find_idcheck = findViewById(R.id.tv_find_idcheck);
        tv_findID = findViewById(R.id.tv_findID);
        tv_find_emailcheck = findViewById(R.id.tv_find_emailcheck);

    }

    public void Check_ID_From_db(String ID,String email){
        String url = "http://13.209.19.188/IDcheck.php?ID="+ID;
//        Log.e("url :", url);
        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
//                        Log.e("리스폰스 ", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                //중복되는 아이디가 없음
                                tv_find_idcheck.setText("아이디가 존재하지 않습니다. ");
                                tv_find_idcheck.setTextColor(Color.RED);
                                tv_find_idcheck.setVisibility(View.VISIBLE);
                            } else {
                                //중복되는 아이디가 있음
                                tv_find_idcheck.setText("");
                                tv_find_idcheck.setTextColor(Color.WHITE);
                                tv_find_idcheck.setVisibility(View.GONE);
                                boolean checkemail = check_Email_form(email);
                                if(checkemail){
                                    sendMail(email);
                                    Toast.makeText(getApplicationContext(), "이메일을 보냈습니다. 인증코드를 확인해주세요.", Toast.LENGTH_SHORT).show();
                                    btn_find_config.setVisibility(View.VISIBLE);
                                    tv_find_codetime.setVisibility(View.VISIBLE);
                                    et_Find_code.requestFocus();
                                    tv_find_emailcheck.setText("");
                                    tv_find_emailcheck.setVisibility(View.VISIBLE);
                                    finID = ID;
                                }else{
                                    tv_find_emailcheck.setTextColor(Color.RED);
                                    tv_find_emailcheck.setText("이메일을 확인해 주세요. ex)id@naver.com");
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override //에러시 처리할 내용
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "에러-> "+error.getMessage());
                    }
                });


        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

    private boolean check_Email_form(String Email) {
        boolean check = false;
        //이메일 패턴이 맞다면
        if (Pattern.matches("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$", Email)) {
            check = true;
        }
        return check;
    }
    void sendMail(String email) {

        MailTread mailTread = new MailTread(email);
        mailTread.start();
        if (mailSend == 0) {
            value = 180;
            //쓰레드 객체 생성
            BackgrounThread backgroundThread = new BackgrounThread();
            //쓰레드 스타트
            backgroundThread.start();
            mailSend += 1;
        } else {
            value = 180;
        }
        mainHandler = new MainHandler();
    }

    class MailTread extends Thread{
        String Email;
        public MailTread(String email) {
            this.Email = email;
        }
        public void run(){
            GMailSender gMailSender = new GMailSender("bbeeco1218@gmail.com", "2ljjos3l!!");
            //GMailSender.sendMail(제목, 본문내용, 받는사람);


            //인증코드
            GmailCode=gMailSender.getEmailCode();
            String body = "인증코드 : \"" + GmailCode + "\" 를 화면에 입력해주세요.";
            try {
                gMailSender.sendMail("DrinkerNote 비밀번호 찾기 이메일 인증", body , Email);
            } catch (SendFailedException e) {

            } catch (MessagingException e) {
                System.out.println("인터넷 문제"+e);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class BackgrounThread extends Thread{
        //180초는 3분
        //메인 쓰레드에 value를 전달하여 시간초가 카운트다운 되게 한다.

        public void run(){
            //180초 보다 밸류값이 작거나 같으면 계속 실행시켜라
            while(true){
                value-=1;
                try{
                    Thread.sleep(1000);
                }catch (Exception e){

                }

                Message message = mainHandler.obtainMessage();
                //메세지는 번들의 객체 담아서 메인 핸들러에 전달한다.
                Bundle bundle = new Bundle();
                bundle.putInt("value", value);
                message.setData(bundle);

                //핸들러에 메세지 객체 보내기기

                mainHandler.sendMessage(message);

                if(value<=0){
                    GmailCode="time out";
                    btn_find_config.setVisibility(View.GONE);

                    break;
                }
            }



        }
    }
    class MainHandler extends Handler {
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            int min, sec;

            Bundle bundle = message.getData();
            int value = bundle.getInt("value");

            min = value/60;
            sec = value % 60;
            //초가 10보다 작으면 앞에 0이 더 붙어서 나오도록한다.
            if(sec<10){
                //텍스트뷰에 시간초가 카운팅
                tv_find_codetime.setText("유효시간 "+"0"+min+" : 0"+sec);
            }else {
                tv_find_codetime.setText("유효시간 "+"0"+min+" : "+sec);
            }
        }
    }

}