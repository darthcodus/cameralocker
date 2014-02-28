package com.anmolahuja.cameralocker.sqldb;

import android.content.Context;

public class CameraLockerDatabase
{

	private Context m_context;
	private PackageDataHelper m_packageDataHelper;
	private static CameraLockerDatabase s_instance;

	public static CameraLockerDatabase instance( Context context )
	{
		if( s_instance == null )
			s_instance = new CameraLockerDatabase( context );
		return s_instance;
	}

	private CameraLockerDatabase( Context context )
	{
		m_context = context.getApplicationContext();
		m_packageDataHelper = new PackageDataHelper( m_context );
	}

	public PackageDataHelper packageDatabase()
	{
		return m_packageDataHelper;
	}

	public void closeDB()
	{
		m_packageDataHelper.close();
	}

}
