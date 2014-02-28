package com.anmolahuja.cameralocker;

import android.content.Intent;
import android.os.Bundle;

import com.anmolahuja.cameralocker.preferences.CameraLockerPreferenceFragment;
import com.anmolahuja.cameralocker.swipeytabs.SwipeyActivity;

public class MainActivity extends SwipeyActivity
{

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		addTab( new AppListFragment(), getString( R.string.tab_applist ) );
		addTab( new CameraLockerPreferenceFragment(), getString( R.string.tab_references ) );
		startService( new Intent( this, CameraLockerService.class ) );
	}

}
