package com.example.drinkernote;

import android.content.Intent;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class frag_searchUser extends Fragment {
    private View view;
    RecyclerView rv_searchuser;
    private String TAG = "searchUser";
    MyAdapter myAdapter;
    LinearLayoutManager layoutManager;
    TextView tv_nosearchUser;
    NestedScrollView nestedScrollView;
    ProgressBar progressBar;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    String searchStr;
    ArrayList<MyData> myData = new ArrayList<>();

    int page = 0, limit = 10;


    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onstart");

//        page = 0;
//        myData.clear();
//        if(page == 0) {
//            page++;
//            if (requestQueue == null) {
//                //RequestQueue 객체 생성하기
//                requestQueue = Volley.newRequestQueue(getContext());
//            }
//
//            getsearchuser(searchStr,page,limit);
//        }else{
            myAdapter.setItems(myData);
//            progressBar.setVisibility(View.GONE);
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_searchuser, container, false);


        rv_searchuser = view.findViewById(R.id.rv_searchuser);
        layoutManager = new LinearLayoutManager(getContext());
        nestedScrollView = view.findViewById(R.id.scroll_view);
        progressBar = view.findViewById(R.id.progress_bar);
        rv_searchuser.setLayoutManager(layoutManager);
        tv_nosearchUser = view.findViewById(R.id.tv_nosearchUser);


        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
                Intent mIntent = new Intent(getContext(), Profile.class);
                mIntent.putExtra("Maker",model.getId());
                startActivity(mIntent);
            }
        });




        rv_searchuser.setAdapter(myAdapter);
        rv_searchuser.setHasFixedSize(true);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    progressBar.setVisibility(View.VISIBLE);
                    page++;
//                    Log.e("페이지", String.valueOf(page));
                    getsearchuser(searchStr, page, limit);
                }
            }
        });



        if (getArguments() != null) {
            page++;
            searchStr = getArguments().getString("searchStr");
            if (requestQueue == null) {
                //RequestQueue 객체 생성하기

                requestQueue = Volley.newRequestQueue(getContext());
            }
            getsearchuser(searchStr,page,limit);
        }





//        myData.add(new MyData("데이터 1"));

        // 어뎁터에 데이터 추가

        return view;
    }



    public void getsearchuser(String str,int pagee,int limit) {
        String url = "http://13.209.19.188/searchUser.php?searchstr="+str+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e(TAG, "onResponse: 응답 : " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean isset= jsonObject.getBoolean("isset");
                            if(isset) {
                                boolean isset_page = jsonObject.getBoolean("isset_page");
                                if(isset_page) {

                                    JSONArray idList = jsonObject.getJSONArray("idList");
                                    JSONArray profileimgList = jsonObject.getJSONArray("profileimgList");
                                    Log.i("페이징 확인 ", "페이지 : "+pagee +"\n아이디 리스트"+idList);
                                    for (int i = 0; i < idList.length(); i++) {
                                        myData.add(new MyData(profileimgList.getString(i), idList.getString(i)));
                                    }
                                    myAdapter.setItems(myData);
                                    tv_nosearchUser.setVisibility(View.GONE);
                                    nestedScrollView.setVisibility(View.VISIBLE);

                                }else{
                                    progressBar.setVisibility(View.GONE);
                                    page --;
                                }
                            }else{
//                                Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                                tv_nosearchUser.setVisibility(View.VISIBLE);
                                nestedScrollView.setVisibility(View.GONE);
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



    // 리사이클러뷰 아이템에 추가할 데이터 클래스
    public static class MyData {
        // TODO : 리사이클러뷰 아이템에 들어갈 텍스트
        String img,id;

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public MyData(String img, String id) {
            this.img = img;
            this.id = id;
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
            //프로필 이미지 표시
            if(item.getImg().equals("null")) {
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

    public void setstr(String str) {
        this.searchStr = str;
//        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
        myData.clear();
        if (requestQueue == null) {
            //RequestQueue 객체 생성하기

            requestQueue = Volley.newRequestQueue(getContext());
        }
        page =1;
        getsearchuser(str, page, limit);
    }
}
