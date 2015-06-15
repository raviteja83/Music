package com.example.music.activities;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;

import com.example.music.R;
import com.example.music.adapters.MyCursorAdapter;
import com.example.music.fragments.NavigationDrawerFragment;
import com.example.music.services.PlayBackPagerService;
import com.example.music.services.PlayBackPagerService.MyPagerAdapterBinder;
import com.example.music.utils.Artist;
import com.example.music.utils.MyProgressDialog;
import com.example.music.utils.Song;
import com.example.music.utils.Utils;

public class MainActivity extends FragmentActivity implements
NavigationDrawerFragment.NavigationDrawerCallbacks {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CharSequence mTitle;
	public static ArrayList<Song> songList;
	public static ArrayList<Song> albumList;
	public static ArrayList<Artist> artistList;
	private Intent playIntent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		albumList = new ArrayList<Song>();
		songList = new ArrayList<Song>();
		artistList = new ArrayList<Artist>();
		getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

	}

	private ServiceConnection musicConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MyPagerAdapterBinder binder = (MyPagerAdapterBinder) service;
			// get service
			PlayBackPager.musicSrv = binder.getService();

			// pass list
			PlayBackPager.musicSrv.setList(songList);
			PlayBackPager.musicBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			PlayBackPager.musicBound = false;
		}
	};

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager
		.beginTransaction()
		.replace(R.id.container,PlaceholderFragment.newInstance(position + 1))
		.addToBackStack(null)
		.commit();
	}
	@Override
	protected void onStart() {
		super.onStart();
		if (playIntent == null) {
			playIntent = new Intent(this, PlayBackPagerService.class);
			getApplicationContext().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			getApplicationContext().startService(playIntent);
		}
	}
	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		/*case 4:
			mTitle = getString(R.string.title_section4);
			break;*/
		}

	}

	@SuppressWarnings("deprecation")
	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.global, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_search) {
			Intent intent = new Intent(MainActivity.this,SearchResultsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(new Utils(this).isMyServiceRunning(PlayBackPagerService.class) && PlayBackPager.musicBound){
			getApplicationContext().unbindService(musicConnection);
			getApplicationContext().stopService(playIntent);
			PlayBackPager.musicBound = false;
		}
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements LoaderCallbacks<Cursor>{
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		private MyCursorAdapter mAdapter;
		private static int selection;
		private Utils utils;
		ViewPager mPager;
		private MyProgressDialog bar;
		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			selection = sectionNumber;
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			getLoaderManager().initLoader(selection, null, this);
			bar = new MyProgressDialog(getActivity());
			bar.show();
			setRetainInstance(true);
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = null;
			GridView songView;
			rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			songView = (GridView) rootView.findViewById(R.id.song_list);
			ListView list = (ListView)rootView.findViewById(R.id.song_listView);
			int[] mToFields = {R.id.song_title,R.id.song_artist};
			int[] mToColumns = {R.id.album_song_title,R.id.album_song_artist};
			switch (selection) {
			case 1:
				list.setVisibility(View.VISIBLE);
				songView.setVisibility(View.GONE);
				String[] mFromColumns={ MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM };
				mAdapter = new MyCursorAdapter(getActivity(), //current context
						R.layout.list_item,  // Layout for a single row
						null,                // No Cursor yet
						mFromColumns,        // Cursor columns to use
						mToColumns,           // Layout fields to use
						selection                  //  flag
						);
				list.setAdapter(mAdapter);
				list.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> av, View v,
							int pos, long id) {
						Intent	intent = new Intent(getActivity(),PlayBackPager.class);
						intent.putExtra("position",pos);
						intent.putExtra("CallingActivity","MainActivity");
						startActivity(intent);
					}
				});
				break;
			case 2:
				String[] mFromAlbumColumns ={MediaStore.Audio.Albums.ALBUM,
						MediaStore.Audio.Albums.ARTIST};
				mAdapter = new MyCursorAdapter(getActivity(), //current context
						R.layout.song,  // Layout for a single row
						null,                // No Cursor yet
						mFromAlbumColumns,        // Cursor columns to use
						mToFields,           // Layout fields to use
						selection                  // No flags
						);
				// Sets the adapter for the view
				songView.setAdapter(mAdapter);
				songView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> av, View v,
							int pos, long id) {
						Intent	intent = new Intent(getActivity(),SongList.class);
						intent.putExtra("_ID",pos);
						startActivity(intent);
					}
				});
				break;
			case 3:
				String[] mFromArtistColumns = {MediaStore.Audio.Artists.ARTIST
						,MediaStore.Audio.Artists.NUMBER_OF_ALBUMS};
				mAdapter = new MyCursorAdapter(getActivity(), //current context
						R.layout.song,  // Layout for a single row
						null,                // No Cursor yet
						mFromArtistColumns,       // Cursor columns to use
						mToFields,           // Layout fields to use
						selection                    // No flags
						);
				songView.setAdapter(mAdapter);
				songView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> av, View v,
							int pos, long id) {
					}
				});

				break;
			}
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			utils = new Utils(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			switch (id) {
			case 1:
				Uri mDataUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				String[] mProjection = {MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.ALBUM_ID};
				String SortOrder = Media.TITLE + " ASC";
				// Returns a new CursorLoader
				return new CursorLoader(
						getActivity(),   // Parent activity context
						mDataUri,        // Table to query
						mProjection,     // Projection to return
						null,            // No selection clause
						null,            // No selection arguments
						SortOrder  // Default sort order
						);
			case 2:
				Uri mAlbumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
				String[] mAlbumProjection = {MediaStore.Audio.Albums.ARTIST,MediaStore.Audio.Albums.ALBUM,
						MediaStore.Audio.Albums.ALBUM_ART,MediaStore.Audio.Albums._ID};
				String AlbumSortOrder = Media.ALBUM + " ASC";
				// Returns a new CursorLoader
				return new CursorLoader(
						getActivity(),   // Parent activity context
						mAlbumUri,        // Table to query
						mAlbumProjection,     // Projection to return
						null,            // No selection clause
						null,            // No selection arguments
						AlbumSortOrder  // Default sort order
						);
			case 3:
				Uri mArtistUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
				String[] mArtistProjection = {MediaStore.Audio.Artists.ARTIST,MediaStore.Audio.Artists._ID,
						MediaStore.Audio.Artists.NUMBER_OF_ALBUMS};
				String mArtistSortOrder = Media.ARTIST + " ASC";
				// Returns a new CursorLoader
				return new CursorLoader(
						getActivity(),   // Parent activity context
						mArtistUri,        // Table to query
						mArtistProjection,     // Projection to return
						null,            // No selection clause
						null,            // No selection arguments
						mArtistSortOrder  // Default sort order
						);
			default:
				return null;
			}
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0,final Cursor arg1){
			Handler handler = new Handler(); 
			handler.postDelayed(new Runnable() { 
				public void run() {
					if(bar.isShowing())
						bar.dismiss();
					if(!arg1.isClosed())
						mAdapter.changeCursor(arg1);
				} 
			}, 2000);
			if(selection == 1)
				utils.getSongList(arg1);
			else if(selection ==2)
				utils.getAlbumList(arg1);
			else if(selection ==3)
				utils.getArtistList(arg1);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.changeCursor(null);
		}
	}
}
