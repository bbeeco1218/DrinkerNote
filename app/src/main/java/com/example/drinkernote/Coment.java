package com.example.drinkernote;



import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


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
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class Coment extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;
    int NoteKey;
    String NoteMaker;
    RecyclerView rv_coment;
    LinearLayoutManager layoutManager;
    MyAdapter myAdapter;
    EditText et_coment;
    TextView tv_nocoment;
    LinearLayout coment_linearLayout;
    Button btn_submit;
    RequestQueue requestQueue;//?????? ????????? ??????????????? ??????????????????.
//    NestedScrollView nestedScrollView;
    ArrayList<MyData> myData = new ArrayList<>();
    int focusComentID = -1;
    int page = 0, limit = 10;
    int minpage =0; //?????? ??????????????? ????????????


    @Override
    protected void onStart() {
        super.onStart();

        if(page == 0) {
            page++;
            if (requestQueue == null) {
                //RequestQueue ?????? ????????????
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            }

            if(focusComentID == -1) {
                getcoment(NoteKey, page, limit);
            }
//            else{
//                getfocuscoment(NoteKey,focusComentID,limit);
//                focusComentID = -1;
//            }

        }else{
            myAdapter.setItems(myData);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coment);
        Intent mIntent = getIntent();
        NoteKey = mIntent.getIntExtra("NoteKey",0);
        NoteMaker = mIntent.getStringExtra("NoteMaker");
        focusComentID = mIntent.getIntExtra("focusComentID",-1);
//        Log.e("?????? ????????? ", String.valueOf(NoteKey));
//        Log.e("?????? ??????????????? ", NoteMaker);
        setToolbar();


        tv_nocoment = findViewById(R.id.tv_nocoment);
        et_coment = findViewById(R.id.et_search);
        btn_submit = findViewById(R.id.btn_cancel);
        rv_coment = findViewById(R.id.rv_coment);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        rv_coment.setLayoutManager(layoutManager);
        coment_linearLayout = findViewById(R.id.coment_linearLayout);
//        nestedScrollView = findViewById(R.id.scroll_view);


        rv_coment.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!rv_coment.canScrollVertically(-1)) {
//                    Log.e("rv scroll", "Top of list");
//                    Log.e("rv scroll", "minpage : "+minpage);
                    if(minpage > 0){
                        getupcoment(NoteKey,minpage,limit);
                        minpage--;
                    }
                } else if (!rv_coment.canScrollVertically(1)) {
//                    Log.e("rv scroll", "End of list");
                    page++;
                    getcoment(NoteKey, page, limit);
                }

            }
        });

