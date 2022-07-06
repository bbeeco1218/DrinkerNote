package com.example.drinkernote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class comentUpdate_pop extends Activity {

    Button btn_popcancel,btn_popsubmit;
    EditText et_popcoment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_coment_update_pop);


        Intent mIntent = getIntent();
        String contents = mIntent.getStringExtra("contents");
        int position = mIntent.getIntExtra("position",-1);
        int comentkey = mIntent.getIntExtra("comentkey",0);
        et_popcoment= findViewById(R.id.et_popcoment);
        et_popcoment.setText(contents);
        et_popcoment.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);

        btn_popcancel= findViewById(R.id.btn_popcancel);
        btn_popcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
                finish();
            }
        });
        btn_popsubmit= findViewById(R.id.btn_popsubmit);
        btn_popsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent();
                mIntent.putExtra("contents",et_popcoment.getText().toString());
                mIntent.putExtra("comentkey",comentkey);
                mIntent.putExtra("position",position);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
                setResult(RESULT_OK,mIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}