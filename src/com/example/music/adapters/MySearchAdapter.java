package com.example.music.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
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

	public void bindView(View view, Context context, Cursor cursor) {
		ImageView imageView = (ImageView) view.findViewById(R.id.song_album_art);
		TextView album = (TextView) view.findViewById(R.id.song_title);
		TextView artist = (TextView) view.findViewById(R.id.song_artist);
		album.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
		artist.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
		String[] proj = { MediaStore.Audio.Albums.ALBUM_ART,MediaStore.Audio.Albums._ID };
		String selection =  MediaStore.Audio.Albums.ALBUM + " =? " ;
		String[] selectionArgs = {cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))};
		Cursor cur =context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,  proj, selection, selectionArgs, null);
		int column_index = cur.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
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


