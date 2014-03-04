package com.anmolahuja.cameralocker;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Fragment;
import org.holoeverywhere.widget.LinearLayout;
import org.holoeverywhere.widget.Switch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class GingerbreadLockControlFragment extends Fragment
{

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		LinearLayout linearLayout = new LinearLayout( getActivity() );
		linearLayout.setOrientation( LinearLayout.VERTICAL );
		Switch s = new Switch( getActivity() );
		s.setChecked( CameraLocker.instance().anyLocked() );
		s.setOnCheckedChangeListener( new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
			{
				Intent intent = new Intent( getActivity(), CameraLockerService.class );
				intent.putExtra( CameraLockerService.CAMERALOCK_ACTION, CameraLockerService.ACTION_TOGGLE );
				getActivity().startService( intent );
			}
		} );
		s.setWidth( linearLayout.getWidth() );
		s.setText( R.string.gingerbread_fragment_toggleButtonDescription );
		linearLayout.addView( s );
		return linearLayout;
	}

}
