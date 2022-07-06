package com.example.drinkernote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

public class Follow extends AppCompatActivity {

    Toolbar toolbar;
    ActionBar actionBar;

    String fromwhere;
    String whoID;

    Fragment frag_follower;
    Fragment frag_following;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        setToolbar();
        Intent mIntent = getIntent();





        //fromwhere에는 Follower 또는 Following
        fromwhere = mIntent.getStringExtra("From");
        whoID = mIntent.getStringExtra("Who");


        frag_follower = new frag_follower(whoID);
        frag_following = new frag_following(whoID);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("팔로워"));
        tabs.addTab(tabs.newTab().setText("팔로잉"));

        if(fromwhere.equals("Follower")){
            getSupportFragmentManager().beginTransaction().replace(R.id.follow_mainfram,frag_follower).commit();
            tabs.selectTab(tabs.getTabAt(0));
        }else{
            getSupportFragmentManager().beginTransaction().replace(R.id.follow_mainfram,frag_following).commit();
            tabs.selectTab(tabs.getTabAt(1));
        }


        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Fragment select = null;
                if(position ==0){
                    select = frag_follower;
                } else{
                    select = frag_following;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.follow_mainfram,select).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
            default:
                break;
        }
        return false;
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