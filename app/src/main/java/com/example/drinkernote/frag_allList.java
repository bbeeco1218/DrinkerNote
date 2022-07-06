package com.example.drinkernote;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;


import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class frag_allList extends Fragment{
    private View view;
    GridLayoutManager layoutManager;
    RecyclerView RV_allList_grid;
    NestedScrollView nestedScrollView;
    ArrayList<MyData> myData = new ArrayList<>();
    MyAdapter myAdapter;
    private String TAG = "frag_allList";
    int page = 0, limit = 12;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.


    @Override
    public void onStart() {
        super.onStart();
//        Log.e(TAG, "onstart");
        if(page == 0) {
            page ++;
            if (requestQueue == null) {
                //RequestQueue 객체 생성하기
                requestQueue = Volley.newRequestQueue(getContext());
            }
            getallList(page, limit);
        }else{
            myAdapter.setItems(myData);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        Log.e(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_alllist, container, false);
        nestedScrollView = view.findViewById(R.id.scroll_view);
        RV_allList_grid = view.findViewById(R.id.RV_allList_grid);
        layoutManager = new GridLayoutManager(getContext(), 3);
        RV_allList_grid.setLayoutManager(layoutManager);

        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData model) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
                Intent mIntent = new Intent(getContext(), NoteInfo.class);
                mIntent.putExtra("NoteKey",model.getNotekey());
                startActivity(mIntent);
            }
        });

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
                    getallList(page,limit);
                }
            }
        });

        RV_allList_grid.setAdapter(myAdapter);
        RV_allList_grid.setHasFixedSize(true);


        return view;
    }

    public void getallList(int pagee,int limit) {
        String url = "http://13.209.19.188/GetallList.php?page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int Note_num = jsonObject.getInt("Note_num"); //노트 갯수
                            if(Note_num >0){
                                JSONArray Note_keyList = jsonObject.getJSONArray("Note_keyList"); //노트 키 리스트
                                if(Note_keyList.getString(0).equals("Nothing")){
                                    page--;
                                }else {
                                    JSONObject Note_ImgList = jsonObject.getJSONObject("NoteImgList"); //노트 이미지 리스트
//                                    Log.i("페이징 확인 ", "페이지 : "+pagee +"\n노트키 리스트"+Note_keyList);
                                    for (int i = 0; i < Note_keyList.length(); i++) {
                                        myData.add(new MyData(Note_ImgList.getString(String.valueOf(Note_keyList.get(i))), Note_keyList.getInt(i)));
                                    }
                                }
                                myAdapter.setItems(myData); // 리사이클러뷰 어뎁터에 아이템을 추가한다.

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
        String img;
        int Notekey;

        public int getNotekey() {
            return Notekey;
        }

        public void setNotekey(int notekey) {
            Notekey = notekey;
        }

        public String getImg() {
            return img;
        }
        public void setImg(String img) {
            this.img = img;
        }

        public MyData(String img, int notekey) {
            this.img = img;
            Notekey = notekey;
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
                    .inflate(R.layout.myprofile_grid_item, parent, false);
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
            if(item.getImg().equals("null")){
                Glide.with(holder.itemView)
                        .load(R.drawable.logo)
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.logo) //아무것도 없을때 그림표시
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .into(holder.iv_gridimg);
            }else {
                Glide.with(holder.itemView)
                        .load("http://13.209.19.188/"+item.getImg())
                        .centerCrop() //가운데를 기준으로 크기맞추기
                        .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                        .fallback(R.drawable.logo) //아무것도 없을때 그림표시
                        .into(holder.iv_gridimg);
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            // TODO : 뷰홀더 코드를 입력하여 주세요
            ImageView iv_gridimg;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO : 뷰홀더 코드를 입력하여 주세요
                iv_gridimg = itemView.findViewById(R.id.iv_gridimg);
            }
        }
    }
}
