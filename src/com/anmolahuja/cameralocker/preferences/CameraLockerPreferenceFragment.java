package com.anmolahuja.cameralocker.preferences;

import org.holoeverywhere.preference.PreferenceFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.anmolahuja.cameralocker.R;

public class CameraLockerPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{

	boolean m_preferencesChanged = false;

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource( R.xml.preferences );
	}

	@Override
	public void onPause()
	{
		CameraLockerPreferenceManager.instance( getActivity() ).unregisterListener( this );
		super.onPause();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		CameraLockerPreferenceManager.instance( getActivity() ).registerListener( this );
	}

	@Override
	public void setUserVisibleHint( boolean isVisibleToUser )
	{
		super.setUserVisibleHint( isVisibleToUser );
		if( this.isVisible() && !isVisibleToUser )
		{
				if( m_preferencesChanged )
				{
					m_preferencesChanged = false;
					LocalBroadcastManager
							.getInstance( getActivity() )
							.sendBroadcast(	new Intent(	CameraLockerPreferenceManager.ACTION_PREFERENCE_CHANGED ) );
				}
		}
	}

	@Override
	public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
	{
		m_preferencesChanged = true;
		if( key == getString( R.string.bool_enable_autounlock ) )
		{
			if( CameraLockerPreferenceManager.instance( getActivity() ).isAutounlockEnabled() )
				Log.w( "TEMP", "TODO do something" );
			// TODO launch app picker if enabled for the first time?
		}
		else if( key == getString( R.string.bool_enable ))
		{
			// TODO toggle app
		}
	}
}
