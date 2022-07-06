package com.example.drinkernote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class optionalam extends AppCompatActivity {


    Switch allAlam,msgAlam,likeAlam,followAlam,comentAlam,replyAlam;
    optionset optionSet;
    Toolbar toolbar;
    ActionBar actionBar;
    TextView tv_replyAlam,tv_comentAlam,tv_likeAlam,tv_msgAlam,tv_followAlam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optionalam);
        setToolbar();

        allAlam = findViewById(R.id.allAlam);
        msgAlam = findViewById(R.id.msgAlam);
        likeAlam = findViewById(R.id.likeAlam);
        followAlam = findViewById(R.id.followAlam);
        comentAlam = findViewById(R.id.comentAlam);
        replyAlam = findViewById(R.id.replyAlam);

        tv_replyAlam = findViewById(R.id.tv_replyAlam);
        tv_comentAlam = findViewById(R.id.tv_comentAlam);
        tv_likeAlam = findViewById(R.id.tv_likeAlam);
        tv_msgAlam = findViewById(R.id.tv_msgAlam);
        tv_followAlam = findViewById(R.id.tv_followAlam);



        optionSet =  AutoLogin.getUseroptionset(this);
        if(optionSet == null){
            optionSet = new optionset();
        }
        allAlam.setChecked(optionSet.isAllAlam());

        msgAlam.setChecked(optionSet.isMsgAlam());
        if(msgAlam.isChecked()){
            tv_msgAlam.setText("받기");
        }else{
            tv_msgAlam.setText("끄기");
        }
        likeAlam.setChecked(optionSet.isNotelikeAlam());
        if(likeAlam.isChecked()){
            tv_likeAlam.setText("받기");
        }else{
            tv_likeAlam.setText("끄기");
        }
        followAlam.setChecked(optionSet.isFollowAlam());
        if(followAlam.isChecked()){
            tv_followAlam.setText("받기");
        }else{
            tv_followAlam.setText("끄기");
        }
        comentAlam.setChecked(optionSet.isComentAlam());
        if(comentAlam.isChecked()){
            tv_comentAlam.setText("받기");
        }else{
            tv_comentAlam.setText("끄기");
        }
        replyAlam.setChecked(optionSet.isReplyAlam());
        if(replyAlam.isChecked()){
            tv_replyAlam.setText("받기");
        }else{
            tv_replyAlam.setText("끄기");
        }




        allAlam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(allAlam.isChecked()) {
                    msgAlam.setChecked(true);
                    likeAlam.setChecked(true);
                    followAlam.setChecked(true);
                    comentAlam.setChecked(true);
                    replyAlam.setChecked(true);
                }else{
                    msgAlam.setChecked(false);
                    likeAlam.setChecked(false);
                    followAlam.setChecked(false);
                    comentAlam.setChecked(false);
                    replyAlam.setChecked(false);
                }
            }
        });


        msgAlam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    tv_msgAlam.setText("끄기");
                    allAlam.setChecked(false);
                }else{
                    tv_msgAlam.setText("받기");
                    if(msgAlam.isChecked() && likeAlam.isChecked() && followAlam.isChecked() && comentAlam.isChecked() && replyAlam.isChecked()){
                        allAlam.setChecked(true);
                    }
                }
            }
        });
        likeAlam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    allAlam.setChecked(false);
                    tv_likeAlam.setText("끄기");
                }else{
                    tv_likeAlam.setText("받기");
                    if(msgAlam.isChecked() && likeAlam.isChecked() && followAlam.isChecked() && comentAlam.isChecked() && replyAlam.isChecked()){
                        allAlam.setChecked(true);
                    }
                }
            }
        });
        followAlam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    allAlam.setChecked(false);
                    tv_followAlam.setText("끄기");
                }else{
                    tv_followAlam.setText("받기");
                    if(msgAlam.isChecked() && likeAlam.isChecked() && followAlam.isChecked() && comentAlam.isChecked() && replyAlam.isChecked()){
                        allAlam.setChecked(true);
                    }
                }
            }
        });
        comentAlam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    allAlam.setChecked(false);
                    tv_comentAlam.setText("끄기");
                }else{
                    tv_comentAlam.setText("받기");
                    if(msgAlam.isChecked() && likeAlam.isChecked() && followAlam.isChecked() && comentAlam.isChecked() && replyAlam.isChecked()){
                        allAlam.setChecked(true);
                    }
                }
            }
        });
        replyAlam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    allAlam.setChecked(false);
                    tv_replyAlam.setText("끄기");
                }else{
                    tv_replyAlam.setText("받기");
                    if(msgAlam.isChecked() && likeAlam.isChecked() && followAlam.isChecked() && comentAlam.isChecked() && replyAlam.isChecked()){
                        allAlam.setChecked(true);
                    }
                }
            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        optionSet.setAllAlam(allAlam.isChecked());
        optionSet.setMsgAlam(msgAlam.isChecked());
        optionSet.setNotelikeAlam(likeAlam.isChecked());
        optionSet.setFollowAlam(followAlam.isChecked());
        optionSet.setComentAlam(comentAlam.isChecked());
        optionSet.setReplyAlam(replyAlam.isChecked());


//        Gson gson = new Gson();
//        optionOBJ = gson.toJson(optionSet);
        AutoLogin.setoption(this,optionSet);
//        Log.e("onDestroy", "allalam : " + optionSet.isAllAlam());
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
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
        actionBar.setDisplayShowTitleEnabled(false);//기본 제목을 없애줍니다
        actionBar.setDisplayHomeAsUpEnabled(true); //뒤로가기
    }
}