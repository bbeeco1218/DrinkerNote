package com.example.drinkernote;
import android.util.Log;

import android.graphics.Bitmap;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class Send implements Runnable {
    DataOutputStream out;

    String contents;
    String type;


    public Send(DataOutputStream out, String contents,String type) {
        this.contents = contents;
        this.out = out;
        this.type = type;
    }


    public void run() {

        try {
            if(type.equals("read")){
                out.writeUTF(type);                //서버로 전송
                out.writeUTF(contents);                //서버로 전송
            }else if(type.equals("news")){
                out.writeUTF(type);                //서버로 전송
                out.writeUTF(contents);                //서버로 전송
            }else{
                out.writeUTF(contents);                //서버로 전송
            }

        } catch (Exception e) {


        }
    }

}