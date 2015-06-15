package com.example.music.activities;

import android.annotation.SuppressLint;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;

import com.example.music.R;
import com.example.music.adapters.SongListCursorAdapter;
import com.example.music.fragments.FooterFragment;
import com.example.music.services.MusicService;
import com.example.music.services.MusicService.MusicBinder;
import com.squareup.picasso.Picasso;

public class SongList extends FragmentActivity{
	private SimpleCursorAdapter mAdapter;
	private ImageView AlbumArt ;
	public static ViewPager footer;
	public static SeekBar playback;
	public static int pos;
	public static Uri imageUri;
	ListView list;
	private static Handler durationHandler = new Handler();
	private Intent playIntent = null;
	public static MusicService musicSrv;
	private static boolean musicBound = false;
	private String album; 
	MyFragmentAdapter mFragmentAdapter;
	public static Cursor cursor;
	private ServiceConnection musicConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder = (MusicBinder) service;
			// get service
			musicSrv = binder.getService();
			// pass list
			musicBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
		}
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song_list);
		pos = getIntent().getExtras().getInt("_ID");
		album= MainActivity.albumList.get(pos).getAlbum();
		AlbumArt = (ImageView) findViewById(R.id.album_image);
		footer = (ViewPager) findViewById(R.id.footer);
		list = (ListView) findViewById(R.id.list);
		playback = (SeekBar) findViewById(R.id.seekBar_footer);

		getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		getActionBar().setHomeAsUpIndicator(R.drawable.back);
		getActionBar().setDisplayShowTitleEnabled(false);
		String[] mFromColumns ={MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.DURATION};
		int[] mToFields = {
				R.id.album_song_title,
				R.id.album_song_artist,
				R.id.album_song_duration
		};
		String album_art =  MainActivity.albumList.get(pos).getAlbum_art();
		imageUri = Uri.parse("file:///" +album_art);
		Picasso.with(this)
		.load(imageUri)
		.resize(800,800)
		.error(R.drawable.album_art)
		.noFade().centerCrop()
		.placeholder(R.drawable.image_loader)
		.into(AlbumArt);
		Uri mDataUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] mProjection = {MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DURATION};
		String selection = 	android.provider.MediaStore.Audio.Media.ALBUM + " =? ";
		String[] selectionArgs = new String[]{album};
		cursor = getContentResolver().query( 
				mDataUri,        // Table to query
				mProjection,     // Projection to return
				selection ,      // No selection clause
				selectionArgs,   // No selection arguments
				null  // Default sort order
				);
		mAdapter = new SongListCursorAdapter(this, // current context
				R.layout.list_item,//layout
				cursor,                 //No cursor
				mFromColumns,        // Cursor columns to use
				mToFields,           // Layout fields to use
				0                    // No flags
				);
		list.setAdapter(mAdapter);
		mFragmentAdapter = new MyFragmentAdapter(getSupportFragmentManager());
		footer.setAdapter(mFragmentAdapter);
		footer.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int pos) {
				musicSrv.setSong(pos);
				musicSrv.playSong(cursor);
			}
			@Override
			public void onPageScrolled(int pos, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		footer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cursor.moveToFirst();
				cursor.move(footer.getCurrentItem());
				String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
				System.out.println(MainActivity.songList.indexOf(title));
				
			}
		});
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				footer.setCurrentItem(position);
			}
		});

	}
	public static Runnable updateSeekBarTime = new Runnable() {
		public void run(){
			try{
				if(musicBound && musicSrv.isPng()){
					MusicService.timeElapsed = musicSrv.getPosn();
					playback.setProgress((int) MusicService.timeElapsed);
					//repeat yourself that again in 1000 miliseconds
					durationHandler.postDelayed(this, 1000);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	@Override
	protected void onStart() {
		super.onStart();
		if (playIntent == null) {
			playIntent = new Intent(SongList.this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(musicBound){
			unbindService(musicConnection);
			stopService(playIntent);
			musicBound = false;
		}
	}

	public static int getItem(int i) {
		return footer.getCurrentItem() + i;
	}

	private class MyFragmentAdapter extends FragmentPagerAdapter  {
		public MyFragmentAdapter(FragmentManager fm) {
			super(fm);
		}
		@Override
		public int getCount() {
			return cursor.getCount();
		}

		@Override
		public Fragment getItem(int pos) {
			return FooterFragment.newInstance(pos);
		}
	}
}
