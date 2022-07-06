package com.example.drinkernote;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;


public class AutoLogin {

    int a;

    private static final String PREF_USER_ID = "MyAutoLogin";

    // 모든 엑티비티에서 인스턴스를 얻기위함
    static SharedPreferences getSharedPreferences(Context ctx) {
        // return PreferenceManager.getDefaultSharedPreferences(ctx);
        return ctx.getSharedPreferences(PREF_USER_ID, Context.MODE_PRIVATE);
    }

    // 계정 정보 저장 : 로그인 시 자동 로그인 여부에 따라 호출 될 메소드, 해당코드는  userId가 저장된다.
    public static void setUserId(Context ctx, String userId) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_ID, userId);
        editor.commit();
    }

    // 저장된 정보 가져오기 : 현재 저장된 정보를 가져오기 위한 메소드
    public static String getUserId(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_ID, "");
    }

    // 로그아웃 : 자동 로그인 해제 및 로그아웃 시 호출 될 메소드
    public static void clearUserId(Context ctx) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear();
        editor.commit();
    }


    public static void setoption(Context ctx, optionset opstionOBJ) {

        Gson gson = new Gson();
        String optionsetjson = gson.toJson(opstionOBJ);
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(getUserId(ctx), optionsetjson);
        editor.commit();
    }

    public static optionset getUseroptionset(Context ctx) {
        optionset optionSet = null;
        Gson gson = new Gson();
        String optionsetjson = getSharedPreferences(ctx).getString(getUserId(ctx), "");
        if(!optionsetjson.equals("")){
            optionSet = gson.fromJson(optionsetjson,optionset.class);
        }

        return optionSet;
    }

}