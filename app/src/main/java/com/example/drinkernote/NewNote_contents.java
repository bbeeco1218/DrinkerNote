package com.example.drinkernote;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.Manifest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewNote_contents extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;
    EditText et_contents;

    Float Body,Sweet,Spice,Malty,Fruit,Tannic,Floral;

    ArrayList<Uri> img_list;
    String whisky_name,whisky_label,whisky_cask,whisky_proof,whisky_price;
    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.
    String from;
    int NoteKey;
    WhiskyData whiskyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note_contents);
        setToolbar();
        et_contents = findViewById(R.id.et_contetns);

        Intent mIntent = getIntent();
        img_list = mIntent.getParcelableArrayListExtra("img_list");
        whisky_name = mIntent.getStringExtra("whisky_name");
        whisky_label = mIntent.getStringExtra("whisky_label");
        whisky_cask = mIntent.getStringExtra("whisky_cask");
        whisky_proof = mIntent.getStringExtra("whisky_proof");
        whisky_price = mIntent.getStringExtra("whisky_price");
        Body = mIntent.getFloatExtra("Body",0);
        Sweet = mIntent.getFloatExtra("Sweet",0);
        Spice = mIntent.getFloatExtra("Spice",0);
        Malty = mIntent.getFloatExtra("Malty",0);
        Fruit = mIntent.getFloatExtra("Fruit",0);
        Tannic = mIntent.getFloatExtra("Tannic",0);
        Floral = mIntent.getFloatExtra("Floral",0);
        from = mIntent.getStringExtra("from");
        if(from != null) {
            if (from.equals("Update")) {
                NoteKey = mIntent.getIntExtra("NoteKey", 0);
                whiskyData = (WhiskyData) mIntent.getSerializableExtra("Notedata");
                et_contents.setText(whiskyData.getContents());
            } else {
                from = "";
            }
        }


        //파일업로드 권한 받기
        int permissionResult = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionResult == PackageManager.PERMISSION_DENIED){
            String[] permissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions,10);
        }




    }
    //액션바 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contents_toolbar_menu, menu) ;

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
            case R.id.toolbar_menu_fin: { //toolbar의 다음 눌렀을 때 동작

                String contents = et_contents.getText().toString();
                WhiskyData whiskyData = new WhiskyData(whisky_name, whisky_label, whisky_cask, whisky_proof, whisky_price, contents, Body, Sweet, Spice, Malty, Fruit, Tannic, Floral);
                Gson gson = new Gson();
                String json_whiskyData = gson.toJson(whiskyData);


                if (requestQueue == null) {
                    //RequestQueue 객체 생성하기
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                }
                makeRequest(json_whiskyData);
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
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다
        actionBar.setDisplayHomeAsUpEnabled(true); //뒤로가기
    }
    public void makeRequest(String json_whiskyData) {
        String url = "http://13.209.19.188/InsertNote.php";

        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String res = jsonObject.getString("response");
                            if (res.equals("true")) {
                                Toast.makeText(getApplicationContext(), "노트를 성공적으로 저장했습니다.", Toast.LENGTH_SHORT).show();
                                Intent mIntent = new Intent(getApplicationContext(), MainUI.class);
                                mIntent.putExtra("from","NewNote");
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                startActivity(mIntent);

                            } else {
                                Toast.makeText(getApplicationContext(), "노트를 저장하는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                Intent mIntent = new Intent(getApplicationContext(), MainUI.class);
                                mIntent.putExtra("from","NewNote");
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                startActivity(mIntent);
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
        request.addStringParam("Note_tittle", whisky_name+" "+whisky_label+" "+whisky_cask);
        request.addStringParam("Note_obj", json_whiskyData);
        String userId = AutoLogin.getUserId(getApplicationContext());
        request.addStringParam("Note_maker", userId);
        request.addStringParam("imgsize", String.valueOf(img_list.size()));


        ArrayList <String> imgarray = new ArrayList<>();
        ArrayList<Integer> imgflag = new ArrayList<>();
        for(int i=0; i<img_list.size(); i++) {
            // uri 절대 경로 구하기
            String abUri = getPathFromUri(getApplicationContext(),img_list.get(i));
            StringBuffer sb = new StringBuffer(abUri);
            sb.deleteCharAt(0);
            if(sb.substring(0,7).equals("PostImg")){
                abUri = sb.toString();
                imgarray.add(abUri);
                imgflag.add(1);
            }else{
                request.addFile("image"+i, abUri);
                imgarray.add("image"+i);
                imgflag.add(0);
            }
        }


        if(from != null) {
            if (from.equals("Update")) {
                request.addStringParam("from", "Update");
                Gson gson = new Gson();
                String json_imgArray = gson.toJson(imgarray);
                String json_imgflag = gson.toJson(imgflag);

                request.addStringParam("ImgArray", json_imgArray);
                request.addStringParam("ImgFlag", json_imgflag);
                request.addStringParam("NoteKey", String.valueOf(NoteKey));
            }
        }




        request.setShouldCache(false); //이미 사용한 것은 제거
        requestQueue.add(request);
        Log.d("TAG", "요청 보냄.");

    }



    public String getPathFromUri(Context context, Uri uri){

        String path;
        String[] proj = {MediaStore.Images.Media.DATA} ;

        Cursor cursor = context.getContentResolver().query(uri,proj , null,null,null);

        if(cursor == null){
            path = uri.getEncodedPath();

        }else{
            cursor.moveToNext();
            path = cursor.getString((cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
            cursor.close();
        }






        return path;
    }
}



