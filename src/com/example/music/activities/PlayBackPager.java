package com.example.music.activities;

import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
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
	ViewPager mPager;
	MyPagerFragmentAdapter myPagerAdapter;
	private static SeekBar playback ;
	private static Handler durationHandler = new Handler();
	ImageButton shuff,repeat,like,dislike,play_pause;
	public static TextView title_nowplaying,duration,totalDuration,artist_nowplaying;
	private boolean paused=true;
	//notif_pause = true;
	private static double timeElapsed = 0, finalTime = 0,timeRemaining = 0;
	private boolean shuffle=false,re=false;
	private boolean liked =false,disliked=false;
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
		Intent intent = getIntent();
		try{
			position = intent.getExtras().getInt("notif_pos");
			myPagerAdapter = new MyPagerFragmentAdapter(getSupportFragmentManager());
			mPager.setAdapter(myPagerAdapter);
			mPager.setCurrentItem(position);
			mPager.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageSelected(int pos) {
					System.out.println("==="+pos);
					currSong = MainActivity.songList.get(pos);
					finalTime =currSong.getDuration();
					totalDuration.setText(String.format("%d : %02d ", TimeUnit.MILLISECONDS.toMinutes((long)finalTime),
							TimeUnit.MILLISECONDS.toSeconds((long)finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));
					playback.setMax((int)finalTime);
					durationHandler.postDelayed(updateSeekBarTime, 1000);
					play_pause.setImageResource(android.R.drawable.ic_media_pause);
				}
				@Override
				public void onPageScrolled(int pos, float arg1, int arg2) {
				}
				@Override
				public void onPageScrollStateChanged(int arg0) {
				}
			});
			mPager.setPageTransformer(true, new DepthPageTransformer());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Override
	protected void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter();
		filter.addAction(PlayBackPagerService.ACTION_PAUSE);
		filter.addAction(PlayBackPagerService.ACTION_PLAY);
		filter.addAction(PlayBackPagerService.ACTION_RESUME);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	private BroadcastReceiver receiver  = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(PlayBackPagerService.ACTION_PLAY.equals(intent.getAction())){
				play_pause.setImageResource(android.R.drawable.ic_media_pause);
				mPager.setCurrentItem(intent.getExtras().getInt("songPosn"));
			}else if(PlayBackPagerService.ACTION_RESUME.equals(intent.getAction()))
				play_pause.setImageResource(android.R.drawable.ic_media_pause);
			if(PlayBackPagerService.ACTION_PAUSE.equals(intent.getAction()))
				play_pause.setImageResource(android.R.drawable.ic_media_play);

		}
	};

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
				mPager.setCurrentItem(mPager.getCurrentItem()+1);
				getResources().getDrawable(R.drawable.dislike).setColorFilter(getResources().getColor(android.R.color.holo_red_light), Mode.MULTIPLY );
			}else
				getResources().getDrawable(R.drawable.dislike).setColorFilter( 0xffffffff, Mode.MULTIPLY );
			dislike.setImageResource(R.drawable.dislike);
			break;
		case R.id.media_next:
			MainActivity.musicSrv.playNext();
			break;
		case R.id.media_play_pause:
			paused = !paused;
			if(MainActivity.musicBound && paused){
				play_pause.setImageResource(android.R.drawable.ic_media_pause);
			}else{
				play_pause.setImageResource(android.R.drawable.ic_media_play);
			}
			break;
		case R.id.media_previous:
			MainActivity.musicSrv.playPrev();
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
			MainActivity.musicSrv.setShuffle();
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
			MainActivity.musicSrv.setRepeat();
		default:
			break;
		}
	}

	private static Runnable updateSeekBarTime = new Runnable() {
		@Override
		public void run(){
			try{
				if(MainActivity.musicBound && MainActivity.musicSrv.isPng()){
					timeElapsed = MainActivity.musicSrv.getPosn();
					playback.setProgress((int)timeElapsed);
					//set time remaing
					timeRemaining = finalTime - timeElapsed;
					duration.setText(String.format("%d : %02d ", TimeUnit.MILLISECONDS.toMinutes((long)timeRemaining), 
							TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));
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
			return PagerFragment.newInstance(position);
		}
	}
}
