package com.example.music.fragments;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.music.R;
import com.example.music.activities.MainActivity;
import com.example.music.utils.Song;
import com.squareup.picasso.Picasso;

public class PagerFragment extends Fragment{
	private static final String ARG_POSITION = "position";
	private Song currSong;
//	private boolean liked =false,disliked=false;
	private ImageView albumArt,artThumb;
	public static PagerFragment newInstance(int pos) {
		PagerFragment fragment = new PagerFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_POSITION, pos);
		fragment.setArguments(args);
		return fragment;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = null;
		TextView title_nowplaying,artist_nowplaying;
		rootView = inflater.inflate(R.layout.fragment_pager, container,
				false);
		currSong = MainActivity.songList.get(getArguments().getInt(ARG_POSITION));
		String album = currSong.getAlbum();
		title_nowplaying  = (TextView)rootView.findViewById(R.id.songtitle);
		artist_nowplaying  = (TextView)rootView.findViewById(R.id.songartist);
		title_nowplaying.setText(currSong.getTitle());
		artist_nowplaying.setText(currSong.getArtist());
		artThumb = (ImageView) rootView.findViewById(R.id.art_thumb);
		getResources().getDrawable(R.drawable.back).setColorFilter(Color.BLACK, Mode.MULTIPLY );
		albumArt  = (ImageView)rootView.findViewById(R.id.mp3Image);
		setAlbumart(album);
		return rootView;
	}

	private void setAlbumart(String album) {
		String[] proj = { AlbumColumns.ALBUM_ART,BaseColumns._ID };
		String selection =  AlbumColumns.ALBUM + " =? " ;
		String[] selectionArgs = {album};
		Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,  proj, selection, selectionArgs, null);
		int column_index = cursor.getColumnIndexOrThrow(AlbumColumns.ALBUM_ART);
		cursor.moveToFirst();
		Picasso.with(getActivity())
		.load(Uri.parse("file:///"+cursor.getString(column_index)))
		.resize(800,800)
		.noFade().centerCrop()
		.error(R.drawable.album_art)
		.placeholder(R.drawable.image_loader)
		.into(albumArt);
	
		Picasso.with(getActivity())
		.load(Uri.parse("file:///"+cursor.getString(column_index)))
		.resize(100,100)
		.noFade().centerCrop()
		.error(R.drawable.album_art)
		.placeholder(R.drawable.image_loader)
		.into(artThumb);
		cursor.close();        
	}
}