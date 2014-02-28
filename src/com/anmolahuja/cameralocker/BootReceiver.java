package com.anmolahuja.cameralocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver
{
	private static final String LOG_TAG = "BootReceiver";

	@Override
	public void onReceive( Context context, Intent intent )
	{
		if( intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) )
		{
			Log.d( LOG_TAG, "Starting CameraLocker service" );
            Intent serviceIntent = new Intent();
            serviceIntent.setClass( context, CameraLockerService.class );
            context.startService( serviceIntent );
		}
	}
}
