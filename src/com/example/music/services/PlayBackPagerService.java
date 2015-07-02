package com.example.music.services;

import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.widget.RemoteViews;

import com.example.music.R;
import com.example.music.activities.MainActivity;
import com.example.music.utils.Song;
import com.squareup.picasso.Picasso;

public class PlayBackPagerService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {
	// media player
	private MediaPlayer player;
	// song list
	private static ArrayList<Song> songs;
	// current position
	private int songPosn;
	private final IBinder musicBind = new MyPagerAdapterBinder();
	private String songTitle = "";
	private static final int NOTIFY_ID = 1;
	private boolean shuffle = false;
	private boolean repeat = false;
	private Random rand;
	//private boolean paused =false;
	public static boolean musicBound = false;
	public static final String ACTION_PAUSE = "com.example.music.Pause";
	public static final String ACTION_PLAY = "com.example.music.Play";
	public static final String ACTION_RESUME = "com.example.music.Resume";
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(receiver, filter);
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
		musicBound = true;
		return musicBind;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if(player.isPlaying())
			player.stop();
		player.release();
		musicBound = false;
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
		RemoteViews remoteViews =new RemoteViews(getPackageName(), R.layout.footer);
		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.putExtra("notif_pos", songPosn);
		notIntent.putExtra("notif", "Notification");
		notIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentIntent(pendInt).setSmallIcon(android.R.drawable.ic_media_play)
		.setOngoing(true)
		.setContent(remoteViews);
		Notification not = builder.build();
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
				new Intent(ACTION_PAUSE), 0);
		remoteViews.setOnClickPendingIntent(R.id.media_play_thumb, pendingIntent);
		remoteViews.setTextViewText(R.id.song_title_bottom, songTitle);
		remoteViews.setTextViewText(R.id.song_artist_bottom, songs.get(songPosn).getArtist());
		try{
			String[] proj = { MediaStore.Audio.Albums.ALBUM_ART,MediaStore.Audio.Albums._ID };
			String selection = MediaStore.Audio.Albums.ALBUM + " =? " ;
			String[] selectionArgs = {songs.get(songPosn).getAlbum()};
			Cursor cur = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,  proj, selection, selectionArgs, null);
			int column_index = cur.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
			cur.moveToFirst();
			if(cur.getCount()<=0){
				Picasso.with(getApplicationContext())
				.load(R.drawable.album_art)
				.into(remoteViews, R.id.album_thumb, NOTIFY_ID, not);
			}else{
				Picasso.with(getApplicationContext())
				.load(Uri.parse("file:///"+cur.getString(column_index)))
				.resize(100,100)
				.noFade().centerCrop()
				.into(remoteViews, R.id.album_thumb, NOTIFY_ID, not);
			}
			cur.close();
		}catch(Exception e){
			e.printStackTrace();
		}
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
			Intent intent = new Intent(ACTION_PLAY);
			intent.putExtra("songPosn",songPosn);
			sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
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
		Intent intent = new Intent(ACTION_PAUSE);
		sendBroadcast(intent);
	}

	public void seek(int posn) {
		player.seekTo(posn);
	}

	public void Resume() {
		player.start();
		Intent intent = new Intent(ACTION_RESUME);
		sendBroadcast(intent);
	}

	public void playPrev() {
		songPosn--;
		if (songPosn < 0)
			songPosn = songs.size() - 1;
		playSong();
	}

	// skip to next
	public void playNext() {
		if (shuffle) {
			int newSong = songPosn;
			while (newSong == songPosn) {
				newSong = rand.nextInt(songs.size());
			}
			songPosn = newSong;
		} else if(repeat) {

		}else {
			songPosn++;
			if (songPosn >= songs.size())
				songPosn = 0;
		}
		playSong();
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		stopSelf();
		unregisterReceiver(receiver);
	}
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				if(musicBound && isPng()) 
					pausePlayer();
			}
		}
	};
}
