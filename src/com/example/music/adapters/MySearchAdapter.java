package com.example.music.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.music.R;
import com.squareup.picasso.Picasso;

public class MySearchAdapter extends SimpleCursorAdapter{
	public MySearchAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to,flags);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView imageView = (ImageView) view.findViewById(R.id.song_album_art);
		TextView album = (TextView) view.findViewById(R.id.song_title);
		TextView artist = (TextView) view.findViewById(R.id.song_artist);
		album.setText(cursor.getString(cursor.getColumnIndex(AudioColumns.ALBUM)));
		artist.setText(cursor.getString(cursor.getColumnIndex(AudioColumns.ARTIST)));
		String[] proj = { AlbumColumns.ALBUM_ART,BaseColumns._ID };
		String selection =  AlbumColumns.ALBUM + " =? " ;
		String[] selectionArgs = {cursor.getString(cursor.getColumnIndex(AudioColumns.ALBUM))};
		Cursor cur =context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,  proj, selection, selectionArgs, null);
		int column_index = cur.getColumnIndexOrThrow(AlbumColumns.ALBUM_ART);
		cur.moveToFirst();
		if(cur.getCount()<=0){
			Picasso.with(context)
			.load(R.drawable.album_art)
			.placeholder(R.drawable.image_loader)
			.into(imageView);
		}else{
			Picasso.with(context)
			.load(Uri.parse("file:///"+cur.getString(column_index)))
			.resize(400,400)
			.noFade().centerCrop()
			.error(R.drawable.album_art)
			.placeholder(R.drawable.image_loader)
			.into(imageView);
		}
		cur.close();
	}
}


