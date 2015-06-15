package com.example.music.activities;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.music.R;
import com.example.music.fragments.PagerFragment;
import com.example.music.services.PlayBackPagerService;
import com.example.music.utils.DepthPageTransformer;
import com.example.music.utils.Song;

public class PlayBackPager extends FragmentActivity implements OnClickListener{
	private int position;
	public static PlayBackPagerService musicSrv;
	public static boolean musicBound = false;
	public static ViewPager mPager;
	MyPagerFragmentAdapter myPagerAdapter;
	public static SeekBar playback ;
	private static Handler durationHandler = new Handler();
	public static ImageButton  play_pause;
	ImageButton shuff,repeat,like,dislike;
	public static TextView title_nowplaying,duration,totalDuration,artist_nowplaying;
	private double finalTime = 0;
	private boolean paused=true,notif_pause = true;
	private boolean shuffle=false,re=false;
	private boolean liked =false,disliked=false;
	private ArrayList<Song> songs;
	private Song currSong;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_back);
		mPager = (ViewPager) findViewById(R.id.pager);
		duration  = (TextView) findViewById(R.id.songDuration);
		totalDuration  = (TextView)findViewById(R.id.songTotalDuration);
		((ImageButton) findViewById(R.id.media_previous)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.media_next)).setOnClickListener(this);
		(play_pause = (ImageButton) findViewById(R.id.media_play_pause)).setOnClickListener(this);
		(like=(ImageButton) findViewById(R.id.like)).setOnClickListener(this);
		(dislike=(ImageButton) findViewById(R.id.dislike)).setOnClickListener(this);
		(shuff=(ImageButton) findViewById(R.id.shuffle)).setOnClickListener(this);
		(repeat=(ImageButton) findViewById(R.id.repeat)).setOnClickListener(this);
		playback = (SeekBar) findViewById(R.id.seekBar);
		songs = MainActivity.songList;
		Intent intent = getIntent();
		Uri data = intent.getData();
		try{
			if(!data.equals(null)){
				System.out.println(data);
				musicSrv.Play(data);
			}
		}catch(Exception e){
			if(intent.getExtras().getString("CallingActivity").equals("service")){
				position = intent.getExtras().getInt("notif_pos");
				musicSrv.setSong(position);
				musicSrv.Resume();
			}else{
				position = intent.getExtras().getInt("position");
				musicSrv.setSong(position);
				musicSrv.playSong();
			}

			myPagerAdapter = new MyPagerFragmentAdapter(getSupportFragmentManager());
			mPager.setAdapter(myPagerAdapter);
			mPager.setCurrentItem(position);
			currSong = songs.get(position);
			finalTime =currSong.getDuration();
			totalDuration.setText(String.format("%d : %02d ", TimeUnit.MILLISECONDS.toMinutes((long)finalTime),
					TimeUnit.MILLISECONDS.toSeconds((long)finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));

			mPager.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageSelected(int pos) {
					musicSrv.setSong(pos);
					musicSrv.playSong();
				}
				@Override
				public void onPageScrolled(int pos, float arg1, int arg2) {
				}
				@Override
				public void onPageScrollStateChanged(int arg0) {
				}
			});
			mPager.setPageTransformer(true, new DepthPageTransformer());
		}
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			notif_pause = !notif_pause;
			if(!notif_pause){
				if(musicSrv.isPng())
					musicSrv.pausePlayer();
			}else
				musicSrv.Resume();
		}
	};

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("NOTIF_PLAY");
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.like:
			liked = !liked;
			if(liked)
				getResources().getDrawable(R.drawable.like).setColorFilter(Color.RED, Mode.MULTIPLY );
			else
				getResources().getDrawable(R.drawable.like).setColorFilter( Color.WHITE, Mode.MULTIPLY );
			like.setImageResource(R.drawable.like);
			break;
		case R.id.dislike:
			disliked = !disliked;
			if(disliked){
				PlayBackPager.mPager.setCurrentItem(PlayBackPager.mPager.getCurrentItem()+1);
				getResources().getDrawable(R.drawable.dislike).setColorFilter(getResources().getColor(android.R.color.holo_red_light), Mode.MULTIPLY );
			}else
				getResources().getDrawable(R.drawable.dislike).setColorFilter( 0xffffffff, Mode.MULTIPLY );
			dislike.setImageResource(R.drawable.dislike);
			break;
		case R.id.media_next:
			musicSrv.playNext();
			break;
		case R.id.media_play_pause:
			paused = !paused;
			if(musicBound && paused){
				musicSrv.Resume();
				play_pause.setImageResource(android.R.drawable.ic_media_pause);
			}else{
				musicSrv.pausePlayer();
				play_pause.setImageResource(android.R.drawable.ic_media_play);
			}
			break;
		case R.id.media_previous:
			musicSrv.playPrev();
			break;
		case R.id.shuffle:
			shuffle=!shuffle;
			if(shuffle){
				getResources().getDrawable(R.drawable.shuffle).setColorFilter(getResources().getColor(android.R.color.holo_red_light), Mode.MULTIPLY );
				shuff.setImageResource(R.drawable.shuffle);
			}else{
				getResources().getDrawable(R.drawable.shuffle).setColorFilter( 0xffffffff, Mode.MULTIPLY );
				shuff.setImageResource(R.drawable.shuffle);
			}
			musicSrv.setShuffle();
			break;
		case R.id.repeat:
			re = !re;
			if(re){
				getResources().getDrawable(R.drawable.repeat).setColorFilter(getResources().getColor(android.R.color.holo_red_light), Mode.MULTIPLY );
				repeat.setImageResource(R.drawable.repeat);
			}else{
				getResources().getDrawable(R.drawable.repeat).setColorFilter( 0xffffffff, Mode.MULTIPLY );
				repeat.setImageResource(R.drawable.repeat);
			}
			musicSrv.setRepeat();
		default:
			break;
		}
	}

	public static Runnable updateSeekBarTime = new Runnable() {
		public void run(){
			try{
				if(musicBound && musicSrv.isPng()){
					PlayBackPagerService.timeElapsed = musicSrv.getPosn();
					playback.setProgress((int)PlayBackPagerService.timeElapsed);
					//set time remaing
					PlayBackPagerService.timeRemaining = PlayBackPagerService.finalTime - PlayBackPagerService.timeElapsed;
					duration.setText(String.format("%d : %02d ", TimeUnit.MILLISECONDS.toMinutes((long) PlayBackPagerService.timeRemaining), 
							TimeUnit.MILLISECONDS.toSeconds((long) PlayBackPagerService.timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) PlayBackPagerService.timeRemaining))));
					durationHandler.postDelayed(this, 1000);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	private class MyPagerFragmentAdapter extends FragmentStatePagerAdapter  {
		public MyPagerFragmentAdapter(FragmentManager fm) {
			super(fm);
		}
		@Override
		public int getCount() {
			return MainActivity.songList.size();
		}

		@Override
		public Fragment getItem(int pos) {
			return PagerFragment.newInstance(pos);
		}
	}
}
