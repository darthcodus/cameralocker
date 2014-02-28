package com.anmolahuja.cameralocker.preferences;

import com.anmolahuja.cameralocker.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class CameraLockerPreferenceManager
{
	public static final String ACTION_PREFERENCE_CHANGED = "preferences_changed";
	private SharedPreferences m_sharedPreferences;
	private Context m_context;
	private boolean m_firstRun;
	private static CameraLockerPreferenceManager s_instance;

	public static CameraLockerPreferenceManager instance( Context context )
	{
		if( s_instance == null )
			s_instance = new CameraLockerPreferenceManager( context.getApplicationContext() );
		return s_instance;
	}

	private CameraLockerPreferenceManager( Context context )
	{
		m_context = context;
		m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
		m_firstRun = m_sharedPreferences.getBoolean( "first_run", true );
		if( m_firstRun )
		{
			m_sharedPreferences.edit().putBoolean( "first_run", false ).apply();
		}
	}

	public boolean isEnabled()
	{
		return m_sharedPreferences.getBoolean( getString(R.string.bool_enable) , true );
	}

	public boolean isAutounlockEnabled()
	{
		return m_sharedPreferences.getBoolean( getString(R.string.bool_enable_autounlock) , true );
	}

	public boolean isFirstRun()
	{
		return m_firstRun;
	}

	public boolean displaySystemApps()
	{
		return m_sharedPreferences.getBoolean( getString(R.string.bool_display_systemapps), true );
	}

	public void registerListener( OnSharedPreferenceChangeListener listener )
	{
		m_sharedPreferences.registerOnSharedPreferenceChangeListener( listener );
	}

	public void unregisterListener( OnSharedPreferenceChangeListener listener )
	{
		m_sharedPreferences.unregisterOnSharedPreferenceChangeListener( listener );
	}

	private String getString( int id )
	{
		return m_context.getString( id );
	}
}
