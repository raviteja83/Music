package com.example.music.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;


public class Song {
	private long id;
	private String title;
	private String artist;
	private String album;
	private String album_art;
	private long duration;
	public Song(long songID, String songAlbum, String songArtist,String songAlbumArt) {
		setId(songID);
		setAlbum(songAlbum);
		setArtist(songArtist);
		setAlbum_art(songAlbumArt);

	}
	public Song(long songID, String songTitle, String Artist,String Album,long duration){
		setId(songID);
		setTitle(songTitle);
		setArtist(Artist);
		setAlbum(Album);
		setDuration(duration);
	}
	
	public String getTitle(){
		return title;
	}
	public String getArtist(){
		return artist;
	}
	public String getAlbum_art() {
		return album_art;
	}
	public static List<Song> getsongs(ArrayList<Song> songList, Context mContext) {
		return songList;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public void setAlbum_art(String album_art) {
		this.album_art = album_art;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}


}
