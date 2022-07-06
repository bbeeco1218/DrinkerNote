package com.example.drinkernote;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class makeDate {

    private static class TIME_MAXIMUM
    {
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }


    public static String formatTimeString(String date) {
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        Date tempDate = null;
        try {
            tempDate = fm.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long curTime = System.currentTimeMillis();


        long regTime = tempDate.getTime();

        long diffTime = (curTime - regTime) / 1000;
        String msg = null;
        if (diffTime < TIME_MAXIMUM.SEC) {

            // sec
            msg = "방금 전";

        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {

            // min
            msg = diffTime + "분 전";

        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {

            // hour

            msg = (diffTime) + "시간 전";

        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {

            // day

            msg = (diffTime) + "일 전";

        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {

            // day

            msg = (diffTime) + "달 전";

        } else {

            msg = (diffTime) + "년 전";

        }
        return msg;

    }

    public static String dateformat(String olddate){
        SimpleDateFormat oldformat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat newformat = new SimpleDateFormat( "yyyy년 MM월 dd일");
        String newdate = "";
        try {
            Date old_date = oldformat.parse(olddate);
            newdate = newformat.format(old_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newdate;
    }


    public static String customdateformat(String olddate,String custompattern){
        SimpleDateFormat oldformat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat newformat = new SimpleDateFormat( custompattern);
        String newdate = "";
        try {
            Date old_date = oldformat.parse(olddate);
            newdate = newformat.format(old_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newdate;
    }
}
