package com.example.drinkernote;


import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class frag_myprofile extends Fragment {
    private View view;
    private String TAG = "마이프로필 프래그먼트";
    RecyclerView RV_myprofile_grid;
    GridLayoutManager layoutManager;
    CircleImageView iv_profileimg;
    TextView tv_NoteNum,tv_Follower,tv_Following,tv_noPost,textView21,textView23;
    MyAdapter myAdapter;
    String userId;
    NestedScrollView nestedScrollView;
    ArrayList<MyData> mData = new ArrayList<>();
    int page = 0, limit = 12;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.


    @Override
    public void onStart() {
        super.onStart();
        if (requestQueue == null) {
            //RequestQueue 객체 생성하기
            requestQueue = Volley.newRequestQueue(getContext());
        }
        GetMyProfile(userId, page, limit);
        if(page == 0) {
            page++;
            if (requestQueue == null) {
                //RequestQueue 객체 생성하기
                requestQueue = Volley.newRequestQueue(getContext());
            }

            GetMyNote(userId, page, limit);

        }else{
            myAdapter.setItems(mData);
        }
    }





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_myprofile, container, false);
        RV_myprofile_grid = view.findViewById(R.id.RV_myprofile_grid);
        layoutManager = new GridLayoutManager(getContext(), 3);
        RV_myprofile_grid.setLayoutManager(layoutManager);

        iv_profileimg = view.findViewById(R.id.iv_feed_profileimg);
        tv_NoteNum = view.findViewById(R.id.tv_NoteNum);
        tv_Follower = view.findViewById(R.id.tv_Follower);
        tv_Following = view.findViewById(R.id.tv_Following);
        tv_noPost = view.findViewById(R.id.tv_noPost);
        textView21 = view.findViewById(R.id.textView21);
        textView23 = view.findViewById(R.id.textView23);
        nestedScrollView = view.findViewById(R.id.scroll_view);
        userId = AutoLogin.getUserId(getContext());
        myAdapter = new MyAdapter(new MyAdapter.OnMyClickListener() {
            @Override
            public void onMyClicked(MyData mItem) {
                // TODO : 리싸이클러뷰 클릭 이벤트 코드 입력
//                Log.e("포지션 ", String.valueOf(NoteKey));
                Intent mIntent = new Intent(getContext(), NoteInfo.class);
                mIntent.putExtra("NoteKey",mItem.getNotekey());
                startActivity(mIntent);
            }
        });
        RV_myprofile_grid.setAdapter(myAdapter);



        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    page++;
                    GetMyNote(userId,page,limit);
                }
            }
        });



        return view;


    }

    //프로필 이미지, 노트갯수 , 팔로워, 팔로잉 을 셋팅하는메서드
    public void setprofile(String Profile_img,int Note_num,int Follower,int Following){
        //프로필 이미지, 게시글 갯수, 팔로워, 팔로잉 을 셋팅한다
        if(Profile_img.equals("Nothing")){
            Glide.with(iv_profileimg)
                    .load(R.drawable.myprofile)
                    .centerCrop() //가운데를 기준으로 크기맞추기
                    .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                    .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                    .into(iv_profileimg);
        }else {
            Glide.with(iv_profileimg)
                    .load("http://13.209.19.188/" + Profile_img)
                    .centerCrop() //가운데를 기준으로 크기맞추기
                    .placeholder(R.drawable.dataloading) //로딩중일때 그림표시
                    .fallback(R.drawable.myprofile) //아무것도 없을때 그림표시
                    .into(iv_profileimg);
        }
        tv_NoteNum.setText(String.valueOf(Note_num));
        tv_Follower.setText(String.valueOf(Follower));
        tv_Following.setText(String.valueOf(Following));
    }

    //작성한 노트가없을때 리사이클러뷰를 없애고 작성된 노트가없다는 텍스트뷰를 띄우는 메서드
    public void setNoPost(){
        RV_myprofile_grid.setVisibility(View.GONE);
        tv_noPost.setVisibility(View.VISIBLE);
    }

    //작성한 노트가 있을때 노트없다는텍스트뷰를 없애고 리사이클러뷰를 띄우는 메서드
    public void setYesPost(){
        RV_myprofile_grid.setVisibility(View.VISIBLE);
        tv_noPost.setVisibility(View.GONE);
    }


    //서버와 통신해서 프로필에 필요한 정보를 가져오는 메서드
    public void GetMyProfile(String myID,int pagee, int limit) {
        String url = "http://13.209.19.188/SelectMyProfile.php?myID="+myID+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            String Profile_img = jsonObject.getString("Profile_img"); //프로필이미지
                            int Follower = jsonObject.getInt("follower"); //팔로워 갯수
                            int Following = jsonObject.getInt("following"); //팔로잉 갯수
                            int Note_num = jsonObject.getInt("Note_num"); //노트 갯수




                            setprofile(Profile_img, Note_num, Follower, Following); //프로필 셋팅

                            String userId = AutoLogin.getUserId(getContext());
                            //팔로워버튼 클릭리스너
                            View.OnClickListener follower = new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent mIntent = new Intent(getContext(), Follow.class);
                                    mIntent.putExtra("Who",userId);
                                    mIntent.putExtra("From","Follower");
                                    startActivity(mIntent);
                                }
                            };
                            tv_Follower.setOnClickListener(follower);
                            textView21.setOnClickListener(follower);

                            View.OnClickListener following = new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent mIntent = new Intent(getContext(), Follow.class);
                                    mIntent.putExtra("Who",userId);
                                    mIntent.putExtra("From","Following");
                                    startActivity(mIntent);
                                }
                            };
                            tv_Following.setOnClickListener(following);
                            textView23.setOnClickListener(following);

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
    public void GetMyNote(String myID,int pagee, int limit) {
        String url = "http://13.209.19.188/SelectMyProfile.php?myID="+myID+"&page="+pagee+"&limit="+limit;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);


                        try {

                            JSONObject jsonObject = new JSONObject(response);



                            int Note_num = jsonObject.getInt("Note_num"); //노트 갯수



                            if (Note_num > 0) { //노트 갯수가 있다면
                                setYesPost();
                                JSONArray Note_keyList = jsonObject.getJSONArray("Note_keyList"); //노트 키 리스트
                                if(Note_keyList.getString(0).equals("Nothing")){
                                    page--;
                                }else {
                                    JSONObject Note_ImgList = jsonObject.getJSONObject("NoteImgList"); //노트 이미지 리스트
//                                    Log.i("페이징 확인 ", "페이지 : "+pagee +"\n노트키 리스트"+Note_keyList);
                                    for (int i = 0; i < Note_keyList.length(); i++) {
                                        mData.add(new MyData(Note_ImgList.getString(String.valueOf(Note_keyList.get(i))), Note_keyList.getInt(i)));
                                    }
                                }
                                myAdapter.setItems(mData); // 리사이클러뷰 어뎁터에 아이템을 추가한다.

                            }else{
                                setNoPost();
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

        public MyData(String img, int notekey) {
            this.img = img;
            Notekey = notekey;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public int getNotekey() {
            return Notekey;
        }

        public void setNotekey(int notekey) {
            Notekey = notekey;
        }
    }
    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        interface OnMyClickListener {
            void onMyClicked(MyData model);
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
                        int position = viewHolder.getAdapterPosition();
//                        int NoteKey = mItems.get(position).getNotekey();
                        mListener.onMyClicked(mItems.get(position));

                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MyData item = mItems.get(position);
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



