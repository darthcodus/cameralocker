package com.anmolahuja.cameralocker;

import java.util.ArrayList;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.ListFragment;
import org.holoeverywhere.widget.Switch;
import org.holoeverywhere.widget.TextView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import com.anmolahuja.cameralocker.preferences.CameraLockerPreferenceManager;

public class AppListFragment extends ListFragment implements PackageData.LoadCallback
{
	private BroadcastReceiver m_packageChangeReceiver;
	private BroadcastReceiver m_localPreferencesChangedReceiver;
	private SearchView m_searchView;
	private String m_curFilter;
	private PackageData m_appData;
	private AppAdapter m_adapter;

	@Override
	public void onViewCreated( View view, Bundle savedInstanceState )
	{
		super.onViewCreated( view, savedInstanceState );
		m_packageChangeReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive( Context context, Intent intent )
			{
				m_appData.reloadPackageList();
				m_adapter.getFilter().filter( m_curFilter );
			}
		};
		m_localPreferencesChangedReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive( Context context, Intent intent )
			{
				// TODO check if system apps to be displayed
			}
		};
		setListShown( false );
        getListView().setTextFilterEnabled( true );
        setEmptyText( getString(R.string.emptyListText) );
		m_appData = PackageData.instance( getActivity() );
		m_adapter = new AppAdapter( getActivity(), m_appData );
		m_appData.reloadSelectedPackages( this );
		setHasOptionsMenu( true );
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch( item.getItemId() )
		{

		default:
			break;
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	public void onStart()
	{
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction( Intent.ACTION_PACKAGE_ADDED );
		intentFilter.addAction( Intent.ACTION_PACKAGE_REMOVED );
		intentFilter.addDataScheme( "package" );
		getActivity().registerReceiver( m_packageChangeReceiver, intentFilter );

		LocalBroadcastManager.getInstance( getActivity() ).registerReceiver(
				m_localPreferencesChangedReceiver,
				new IntentFilter( CameraLockerPreferenceManager.ACTION_PREFERENCE_CHANGED ) );

		super.onStart();
	}

	@Override
	public void onStop()
	{
		try
		{
			getActivity().unregisterReceiver( m_packageChangeReceiver );
		}
		catch( IllegalArgumentException e )
		{
			e.printStackTrace();
		}
		try
		{
			LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver( m_localPreferencesChangedReceiver );
		}
		catch( IllegalArgumentException e )
		{
			e.printStackTrace();
		}
		super.onStop();
	}

	private void setFilter( String filter )
	{
		m_curFilter = filter;
		m_adapter.getFilter().filter( m_curFilter );
	}

	@Override
	public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
	{
		super.onCreateOptionsMenu( menu, inflater );
		m_searchView = new SearchView( ((ActionBarActivity) getActivity())
				.getSupportActionBar().getThemedContext() );
		m_searchView.setQueryHint( "Search" );
		MenuItem item = menu.add( Menu.NONE, Menu.NONE, 1, "Search" )
				.setIcon( R.drawable.ic_menu_search_holo_light );
		MenuItemCompat.setActionView( item, m_searchView );
		MenuItemCompat.setShowAsAction( item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW );

		m_searchView.setOnQueryTextListener( new OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextChange( String newText )
			{
				setFilter( newText );
				return false;
			}

			@Override
			public boolean onQueryTextSubmit( String query )
			{
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService( Context.INPUT_METHOD_SERVICE );
				imm.hideSoftInputFromWindow( m_searchView.getWindowToken(), 0 );
				return false;
			}
		} );
	}

	@Override
	public void onLoad()
	{
		getListView().setAdapter( m_adapter );
		if( isResumed() )
			setListShown( true );
		else
			setListShownNoAnimation( true );
	}

}

class AppAdapter extends BaseAdapter implements Filterable
{
	private Context m_context;
	private PackageData m_appData;
	private ItemsFilter m_filter;
	private ArrayList<PackageInfo> m_filteredPackages;

