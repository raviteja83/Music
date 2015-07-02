package com.example.music.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

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
						.getColumnIndex(AudioColumns.ALBUM);
				int idColumn = mDataCursor
						.getColumnIndex(BaseColumns._ID);
				int artistColumn = mDataCursor
						.getColumnIndex(AudioColumns.ARTIST);
				int titleColumn = mDataCursor
						.getColumnIndex(MediaColumns.TITLE);
				int durColumn = mDataCursor
						.getColumnIndex(AudioColumns.DURATION);
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
						.getColumnIndex(AlbumColumns.ALBUM);
				int idColumn = mDataCursor
						.getColumnIndex(BaseColumns._ID);
				int artistColumn = mDataCursor
						.getColumnIndex(AlbumColumns.ARTIST);
				int albumArt = mDataCursor
						.getColumnIndex(AlbumColumns.ALBUM_ART);

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
						.getColumnIndex(BaseColumns._ID);
				int artistColumn = mDataCursor
						.getColumnIndex(ArtistColumns.ARTIST);

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
