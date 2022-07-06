package com.example.drinkernote;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

public class NewNote_taste extends AppCompatActivity {
    Toolbar toolbar;
    ActionBar actionBar;

    RatingBar RB_BODY,RB_SWEET,RB_SPICE,RB_MALTY,RB_FRUIT,RB_TANNIC,RB_FLORAL;
    Float Body,Sweet,Spice,Malty,Fruit,Tannic,Floral;

    ArrayList<Uri> img_list;
    String whisky_name,whisky_label,whisky_cask,whisky_proof,whisky_price;

    String from;
    int NoteKey;

    WhiskyData whiskyData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note_taste);
        setToolbar();
        GetViewModel();
        Intent mIntent = getIntent();

        img_list = mIntent.getParcelableArrayListExtra("img_list");
        whisky_name = mIntent.getStringExtra("whisky_name");
        whisky_label = mIntent.getStringExtra("whisky_label");
        whisky_cask = mIntent.getStringExtra("whisky_cask");
        whisky_proof = mIntent.getStringExtra("whisky_proof");
        whisky_price = mIntent.getStringExtra("whisky_price");

        from = mIntent.getStringExtra("from");
        if(from != null) {
            if (from.equals("Update")) {
                NoteKey = mIntent.getIntExtra("NoteKey", 0);
                whiskyData = (WhiskyData) mIntent.getSerializableExtra("Notedata");
                RB_BODY.setRating(whiskyData.getBody());
                RB_SWEET.setRating(whiskyData.getSweet());
                RB_SPICE.setRating(whiskyData.getSpice());
                RB_MALTY.setRating(whiskyData.getMalty());
                RB_FRUIT.setRating(whiskyData.getFruit());
                RB_TANNIC.setRating(whiskyData.getTannic());
                RB_FLORAL.setRating(whiskyData.getFloral());
            } else {
                from = "";
            }
        }

        RB_BODY.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Body = v;
            }
        });
        RB_SWEET.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Sweet = v;
            }
        });
        RB_SPICE.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Spice = v;
            }
        });
        RB_MALTY.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Malty = v;
            }
        });
        RB_FRUIT.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Fruit = v;
            }
        });
        RB_TANNIC.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Tannic = v;
            }
        });
        RB_FLORAL.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Floral = v;
            }
        });



    }

    void GetViewModel(){
        RB_BODY = findViewById(R.id.RB_BODY);
        RB_SWEET = findViewById(R.id.RB_SWEET);
        RB_SPICE = findViewById(R.id.RB_SPICE);
        RB_MALTY = findViewById(R.id.RB_MALTY);
        RB_FRUIT = findViewById(R.id.RB_FRUIT);
        RB_TANNIC = findViewById(R.id.RB_TANNIC);
        RB_FLORAL = findViewById(R.id.RB_FLORAL);


    }
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
                Intent mIntent = new Intent(getApplicationContext(), NewNote_contents.class);

                mIntent.putExtra("img_list",img_list);
                mIntent.putExtra("whisky_name",whisky_name);
                mIntent.putExtra("whisky_label",whisky_label);
                mIntent.putExtra("whisky_cask",whisky_cask);
                mIntent.putExtra("whisky_proof",whisky_proof);
                mIntent.putExtra("whisky_price",whisky_price);
                mIntent.putExtra("Body",RB_BODY.getRating());
                mIntent.putExtra("Sweet",RB_SWEET.getRating());
                mIntent.putExtra("Spice",RB_SPICE.getRating());
                mIntent.putExtra("Malty",RB_MALTY.getRating());
                mIntent.putExtra("Fruit",RB_FRUIT.getRating());
                mIntent.putExtra("Tannic",RB_TANNIC.getRating());
                mIntent.putExtra("Floral",RB_FLORAL.getRating());
                if(from != null) {
                    if (from.equals("Update")) {
                        mIntent.putExtra("from", from);
                        mIntent.putExtra("NoteKey", NoteKey);
                        mIntent.putExtra("Notedata", whiskyData);
                    }
                }

                startActivity(mIntent);
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