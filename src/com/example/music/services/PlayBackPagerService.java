package com.example.music.services;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import com.example.music.activities.PlayBackPager;
import com.example.music.utils.Song;

public class PlayBackPagerService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {
	// media player
	private MediaPlayer player;
	// song list
	private ArrayList<Song> songs;
	// current position
	private int songPosn;
	private final IBinder musicBind = new MyPagerAdapterBinder();
	private String songTitle = "";
	private static final int NOTIFY_ID = 1;
	private boolean shuffle = false;
	private boolean repeat = false;
	private Random rand;
	private Handler durationHandler = new Handler();
	public static double timeElapsed = 0, finalTime = 0,timeRemaining = 0;
	public static boolean musicBound = false;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_NOT_STICKY;
	}
	public void onCreate() {
		// create the service
		super.onCreate();
		// initialize position
		songPosn = 0;
		// create player
		player = new MediaPlayer();
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		initMusicPlayer();
		rand = new Random();
	}

	public void setShuffle() {
		if (shuffle)
			shuffle = false;
		else
			shuffle = true;
	}

	public void setRepeat() {
		if (repeat)
			repeat = false;
		else
			repeat = true;
	}

	public void initMusicPlayer() {
		// set player properties
		player.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}

	public class MyPagerAdapterBinder extends Binder {
		public PlayBackPagerService getService() {
			return PlayBackPagerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return musicBind;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if(player.isPlaying())
			player.stop();
		player.release();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (player.getCurrentPosition() > 0) {
			mp.reset();
			playNext();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}

	@SuppressLint("NewApi")
	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
		Intent notIntent = new Intent(this, PlayBackPager.class);
		notIntent.putExtra("position", songPosn);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentIntent(pendInt)
		.setSmallIcon(android.R.drawable.ic_media_play)
		.setTicker(songTitle).setOngoing(true)
		.setContentTitle(songTitle)
		.setContentText(songs.get(songPosn).getArtist());
		Notification not = builder.build();
		startForeground(NOTIFY_ID, not);
	}

	public void setSong(int songindex) {
		songPosn = songindex;
	}

	public int getSongPosn() {
		return songPosn;
	}
	public void setList(ArrayList<Song> songlist){
		songs = songlist;
	}
	public void playSong() {
		// play a song
		player.reset();
		// get song
		Song playSong = songs.get(songPosn);
		songTitle = playSong.getTitle();
		// get id
		long currSong = playSong.getId();
		// set uri
		try {
			Uri trackUri = ContentUris.withAppendedId(
					android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					currSong);
			player.setDataSource(getApplicationContext(), trackUri);
			player.prepareAsync();
			finalTime =playSong.getDuration();
			PlayBackPager.totalDuration.setText(String.format("%d : %02d ", TimeUnit.MILLISECONDS.toMinutes((long)finalTime),
					TimeUnit.MILLISECONDS.toSeconds((long)finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));
			PlayBackPager.playback.setProgress(0);
			PlayBackPager.playback.setMax((int)finalTime);
			durationHandler.postDelayed(PlayBackPager.updateSeekBarTime, 1000);
			PlayBackPager.play_pause.setImageResource(android.R.drawable.ic_media_pause);
		} catch (Exception e) {
			System.out.println("MUSIC SERVICE playbackpager service Error setting data source"+ e);
		}
	}

	public int getPosn() {
		return player.getCurrentPosition();
	}

	public int getDur() {
		return player.getDuration();
	}

	public boolean isPng() {
		return player.isPlaying();
	}

	public void pausePlayer() {
		player.pause();
		PlayBackPager.play_pause.setImageResource(android.R.drawable.ic_media_play);
	}

	public void seek(int posn) {
		player.seekTo(posn);
	}

	public void Resume() {
		player.start();
		PlayBackPager.play_pause.setImageResource(android.R.drawable.ic_media_pause);
		durationHandler.postDelayed(PlayBackPager.updateSeekBarTime, 1000);
	}

	public void playPrev() {
		songPosn--;
		if (songPosn < 0)
			songPosn = songs.size() - 1;
		PlayBackPager.mPager.setCurrentItem(songPosn);
	}

	// skip to next
	public void playNext() {
		if (shuffle) {
			int newSong = songPosn;
			while (newSong == songPosn) {
				newSong = rand.nextInt(songs.size());
			}
			songPosn = newSong;
			PlayBackPager.mPager.setCurrentItem(songPosn);
		} else if(repeat) {
			PlayBackPager.mPager.setCurrentItem(PlayBackPager.mPager.getCurrentItem());
		}else {
			songPosn++;
			if (songPosn >= songs.size())
				songPosn = 0;
			PlayBackPager.mPager.setCurrentItem(songPosn);
		}

	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		stopSelf();
	}
	public void Play(Uri data) {
		try {
			player.setDataSource(getApplicationContext(), data);
			player.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