//        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
//                    Log.e("????????????", "??????");
//                }
//            }
//        });

        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : ?????????????????? ?????? ????????? ?????? ??????
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
                    //RequestQueue ?????? ????????????

                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                String userId = AutoLogin.getUserId(getApplicationContext());
                LikeComent(userId,model.getComentKey(),viewHolder);
            }

            @Override
            public void onUnLikeClicked(MyData model, MyAdapter.MyViewHolder viewHolder) {
                if (requestQueue == null) {
                    //RequestQueue ?????? ????????????

                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                String userId = AutoLogin.getUserId(getApplicationContext());
                unLikeComent(userId,model.getComentKey(),viewHolder);
            }

            @Override
            public void onLikeListClicked(MyData model) {
                Intent mIntent = new Intent(getApplicationContext(), LikeList.class);
                mIntent.putExtra("NoteKey",model.getComentKey());
                mIntent.putExtra("from","coment");
                startActivity(mIntent);

            }

            @Override
            public void onReplyClicked(MyData model,int position) {
                Intent mIntent = new Intent(getApplicationContext(), Reply.class);
                mIntent.putExtra("comentKey",model.getComentKey());
                mIntent.putExtra("position",position);
                mIntent.putExtra("NoteKey",NoteKey);
//                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(mIntent,11);
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contents = et_coment.getText().toString();
                if (requestQueue == null) {
                    //RequestQueue ?????? ????????????
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                // TODO : ????????? ???????????? ????????????????????? ???????????? ?????????????????????
                String userId = AutoLogin.getUserId(getApplicationContext());
                InsertComent(NoteKey,userId,contents);


            }
        });

        rv_coment.setAdapter(myAdapter);
        rv_coment.setHasFixedSize(true);

        MySwipeHelper_a swipeHelper= new MySwipeHelper_a(getApplicationContext(),rv_coment,300) {
            @Override
            public void instantiatrMyButton(RecyclerView.ViewHolder viewHolder, List<MySwipeHelper_a.MyButton> buffer) {

                MyData mdata = myAdapter.getItem(viewHolder.getAdapterPosition());
                String userId = AutoLogin.getUserId(getApplicationContext());
                if(mdata.getID().equals(userId)) {
                    buffer.add(new MyButton(getApplicationContext(),
                            "Delete",
                            30,
                            R.drawable.ic_delete,
                            Color.parseColor("#B13333"),
                            new MyButtonClickListener_a() {
                                @Override
                                public void onClick(int pos) {
//                                    Log.d("TAG", viewHolder.getAdapterPosition() + "");
                                    if (requestQueue == null) {
                                        //RequestQueue ?????? ????????????
                                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                                    }
                                    DeleteComent(mdata.getComentKey(),pos);

                                }
                            }));
                    buffer.add(new MyButton(getApplicationContext(),
                            "Update",
                            30,
                            R.drawable.ic_update,
                            Color.parseColor("#A8A8A8"),
                            new MyButtonClickListener_a() {
                                @Override
                                public void onClick(int pos) {
                                    //TODO: ????????? ??????
                                    if (requestQueue == null) {
                                        //RequestQueue ?????? ????????????
                                        requestQueue = Volley.newRequestQueue(getApplicationContext());
                                    }

                                    //TODO :????????? ???????????? ????????????
                                    Intent mIntent = new Intent(getApplicationContext(), comentUpdate_pop.class);
                                    mIntent.putExtra("contents",mdata.getContents());
                                    mIntent.putExtra("comentkey",mdata.getComentKey());
                                    mIntent.putExtra("position",viewHolder.getAdapterPosition());
                                    startActivityForResult(mIntent,10);

                                }
                            }));
                }else{
                    buffer.add(new MyButton(getApplicationContext(),
                            "reply",
                            30,
                            R.drawable.ic_reply,
                            Color.parseColor("#A8A8A8"),
                            new MyButtonClickListener_a() {
                                @Override
                                public void onClick(int pos) {
                                    //TODO: ????????? ??????
                                    Intent mIntent = new Intent(getApplicationContext(), Reply.class);
                                    mIntent.putExtra("comentKey",mdata.getComentKey());
                                    mIntent.putExtra("position",viewHolder.getAdapterPosition());
                                    mIntent.putExtra("NoteKey",NoteKey);
                                    startActivityForResult(mIntent,11);
                                }
                            }));
                }
            }
        };// swipeHelper

        coment_linearLayout.bringToFront();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10){
            //???????????????????????? ?????????
            if(resultCode == RESULT_OK) {

                String contents = data.getStringExtra("contents");
                int comentkey = data.getIntExtra("comentkey", 0);
                int position = data.getIntExtra("position",-1);
                //????????? ???????????? ?????????
                if (requestQueue == null) {
                    //RequestQueue ?????? ????????????

                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                UpdateComent(comentkey,contents,position);
            }
        }
        if(requestCode == 11){
            //?????????????????? ?????????
            //????????? ????????? ????????????
            int position,Reply_num;
            Reply_num = data.getIntExtra("Reply_num",-1);
            position = data.getIntExtra("position",-1);
            myData.get(position).setReplynum(Reply_num);
            myAdapter.setItems(myData);
        }
    }

    //????????? ????????? ?????????
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar??? back??? ????????? ??? ??????
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
        actionBar.setDisplayShowTitleEnabled(false);//?????? ????????? ???????????????
        actionBar.setDisplayHomeAsUpEnabled(true); //????????????
    }

    public void getupcoment(int NoteKey,int pagee,int limit) {
        String userId = AutoLogin.getUserId(this);
        String url = "http://13.209.19.188/GetComent.php?myID="+userId+"&notekey="+NoteKey+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

//                        Log.e("??????????????? ???????????? ", "onResponse: ?????? : " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int allcoment_num = jsonObject.getInt("allcoment_num");
                            if(allcoment_num > 0){ //????????? ????????????
                                rv_coment.setVisibility(View.VISIBLE);
                                tv_nocoment.setVisibility(View.GONE);
                                String nocoment = jsonObject.getString("nocoment");
                                if(nocoment.equals("yes")) {
                                    JSONArray comentID_List = jsonObject.getJSONArray("comentID_List");
                                    JSONArray coment_contents = jsonObject.getJSONArray("coment_contents");
                                    JSONArray coment_date = jsonObject.getJSONArray("coment_date");
                                    JSONArray coment_profileImg = jsonObject.getJSONArray("coment_profileImg");
                                    JSONArray coment_likenum = jsonObject.getJSONArray("coment_likenum");
                                    JSONArray amilike = jsonObject.getJSONArray("amilike");
                                    JSONArray Coment_key = jsonObject.getJSONArray("Coment_key");
                                    JSONArray Reply_num = jsonObject.getJSONArray("Reply_num");
                                    Log.i("????????? ?????? ", "????????? : "+pagee +"\n????????? ?????????"+comentID_List);
                                    for (int i = comentID_List.length()-1; i >= 0 ; i--) {
                                        String date = makeDate.formatTimeString(coment_date.getString(i));

                                        myData.add(0,new MyData(coment_profileImg.getString(i),
                                                comentID_List.getString(i),
                                                coment_contents.getString(i),
                                                date,
                                                coment_likenum.getInt(i),
                                                amilike.getBoolean(i),
                                                Coment_key.getInt(i),
                                                Reply_num.getInt(i),false));
                                        myAdapter.notifyItemInserted(0);
                                    }


                                    // ???????????? ????????? ??????
//                                    myAdapter.setItems(myData);


                                }
                            }else{ //????????? ????????????
//                                page --;
                                rv_coment.setVisibility(View.GONE);
                                tv_nocoment.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "??????-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }

    //?????? ???????????? ???????????? ???????????? ?????????
    public void getcoment(int NoteKey,int pagee,int limit) {
        String userId = AutoLogin.getUserId(this);
        String url = "http://13.209.19.188/GetComent.php?myID="+userId+"&notekey="+NoteKey+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

//                        Log.e("??????????????? ???????????? ", "onResponse: ?????? : " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int allcoment_num = jsonObject.getInt("allcoment_num");
                            if(allcoment_num > 0){ //????????? ????????????
                                rv_coment.setVisibility(View.VISIBLE);
                                tv_nocoment.setVisibility(View.GONE);
                                String nocoment = jsonObject.getString("nocoment");
                                if(nocoment.equals("yes")) {
                                    JSONArray comentID_List = jsonObject.getJSONArray("comentID_List");
                                    JSONArray coment_contents = jsonObject.getJSONArray("coment_contents");
                                    JSONArray coment_date = jsonObject.getJSONArray("coment_date");
                                    JSONArray coment_profileImg = jsonObject.getJSONArray("coment_profileImg");
                                    JSONArray coment_likenum = jsonObject.getJSONArray("coment_likenum");
                                    JSONArray amilike = jsonObject.getJSONArray("amilike");
                                    JSONArray Coment_key = jsonObject.getJSONArray("Coment_key");
                                    JSONArray Reply_num = jsonObject.getJSONArray("Reply_num");
                                    Log.i("????????? ?????? ", "????????? : "+pagee +"\n????????? ?????????"+comentID_List);
                                    for (int i = 0; i < comentID_List.length(); i++) {
                                        String date = makeDate.formatTimeString(coment_date.getString(i));

                                        myData.add(new MyData(coment_profileImg.getString(i),
                                                comentID_List.getString(i),
                                                coment_contents.getString(i),
                                                date,
                                                coment_likenum.getInt(i),
                                                amilike.getBoolean(i),
                                                Coment_key.getInt(i),
                                                Reply_num.getInt(i),false));
                                    }


                                    // ???????????? ????????? ??????
                                    myAdapter.setItems(myData);


                                }else{
                                    page --;
                                }
                            }else{ //????????? ????????????
                                page --;
                                rv_coment.setVisibility(View.GONE);
                                tv_nocoment.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "??????-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }

//    public void getfocuscoment(int NoteKey,int focusComentID,int limit) {
//
//        String userId = AutoLogin.getUserId(this);
//        String url = "http://13.209.19.188/GetfocusComent.php?myID="+userId+"&notekey="+NoteKey+"&focuscomentkey="+focusComentID+"&limit="+limit;
//
//        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override //?????? ????????? ????????? ??????
//                    public void onResponse(String response) {
//
////                        Log.e("??????????????? ???????????? ", "onResponse: ?????? : " + response);
//
//                        try {
//                            JSONObject jsonObject = new JSONObject(response);
//                            int allcoment_num = jsonObject.getInt("allcoment_num");
//                            if(allcoment_num > 0){ //????????? ????????????
//                                rv_coment.setVisibility(View.VISIBLE);
//                                tv_nocoment.setVisibility(View.GONE);
//                                String nocoment = jsonObject.getString("nocoment");
//                                if(nocoment.equals("yes")) {
//                                    JSONArray comentID_List = jsonObject.getJSONArray("comentID_List");
//                                    JSONArray coment_contents = jsonObject.getJSONArray("coment_contents");
//                                    JSONArray coment_date = jsonObject.getJSONArray("coment_date");
//                                    JSONArray coment_profileImg = jsonObject.getJSONArray("coment_profileImg");
//                                    JSONArray coment_likenum = jsonObject.getJSONArray("coment_likenum");
//                                    JSONArray amilike = jsonObject.getJSONArray("amilike");
//                                    JSONArray Coment_key = jsonObject.getJSONArray("Coment_key");
//                                    JSONArray Reply_num = jsonObject.getJSONArray("Reply_num");
////                                    Log.i("????????? ?????? ", "????????? : "+pagee +"\n????????? ?????????"+comentID_List);
//                                    int focusposition= 0;
//                                    for (int i = 0; i < comentID_List.length(); i++) {
//                                        String date = makeDate.formatTimeString(coment_date.getString(i));
//                                        boolean focus = false;
//                                        if(Coment_key.getInt(i) == focusComentID){
//                                            focusposition = i;
//                                            focus = true;
//                                        }
//                                        myData.add(new MyData(coment_profileImg.getString(i),
//                                                comentID_List.getString(i),
//                                                coment_contents.getString(i),
//                                                date,
//                                                coment_likenum.getInt(i),
//                                                amilike.getBoolean(i),
//                                                Coment_key.getInt(i),
//                                                Reply_num.getInt(i),focus));
//                                    }
//
//                                    page = jsonObject.getInt("page");
//                                    minpage =jsonObject.getInt("page");
//                                    page ++;
//
//                                    Log.e("page", String.valueOf(page));
//                                    // ???????????? ????????? ??????
//                                    myAdapter.setItems(myData);
//                                    rv_coment.scrollToPosition(focusposition);
//
//                                }else{
//                                    page=0;
//                                }
//                            }else{ //????????? ????????????
//                                page =0;
//                                rv_coment.setVisibility(View.GONE);
//                                tv_nocoment.setVisibility(View.VISIBLE);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override //????????? ????????? ??????
//                    public void onErrorResponse(VolleyError error) {
//
//                        Log.d("TAG", "??????-> " + error.getMessage());
//                    }
//                });
//        //request.addStringParam("Note_tittle", whisky_name); //POST???????????? ??????
//
//        request.setShouldCache(false); //?????? ????????? ?????? ??????
//        requestQueue.add(request);
//        Log.d("TAG", "?????? ??????.");
//
//    }

    //????????? ????????????????????? ???????????? ?????????
    public void InsertComent(int NoteKey,String comentMaker,String contents) {
        String url = "http://13.209.19.188/InsertComent.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

//                        Log.d("TAG", "onResponse: ?????? : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                int comentkey = jsonObject.getInt("comentkey");
                                if(!comentMaker.equals(NoteMaker)){ //?????? ???????????? ?????? ???????????? ?????? ?????????
                                    //?????? ??????????????? ????????? ????????????
                                    Newspeed_item comentnews = new Newspeed_item(0,NoteKey,comentMaker,comentkey,NoteMaker,contents);
                                    Gson gson = new Gson();
                                    String jsonMSG = gson.toJson(comentnews);
                                    InsertNews(NoteMaker,jsonMSG,comentMaker,comentkey);
                                    sendNewsToChatServer(comentnews);

                                }
                                Toast.makeText(getApplicationContext(), "????????? ??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                et_coment.setText("");
                                InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                page =1;
                                myData.clear();
                                getcoment(NoteKey,page,limit);
                            } else {
                                Toast.makeText(getApplicationContext(), "?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "??????-> " + error.getMessage());
                    }
                });
        request.addStringParam("NoteKey", String.valueOf(NoteKey)); //POST???????????? ??????
        request.addStringParam("comentMaker", comentMaker); //POST???????????? ??????
        request.addStringParam("contents", contents); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }


    //????????? ???????????? ?????????
    public void DeleteComent(int comentKey,int position) {
        String url = "http://13.209.19.188/DeleteComent.php?comentKey="+comentKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

                        Log.e("deletecoment", "onResponse: ?????? : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String res = jsonObject.getString("response");
                            if(res.equals("true")){
//                                getcoment(NoteKey);
                                myAdapter.deleteItems(position);
                                if(myAdapter.getItemsize()<=0){
                                    rv_coment.setVisibility(View.GONE);
                                    tv_nocoment.setVisibility(View.VISIBLE);
                                }
                            }else{

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "??????-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }

    //?????? ????????? ?????????
    public void LikeComent(String myID, int comentKey, MyAdapter.MyViewHolder viewHolder) {
        String url = "http://13.209.19.188/LikeComent.php?myID="+myID+"&Coment_key="+comentKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

//                        Log.e("?????? ?????????", "onResponse: ?????? : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                if(Like_num == 0){
                                    viewHolder.tv_coment_likenum.setText("");
                                }else {
                                    viewHolder.tv_coment_likenum.setText("????????? " + Like_num + "???");
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "??????-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }
    public void unLikeComent(String myID, int comentKey, MyAdapter.MyViewHolder viewHolder) {
        String url = "http://13.209.19.188/unLikeComent.php?myID="+myID+"&Coment_key="+comentKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

//                        Log.e("?????? ????????????", "onResponse: ?????? : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "?????? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                                int Like_num = jsonObject.getInt("Like_num");
                                if(Like_num == 0){
                                    viewHolder.tv_coment_likenum.setText("");
                                }else {
                                    viewHolder.tv_coment_likenum.setText("????????? " + Like_num + "???");
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "??????-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }

    //?????? ???????????? ?????????
    public void UpdateComent(int comentKey,String contents,int position) {
        String url = "http://13.209.19.188/UpdateComent.php?Coment_key="+comentKey+"&contents="+contents;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {
//                        Log.d("TAG", "onResponse: ?????? : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                myData.get(position).setContents(contents);
                                myAdapter.setItems(myData);
                            } else {
                                Toast.makeText(getApplicationContext(), "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.d("TAG", "??????-> " + error.getMessage());
                    }
                });
        //request.addStringParam("Note_tittle", whisky_name); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }


    public void InsertNews(String userID,String newsOBJ,String fromID,int comentKey) {
        String url = "http://13.209.19.188/InsertNews.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //?????? ????????? ????????? ??????
                    public void onResponse(String response) {

                        Log.e("insertnews", "onResponse: ?????? : " + response);

                    }
                },
                new Response.ErrorListener() {
                    @Override //????????? ????????? ??????
                    public void onErrorResponse(VolleyError error) {

                        Log.e("TAG", "??????-> " + error.getMessage());
                    }
                });
        request.addStringParam("userID", userID); //POST???????????? ??????
        request.addStringParam("newsOBJ", newsOBJ); //POST???????????? ??????
        request.addStringParam("fromID", fromID); //POST???????????? ??????
        request.addStringParam("comentKey", String.valueOf(comentKey)); //POST???????????? ??????

        request.setShouldCache(false); //?????? ????????? ?????? ??????
        requestQueue.add(request);
        Log.d("TAG", "?????? ??????.");

    }
    void sendNewsToChatServer(Newspeed_item newsitem){
        Gson gson = new Gson();
        String jsonMSG = gson.toJson(newsitem);
        Intent mIntent = new Intent("Sendmsg");
        mIntent.putExtra("news",jsonMSG);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mIntent);
    }

    // ?????????????????? ???????????? ????????? ????????? ?????????
    public static class MyData {
        // TODO : ?????????????????? ???????????? ????????? ?????????
        String img;
        String ID;
        String contents;
        String date;
        int likenum;
        boolean amilike;
        int comentKey;
        int Replynum;
        boolean focus;

        public boolean isFocus() {
            return focus;
        }

        public void setFocus(boolean focus) {
            this.focus = focus;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getID() {
            return ID;
        }

        public int getComentKey() {
            return comentKey;
        }

        public void setComentKey(int comentKey) {
            this.comentKey = comentKey;
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

        public int getReplynum() {
            return Replynum;
        }

        public void setReplynum(int replynum) {
            Replynum = replynum;
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

        public MyData(String img, String ID, String contents, String date, int likenum, boolean amilike, int comentKey, int replynum, boolean focus) {
            this.img = img;
            this.ID = ID;
            this.contents = contents;
            this.date = date;
            this.likenum = likenum;
            this.amilike = amilike;
            this.comentKey = comentKey;
            Replynum = replynum;
            this.focus = focus;
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
            void onprofileClicked(MyData model);
            void onLikeClicked(MyData model,MyViewHolder viewHolder);
            void onUnLikeClicked(MyData model,MyViewHolder viewHolder);
            void onLikeListClicked(MyData model);
            void onReplyClicked(MyData model,int position);
        }

        private OnMyClickListener mListener;

        private ArrayList<MyData> mItems = new ArrayList<>();

        public MyAdapter() {}

        public MyAdapter(OnMyClickListener listener) {
            mListener = listener;
        }

        public void setItems(ArrayList<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }

        public void deleteItems(int position){
            mItems.remove(position);
            notifyItemRemoved(position);
        }

        public MyData getItem(int position){
            MyData rdata = mItems.get(position);
            return rdata;
        }

        public int getItemsize(){
            return mItems.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_coment, parent, false);
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
            viewHolder.iv_coment_profileimg.setOnClickListener(profileclick);
            viewHolder.tv_coment_ID.setOnClickListener(profileclick);

            viewHolder.iv_coment_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view.isSelected()) {
                        view.setSelected(false);

                        // ????????? ??????
                        mListener.onUnLikeClicked(mItems.get(viewHolder.getAdapterPosition()),viewHolder);
                    }else{
                        view.setSelected(true);
                        //?????????
                        mListener.onLikeClicked(mItems.get(viewHolder.getAdapterPosition()),viewHolder);
                    }
                }
            });

            viewHolder.tv_coment_likenum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onLikeListClicked(mItems.get(viewHolder.getAdapterPosition()));
                }
            });

            viewHolder.tv_coment_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onReplyClicked(mItems.get(viewHolder.getAdapterPosition()),viewHolder.getAdapterPosition());
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//            Log.e("myadabter", "onBindViewHolder position: " + position);
            MyData item = mItems.get(position);
            if(item.isFocus()){
                holder.coment_layout.setBackgroundColor(Color.DKGRAY);
            }else{
                holder.coment_layout.setBackgroundColor(Color.TRANSPARENT);
            }
            // TODO : ???????????? ???????????? ???????????????
            if(item.getImg().equals("Nothing")) {
                Glide.with(holder.itemView)
                        .load(R.drawable.myprofile)
                        .placeholder(R.drawable.dataloading)
                        .fallback(R.drawable.myprofile) //???????????? ????????? ????????????
                        .centerCrop() //???????????? ???????????? ???????????????
                        .into(holder.iv_coment_profileimg);
            }else {
                Glide.with(holder.itemView)
                        .load("http://13.209.19.188/"+item.getImg())
                        .centerCrop() //???????????? ???????????? ???????????????
                        .placeholder(R.drawable.dataloading) //??????????????? ????????????
                        .fallback(R.drawable.myprofile) //???????????? ????????? ????????????
                        .into(holder.iv_coment_profileimg);
            }

            holder.tv_coment_ID.setText(item.getID());
            holder.tv_coment_contents.setText(item.getContents());
            holder.tv_coment_date.setText(item.getDate());

            if(item.getLikenum() == 0){
                holder.tv_coment_likenum.setText("");
            }else{
                holder.tv_coment_likenum.setText("????????? "+item.getLikenum()+"???");
            }
            holder.iv_coment_like.setSelected(item.isAmilike());
            if(item.getReplynum() == 0){
                holder.tv_coment_reply.setText("?????? ??????");
            }else{
                holder.tv_coment_reply.setText("?????? "+item.getReplynum()+"???");
            }

        }

        @Override
        public int getItemCount() {

            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : ????????? ????????? ???????????? ?????????
            CircleImageView iv_coment_profileimg;
            TextView tv_coment_ID,tv_coment_contents,tv_coment_date,tv_coment_likenum,tv_coment_reply;
            Button iv_coment_like;
            ConstraintLayout coment_layout;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : ????????? ????????? ???????????? ?????????
                iv_coment_profileimg = itemView.findViewById(R.id.iv_coment_profileimg);
                tv_coment_ID = itemView.findViewById(R.id.tv_coment_ID);
                tv_coment_contents = itemView.findViewById(R.id.tv_coment_contents);
                tv_coment_date = itemView.findViewById(R.id.tv_coment_date);
                tv_coment_likenum = itemView.findViewById(R.id.tv_coment_likenum);
                tv_coment_reply = itemView.findViewById(R.id.tv_coment_reply);
                iv_coment_like = itemView.findViewById(R.id.iv_coment_like);
                coment_layout = itemView.findViewById(R.id.coment_layout);
            }
        }
    }
}