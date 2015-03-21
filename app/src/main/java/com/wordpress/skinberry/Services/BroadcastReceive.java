package com.wordpress.skinberry.Services;

import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.wordpress.skinberry.utils.ConnectionDetector;

public class BroadcastReceive extends BroadcastReceiver {
    Context c;
    @Override
    public void onReceive(Context arg0, Intent arg1) {
        ConnectionDetector cd = new ConnectionDetector(arg0);
        Boolean isInternetPresent = false;
        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            this.c=arg0;
            arg0.startService(new Intent(c, BroadcastService.class));
            RescheduleService scheduler=new RescheduleService();
            Timer mTimer=new Timer();
            try{
                mTimer.schedule(scheduler, 30000);
            }catch(Exception e){
                Toast.makeText(arg0, "Receiver Error",Toast.LENGTH_LONG).show();
            }
        }
    }
    class RescheduleService extends TimerTask{
        @Override
        public void run() {
            Intent i = new Intent(c,BroadcastService.class);
            if (c.startService(i)!=null){
                c.startService(i);
            }
        }
    }

}

