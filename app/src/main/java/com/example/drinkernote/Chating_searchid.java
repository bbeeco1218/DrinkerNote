package com.example.drinkernote;
import android.content.Intent;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chating_searchid extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;
    LinearLayoutManager layoutManager;
    RecyclerView rv_searchid;
    NestedScrollView nestedScrollView;
    EditText et_search;
    MyAdapter myAdapter;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    ArrayList<MyData> myData = new ArrayList<>();
    int page = 0, limit = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chating_searchid);
        setToolbar();

        nestedScrollView = findViewById(R.id.scroll_view);
        rv_searchid = findViewById(R.id.rv_searchid);
        et_search = findViewById(R.id.et_search);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        rv_searchid.setLayoutManager(layoutManager);

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        DialogInterface.OnClickListener chatListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
                new AlertDialog.Builder(Chating_searchid.this)
                        .setTitle(model.getId())
                        .setPositiveButton("메세지 보내기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkrequestQueue();
                                String userId = AutoLogin.getUserId(getApplicationContext());
                                CheckRoom(userId,model.getId());
                            }
                        })
                        .setNeutralButton("취소", cancelListener)
                        .setNegativeButton("프로필", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent mIntent = new Intent(getApplicationContext(), Profile.class);
                                mIntent.putExtra("Maker",model.getId());
                                startActivity(mIntent);
                            }
                        })
                        .show();
            }
        });


        rv_searchid.setAdapter(myAdapter);
        rv_searchid.setHasFixedSize(true);


        checkrequestQueue();
        String userId = AutoLogin.getUserId(this);
        getFollowList(userId);


        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().equals("")){
                    checkrequestQueue();
                    String userId = AutoLogin.getUserId(getApplicationContext());
                    getFollowList(userId);
                }else {
                    checkrequestQueue();
                    page = 1;
                    getsearchuser(charSequence.toString(), page, limit);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



    }


    public void CheckRoom(String myID,String targetID) {
        //내아이디와 채팅을 하려고하는 아이디를 넘겨줘서 두명이 속한 방이 있는지 체크한다.

        String url = "http://13.209.19.188/CheckRoom.php?myID="+myID+"&targetID="+targetID;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int RoomNum = jsonObject.getInt("response");
                            Intent messageIntent = new Intent(getApplicationContext(), Chat.class);
                            messageIntent.putExtra("RoomNum",RoomNum);
                            messageIntent.putExtra("targetID",targetID);
                            startActivity(messageIntent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.e("상대방과 같은 방에 속해있는지", "onResponse: 응답 : " + response);


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

    public void getFollowList(String myID) {
        String url = "http://13.209.19.188/GetFollowingList.php?myID="+myID;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int followingnum = jsonObject.getInt("followingnum");
                            if(followingnum>0) {
                                JSONArray followingIDlist = jsonObject.getJSONArray("followingIDlist");
                                JSONArray followingIMGlist = jsonObject.getJSONArray("followingIMGlist");
                                myData.clear();
                                for (int i = 0; i < followingnum; i++) {
                                    myData.add(new MyData(followingIDlist.getString(i),followingIMGlist.getString(i)));
                                }
                                myAdapter.setItems(myData);
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

    public void getsearchuser(String str,int pagee,int limit) {
        String url = "http://13.209.19.188/searchUser.php?searchstr="+str+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean isset= jsonObject.getBoolean("isset");
                            if(isset) {
                                boolean isset_page = jsonObject.getBoolean("isset_page");
                                if(isset_page) {
                                    JSONArray idList = jsonObject.getJSONArray("idList");
                                    JSONArray profileimgList = jsonObject.getJSONArray("profileimgList");
                                    myData.clear();
                                    String userId = AutoLogin.getUserId(getApplicationContext());
                                    for (int i = 0; i < idList.length(); i++) {
                                        if(!idList.getString(i).equals(userId)) { //로그인한 아이디와 같지 않을때만 아이템에 넣어준다
                                            myData.add(new MyData(idList.getString(i), profileimgList.getString(i)));
                                        }
                                    }
                                    myAdapter.setItems(myData);
                                }
                            }else{
                                myData.clear();
                                myAdapter.setItems(myData);
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

    void checkrequestQueue(){
        if (requestQueue == null){
            //RequestQueue 객체 생성하기

            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다
        actionBar.setDisplayHomeAsUpEnabled(true); //뒤로가기
    }


    // 리사이클러뷰 아이템에 추가할 데이터 클래스
    public static class MyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트
        String id;
        String img;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public MyData(String id, String img) {
            this.id = id;
            this.img = img;
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
        }

        private OnMyClickListener mListener;

        private List<MyData> mItems = new ArrayList<>();

        public MyAdapter() {}

        public MyAdapter(OnMyClickListener listener) {
            mListener = listener;
        }

        public void setItems(List<MyData> items) {
            this.mItems = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_searchuser, parent, false);
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
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MyData item = mItems.get(position);
            // TODO : 데이터를 뷰홀더에 표시하시오
            holder.tv_searchuser_id.setText(item.getId());

            if(item.getImg().equals("Nothing") || item.getImg().equals("null")) {
                Glide.with(holder.itemView)
                        .load(R.drawable.myprofile)
                        .placeholder(R.drawable.dataloading)
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .into(holder.iv_searchuser_profileimg);
            }else {
                Glide.with(holder.itemView)
                        .load("http://13.209.19.188/"+item.getImg())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                        .into(holder.iv_searchuser_profileimg);
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            TextView tv_searchuser_id;
            CircleImageView iv_searchuser_profileimg;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : 뷰홀더 코드를 입력하여 주세요
                tv_searchuser_id = itemView.findViewById(R.id.tv_searchuser_id);
                iv_searchuser_profileimg = itemView.findViewById(R.id.iv_searchuser_profileimg);
            }
        }
    }
}