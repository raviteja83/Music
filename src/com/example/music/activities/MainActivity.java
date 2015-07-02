package com.example.music.activities;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music.R;
import com.example.music.adapters.MyCursorAdapter;
import com.example.music.adapters.MyFragmentAdapter;
import com.example.music.fragments.NavigationDrawerFragment;
import com.example.music.services.PlayBackPagerService;
import com.example.music.services.PlayBackPagerService.MyPagerAdapterBinder;
import com.example.music.utils.Artist;
import com.example.music.utils.DepthPageTransformer;
import com.example.music.utils.MyProgressDialog;
import com.example.music.utils.Song;
import com.example.music.utils.Utils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

public class MainActivity extends FragmentActivity implements
NavigationDrawerFragment.NavigationDrawerCallbacks {

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CharSequence mTitle;
	public static TextView duration,totalDuration;
	private static Handler durationHandler = new Handler();
	private static SlidingUpPanelLayout mLayout;
	private static ImageButton play_pause;
	public static ArrayList<Song> songList;
	public static ArrayList<Song> albumList;
	private static ViewPager mPager;
	public static ArrayList<Artist> artistList;
	public static boolean musicBound= false,shuffle = false;
	public static PlayBackPagerService musicSrv;
	private Intent playIntent;
	private static SeekBar playback ;
	public static double timeElapsed = 0, finalTime = 0,timeRemaining = 0;

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
		try{
			Intent intent = getIntent();
			Toast.makeText(this,"launch from notification"+intent.getExtras().getInt("notif_pos"),Toast.LENGTH_SHORT).show();
			mPager.setCurrentItem(intent.getExtras().getInt("notif_pos"));	
			mLayout.setPanelState(PanelState.EXPANDED);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private ServiceConnection musicConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MyPagerAdapterBinder binder = (MyPagerAdapterBinder) service;
			// get service
			musicSrv = binder.getService();
			// pass list
			musicSrv.setList(songList);
			musicBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
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
	private BroadcastReceiver receiver  = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(PlayBackPagerService.ACTION_PLAY.equals(intent.getAction())){
				play_pause.setImageResource(android.R.drawable.ic_media_pause);
				mPager.setCurrentItem(intent.getExtras().getInt("songPosn"));
			}else if(PlayBackPagerService.ACTION_RESUME.equals(intent.getAction()))
				play_pause.setImageResource(android.R.drawable.ic_media_pause);
			else if(PlayBackPagerService.ACTION_PAUSE.equals(intent.getAction()))
				play_pause.setImageResource(android.R.drawable.ic_media_play);
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(PlayBackPagerService.ACTION_PAUSE);
		filter.addAction(PlayBackPagerService.ACTION_PLAY);
		filter.addAction(PlayBackPagerService.ACTION_RESUME);
		registerReceiver(receiver, filter);
		if (playIntent == null) {
			playIntent = new Intent(this, PlayBackPagerService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
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

	private static Runnable updateSeekBarTime = new Runnable() {
		@Override
		public void run(){
			try{
				if(musicBound && musicSrv.isPng()){
					timeElapsed = musicSrv.getPosn();
					playback.setProgress((int)timeElapsed);
					//set time remaing
					timeRemaining = finalTime - timeElapsed;
					duration.setText(String.format("%d : %02d ", TimeUnit.MILLISECONDS.toMinutes((long)timeRemaining), 
							TimeUnit.MILLISECONDS.toSeconds((long)timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)timeRemaining))));
					durationHandler.postDelayed(this, 1000);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(new Utils(this).isMyServiceRunning(PlayBackPagerService.class) && musicBound){
			unbindService(musicConnection);
			stopService(playIntent);
			musicBound = false;
		}
		unregisterReceiver(receiver);
	}

	@Override
	public void onBackPressed() {
		if (mLayout != null &&
				(mLayout.getPanelState() == PanelState.EXPANDED || mLayout.getPanelState() == PanelState.ANCHORED)) {
			mLayout.setPanelState(PanelState.COLLAPSED);
		} else {
			super.onBackPressed();
			finish();
		}
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
		private MyProgressDialog bar;
		private Utils utils;
		ImageButton shuff,repeat,like,dislike,previous,next;
		Song CurrSong;
		private MyFragmentAdapter mFragmentAdapter;
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
		@SuppressWarnings("deprecation")
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = null;
			GridView songView;
			rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			mLayout = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout);
			mPager = (ViewPager) mLayout.findViewById(R.id.pager);
			duration  = (TextView) mLayout.findViewById(R.id.songDuration);
			totalDuration  = (TextView)mLayout.findViewById(R.id.songTotalDuration);
			((ImageButton) mLayout.findViewById(R.id.media_previous)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					musicSrv.playPrev();
				}
			});
			((ImageButton) mLayout.findViewById(R.id.media_next)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					musicSrv.playNext();
				}
			});

			play_pause = (ImageButton) mLayout.findViewById(R.id.media_play_pause);
			play_pause.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(musicBound && musicSrv.isPng()){
						musicSrv.pausePlayer();
						play_pause.setImageResource(android.R.drawable.ic_media_play);
					}else if(musicBound ){
						musicSrv.Resume();
						play_pause.setImageResource(android.R.drawable.ic_media_pause);
					}
				}
			});
			//like=(ImageButton) mLayout.findViewById(R.id.like);
			//dislike=(ImageButton) mLayout.findViewById(R.id.dislike);
			shuff=(ImageButton) mLayout.findViewById(R.id.shuffle);
			shuff.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					shuffle=!shuffle;
					if(shuffle){
						getResources().getDrawable(R.drawable.shuffle).setColorFilter(getResources().getColor(android.R.color.holo_red_light), Mode.MULTIPLY );
						shuff.setImageResource(R.drawable.shuffle);
					}else{
						getResources().getDrawable(R.drawable.shuffle).setColorFilter( 0xffffffff, Mode.MULTIPLY );
						shuff.setImageResource(R.drawable.shuffle);
					}
					musicSrv.setShuffle();

				}
			});
			repeat=(ImageButton) mLayout.findViewById(R.id.repeat);
			playback = (SeekBar) mLayout.findViewById(R.id.seekBar);

			mPager.setOnPageChangeListener(new OnPageChangeListener() {				
				@Override
				public void onPageSelected(int pos) {
					musicSrv.setSong(pos);
					playback.setProgress(0);
					musicSrv.playSong();
					CurrSong = songList.get(pos);
					finalTime =CurrSong.getDuration();
					playback.setMax((int)finalTime);
					totalDuration.setText(String.format("%d : %02d ", TimeUnit.MILLISECONDS.toMinutes((long)finalTime),
							TimeUnit.MILLISECONDS.toSeconds((long)finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));
					durationHandler.postDelayed(updateSeekBarTime, 1000);

				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
				}
			});

			mPager.setPageTransformer(true, new DepthPageTransformer());

			mLayout.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					mLayout.setPanelState(PanelState.EXPANDED);
				}
			});
			mLayout.setPanelSlideListener(new PanelSlideListener() {
				@Override
				public void onPanelSlide(View panel, float slideOffset) {
					if(slideOffset >=0.6f)
						getActivity().getActionBar().hide();
				}

				@Override
				public void onPanelExpanded(View panel) {
				}

				@Override
				public void onPanelCollapsed(View panel) {
					getActivity().getActionBar().show();
				}

				@Override
				public void onPanelAnchored(View panel) {
				}

				@Override
				public void onPanelHidden(View panel) {
				}
			});
			songView = (GridView) rootView.findViewById(R.id.song_list);
			ListView list = (ListView)rootView.findViewById(R.id.song_listView);
			int[] mToFields = {R.id.song_title,R.id.song_artist};
			int[] mToColumns = {R.id.album_song_title,R.id.album_song_artist};
			switch (selection) {
			case 1:
				list.setVisibility(View.VISIBLE);
				songView.setVisibility(View.GONE);
				String[] mFromColumns={ MediaColumns.TITLE,AudioColumns.ALBUM };
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
						mPager.setCurrentItem(pos);
						//mLayout.setPanelState(PanelState.EXPANDED);
					}
				});
				break;
			case 2:
				String[] mFromAlbumColumns ={AlbumColumns.ALBUM,
						AlbumColumns.ARTIST};
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
				String[] mFromArtistColumns = {ArtistColumns.ARTIST
						,ArtistColumns.NUMBER_OF_ALBUMS};
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
				String[] mProjection = {MediaColumns.TITLE,AudioColumns.ALBUM,AudioColumns.ARTIST,
						BaseColumns._ID,AudioColumns.DURATION,AudioColumns.ALBUM_ID};
				String SortOrder = MediaColumns.TITLE + " ASC";
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
				String[] mAlbumProjection = {AlbumColumns.ARTIST,AlbumColumns.ALBUM,
						AlbumColumns.ALBUM_ART,BaseColumns._ID};
				String AlbumSortOrder = AudioColumns.ALBUM + " ASC";
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
				String[] mArtistProjection = {ArtistColumns.ARTIST,BaseColumns._ID,
						ArtistColumns.NUMBER_OF_ALBUMS};
				String mArtistSortOrder = AudioColumns.ARTIST + " ASC";
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
				@Override
				public void run() {
					if(bar.isShowing())
						bar.dismiss();
					if(!arg1.isClosed())
						mAdapter.changeCursor(arg1);
				} 
			}, 2000);
			if(selection == 1){
				utils.getSongList(arg1);

			}else if(selection ==2){
				utils.getAlbumList(arg1);
			}else if(selection ==3)
				utils.getArtistList(arg1);
			mFragmentAdapter = new MyFragmentAdapter(getFragmentManager());
			mPager.setAdapter(mFragmentAdapter);
			try{
				if(musicBound && !musicSrv.equals(null)){
					musicSrv.setList(songList);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.changeCursor(null);
		}
	}
}
