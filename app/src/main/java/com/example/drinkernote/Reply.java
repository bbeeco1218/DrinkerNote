package com.example.drinkernote;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class Reply extends AppCompatActivity {

    Toolbar toolbar;
    ActionBar actionBar;
    CircleImageView iv_mainreply_profileimg;
    TextView tv_mainreply_ID,tv_mainreply_contents,tv_mainreply_date,tv_mainreply_likenum,tv_noreply;
    EditText et_reply;
    Button iv_mainreply_like,btn_reply_submit;
    RecyclerView rv_reply;
    MyAdapter myAdapter;
    LinearLayoutManager layoutManager;
    RequestQueue requestQueue;
    int comentKey,position,NoteKey;
    NestedScrollView nestedScrollView;
    ArrayList<MyData> myData = new ArrayList<>();
    int page = 0, limit = 10;
    String Coment_maker;

    @Override
    protected void onStart() {
        if(page ==0) {
            page ++;
            if (requestQueue == null) {
                //RequestQueue 객체 생성하기
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }
            String userId = AutoLogin.getUserId(this);
            getReply(comentKey, userId, page, limit);
        }else{
            myAdapter.setItems(myData);
        }

        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        setToolbar();

        Intent mIntent = getIntent();
        comentKey = mIntent.getIntExtra("comentKey",0);
        position = mIntent.getIntExtra("position",-1);
        NoteKey = mIntent.getIntExtra("NoteKey",0);
        iv_mainreply_profileimg = findViewById(R.id.iv_mainreply_profileimg);
        tv_mainreply_ID = findViewById(R.id.tv_mainreply_ID);
        tv_mainreply_contents = findViewById(R.id.tv_mainreply_contents);
        tv_mainreply_date = findViewById(R.id.tv_mainreply_date);
        tv_mainreply_likenum = findViewById(R.id.tv_mainreply_likenum);
        iv_mainreply_profileimg = findViewById(R.id.iv_mainreply_profileimg);
        iv_mainreply_like = findViewById(R.id.iv_mainreply_like);
        rv_reply = findViewById(R.id.rv_reply);
        et_reply = findViewById(R.id.et_reply);
        btn_reply_submit =findViewById(R.id.btn_reply_submit);
        tv_noreply = findViewById(R.id.tv_noreply);
        nestedScrollView = findViewById(R.id.scroll_view);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
                    String userId = AutoLogin.getUserId(getApplicationContext());
                    getReply(comentKey,userId,page,limit);
                }
            }
        });
        layoutManager = new LinearLayoutManager(getApplicationContext());
        rv_reply.setLayoutManager(layoutManager);
        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
            }

            @Override
            public void onprofileClicked(MyData model) {
                Intent mIntent = new Intent(getApplicationContext(), Profile.class);
                mIntent.putExtra("Maker",model.getID());
                startActivity(mIntent);
            }

            @Override
            public void onLikeClicked(MyData model, MyAdapter.MyViewHolder viewHolder) {
                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기

                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                String userId = AutoLogin.getUserId(getApplicationContext());
                LikeReply(userId,model.getReplyKey(),viewHolder);
            }

            @Override
            public void onUnLikeClicked(MyData model, MyAdapter.MyViewHolder viewHolder) {
                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기

                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                String userId = AutoLogin.getUserId(getApplicationContext());
                unLikeReply(userId,model.getReplyKey(),viewHolder);
            }

            @Override
            public void onLikeListClicked(MyData model) {
                Intent mIntent = new Intent(getApplicationContext(), LikeList.class);
                mIntent.putExtra("NoteKey",model.getReplyKey());
                mIntent.putExtra("from","reply");
                startActivity(mIntent);
            }
        });

        MySwipeHelper swipeHelper= new MySwipeHelper(getApplicationContext(),rv_reply,300) {
            @Override
            public void instantiatrMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) {

                MyData mdata = myAdapter.getItem(viewHolder.getAdapterPosition());
                String userId = AutoLogin.getUserId(getApplicationContext());
                if(mdata.getID().equals(userId)) {
                    buffer.add(new MyButton(getApplicationContext(),
                            "Delete",
                            30,
                            R.drawable.ic_delete,
                            Color.parseColor("#B13333"),
                            new MyButtonClickListener() {
                                @Override
                                public void onClick(int pos) {
//                                    Log.d("TAG", viewHolder.getAdapterPosition() + "");
                                    if (requestQueue == null) {
                                        //RequestQueue 객체 생성하기
                                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                                    }
                                    DeleteReply(mdata.getReplyKey(), pos);

                                }
                            }));
                    buffer.add(new MyButton(getApplicationContext(),
                            "Update",
                            30,
                            R.drawable.ic_update,
                            Color.parseColor("#A8A8A8"),
                            new MyButtonClickListener() {
                                @Override
                                public void onClick(int pos) {
                                    //TODO: 편집할 코드
                                    if (requestQueue == null) {
                                        //RequestQueue 객체 생성하기
                                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                                    }

                                    //TODO :여기에 댓글수정 넣어야함
                                    Intent mIntent = new Intent(getApplicationContext(), comentUpdate_pop.class);
                                    mIntent.putExtra("contents", mdata.getContents());
                                    mIntent.putExtra("comentkey", mdata.getReplyKey());
                                    startActivityForResult(mIntent, 10);

                                }
                            }));
                }
            }
        };// swipeHelper
        rv_reply.setAdapter(myAdapter);
        rv_reply.setHasFixedSize(true);

        btn_reply_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contents = et_reply.getText().toString();
                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                // TODO : 댓글을 전송해서 데이터베이스에 저장하고 새로고침해야함

                String userId = AutoLogin.getUserId(getApplicationContext());
                InsertReply(comentKey,userId,contents);
            }
        });

        iv_mainreply_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.isSelected()) {
                    view.setSelected(false);

                    // 좋아요 취소
                    if (requestQueue == null) {
                        //RequestQueue 객체 생성하기

                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                    }
                    String userId = AutoLogin.getUserId(getApplicationContext());
                    unLikeComent(userId,comentKey);
                }else{
                    view.setSelected(true);
                    //좋아요
                    if (requestQueue == null) {
                        //RequestQueue 객체 생성하기
                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                    }
                    String userId = AutoLogin.getUserId(getApplicationContext());
                    LikeComent(userId,comentKey);
                }
            }
        });
        tv_mainreply_likenum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getApplicationContext(), LikeList.class);
                mIntent.putExtra("NoteKey",comentKey);
                mIntent.putExtra("from","coment");
                startActivity(mIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10){

            if(resultCode == RESULT_OK) {

                String contents = data.getStringExtra("contents");
                int replykey = data.getIntExtra("comentkey", 0);

                //댓글을 업데이트 해야함
                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기

                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                UpdateReply(replykey,contents);
            }
        }
    }

    public void getReply(int comentkey,String myID,int pagee,int limit) {
        String url = "http://13.209.19.188/GetReply.php?comentKey="+comentkey+"&myID="+myID+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("대댓글 ", "onResponse: 응답 : " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String noreply = jsonObject.getString("noreply");
                            String Coment_contents = jsonObject.getString("Coment_contents");
                            Coment_maker = jsonObject.getString("Coment_maker");
                            String Coment_date = jsonObject.getString("Coment_date");
                            String date = makeDate.formatTimeString(Coment_date);
                            String Coment_profile = jsonObject.getString("Coment_profile");
                            int coment_likenum = jsonObject.getInt("coment_likenum");
                            boolean amilike = jsonObject.getBoolean("amilike");

                            if (Coment_profile.equals("Nothing")) {
                                Glide.with(iv_mainreply_profileimg)
                                        .load(R.drawable.myprofile)
                                        .placeholder(R.drawable.dataloading)
                                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                                        .centerCrop() //가운데를 기준으로 크기맞추기
                                        .into(iv_mainreply_profileimg);
                            } else {
                                Glide.with(iv_mainreply_profileimg)
                                        .load("http://13.209.19.188/" + Coment_profile)
                                        .centerCrop() //가운데를 기준으로 크기맞추기
                                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                                        .into(iv_mainreply_profileimg);
                            }
                            tv_mainreply_ID.setText(Coment_maker);
                            tv_mainreply_contents.setText(Coment_contents);

                            if (coment_likenum == 0) {
                                tv_mainreply_likenum.setText("");
                            } else {
                                tv_mainreply_likenum.setText("좋아요 " + coment_likenum + "개");
                            }
                            iv_mainreply_like.setSelected(amilike);
                            tv_mainreply_date.setText(date);
                            int Reply_num = jsonObject.getInt("Reply_num");
                            if(Reply_num > 0) {
                                if (noreply.equals("yes")) {
                                    rv_reply.setVisibility(View.VISIBLE);
                                    tv_noreply.setVisibility(View.GONE);
                                    //타겟댓글 셋팅하는부분


                                    //대댓글 리스트업

                                    JSONArray Reply_ID = jsonObject.getJSONArray("Reply_ID");
                                    JSONArray Reply_date = jsonObject.getJSONArray("Reply_date");
                                    JSONArray Reply_contents = jsonObject.getJSONArray("Reply_contents");
                                    JSONArray Reply_profile = jsonObject.getJSONArray("Reply_profile");
                                    JSONArray Reply_likenum = jsonObject.getJSONArray("Reply_likenum");
                                    JSONArray Reply_amilike = jsonObject.getJSONArray("Reply_amilike");
                                    JSONArray Reply_key = jsonObject.getJSONArray("Reply_key");

                                    Log.i("페이징 확인 ", "페이지 : "+pagee +"\n아이디 리스트"+Reply_ID);
                                    for (int i = 0; i < Reply_ID.length(); i++) {
                                        myData.add(new MyData(Reply_profile.getString(i),
                                                Reply_ID.getString(i),
                                                Reply_contents.getString(i),
                                                makeDate.formatTimeString(Reply_date.getString(i)),
                                                Reply_likenum.getInt(i),
                                                Reply_amilike.getBoolean(i),
                                                Reply_key.getInt(i)
                                        ));
                                    }

                                    myAdapter.setItems(myData);
                                } else {
                                    page --;
                                }
                            }else{
                                rv_reply.setVisibility(View.GONE);
                                tv_noreply.setVisibility(View.VISIBLE);

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
    public void InsertReply(int comentkey,String myID,String contents) {
        String url = "http://13.209.19.188/InsertReply.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("대댓글 ", "onResponse: 응답 : " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if(res.equals("true")){
                                int replykey = jsonObject.getInt("replykey");
                                if(!Coment_maker.equals(myID)){//댓글작성자와 대댓글작성자가 다르다면
                                    //댓글작성자에게 뉴스를 보내야함
                                    Newspeed_item replynews = new Newspeed_item(3,comentkey,myID,Coment_maker,replykey,contents,NoteKey);
                                    Gson gson = new Gson();
                                    String jsonMSG = gson.toJson(replynews);
                                    InsertNews(Coment_maker,jsonMSG,myID,replykey,"reply");
                                    sendNewsToChatServer(replynews);
                                }
                                Toast.makeText(getApplicationContext(), "댓글이 성공적으로 게시되었습니다.", Toast.LENGTH_SHORT).show();
                                et_reply.setText("");
                                InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                page = 1;
                                myData.clear();
                                getReply(comentkey,myID,page,limit);
                            }else{
                                Toast.makeText(getApplicationContext(), "댓글 게시를 실패했습니다.", Toast.LENGTH_SHORT).show();
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
        request.addStringParam("comentkey", String.valueOf(comentkey)); //POST파라미터 넣기
        request.addStringParam("myID", myID); //POST파라미터 넣기
        request.addStringParam("contents", contents); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }
    public void DeleteReply(int replyKey,int position) {
        String url = "http://13.209.19.188/DeleteReply.php?replyKey="+replyKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.d("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String res = jsonObject.getString("response");
                            if(res.equals("true")){
                                myAdapter.deleteItems(position);
                                if(myAdapter.getItemsize()<=0){
                                    rv_reply.setVisibility(View.GONE);
                                    tv_noreply.setVisibility(View.VISIBLE);
                                }
                            }else{

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
    public void UpdateReply(int replyKey,String contents) {
        String url = "http://13.209.19.188/UpdateReply.php?Reply_key="+replyKey+"&contents="+contents;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
//                        Log.d("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "성공적으로 수정하였습니다.", Toast.LENGTH_SHORT).show();
                                String userId = AutoLogin.getUserId(getApplicationContext());
                                getReply(comentKey,userId,page,limit);
                            } else {
                                Toast.makeText(getApplicationContext(), "수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
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

    public void LikeComent(String myID, int comentKey) {
        String url = "http://13.209.19.188/LikeComent.php?myID="+myID+"&Coment_key="+comentKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("댓글 좋아요", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "해당 댓글을 좋아합니다.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                if(Like_num == 0){
                                    tv_mainreply_likenum.setText("");
                                }else {
                                    tv_mainreply_likenum.setText("좋아요 " + Like_num + "개");
                                }
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
        //request.addStringParam("Note_tittle", whisky_name); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }
    public void unLikeComent(String myID, int comentKey) {
        String url = "http://13.209.19.188/unLikeComent.php?myID="+myID+"&Coment_key="+comentKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("댓글 안좋아요", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "해당 댓글을 좋아하지 않습니다.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                if(Like_num == 0){
                                    tv_mainreply_likenum.setText("");
                                }else {
                                    tv_mainreply_likenum.setText("좋아요 " + Like_num + "개");
                                }
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
        //request.addStringParam("Note_tittle", whisky_name); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

    public void LikeReply(String myID, int replyKey, MyAdapter.MyViewHolder viewHolder) {
        String url = "http://13.209.19.188/LikeReply.php?myID="+myID+"&Reply_key="+replyKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("댓글 좋아요", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "해당 댓글을 좋아합니다.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                if(Like_num == 0){
                                    viewHolder.tv_reply_likenum.setText("");
                                }else {
                                    viewHolder.tv_reply_likenum.setText("좋아요 " + Like_num + "개");
                                }
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
        //request.addStringParam("Note_tittle", whisky_name); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }
    public void unLikeReply(String myID, int replyKey, MyAdapter.MyViewHolder viewHolder) {
        String url = "http://13.209.19.188/unLikeReply.php?myID="+myID+"&Reply_key="+replyKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("댓글 안좋아요", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Log.e("대댓글 안좋아요", "들어옴/");
                                Toast.makeText(getApplicationContext(), "해당 댓글을 좋아하지 않습니다.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                if(Like_num == 0){
                                    viewHolder.tv_reply_likenum.setText("");
                                }else {
                                    viewHolder.tv_reply_likenum.setText("좋아요 " + Like_num + "개");
                                }
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
        //request.addStringParam("Note_tittle", whisky_name); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

    public void InsertNews(String userID,String newsOBJ,String fromID,int comentKey,String from) {
        String url = "http://13.209.19.188/InsertNews.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

                        Log.e("insertnews", "onResponse: 응답 : " + response);

                    }
                },
                new Response.ErrorListener() {
                    @Override //에러시 처리할 내용
                    public void onErrorResponse(VolleyError error) {

                        Log.e("TAG", "에러-> " + error.getMessage());
                    }
                });
        request.addStringParam("userID", userID); //POST파라미터 넣기
        request.addStringParam("newsOBJ", newsOBJ); //POST파라미터 넣기
        request.addStringParam("fromID", fromID); //POST파라미터 넣기
        request.addStringParam("comentKey", String.valueOf(comentKey)); //POST파라미터 넣기
        request.addStringParam("from", from); //POST파라미터 넣기

        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }

    void sendNewsToChatServer(Newspeed_item newsitem){
        Gson gson = new Gson();
        String jsonMSG = gson.toJson(newsitem);
        Intent mIntent = new Intent("Sendmsg");
        mIntent.putExtra("news",jsonMSG);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
    }

    // 리사이클러뷰 아이템에 추가할 데이터 클래스
    public static class MyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트
        String img;
        String ID;
        String contents;
        String date;
        int likenum;
        boolean amilike;
        int replyKey;

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getContents() {
            return contents;
        }

        public void setContents(String contents) {
            this.contents = contents;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getLikenum() {
            return likenum;
        }

        public void setLikenum(int likenum) {
            this.likenum = likenum;
        }

        public boolean isAmilike() {
            return amilike;
        }

        public void setAmilike(boolean amilike) {
            this.amilike = amilike;
        }

        public int getReplyKey() {
            return replyKey;
        }

        public void setReplyKey(int replyKey) {
            this.replyKey = replyKey;
        }

        public MyData(String img, String ID, String contents, String date, int likenum, boolean amilike, int replyKey) {
            this.img = img;
            this.ID = ID;
            this.contents = contents;
            this.date = date;
            this.likenum = likenum;
            this.amilike = amilike;
            this.replyKey = replyKey;
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
            void onprofileClicked(MyData model);
            void onLikeClicked(MyData model, MyAdapter.MyViewHolder viewHolder);
            void onUnLikeClicked(MyData model, MyAdapter.MyViewHolder viewHolder);
            void onLikeListClicked(MyData model);
        }

        private OnMyClickListener mListener;

        private List<MyData> mItems = new ArrayList<>();

        public MyAdapter(OnMyClickListener listener) {
            mListener = listener;
        }

        public void setItems(List<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }

        public MyData getItem(int position){
            MyData rdata = mItems.get(position);
            return rdata;
        }

        public int getItemsize(){
            return mItems.size();
        }

        public void deleteItems(int position){
            mItems.remove(position);
            notifyItemRemoved(position);
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reply, parent, false);
            final MyViewHolder viewHolder = new MyViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        final MyData item = mItems.get(viewHolder.getAdapterPosition());
                        mListener.onMyClicked(item);
                    }
                }
            });

            View.OnClickListener profileclick = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onprofileClicked(mItems.get(viewHolder.getAdapterPosition()));
                }
            };
            viewHolder.iv_reply_profileimg.setOnClickListener(profileclick);
            viewHolder.tv_reply_ID.setOnClickListener(profileclick);

            viewHolder.iv_reply_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view.isSelected()) {
                        view.setSelected(false);

                        // 좋아요 취소
                        mListener.onUnLikeClicked(mItems.get(viewHolder.getAdapterPosition()),viewHolder);
                    }else{
                        view.setSelected(true);
                        //좋아요
                        mListener.onLikeClicked(mItems.get(viewHolder.getAdapterPosition()),viewHolder);
                    }
                }
            });

            viewHolder.tv_reply_likenum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onLikeListClicked(mItems.get(viewHolder.getAdapterPosition()));
                }
            });

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MyData item = mItems.get(position);
            // TODO : 데이터를 뷰홀더에 표시하시오
            if(item.getImg().equals("Nothing")) {
                Glide.with(holder.itemView)
                        .load(R.drawable.myprofile)
                        .placeholder(R.drawable.dataloading)
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .into(holder.iv_reply_profileimg);
            }else {
                Glide.with(holder.itemView)
                        .load("http://13.209.19.188/"+item.getImg())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .into(holder.iv_reply_profileimg);
            }

            holder.tv_reply_ID.setText(item.getID());
            holder.tv_reply_contents.setText(item.getContents());
            holder.tv_reply_date.setText(item.getDate());

            if(item.getLikenum() == 0){
                holder.tv_reply_likenum.setText("");
            }else{
                holder.tv_reply_likenum.setText("좋아요 "+item.getLikenum()+"개");
            }
            holder.iv_reply_like.setSelected(item.isAmilike());

        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            CircleImageView iv_reply_profileimg;
            TextView tv_reply_ID,tv_reply_date,tv_reply_contents,tv_reply_likenum;
            Button iv_reply_like;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : 뷰홀더 코드를 입력하여 주세요
                iv_reply_profileimg = itemView.findViewById(R.id.iv_reply_profileimg);
                tv_reply_ID = itemView.findViewById(R.id.tv_reply_ID);
                tv_reply_date = itemView.findViewById(R.id.tv_reply_date);
                tv_reply_contents = itemView.findViewById(R.id.tv_reply_contents);
                tv_reply_likenum = itemView.findViewById(R.id.tv_reply_likenum);
                iv_reply_like = itemView.findViewById(R.id.iv_reply_like);

            }
        }
    }


    void setToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다
        actionBar.setDisplayHomeAsUpEnabled(true); //뒤로가기
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                Intent mIntent = new Intent();
                mIntent.putExtra("Reply_num",myData.size());
                mIntent.putExtra("position", position);
                setResult(RESULT_OK,mIntent);
                finish();
                return true;
            }

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface MyButtonClickListener {
        void onClick(int pos);
    }
    public abstract class MySwipeHelper extends ItemTouchHelper.SimpleCallback {

        int buttonWidth;
        private RecyclerView recyclerView;
        private List<MySwipeHelper.MyButton> buttonList;
        private GestureDetector gestureDetector;
        private int swipePosition=-1;
        private float swipeThreshold = 0.5f;
        private Map<Integer,List<MySwipeHelper.MyButton>> buttonBuffer;
        private Queue<Integer> removerQueue;
        private  GestureDetector.SimpleOnGestureListener gestureListener= new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                for(MySwipeHelper.MyButton button:buttonList){
                    if(button.onClick(e.getX(),e.getY()))
                        break;
                }
                return super.onSingleTapUp(e);
            }
        };

        private View.OnTouchListener onTouchListener= new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if(swipePosition<0) return false;
                Point point =new Point((int)motionEvent.getRawX(),(int)motionEvent.getRawY());

                RecyclerView.ViewHolder swipeViewHolder = recyclerView.findViewHolderForAdapterPosition(swipePosition);
                View swipedItem=swipeViewHolder.itemView;
                Rect rect= new Rect();
                swipedItem.getGlobalVisibleRect(rect);

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN ||
                        motionEvent.getAction() == MotionEvent.ACTION_UP ||
                        motionEvent.getAction() == MotionEvent.ACTION_MOVE){

                    if (rect.top<point.y && rect.bottom> point.y) gestureDetector.onTouchEvent(motionEvent);
                    else {
                        removerQueue.add(swipePosition);
                        swipePosition = -1;
                        recoverSwipedItem();
                    }
                }
                return false;
            }
        };

        protected  synchronized void recoverSwipedItem(){
            while (!removerQueue.isEmpty()){
                int pos=removerQueue.poll();
                if(pos> -1) recyclerView.getAdapter().notifyItemChanged(pos);
            }
        }


        public MySwipeHelper(Context context, RecyclerView recyclerView, int buttonWidth) {
            super(0,ItemTouchHelper.LEFT);
            this.recyclerView= recyclerView;
            this.buttonList= new ArrayList<>();
            this.gestureDetector= new GestureDetector(context,gestureListener);
            this.recyclerView.setOnTouchListener(onTouchListener);
            this.buttonBuffer= new HashMap<>();
            this.buttonWidth= buttonWidth;

            removerQueue= new LinkedList<Integer>(){
                @Override
                public boolean add(Integer integer) {
                    if(contains(integer)) return false;
                    else return super.add(integer);
                }
            };

            attachSwipe();

        }// MySwipeHelper*()..

        private void attachSwipe() {
            ItemTouchHelper itemTouchHelper=new ItemTouchHelper(this);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

        public class MyButton{
            private String text;
            private int imageResId,textSize, color,pos;
            private RectF clickRegion;
            private MyButtonClickListener listener;
            private Context context;
            private Resources resources;

            public MyButton(Context context, String text,  int textSize,int imageResId, int color, MyButtonClickListener listener) {
                this.text = text;
                this.imageResId = imageResId;
                this.textSize = textSize;
                this.color = color;

                this.listener = listener;
                this.context = context;
                resources= context.getResources();
            }// MyButton()..

            public boolean onClick(float x, float y){
                if(clickRegion != null && clickRegion.contains(x,y) ){
                    listener.onClick(pos);
                    return true;
                }
                return false;
            }// onClick()..

            public void onDraw(Canvas c, RectF rectF, int pos){
                Paint p = new Paint();
                p.setColor(color);
                c.drawRect(rectF,p);
                //text
                p.setColor(Color.WHITE);
                p.setTextSize(textSize);

                Rect r = new Rect();
                float cHeight= rectF.height();
                float cWidth= rectF.width();
                p.setTextAlign(Paint.Align.LEFT);
                p.getTextBounds(text, 0,text.length(),r);
                float x=0, y=0;
                if(imageResId == 0){
                    x=cWidth/2f-r.width()/2f -r.left;
                    y=cHeight/2f + r.height()/2f -r.bottom;
                    c.drawText(text, rectF.left+x,rectF.top+y,p);
                }else{
                    Drawable d = ContextCompat.getDrawable(context,imageResId);
                    Bitmap bitmap= drawableToBitmap(d);
                    c.drawBitmap(bitmap,(rectF.left+rectF.right)/2,(rectF.top+rectF.bottom)/2,p);
                }
                clickRegion= rectF;
                this.pos=pos;
            }

        }// MyButton class..

        protected  Bitmap drawableToBitmap(Drawable d){
            if(d instanceof BitmapDrawable) return ((BitmapDrawable)d).getBitmap();
            Bitmap  bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(),d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas= new Canvas(bitmap);
            d.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
            d.draw(canvas);
            return bitmap;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos=viewHolder.getAdapterPosition();
            if(swipePosition != pos) removerQueue.add(swipePosition);
            swipePosition=pos;
            if(buttonBuffer.containsKey(swipePosition)) buttonList = buttonBuffer.get(swipePosition);
            else buttonList.clear();
            buttonBuffer.clear();
            swipeThreshold= 0.5f*buttonList.size()*buttonWidth;
            recoverSwipedItem();
        }

        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return swipeThreshold;
        }

        @Override
        public float getSwipeEscapeVelocity(float defaultValue) {
            return 0.1f*defaultValue;
        }

        @Override
        public float getSwipeVelocityThreshold(float defaultValue) {
            return 5.0f*defaultValue;
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            int pos= viewHolder.getAdapterPosition();
            float translationX =dX;
            View itemView= viewHolder.itemView;
            if (pos<0){
                swipePosition = pos;
                return;
            }
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                if(dX<0){
                    List<MySwipeHelper.MyButton> buffer= new ArrayList<>();
                    if(!buttonBuffer.containsKey(pos)){
                        instantiatrMyButton(viewHolder,buffer);
                        buttonBuffer.put(pos,buffer);
                    }else{
                        buffer = buttonBuffer.get(pos);
                    }
                    translationX=dX*buffer.size()*buttonWidth / itemView.getWidth();
                    drawButton(c,itemView,buffer,pos,translationX);
                }
            }
            super.onChildDraw(c,recyclerView,viewHolder,translationX,dY,actionState,isCurrentlyActive);
        }

        private void drawButton(Canvas c, View itemView, List<MySwipeHelper.MyButton> buffer, int pos, float translationX) {
            float right = itemView.getRight();
            float dButtonWidth = -1*translationX / buffer.size();
            for(MySwipeHelper.MyButton button:buffer){
                float left= right - dButtonWidth;
                button.onDraw(c,new RectF(left,itemView.getTop(),right,itemView.getBottom()),pos);
                right=left;
            }
        }

        public abstract void instantiatrMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper.MyButton> buffer) ;
    }// MySwipeHelper class..
}