package com.example.music.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.music.R;
import com.example.music.activities.SongList;
import com.squareup.picasso.Picasso;

public class FooterFragment extends Fragment{
	private static final String ARG_POSITION = "position";
	ImageButton playpause;;
	public static TextView title_nowplaying,artist_nowplaying;
	Cursor cursor=null;
	private boolean playbackPaused = false;
	public static FooterFragment newInstance(int pos) {
		FooterFragment fragment = new FooterFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_POSITION, pos);
		fragment.setArguments(args);
		return fragment;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = null;
		cursor = SongList.cursor;
		cursor.moveToFirst();
		cursor.move(getArguments().getInt(ARG_POSITION));
		view = inflater.inflate(R.layout.footer, container,
				false);
		TextView title = (TextView) view.findViewById(R.id.song_title_bottom);
		TextView artist = (TextView) view.findViewById(R.id.song_artist_bottom);
		ImageView songAlbumArt = (ImageView) view.findViewById(R.id.album_thumb);
		playpause = (ImageButton) view.findViewById(R.id.media_play_thumb);
		playpause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				playbackPaused = !playbackPaused;
				if(playbackPaused){
					playpause.setImageResource(android.R.drawable.ic_media_play);
					SongList.musicSrv.pausePlayer();
				}else{
					SongList.musicSrv.resumePlayer();
					playpause.setImageResource(android.R.drawable.ic_media_pause);
				}
			}	
		});
		title.setText(SongList.cursor.getString(SongList.cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
		artist.setText(SongList.cursor.getString(SongList.cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
		Picasso.with(getActivity())
		.load(SongList.imageUri)
		.resize(200,200)
		.noFade().centerCrop()
		.error(R.drawable.ic_launcher)
		.into(songAlbumArt);
		return view;
	}
}