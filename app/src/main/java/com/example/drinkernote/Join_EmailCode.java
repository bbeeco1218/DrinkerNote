package com.example.drinkernote;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import javax.mail.MessagingException;
import javax.mail.SendFailedException;

public class Join_EmailCode extends AppCompatActivity {
    EditText et_Join_CertificationNumber;
    Button btn_certification;
    TextView tv_Email_check;
    TextView tv_time;
    TextView tv_SendAgain;
    MainHandler mainHandler;


//    인증코드
    String GmailCode;
    String email;
    int mailSend=0;
    static int value;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_email_code);
        GetViewModel();

        Intent mIntent = getIntent();
        email = mIntent.getStringExtra("Email");
        tv_Email_check.setText(email+" 주소로 전송된 인증 코드를 입력 하세요.");
        sendMail(email);
        et_Join_CertificationNumber.requestFocus();
        tv_SendAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMail(email);
                Toast.makeText(getApplicationContext(), "인증코드를 다시 보냈습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btn_certification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GmailCode == "time out") { //시간이 다돼서 인증코드가 바뀌었을때
                    Toast.makeText(getApplicationContext(), "인증시간이 다되었습니다. 다시 인증번호를 전송해주세요.", Toast.LENGTH_SHORT).show();
                }
                //이메일로 전송한 인증코드와 내가 입력한 인증코드가 같을 때
                else if (et_Join_CertificationNumber.getText().toString().equals(GmailCode)) {
                    Intent mIntent = new Intent(getApplicationContext(), Join_IDPW.class);
                    mIntent.putExtra("Email", email);
                    Toast.makeText(getApplicationContext(), "인증에 성공했습니다.", Toast.LENGTH_SHORT).show();
                    startActivity(mIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "인증번호를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    void GetViewModel(){
        et_Join_CertificationNumber = findViewById(R.id.et_Join_CertificationNumber);
        btn_certification = findViewById(R.id.btn_certification);
        tv_Email_check = findViewById(R.id.tv_Email_check);
        tv_time = findViewById(R.id.tv_time);
        tv_SendAgain = findViewById(R.id.tv_SendAgain);
    }

    void sendMail(String email) {

        MailTread mailTread = new MailTread(email);
        mailTread.start();
        if (mailSend == 0) {
            value = 180;
            //쓰레드 객체 생성
            BackgrounThread backgroundThread = new BackgrounThread();
            //쓰레드 스타트
            backgroundThread.start();
            mailSend += 1;
        } else {
            value = 180;
        }
        mainHandler = new MainHandler();
    }

    class MailTread extends Thread{
        String Email;
        public MailTread(String email) {
            this.Email = email;
        }
        public void run(){
            GMailSender gMailSender = new GMailSender("bbeeco1218@gmail.com", "2ljjos3l!!");
            //GMailSender.sendMail(제목, 본문내용, 받는사람);


            //인증코드
            GmailCode=gMailSender.getEmailCode();
            String body = "인증코드 : \"" + GmailCode + "\" 를 화면에 입력해주세요.";
            try {
                gMailSender.sendMail("DrinkerNote 회원가입 이메일 인증", body , Email);
            } catch (SendFailedException e) {

            } catch (MessagingException e) {
                System.out.println("인터넷 문제"+e);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

        //시간초가 카운트 되는 쓰레드
    class BackgrounThread extends Thread{
        //180초는 3분
        //메인 쓰레드에 value를 전달하여 시간초가 카운트다운 되게 한다.

        public void run(){
            //180초 보다 밸류값이 작거나 같으면 계속 실행시켜라
            while(true){
                value-=1;
                try{
                    Thread.sleep(1000);
                }catch (Exception e){

                }

                Message message = mainHandler.obtainMessage();
                //메세지는 번들의 객체 담아서 메인 핸들러에 전달한다.
                Bundle bundle = new Bundle();
                bundle.putInt("value", value);
                message.setData(bundle);

                //핸들러에 메세지 객체 보내기기

                mainHandler.sendMessage(message);

                if(value<=0){
                    GmailCode="time out";
                    btn_certification.setVisibility(View.GONE);

                    break;
                }
            }



        }
    }


    //쓰레드로부터 메시지를 받아 처리하는 핸들러
    //메인에서 생성된 핸들러만이 Ui를 컨트롤 할 수 있다.
    class MainHandler extends Handler {
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            int min, sec;

            Bundle bundle = message.getData();
            int value = bundle.getInt("value");

            min = value/60;
            sec = value % 60;
            //초가 10보다 작으면 앞에 0이 더 붙어서 나오도록한다.
            if(sec<10){
                //텍스트뷰에 시간초가 카운팅
                tv_time.setText("유효시간 "+"0"+min+" : 0"+sec);
            }else {
                tv_time.setText("유효시간 "+"0"+min+" : "+sec);
            }
        }
    }

}