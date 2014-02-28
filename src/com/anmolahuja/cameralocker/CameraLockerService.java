package com.anmolahuja.cameralocker;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.anmolahuja.cameralocker.preferences.CameraLockerPreferenceManager;

public class CameraLockerService extends Service
{
	private static final int m_notificationID = 123;

	private static final String LOG_TAG = "CameraLocker Service";

	public static final String CAMERALOCK_ACTION = "toggle_action";
	public static final int ACTION_LOCK = 2;
	public static final int ACTION_UNLOCK = 3;
	public static final int ACTION_TOGGLE = 4;

	private SharedPreferences m_statePreferences;
	private static final String CAMERAS_LOCKED_KEY = "cameras_locked";

	private CameraLockerPreferenceManager m_preferences;
	private boolean m_cameraLocked = false;

	private Thread m_monitorThread = null;
	private static final long CHECK_INTERVAL_MILLISECONDS = 300;
	private PackageData m_packageData;

	BroadcastReceiver m_preferencesChangeReceiver;
	@Override
	public void onCreate()
	{
		super.onCreate();
		m_preferencesChangeReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive( Context arg0, Intent arg1 )
			{
				loadPreferencesAndInit();
			}
		};
		LocalBroadcastManager.getInstance( this ).registerReceiver(
				m_preferencesChangeReceiver,
				new IntentFilter( CameraLockerPreferenceManager.ACTION_PREFERENCE_CHANGED ) );
		Log.w( LOG_TAG, "CameraLocker service started." );
		m_statePreferences = getSharedPreferences( "CameraLockerServiceState",
				Context.MODE_PRIVATE );
		m_preferences = CameraLockerPreferenceManager.instance( this );
		if( savedLockState() )
		{
			Log.e( LOG_TAG, "The app was killed!" );
			// TODO warn user app was killed, probably log every 5minutes that the app's alive.
		}
		m_packageData = PackageData.instance( this );
		loadPreferencesAndInit();
	}

	private void loadPreferencesAndInit()
	{
		if( !m_preferences.isEnabled() )
		{
			stopSelf();
			return;
		}

		startForeground();
		Log.v( LOG_TAG, "Restoring lock state..." );
		if( savedLockState() )
		{
			Log.v( LOG_TAG, "Saved state: locked" );
			if( lock() )
			{
				Log.v( LOG_TAG, "Lock state successfully restored." );
			}
			else
			{
				// TODO warn?
			}
		}
		if( m_preferences.isAutounlockEnabled() )
			startMonitorThread();
		else
			stopMonitorThread();
	}

	private void startForeground()
	{
		Intent intent = new Intent( getApplicationContext(),
				CameraLockerService.class );
		intent.putExtra( CAMERALOCK_ACTION, ACTION_TOGGLE );
		PendingIntent pendingIntent = PendingIntent.getService(
				getApplicationContext(), 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT );
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this ).setSmallIcon( R.drawable.ic_notification )
				.setContentTitle( getString( R.string.app_name ) )
				.setOnlyAlertOnce( true )
				.setContentText( getString( R.string.Status ) );

		if( !m_cameraLocked )
		{
			builder.addAction( R.drawable.ic_lockscreen_lock_normal,
					getString( R.string.Lock ), pendingIntent );
		}
		else
		{
			builder.addAction( R.drawable.ic_lockscreen_unlock_normal,
					getString( R.string.Unlock ), pendingIntent );
		}

		PendingIntent settingsPendingIntent = PendingIntent.getActivity( this,
				0, new Intent( this, MainActivity.class ),
				PendingIntent.FLAG_UPDATE_CURRENT );
		builder.setContentIntent( settingsPendingIntent );
		startForeground( m_notificationID, builder.build() );
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId )
	{
		Bundle bundle = ( intent != null ? intent.getExtras() : null );
		if( bundle != null )
		{
			int lockAction = intent.getExtras().getInt( CAMERALOCK_ACTION );
			switch( lockAction )
			{
			case 0:
				Log.v( LOG_TAG, "No CAMERALOCK_ACTION in intent." );
				break;

			case ACTION_LOCK:
				Log.v( LOG_TAG, "Lock action in intent." );
				lock();
				break;

			case ACTION_UNLOCK:
				Log.v( LOG_TAG, "Unlock action in intent." );
				unlock();
				break;

			case ACTION_TOGGLE:
				if( m_cameraLocked )
					unlock();
				else
					lock();
				break;
			}
		}
		return START_STICKY;
	}

	private boolean lock()
	{
		if( m_cameraLocked )
		{
			Log.v( LOG_TAG, "Camera already locked." );
			return true;
		}
		Log.v( LOG_TAG, "Attempting to lock front camera." );
		saveLockState( true );
		m_cameraLocked = CameraLocker.instance().lockFirst();
		startForeground();
		return m_cameraLocked;
	}

	private void unlock()
	{
		if( !m_cameraLocked )
		{
			Log.v( LOG_TAG, "Camera already unlocked." );
			return;
		}
		Log.v( LOG_TAG, "Unlocking all locked cameras." );
		saveLockState( false );
		CameraLocker.instance().unlockAll();
		m_cameraLocked = false;
		startForeground();
	}

	@Override
	public IBinder onBind( Intent intent )
	{
		return null;
	}

	private void saveLockState( boolean locked )
	{
		m_statePreferences.edit().putBoolean( CAMERAS_LOCKED_KEY, locked ).apply();
	}

	private boolean savedLockState()
	{
		return m_statePreferences.getBoolean( CAMERAS_LOCKED_KEY, false );
	}

	private void startMonitorThread()
	{
		if( m_monitorThread != null && m_monitorThread.isInterrupted() != true )
			m_monitorThread.interrupt();
		final ActivityManager am = (ActivityManager) getBaseContext()
				.getSystemService( ACTIVITY_SERVICE );
		m_monitorThread = new Thread( new Runnable()
		{
			private static final String LOG_TAG = "CLService::MonitorThread";

			private void restoreLockState( final String packageName )
			{
				while( !Thread.currentThread().isInterrupted() )
				{
					final RunningTaskInfo foregroundTaskInfo = am.getRunningTasks( 1 ).get( 0 );
					final String currentForegroundPackage = foregroundTaskInfo.topActivity.getPackageName();
					if( !currentForegroundPackage.equals( packageName ) )
					{
						if( m_cameraLocked == false ) // ensure the user did not toggle lock while using the app
							lock();
						return;
					}
					try
					{
						Thread.sleep( CHECK_INTERVAL_MILLISECONDS );
					}
					catch( InterruptedException e )
					{
						Thread.currentThread().interrupt();
						e.printStackTrace();
					}
				}
			}

			@Override
			public void run()
			{
				Log.d( LOG_TAG, "In monitor thread..." );
				String lastForegroundPackage = null;
				// how about checking priorities, like:
				// pi.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
				while( !Thread.currentThread().isInterrupted() )
				{
					try
					{
						Thread.sleep( CHECK_INTERVAL_MILLISECONDS );
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
						Thread.currentThread().interrupt();
					}
					RunningTaskInfo foregroundTaskInfo = am.getRunningTasks( 1 ).get( 0 );
					String currentForegroundPackage = foregroundTaskInfo.topActivity.getPackageName();
					if( currentForegroundPackage.equals( lastForegroundPackage ) )
						continue;
					lastForegroundPackage = currentForegroundPackage;
					if( !m_packageData.usesCamera( currentForegroundPackage ) )
						continue;
					Log.d( LOG_TAG, currentForegroundPackage + " uses camera." );
					if( m_packageData.isChecked( currentForegroundPackage ) && m_cameraLocked )
					{
						Log.d( LOG_TAG, currentForegroundPackage
								+ " is allowed camera access, killing app." );
						am.killBackgroundProcesses( currentForegroundPackage );
						Log.d( LOG_TAG, "Unlocking camera." );
						unlock();
						// TODO display toast maybe? Configurable in settings.
						try
						{
							Log.d( LOG_TAG, "Restarting package " + currentForegroundPackage );
							Intent i = getApplicationContext()
									.getPackageManager()
									.getLaunchIntentForPackage( currentForegroundPackage );
							if( i != null )
								startActivity( i );
							restoreLockState( currentForegroundPackage );
						}
						catch( ActivityNotFoundException e )
						{
							Log.e( LOG_TAG, "Shouldn't reach here." );
							e.printStackTrace();
						}
					}
				}
			}
		} );
		m_monitorThread.start();
	}

	private void stopMonitorThread()
	{
		m_monitorThread.interrupt();
	}

	@Override
	public void onDestroy()
	{
		stopMonitorThread();
		unlock();
		stopForeground( true );
		LocalBroadcastManager.getInstance( this ).unregisterReceiver( m_preferencesChangeReceiver );
	}
}
