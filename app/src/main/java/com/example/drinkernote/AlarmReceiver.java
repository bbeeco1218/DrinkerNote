package com.example.drinkernote;
import android.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e("alarmreceiver", "onReceive");
            Intent in = new Intent(context, RestartService.class);
            context.startForegroundService(in);
        } else {

            Intent in = new Intent(context, ChatService.class);
            context.startService(in);
        }
    }
}
