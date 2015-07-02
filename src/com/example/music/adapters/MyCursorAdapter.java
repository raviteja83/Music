package com.example.music.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.music.R;
import com.squareup.picasso.Picasso;

public class MyCursorAdapter extends SimpleCursorAdapter{
	private int FLAG ;
	public MyCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to,flags);
		FLAG = flags;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		//int lastPosition =-1;
		//int position = cursor.getPosition();
		ImageView imageView = (ImageView) view.findViewById(R.id.song_album_art);
		TextView album = (TextView) view.findViewById(R.id.song_title);
		TextView artist = (TextView) view.findViewById(R.id.song_artist);
		/*if(position > lastPosition){
			Animation animation = AnimationUtils.loadAnimation(context,R.animator.up_from_bottom);
			view.startAnimation(animation);
		}
		lastPosition = position;*/

		switch (FLAG) {
		case 1:
			ImageView art_thumb = (ImageView) view.findViewById(R.id.album_image_thumb);
			art_thumb.setBackgroundResource(R.drawable.border);
			TextView alb = (TextView) view.findViewById(R.id.album_song_title);
			TextView art = (TextView) view.findViewById(R.id.album_song_artist);
			alb.setText(cursor.getString(cursor.getColumnIndex(MediaColumns.TITLE)));
			art.setText(cursor.getString(cursor.getColumnIndex(AudioColumns.ALBUM)));
			String[] proj = { AlbumColumns.ALBUM_ART,BaseColumns._ID };
			String selection =  BaseColumns._ID + " =? or " +AlbumColumns.ALBUM + " =? " ;
			String[] selectionArgs = {String.valueOf(cursor.getLong(cursor.getColumnIndex(AudioColumns.ALBUM_ID))),cursor.getString(cursor.getColumnIndex(AudioColumns.ALBUM))};
			Cursor cur =context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,  proj, selection, selectionArgs, null);
			int column_index = cur.getColumnIndexOrThrow(AlbumColumns.ALBUM_ART);
			cur.moveToFirst();
			if(cur.getCount()<=0){
				Picasso.with(context)
				.load(R.drawable.album_art)
				.placeholder(R.drawable.image_loader)
				.into(art_thumb);
			}else{
				Picasso.with(context)
				.load(Uri.parse("file:///"+cur.getString(column_index)))
				.resize(100,100)
				.noFade().centerCrop()
				.error(R.drawable.album_art)
				.placeholder(R.drawable.image_loader)
				.into(art_thumb);
			}
			cur.close();
			break;
		case 2:
			String album_art = cursor.getString(cursor.getColumnIndex(AlbumColumns.ALBUM_ART));
			Uri albumartUri = Uri.parse("file:///" + album_art);
			Picasso.with(context)
			.load(albumartUri)
			.error(R.drawable.album_art)
			.placeholder(R.drawable.image_loader)
			.resize(400,400)
			.noFade().centerCrop()
			.into(imageView);
			album.setText(cursor.getString(cursor.getColumnIndex(AlbumColumns.ALBUM)));
			artist.setText(cursor.getString(cursor.getColumnIndex(AlbumColumns.ARTIST)));
			break;
		case 3:
			Picasso.with(context)
			.load(R.drawable.album_art)
			.placeholder(R.drawable.image_loader)
			.resize(400,400)
			.noFade().centerCrop()
			.into(imageView);
			album.setText(cursor.getString(cursor.getColumnIndex(ArtistColumns.ARTIST)));
			artist.setText(cursor.getString(cursor.getColumnIndex(ArtistColumns.NUMBER_OF_ALBUMS))+" Albums");
			break;
		default:
			break;
		}
	}
}