	private class ViewHolder
	{
		TextView appName;
		ImageView appIcon;
		Switch appSwitch;
		OnCheckedChangeListener checkedChangeListener; // workaround
	}

	@Override
	public Filter getFilter()
	{
		if( m_filter == null )
		{
			m_filter = new ItemsFilter();
		}
		return m_filter;
	}

	public AppAdapter( Context context, PackageData appData )
	{
		m_context = context;
		m_appData = appData;
		m_filteredPackages = m_appData.packages();
	}

	@Override
	public View getView( final int position, View convertView, ViewGroup parent )
	{
		final ViewHolder holder;
		LayoutInflater inflater = LayoutInflater.from( m_context );

		if( convertView == null )
		{
			convertView = inflater.inflate( R.layout.cv_apppicker_rowitem, null );
			holder = new ViewHolder();
			holder.appName = (TextView) convertView.findViewById( R.id.apppicker_rowitem_appname );
			holder.appIcon = (ImageView) convertView.findViewById( R.id.apppicker_rowitem_appicon );
			holder.appSwitch = (Switch) convertView.findViewById( R.id.apppicker_rowitem_switch );
			holder.checkedChangeListener =  new OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
				{
					final String packageName = (String)buttonView.getTag();
					m_appData.setChecked( packageName, ((Switch)buttonView).isChecked() );
				}
			};

			// weirdly, an onClickListener is never called,
			// and onCheckChangedListener gets called even when programmatically changed :/
			holder.appSwitch.setOnCheckedChangeListener( holder.checkedChangeListener );

			convertView.setTag( holder );
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		final PackageInfo info = m_filteredPackages.get( position );
		final Drawable appIcon = m_appData.getIcon( info );
		final String packageName = info.packageName;
		final String appName = m_appData.getName( info );
		holder.appIcon.setImageDrawable( appIcon );
		holder.appName.setText( appName );

		holder.appSwitch.setOnCheckedChangeListener( null ); // workaround, don't trigger listener unless changed by user
		holder.appSwitch.setChecked( m_appData.isChecked( packageName ) );
		holder.appSwitch.setOnCheckedChangeListener( holder.checkedChangeListener );

		holder.appSwitch.setTag( packageName );

		return convertView;
	}

	@Override
	public int getCount()
	{
		return m_filteredPackages.size();
	}

	@Override
	public PackageInfo getItem( int position )
	{
		return m_filteredPackages.get( position );
	}

	private class ItemsFilter extends Filter
	{
		protected FilterResults performFiltering( final CharSequence prefix )
		{
			final ArrayList<PackageInfo> packagesList;
			synchronized( m_appData )
			{
				packagesList = new ArrayList<PackageInfo>( m_appData.packages() );
			}
			FilterResults results = new FilterResults();
			if( prefix == null || prefix.length() == 0 )
			{
				results.values = packagesList;
				results.count = packagesList.size();
			}
			else
			{
				final String prefixString = prefix.toString().toLowerCase();
				final int count = packagesList.size();
				final ArrayList<PackageInfo> newItems = new ArrayList<PackageInfo>();
				for( int i = 0; i < count; i++ )
				{
					final PackageInfo packageInfo = packagesList.get( i );
					final String appNameLower = m_appData.getName( packageInfo ).toLowerCase();
					if( appNameLower.indexOf( prefixString ) != -1 )
						newItems.add( packageInfo );
				}
				results.values = newItems;
				results.count = newItems.size();
			}
			return results;
		}

		protected void publishResults( CharSequence prefix,	FilterResults results )
		{
			m_filteredPackages = (ArrayList<PackageInfo>) results.values;
			if( results.count > 0 )
			{
				notifyDataSetChanged();
			}
			else
			{
				notifyDataSetInvalidated();
			}
		}
	}

	@Override
	public long getItemId( int position )
	{
		return 0;
	}
}
