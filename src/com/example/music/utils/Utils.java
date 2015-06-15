package com.example.music.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.music.activities.MainActivity;

public class Utils {
	Context mContext;
	public Utils(Context context) {
		mContext = context;
	}

	public void getSongList(Cursor mDataCursor) {
		try{
			if (mDataCursor != null && mDataCursor.moveToFirst() ) {
				int albumColumn = mDataCursor
						.getColumnIndex(MediaStore.Audio.Media.ALBUM);
				int idColumn = mDataCursor
						.getColumnIndex(MediaStore.Audio.Media._ID);
				int artistColumn = mDataCursor
						.getColumnIndex(MediaStore.Audio.Media.ARTIST);
				int titleColumn = mDataCursor
						.getColumnIndex(MediaStore.Audio.Media.TITLE);
				int durColumn = mDataCursor
						.getColumnIndex(MediaStore.Audio.Media.DURATION);
				do {
					long thisId = mDataCursor.getLong(idColumn);
					String thisTitle = mDataCursor.getString(titleColumn);
					String thisAlbum = mDataCursor.getString(albumColumn);
					String thisArtist = mDataCursor.getString(artistColumn);
					long thisDuration = mDataCursor.getLong(durColumn);
					MainActivity.songList.add(new Song(thisId, thisTitle,thisArtist,thisAlbum,thisDuration));
				} while (mDataCursor.moveToNext());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void getAlbumList(Cursor mDataCursor) {
		try{
			if (mDataCursor != null && mDataCursor.moveToFirst() ) {
				int titleColumn = mDataCursor
						.getColumnIndex(android.provider.MediaStore.Audio.Albums.ALBUM);
				int idColumn = mDataCursor
						.getColumnIndex(android.provider.MediaStore.Audio.Albums._ID);
				int artistColumn = mDataCursor
						.getColumnIndex(android.provider.MediaStore.Audio.Albums.ARTIST);
				int albumArt = mDataCursor
						.getColumnIndex(android.provider.MediaStore.Audio.Albums.ALBUM_ART);

				do {
					long thisId = mDataCursor.getLong(idColumn);
					String thisAlbumArt = mDataCursor.getString(albumArt);
					String thisAlbum = mDataCursor.getString(titleColumn);
					String thisArtist = mDataCursor.getString(artistColumn);
					MainActivity.albumList.add(new Song(thisId, thisAlbum, thisArtist,thisAlbumArt));
				} while (mDataCursor.moveToNext());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void getArtistList(Cursor mDataCursor) {
		try{
			if (mDataCursor != null && mDataCursor.moveToFirst() ) {

				int idColumn = mDataCursor
						.getColumnIndex(android.provider.MediaStore.Audio.Artists._ID);
				int artistColumn = mDataCursor
						.getColumnIndex(android.provider.MediaStore.Audio.Artists.ARTIST);

				do {
					long thisId = mDataCursor.getLong(idColumn);
					String thisArtist = mDataCursor.getString(artistColumn);
					MainActivity.artistList.add(new Artist(thisId,thisArtist));
				} while (mDataCursor.moveToNext());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
