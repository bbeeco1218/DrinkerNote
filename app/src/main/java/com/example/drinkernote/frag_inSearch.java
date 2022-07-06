package com.example.drinkernote;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

public class frag_inSearch extends Fragment {
    private View view;
    Fragment frag_searchUser, frag_searchNote;
    TabLayout tabs;
    private String TAG = "프래그먼트 insearch";

    public frag_inSearch(Fragment frag_searchUser, Fragment frag_searchNote) {
        this.frag_searchUser = frag_searchUser;
        this.frag_searchNote = frag_searchNote;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        view = inflater.inflate(R.layout.frag_insearch, container, false);
        tabs = view.findViewById(R.id.insearch_tabs);

        //탭을 추가한다
        tabs.addTab(tabs.newTab().setText("회원"));
        tabs.addTab(tabs.newTab().setText("노트"));


        //최초 탭 지정
        getChildFragmentManager().beginTransaction().replace(R.id.insearch_mainfram, frag_searchUser).commit();
        tabs.selectTab(tabs.getTabAt(0));


        //탭선택 리스너를 설정한다.
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                int position = tab.getPosition();

                Fragment select = null;
                if (position == 0) {

                    select = frag_searchUser;
                } else {

                    select = frag_searchNote;
                }
                getChildFragmentManager().beginTransaction().replace(R.id.insearch_mainfram, select).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });



        return view;
    }

    public int getposition(){
        return tabs.getSelectedTabPosition();
    }




}
