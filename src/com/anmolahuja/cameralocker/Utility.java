package com.anmolahuja.cameralocker;

import org.holoeverywhere.app.Application;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;

import com.anmolahuja.cameralocker.preferences.CameraLockerPreferenceManager;

public class Utility extends Application
{

	@Override
	public void onCreate()
	{
		super.onCreate();
		if( CameraLockerPreferenceManager.instance( this ).isFirstRun() )
		{
			// shouldn't really be needed, it's specified in the manifest.
			if( !getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) )
			{
				// TODO do something
			}
		}
	}

    public static void showNotification( Context context, int id, String title, String body, Intent activityIntent )
    {
    	NotificationCompat.Builder builder = new NotificationCompat.Builder( context )
    		.setSmallIcon( R.drawable.ic_notification )
	        .setContentTitle( title )
	        .setDefaults( Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL )
	        .setContentText( body );
    	builder.setContentIntent( PendingIntent.getActivity( context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT ) );
    	NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	notificationManager.notify( id, builder.build() );
    }

}
