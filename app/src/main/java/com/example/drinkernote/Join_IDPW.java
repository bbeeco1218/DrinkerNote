package com.example.drinkernote;
import android.content.Intent;
import com.android.volley.RequestQueue;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;


import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class Join_IDPW extends AppCompatActivity {
    EditText et_Join_ID;
    EditText et_Join_PW;
    EditText et_Join_PW_check;
    Button btn_CheckID;
    TextView tv_IDcheck;
    TextView tv_PWcheck;
    Button btn_next;
    boolean ID_from_check = false;
    boolean ID_duplication_check = false;
    boolean PW_from_check = false;
    boolean PW_from_doublecheck = false;
    String finID, finPW;
    String finEmail;


    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_idpw);
        GetViewModel();
        Intent mIntent = getIntent();
        finEmail = mIntent.getStringExtra("Email");
        if (finEmail == null) finEmail = "";
        Log.e("오류태그", finEmail);
        //아이디 입력창이 바뀔때마다 양식 확인
        et_Join_ID.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                set_tv_IDcheck("가입하실 ID를 입력해주세요.\n(영문 4자 이상,10자 이하,\"_\"가능)", Color.WHITE, true);
                finID = "";
                ID_from_check = false;
                ID_duplication_check = false;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                boolean check = check_ID_form(charSequence.toString());

                if (check) { //아이디 형식이 맞다면
                    set_tv_IDcheck("", Color.WHITE, false);
                    ID_from_check = true;
                } else { //아이디형식이 아닐때
                    ID_duplication_check = false;
                    ID_from_check = false;
                    finID = "";
                    if (i == 0) {
                        //텍스트에 아무것도 없다면
                        set_tv_IDcheck("가입하실 ID를 입력해주세요.\n(영문 4자 이상,10자 이하,\"_\"가능)", Color.WHITE, true);

                    } else {
                        //텍스트에 무언가있는데 아이디형식이 아닐경우
                        set_tv_IDcheck("가입하실 ID를 입력해주세요.\n(영문 4자 이상,10자 이하,\"_\"가능)", Color.RED, true);

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //아이디 중복체크버튼 클릭리스너
        btn_CheckID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ID_from_check == false) { //양식 체크가 안되었다면
                    et_Join_ID.requestFocus();
                } else {//아이디 양식체크가 되었다면
                    //서버에 통신해야함
                    if (requestQueue == null) {
                        //RequestQueue 객체 생성하기
                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                    }
                    String id = et_Join_ID.getText().toString();
                    Check_ID_From_db(id);

                }
            }
        });

        //비밀번호 입력창이 바뀔때마다 양식 확인
        et_Join_PW.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                set_tv_PWcheck("비밀번호를 입력해주세요.\n (8~16자, 하나이상의 문자,숫자,특수문자)", Color.WHITE, true);
                finPW = "";
                PW_from_check = false;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean check = check_PW_form(charSequence.toString());
                if (check) { //비밀번호 형식이 맞다면
                    set_tv_PWcheck("", Color.WHITE, false);
                    PW_from_check = true;

                } else { //비밀번호 형식이 아닐때
                    PW_from_check = false;
                    finPW = "";
                    if (i == 0) {
                        //텍스트에 아무것도 없다면
                        set_tv_PWcheck("비밀번호를 입력해주세요.\n (8~16자, 하나이상의 문자,숫자,특수문자)", Color.WHITE, true);

                    } else {
                        //텍스트에 무언가있는데 아이디형식이 아닐경우
                        set_tv_PWcheck("비밀번호를 입력해주세요.\n (8~16자, 하나이상의 문자,숫자,특수문자)", Color.RED, true);

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

        //비밀번호 확인 입력창이 바뀔때마다 확인
        et_Join_PW_check.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                set_tv_PWcheck("입력하신 비밀번호 와 똑같이 입력해주세요.", Color.WHITE, true);
                PW_from_doublecheck = false;
//                Log.e("비번더블체크", String.valueOf(PW_from_doublecheck));
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals(finPW)) { //비밀번호가 같다면
                    set_tv_PWcheck("", Color.WHITE, false);
                    PW_from_doublecheck = true;
//                    Log.e("비번더블체크", String.valueOf(PW_from_doublecheck));
                } else {
                    set_tv_PWcheck("입력하신 비밀번호와 다릅니다. 다시 확인해주세요.", Color.RED, true);
                    PW_from_doublecheck = false;
//                    Log.e("비번더블체크", String.valueOf(PW_from_doublecheck));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //다음 버튼 클릭리스너
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //체크항목이 전부 트루 라면 다음 으로 넘어간다.
                if (ID_from_check && ID_duplication_check && PW_from_check && PW_from_doublecheck) {
                    Intent mIntent = new Intent(getApplicationContext(), PrivacyPolicy.class);
                    mIntent.putExtra("Email", finEmail);
                    mIntent.putExtra("ID", finID);
                    mIntent.putExtra("PW", finPW);


                    startActivity(mIntent);
                } else {
                    if (!ID_from_check) {
                        Toast.makeText(getApplicationContext(), "아이디를 확인해주세요.", Toast.LENGTH_SHORT).show();
                        et_Join_ID.requestFocus();
                    } else if (!ID_duplication_check) {
                        Toast.makeText(getApplicationContext(), "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show();
                        et_Join_ID.requestFocus();
                    } else if (!PW_from_check) {
                        et_Join_PW.requestFocus();
                        Toast.makeText(getApplicationContext(), "비밀번호를 확인 해주세요.", Toast.LENGTH_SHORT).show();
                    } else if (!PW_from_doublecheck) {
                        et_Join_PW_check.requestFocus();
                        Toast.makeText(getApplicationContext(), "같은 비밀번호를 입력 해주세요.", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    //뷰모델 가져오는 메서드
    void GetViewModel() {
        et_Join_ID = findViewById(R.id.et_Join_ID);
        et_Join_PW = findViewById(R.id.et_Join_PW);
        et_Join_PW_check = findViewById(R.id.et_Join_PW_check);
        btn_CheckID = findViewById(R.id.btn_CheckID);
        tv_IDcheck = findViewById(R.id.tv_IDcheck);
        tv_PWcheck = findViewById(R.id.tv_PWcheck);
        btn_next = findViewById(R.id.btn_next);
    }

    //아이디 정규식 확인하는 메서드
    private boolean check_ID_form(String ID) {
        boolean check = false;
        //이메일 패턴이 맞다면
        if (Pattern.matches("^[a-zA-Z0-9_]{4,10}$", ID)) {
            check = true;
        }
        return check;
    }

    //비밀번호 정규식 확인하는 메서드
    private boolean check_PW_form(String PW) {
        boolean check = false;
        //이메일 패턴이 맞다면
        if (Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$", PW)) {
            check = true;
        }
        return check;
    }

    //아이디 정규식확인,중복확인 을 사용자에게 보여주는 텍스트 변경 메서드
    void set_tv_IDcheck(String settxt, int color, Boolean visible) {
        tv_IDcheck.setText(settxt);
        tv_IDcheck.setTextColor(color);
        if (visible == true) {
            tv_IDcheck.setVisibility(View.VISIBLE);
        } else {
            tv_IDcheck.setVisibility(View.GONE);
        }

    }

    //비밀번호 정규식확인 한번더 확인 사용자에게 보여주는 텍스트 변경 메서드
    void set_tv_PWcheck(String settxt, int color, Boolean visible) {
        tv_PWcheck.setText(settxt);
        tv_PWcheck.setTextColor(color);
        if (visible == true) {
            tv_PWcheck.setVisibility(View.VISIBLE);
        } else {
            tv_PWcheck.setVisibility(View.GONE);
        }

    }

    //아이디 중복확인위해서 서버통신하는 메서드
    public void Check_ID_From_db(String ID) {
        String url = "http://13.209.19.188/IDcheck.php?ID=" + ID;
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
                                ID_duplication_check = true;
                                finID = ID;
                                set_tv_IDcheck("가입 가능한 아이디 입니다.", Color.WHITE, true);
                                et_Join_PW.requestFocus();
                            } else {
                                //중복되는 아이디가 있음
                                ID_duplication_check = false;
                                set_tv_IDcheck("이미 해당 아이디가 존재합니다.", Color.WHITE, true);
                                et_Join_ID.requestFocus();
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
        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

}