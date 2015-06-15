package com.example.music.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;

import com.example.music.activities.PlayBackPager;
import com.example.music.activities.SongList;

public class MusicService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {
	// media player
	private MediaPlayer player;
	// current position
	private int songPosn;
	private static Cursor cursor;
	private final IBinder musicBind = new MusicBinder();
	private String songTitle = "";
	private static final int NOTIFY_ID = 1;
	private boolean shuffle = false;
	private Handler durationHandler = new Handler();
	public static double timeElapsed = 0, finalTime = 0,pausedTime=0;
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
		player.setLooping(true);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		initMusicPlayer();
	}

	public void setShuffle() {
		if (shuffle)
			shuffle = false;
		else
			shuffle = true;
	}

	public void initMusicPlayer() {
		// set player properties
		player.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}

	public class MusicBinder extends Binder {
		public MusicService getService() {
			return MusicService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return musicBind;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		player.stop();
		player.release();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (player.getCurrentPosition() > 0) {
			mp.reset();
			SongList.footer.setCurrentItem(SongList.getItem(+1));
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
		System.out.println("==onprepared===");
		Intent notIntent = new Intent(this, SongList.class);
		notIntent.putExtra("_ID", SongList.pos);
		cursor.moveToFirst();
		cursor.move(songPosn);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification.Builder builder = new Notification.Builder(this);
			builder.setContentIntent(pendInt).setSmallIcon(android.R.drawable.ic_media_play)
			.setTicker(songTitle).setOngoing(true)
			.setContentTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)))
			.setContentText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
		Notification not = builder.build();
		startForeground(NOTIFY_ID, not);
	}

	public void setSong(int pos) {
		songPosn = pos;
	}
	
	public void playSong(Cursor cur) {
		// play a song
		player.reset();
		cur.moveToFirst();
		cur.move(songPosn);
		long songId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
		try {
			Uri trackUri = ContentUris.withAppendedId(
					android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					songId);
			player.setDataSource(getApplicationContext(), trackUri);
			player.prepareAsync();
			if(PlayBackPager.musicSrv.isPng())
				PlayBackPager.musicSrv.pausePlayer();
			timeElapsed = getPosn();
			finalTime =cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
			SongList.playback.setMax((int)finalTime);
			durationHandler.postDelayed(SongList.updateSeekBarTime, 1000);
		} catch (Exception e) {
			System.out.println("MUSIC SERVICE"+ "Error setting data source"+e);
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
	}
	public void resumePlayer() {
		player.start();
	}
	
	public void seek(int posn) {
		player.seekTo(posn);
	}

	// skip to next
	public void playNext() {
		songPosn++;
		if (songPosn >= cursor.getCount())
			songPosn = 0;
		playSong(cursor);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopSelf();
		stopForeground(true);
	}
}
