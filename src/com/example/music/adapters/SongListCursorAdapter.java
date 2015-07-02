package com.example.music.adapters;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.music.R;

public class SongListCursorAdapter extends SimpleCursorAdapter{
	View v;
	public SongListCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView title = (TextView) view.findViewById(	R.id.album_song_title);
		TextView artist = (TextView) view.findViewById(R.id.album_song_artist);
		TextView duration = (TextView) view.findViewById(R.id.album_song_duration);
		title.setText(cursor.getString(cursor.getColumnIndex(MediaColumns.TITLE)));
		artist.setText(cursor.getString(cursor.getColumnIndex(AudioColumns.ARTIST)));
		long millis = cursor.getLong(cursor.getColumnIndex(AudioColumns.DURATION));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
		duration.setText(String.format("%d : %02d",minutes,seconds));
	}
	
	@Override
	public Object getItem(int position) {
		return super.getItem(position);
	}
}
