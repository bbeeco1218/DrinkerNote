package com.example.drinkernote;
import android.widget.Toast;
import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class frag_search extends Fragment {
    private View view;
    private String TAG = "프래그 서치";
    EditText et_search;
    Button btn_cancel,btn_submit;
    FrameLayout frame_search;
    Fragment frag_allList;
    Fragment frag_inSearch;
    searchClicklistner searclistner;


    public frag_search(Fragment frag_allList, Fragment frag_inSearch) {
        this.frag_allList = frag_allList;
        this.frag_inSearch = frag_inSearch;
    }





    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        Log.e(TAG, "onattach");
        if (context instanceof searchClicklistner) {
            searclistner = (searchClicklistner) context;
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_search, frag_allList).commitAllowingStateLoss();


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_search, container, false);

        et_search = view.findViewById(R.id.et_search);
        btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_submit = view.findViewById(R.id.btn_submit);
        frame_search = view.findViewById(R.id.frame_search);






        //에딧텍스트가 포커스 될때의 리스너
        et_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

                if(b){ //에딧텍스트가 포커스된다면 프레임에 검색창을 띄운다.
                    transaction.replace(R.id.frame_search, frag_inSearch).commitAllowingStateLoss();
                }
            }
        });

        //취소 버튼 클릭시
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //에딧텍스트의 포커스를 지우고 키보드를 내린다.
                et_search.setText("");
                et_search.clearFocus();
                InputMethodManager mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                mInputMethodManager.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_search, frag_allList).commitAllowingStateLoss();


            }
        });

        //검색버튼 클릭시
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchStr = et_search.getText().toString();
                if(searchStr.equals("")){
                    Toast.makeText(getContext(), "검색어가 없습니다.", Toast.LENGTH_SHORT).show();
                    et_search.requestFocus();
                }else{


                    getChildFragmentManager().beginTransaction().replace(R.id.frame_search, frag_inSearch).commitAllowingStateLoss();
                    et_search.requestFocus();

                    searclistner.onsearchclicked(searchStr);

                }
            }
        });
        return view;
    }

}
