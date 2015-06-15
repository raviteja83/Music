package com.example.music.utils;

public class Artist {
	private long id;
	private String artist;
	public Artist(long songID,String Artist) {
		setId(songID);
		setArtist(Artist);

	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
}
