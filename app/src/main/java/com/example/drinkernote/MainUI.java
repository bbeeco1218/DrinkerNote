package com.example.drinkernote;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainUI extends AppCompatActivity implements searchClicklistner{

    Fragment frag_home;
    Fragment frag_search;
    Fragment frag_myprofile;

    Fragment frag_allList;
    frag_inSearch frag_inSearch;
    frag_searchUser frag_searchUser;
    frag_searchNote frag_searchNote;

    BottomNavigationView bottomNavigationView;

    Toolbar toolbar;
    ActionBar actionBar;
    TextView tv_tittle;
    String From;
    MenuItem Newsbell;
    String myID;

    RequestQueue requestQueue;//해당 변수를 전역변수로 선언해주세요.

    Intent serviceIntent = ChatService.serviceIntent;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Newsbell.setIcon(R.drawable.bell_dot);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu) ;

        Newsbell = menu.getItem(1);
        checkrequestQueue();
        //http통신으로 새로운 알람이 있는지 확인
        myID = AutoLogin.getUserId(this);
        getNewsnum(myID);

//        Newsbell.setIcon(R.drawable.bell_dot);
//        Newsbell.setIcon(R.drawable.bell);
        return true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("MainUI", "onresume");
        checkrequestQueue();
        getNewsnum(myID);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
          case  R.id.newNote:
              Intent newnoteIntent = new Intent(getApplicationContext(), NewNote.class);
              startActivity(newnoteIntent);
              return true;

            case R.id.logout:
                AutoLogin.clearUserId(getApplicationContext());
                Intent logoutIntent = new Intent(getApplicationContext(), Login.class);
                logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                //로그아웃시 채팅 서비스를 중단한다.
                if (serviceIntent!=null) {
                    stopService(serviceIntent);
                    serviceIntent = null;
                }
                startActivity(logoutIntent);
                return true;
            case R.id.message:
                Intent messageIntent = new Intent(getApplicationContext(), Chating.class);
                startActivity(messageIntent);
                return true;

            case R.id.News:
                Newsbell.setIcon(R.drawable.bell);
                Intent mIntent = new Intent(getApplicationContext(), Newspeed.class);
                startActivity(mIntent);

                return true;
            case R.id.option:
                Intent optionIntent = new Intent(getApplicationContext(),option.class);
                startActivity(optionIntent);
                return true;
          default:
            break;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("mainUI", "oncreate");
        setContentView(R.layout.activity_main_ui);
        GetViewModel();
        setToolbar();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,new IntentFilter("news"));


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.home:
                        tv_tittle.setText("Drinker Note");
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_home).commitAllowingStateLoss();

                        break;
                    case R.id.search:
                        tv_tittle.setText("Drinker Note");

                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_search).commitAllowingStateLoss();
//                        frag_search.getChildFragmentManager().beginTransaction().replace(R.id.frame_search,frag_allList)
                        break;
                    case R.id.myprofile:
                        String userId = AutoLogin.getUserId(getApplicationContext());
                        tv_tittle.setText(userId);
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_myprofile).commitAllowingStateLoss();
                        break;
                }
                return true;
            }
        });

        Intent mIntent = getIntent();
        boolean restartService = mIntent.getBooleanExtra("restartService",false);
//        Log.e("restartService", String.valueOf(restartService));
        if(restartService){
            int RoomNum = mIntent.getIntExtra("RoomNum",-1);
            String targetID = mIntent.getStringExtra("targetID");

            Intent mmIntent = new Intent(this,Chating.class);
            mmIntent.putExtra("restartService",restartService);
            mmIntent.putExtra("RoomNum",RoomNum);
            mmIntent.putExtra("targetID",targetID);
            startActivity(mmIntent);


        }else {
            From = mIntent.getStringExtra("from");
            if (From != null) {
                if (From.equals("NewNote")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_myprofile).commitAllowingStateLoss();
                    bottomNavigationView.setSelectedItemId(R.id.myprofile);
                } else {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_home).commitAllowingStateLoss();
                }
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_home).commitAllowingStateLoss();
            }
        }
    }



    void checkrequestQueue() {
        if (requestQueue == null) {
            //RequestQueue 객체 생성하기

            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    public void getNewsnum(String myID) {
        String url = "http://13.209.19.188/GetNewsnum.php?myID="+myID;
//        Log.e("getnewsnum", "ishere?");
        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override //서버 응답시 처리할 내용
                    public void onResponse(String response) {

//                        Log.e("TAG", "onResponse: 응답 : " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int Newsnum = jsonObject.getInt("response");
                            if(Newsnum > 0) {
                                Newsbell.setIcon(R.drawable.bell_dot);
                            }else{
                                Newsbell.setIcon(R.drawable.bell);
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

    void GetViewModel() {
//        Log.e("new frag", "new!!!!");

        frag_searchNote = new frag_searchNote();
        frag_searchUser = new frag_searchUser();


        frag_inSearch = new frag_inSearch(frag_searchUser, frag_searchNote);
        frag_allList = new frag_allList();


        frag_home = new frag_home();
        frag_search = new frag_search(frag_allList, frag_inSearch);
        frag_myprofile = new frag_myprofile();

//        frag_search.getChildFragmentManager().beginTransaction().replace(R.id.frame_search,frag_allList).commitAllowingStateLoss();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        tv_tittle = findViewById(R.id.tv_tittle);

    }
    void setToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다
//        actionBar.setDisplayHomeAsUpEnabled(true); //뒤로가기
    }


    @Override
    public void onsearchclicked(String searchStr) {
        int position = frag_inSearch.getposition(); //현재 선택되어있는 탭포지션을 가져온다
        if(position == 0){ //0번탭 (유저검색)
            frag_searchUser.setstr(searchStr);
        }else{ //1번탭 (노트검색)
            frag_searchNote.setstr(searchStr);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("메인ui", "ondestory");
        if (serviceIntent!=null) {
            stopService(serviceIntent);
            serviceIntent = null;
        }
    }
}