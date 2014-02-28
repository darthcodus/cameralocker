package com.anmolahuja.cameralocker.sqldb;

import com.commonsware.cwac.loaderex.acl.SQLiteCursorLoader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class PackageDataHelper extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME= "packages.db";
	private static final int DATABASE_VERSION= 1;
	private Context m_context = null;

	public interface PackageConstants extends BaseColumns
	{
		public static final String TABLE_NAME_PACKAGES = "includedPackagesTable";
		public static final String PACKAGE_NAME = "packageName";
		public static final String ALLOW = "allowCamera";
		public static final String SYSTEM = "isSystemApp";
	}

	public PackageDataHelper( Context context )
	{
		super( context, DATABASE_NAME, null, DATABASE_VERSION );
		m_context = context;
	}

	@Override
	public void onCreate( SQLiteDatabase db )
	{
		db.execSQL( "CREATE TABLE " + PackageConstants.TABLE_NAME_PACKAGES + " (" +
					PackageConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					PackageConstants.PACKAGE_NAME + " TEXT NOT NULL UNIQUE, " +
					PackageConstants.SYSTEM + " INTEGER NOT NULL DEFAULT 0, " +
					PackageConstants.ALLOW + " INTEGER NOT NULL DEFAULT 1);" );
	}

	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
	{
	}

	public SQLiteCursorLoader loaderForQuery( final String query )
	{
		return new SQLiteCursorLoader( m_context, this, query, null );
	}
}
