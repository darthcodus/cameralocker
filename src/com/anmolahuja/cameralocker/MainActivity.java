package com.anmolahuja.cameralocker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.anmolahuja.cameralocker.preferences.CameraLockerPreferenceFragment;
import com.anmolahuja.cameralocker.swipeytabs.SwipeyActivity;

public class MainActivity extends SwipeyActivity
{

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1 )
		{
			addTab( new GingerbreadLockControlFragment(), getString( R.string.tab_gingerbreadsupport ) );
		}
		addTab( new AppListFragment(), getString( R.string.tab_applist ) );
		addTab( new CameraLockerPreferenceFragment(), getString( R.string.tab_references ) );
		startService( new Intent( this, CameraLockerService.class ) );
	}

}
