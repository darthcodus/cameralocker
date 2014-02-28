package com.anmolahuja.cameralocker;

import android.hardware.Camera;
import android.util.Log;

public final class CameraLocker
{

	private Camera m_cameras[];
	private static CameraLocker s_instance;
	private final String LOG_TAG = "CameraLocker";

	public static CameraLocker instance()
	{
		if( s_instance == null )
			s_instance = new CameraLocker();
		return s_instance;
	}

	private CameraLocker()
	{
		int numberOfCameras = Camera.getNumberOfCameras();
		m_cameras = new Camera[numberOfCameras];
		Log.v( LOG_TAG, "Found " + numberOfCameras + " cameras." );
	}

	public boolean lockFirst()
	{
		return lock( 0 );
	}

	public final boolean lockAll()
	{
		boolean lockedAll = true;
		Log.v( LOG_TAG, "Locking cameras..." );
		for( int i = 0; i < m_cameras.length; ++i )
		{
			Log.v( LOG_TAG, "Locking camera " + i );
			if( m_cameras[i] != null )
			{
				Log.v( LOG_TAG, "Camera " + i + " is already locked." );
				continue;
			}
			try
			{
				m_cameras[i] = Camera.open( i );
			}
			catch( Exception e )
			{
				Log.e( LOG_TAG, "Failed to open camera #" + i );
				e.printStackTrace();
			}
			if( m_cameras[i] == null )
			{
				Log.v( LOG_TAG, "Camera " + i + " could not be locked." );
				lockedAll = false;
			}
		}
		return lockedAll;
	}

	public final boolean lock( int cameraID )
	{
		if( cameraID >= m_cameras.length )
			return false;
		if( m_cameras[cameraID] != null )
			return true;
		try
		{
			m_cameras[cameraID] = Camera.open( cameraID );
		}
		catch( Exception e )
		{
			Log.e( LOG_TAG, "Failed to open the camera with id " + cameraID );
			e.printStackTrace();
		}
		return m_cameras[cameraID] != null;
	}

	public final void unlockAll()
	{
		Log.v( LOG_TAG, "Unlocking cameras..." );
		for( int i=0; i < m_cameras.length; ++i )
		{
			Log.v( LOG_TAG, "Unlocking camera " + i );
			if( m_cameras[i] != null )
			{
				m_cameras[i].release();
				m_cameras[i] = null;
				Log.v( LOG_TAG, "Camera " + i + " unlocked." );
			}
			else
				Log.v( LOG_TAG, "Camera " + i + " is not locked by camlocker." );
		}
	}

	public final void unlock( int cameraID )
	{
		Camera camera = m_cameras[cameraID];
		if( camera != null )
		{
			camera.release();
			camera = null;
		}
	}
}
