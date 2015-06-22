package com.example.music.services;

import com.example.music.activities.MainActivity;
import com.example.music.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

public class MyBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		try{
			if (extras != null) {
				String state = extras.getString(TelephonyManager.EXTRA_STATE);
				if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
					if(new Utils(context).isMyServiceRunning(PlayBackPagerService.class)){
						if(MainActivity.musicBound && MainActivity.musicSrv.isPng())
							MainActivity.musicSrv.pausePlayer();
					}else if(state.equals(TelephonyManager.EXTRA_STATE_IDLE) ){
						MainActivity.musicSrv.Resume();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
