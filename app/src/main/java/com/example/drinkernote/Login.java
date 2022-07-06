package com.example.drinkernote;

import android.app.NotificationManager;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


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

public class Login extends AppCompatActivity {

    Button btn_join;
    Button btn_login;
    EditText et_ID;
    EditText et_pw;
    TextView tv_FindPW;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    private Intent serviceIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent mIntent = getIntent();
        boolean restartService = mIntent.getBooleanExtra("restartService",false);

        if(restartService){ //다시시작된 서비스라면
            String userId = AutoLogin.getUserId(this);

            if (userId.length() != 0){
//                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                int RoomNum = mIntent.getIntExtra("RoomNum",-1);
//                notificationManager.cancel(RoomNum);
//                Log.e("restartServicelogin 로그인아이디 ", userId);
                // 쉐어드에 자동 로그인 정보 있으면 실행될 코드

                String targetID = mIntent.getStringExtra("targetID");

                Intent mmIntent = new Intent(this,MainUI.class);
                mmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mmIntent.putExtra("RoomNum",RoomNum);
                mmIntent.putExtra("targetID",targetID);
                mmIntent.putExtra("restartService",true);
                mmIntent.putExtra("ID",userId);

                startActivity(mmIntent);
            }
        }else {
            if (AutoLogin.getUserId(this).length() != 0) {
                // 쉐어드에 자동 로그인 정보 있으면 실행될 코드
                Intent gotomainIntent = new Intent(getApplicationContext(), MainUI.class);
                String userId = AutoLogin.getUserId(this);
                gotomainIntent.putExtra("ID", userId);
                gotomainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                setStartService();
                startActivity(gotomainIntent);
            }
        }

        GetViewModel();

        //회원가입 클릭했을때
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_ID.setText("");
                et_pw.setText("");
                Intent intent = new Intent(Login.this, Join.class);
                startActivity(intent);
            }
        });

        //로그인 버튼 클릭 리스너
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ID = et_ID.getText().toString();
                String PW = et_pw.getText().toString();
                if (ID.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else if (PW.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    if (requestQueue == null) {
                        //RequestQueue 객체 생성하기
                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                    }
                    check_ID_PW_From_db(ID, PW);
                }
            }
        });

        tv_FindPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getApplicationContext(), FindPW.class);
                startActivity(mIntent);
            }
        });
    }

    void GetViewModel() {
        btn_join = findViewById(R.id.btn_join);
        btn_login = findViewById(R.id.btn_login);
        et_ID = findViewById(R.id.et_ID);
        et_pw = findViewById(R.id.et_pw);
        tv_FindPW = findViewById(R.id.tv_FindPW);
    }


    public void check_ID_PW_From_db(String ID, String PW) {
        String url = "http://13.209.19.188/Login.php";
//        Log.e("url :", url);
        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
//                        Log.e("리스폰스 ", response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Intent mIntent = new Intent(getApplicationContext(), MainUI.class);
                                AutoLogin.setUserId(getApplicationContext(), ID);
                                mIntent.putExtra("ID", ID);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                setStartService();

                                startActivity(mIntent);
                            } else if (res.equals("IDfalse")) {
                                Toast.makeText(getApplicationContext(), "아이디가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();

                            } else if (res.equals("PWfalse")) {
                                Toast.makeText(getApplicationContext(), "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
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
        request.addStringParam("ID", ID);
        request.addStringParam("PW", PW);


        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");
    }


    private void setStartService() {
        if (ChatService.serviceIntent==null) {
            serviceIntent = new Intent(this, ChatService.class);
            startService(serviceIntent);
        } else {
            serviceIntent = ChatService.serviceIntent;//getInstance().getApplication();
            Toast.makeText(getApplicationContext(), "already", Toast.LENGTH_LONG).show();
        }
//        startService(new Intent(Login.this, ChatService.class));
    }
}