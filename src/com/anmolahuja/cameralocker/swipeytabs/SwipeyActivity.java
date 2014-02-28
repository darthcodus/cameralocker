package com.anmolahuja.cameralocker.swipeytabs;

import java.util.ArrayList;
import java.util.List;

import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Dialog;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.anmolahuja.cameralocker.R;

public abstract class SwipeyActivity extends Activity
{
	private SwipeyPagerAdapter m_pagerAdapter;
	private ViewPager m_viewPager;
	private ActionBar m_actionBar;
	ActionBar.TabListener m_tabListener;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_swipeytabs );
		m_actionBar = getSupportActionBar();

		m_pagerAdapter = new SwipeyPagerAdapter( getSupportFragmentManager() );
		m_viewPager = (ViewPager) findViewById( R.id.pager );
		m_viewPager.setAdapter( m_pagerAdapter );
		m_viewPager
				.setOnPageChangeListener( new ViewPager.SimpleOnPageChangeListener()
				{
					@Override
					public void onPageSelected( int pos )
					{
						m_actionBar.setSelectedNavigationItem( pos );
					}
				} );

		m_actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_TABS );
		m_tabListener = new ActionBar.TabListener()
		{
			@Override
			public void onTabUnselected( Tab tab, FragmentTransaction ft )
			{
			}

			@Override
			public void onTabSelected( Tab tab, FragmentTransaction ft )
			{
				m_viewPager.setCurrentItem( tab.getPosition() );
			}

			@Override
			public void onTabReselected( Tab tab, FragmentTransaction ft )
			{
			}
		};
	}

	public void addTab( Fragment fragment, String title )
	{
		m_pagerAdapter.addFragment( fragment );
		m_actionBar.addTab( m_actionBar.newTab().setText( title )
				.setTabListener( m_tabListener ) );
	}

	public ViewPager viewPager()
	{
		return m_viewPager;
	}

	public PagerAdapter pageAdapter()
	{
		return m_pagerAdapter;
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		super.onCreateOptionsMenu( menu );
		getMenuInflater().inflate( R.menu.activity_tabbedbase, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch( item.getItemId() )
		{
		case R.id.menu_about:
			final Dialog dialog = new Dialog( this );
			dialog.setContentView( R.layout.about );
			dialog.setTitle( R.string.dialog_about_title );
			dialog.setCancelable( true );
			Button okay = (Button) dialog.findViewById( R.id.dialog_about_button_okay );
			okay.setOnClickListener( new Button.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					dialog.dismiss();
				}
			} );
			dialog.show();
			return true;
		}
		return super.onOptionsItemSelected( item );
	}

	class SwipeyPagerAdapter extends FragmentPagerAdapter
	{
		private List<Fragment> mFragments = new ArrayList<Fragment>();
		private FragmentManager m_fragmentManager;
		public boolean disableSwipe = false;

		public SwipeyPagerAdapter( FragmentManager fm )
		{
			super( fm );
			m_fragmentManager = fm;
		}

		public void addFragment( Fragment fragment )
		{
			mFragments.add( fragment );
			notifyDataSetChanged();
		}

		@Override
		public int getCount()
		{
			return mFragments.size();
		}

		@Override
		public Fragment getItem( int pos )
		{
			Fragment f = mFragments.get( pos );
			return f;
		}

		public Fragment getActiveFragment( ViewPager container, int pos )
		{
			String name = "android:switcher:" + container.getId() + ":" + pos;
			return m_fragmentManager.findFragmentByTag( name );
		}
	}

}
