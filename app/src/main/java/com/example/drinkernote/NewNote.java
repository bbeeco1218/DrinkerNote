package com.example.drinkernote;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


public class NewNote extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;
    TextView tv_imgcount;
    private static final String TAG = "MultiImageActivity";
    ArrayList<Uri> uriList = new ArrayList<>();
    RecyclerView recyclerView;
    MultiImageAdapter adapter;
    img_delete_listner img_delete_listner;
    String from ="";
    int NoteKey;
    private ItemTouchHelper mItemTouchHelper;

    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.


    //액션바 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_note_toolbar_menu, menu) ;

        return true;

    }


    //액션바 아이템 클릭시
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
            case R.id.toolbar_menu_next:{ //toolbar의 다음 눌렀을 때 동작
                if(uriList.size() == 0 ){
                    Toast.makeText(this, "이미지를 한장이상 선택해주세요.", Toast.LENGTH_SHORT).show();
                }else {
                    Intent mIntent = new Intent(getApplicationContext(), NewNote_info.class);

                    if(from != null) {
                        if (from.equals("Update")) {
                            mIntent.putExtra("from", from);
                            mIntent.putExtra("NoteKey", NoteKey);
                        }
                    }

                    mIntent.putExtra("img_list", uriList);
                    startActivity(mIntent);
                }
                return true;
            }

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        setToolbar();
        recyclerView = findViewById(R.id.recyclerView);
        Button btn_getImage = findViewById(R.id.getImage);
        tv_imgcount= findViewById(R.id.tv_imgcount);




        img_delete_listner= new img_delete_listner() {
            @Override
            public void on_delete_click(View v, int position) {
                uriList.remove(position);
                adapter.notifyItemRemoved(position);
                tv_imgcount.setText("위스키와 함께한 사진을 등록하세요. "+ uriList.size()+"/10");
            }

            @Override
            public void on_item_click(View v, int position) {
                //나중에 뷰페이저 써야함
            }
        };


        btn_getImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2222);
            }
        });



        adapter = new MultiImageAdapter(uriList, getApplicationContext(),img_delete_listner);


        recyclerView.setAdapter(adapter);   // 리사이클러뷰에 어댑터 세팅
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));     // 리사이클러뷰 수평 스크롤 적용

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));

        mItemTouchHelper.attachToRecyclerView(recyclerView);



        Intent mIntent = getIntent();
        from = mIntent.getStringExtra("from");
        if(from != null) {
            if (from.equals("Update")) {
                NoteKey = mIntent.getIntExtra("NoteKey", 0);

                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                getNoteImg(NoteKey);
            } else {
                from = "";
            }
        }






    }

    public void getNoteImg(int NoteKey) {
        String url = "http://13.209.19.188/GetNoteImg.php?key="+NoteKey;

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray NoteImg = jsonObject.getJSONArray("NoteImg");
                            for (int i = 0; i < NoteImg.length(); i++){

                                Uri uri = Uri.parse("http://13.209.19.188/"+NoteImg.getString(i));
                                uriList.add(uri);
                            }
                            tv_imgcount.setText("위스키와 함께한 사진을 등록하세요. "+ uriList.size()+"/10");
                            adapter.setItems(uriList);

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

    // 앨범에서 액티비티로 돌아온 후 실행되는 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {   // 어떤 이미지도 선택하지 않은 경우
            Toast.makeText(getApplicationContext(), "이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
        } else {   // 이미지를 하나라도 선택한 경우
            ClipData clipData = data.getClipData();
            Log.e("clipData", String.valueOf(clipData.getItemCount()));

            if (clipData.getItemCount() > 10) {   // 선택한 이미지가 11장 이상인 경우
                Toast.makeText(getApplicationContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
            } else {   // 선택한 이미지가 1장 이상 10장 이하인 경우
                if(uriList.size()<10) {


                    Log.e(TAG, "multiple choice");

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri imageUri = clipData.getItemAt(i).getUri();  // 선택한 이미지들의 uri를 가져온다.
                        try {
                            uriList.add(imageUri);  //uri를 list에 담는다.

                        } catch (Exception e) {
                            Log.e(TAG, "File select error", e);
                        }
                    }
                    tv_imgcount.setText("위스키와 함께한 사진을 등록하세요. "+ uriList.size()+"/10");
                }else if(uriList.size()>=10){
                    Toast.makeText(getApplicationContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                }

                adapter.setItems(uriList);

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

}


interface ItemTouchHelperListener {
    boolean onItemMove(int form_position, int to_position);
    void onItemSwipe(int position);
}


class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private ItemTouchHelperListener listener;

    public ItemTouchHelperCallback(ItemTouchHelperListener listener) { this.listener = listener; }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        int drag_flags = ItemTouchHelper.START|ItemTouchHelper.END;

        return makeMovementFlags(drag_flags,0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return listener.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onItemSwipe(viewHolder.getAdapterPosition());
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }
}




class MultiImageAdapter extends RecyclerView.Adapter<MultiImageAdapter.ViewHolder> implements ItemTouchHelperListener{
    private ArrayList<Uri> mData = null ;
    private Context mContext = null ;
    private img_delete_listner img_delete_listner;
    // 생성자에서 데이터 리스트 객체, Context를 전달받음.
    public MultiImageAdapter(ArrayList<Uri> list, Context context,img_delete_listner listner) {
        mData = list ;
        mContext = context;
        img_delete_listner = listner;
    }

    public void setItems(ArrayList<Uri> data){
        mData = data;

        notifyDataSetChanged();
    }



    @Override
    public boolean onItemMove(int form_position, int to_position) {
        Uri item = mData.get(form_position);
        mData.remove(form_position);
        mData.add(to_position,item);
        notifyItemMoved(form_position, to_position);
        return true;
    }

    @Override
    public void onItemSwipe(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
    }


    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        Button btn_delete;

        ViewHolder(View itemView) {
            super(itemView) ;
            // 뷰 객체에 대한 참조.
            image = itemView.findViewById(R.id.image);
            btn_delete = itemView.findViewById(R.id.btn_img_delete);

            //삭제버튼 클릭시 리스너의 온델리트 클릭 실행
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition ();
                    img_delete_listner.on_delete_click(view,position);
                }
            });


            //아이템클릭시 리스너 의 온아이템클릭 실행
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition ();
                    img_delete_listner.on_item_click(view,position);
                }
            });
        }
    }


    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    // LayoutInflater - XML에 정의된 Resource(자원) 들을 View의 형태로 반환.
    @Override
    public MultiImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;    // context에서 LayoutInflater 객체를 얻는다.
        View view = inflater.inflate(R.layout.multi_image_item, parent, false) ;	// 리사이클러뷰에 들어갈 아이템뷰의 레이아웃을 inflate.
        MultiImageAdapter.ViewHolder vh = new MultiImageAdapter.ViewHolder(view) ;


        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(MultiImageAdapter.ViewHolder holder, int position) {
        Uri image_uri = mData.get(position) ;

        Glide.with(mContext)
                .load(image_uri)
                .into(holder.image);
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }

}