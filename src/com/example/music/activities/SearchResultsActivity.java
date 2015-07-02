package com.example.music.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.example.music.R;
import com.example.music.adapters.MySearchAdapter;

public class SearchResultsActivity extends Activity implements LoaderCallbacks<Cursor> {
	String query;
	TextView empty;
	MySearchAdapter mAdapter;
	static int i=0;
	private static final int LOADER = 0;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_results);
		ActionBar actionBar = getActionBar();
		// Enabling Back navigation on Action Bar icon
		getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back_dark));
		GridView list = (GridView)findViewById(R.id.song_list);
		empty = (TextView)findViewById(android.R.id.empty);
		String[] mFromColumns={ MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM };
		int[] mToFields = {R.id.song_title,R.id.song_artist};
		mAdapter = new MySearchAdapter(this, //current context
				R.layout.song,  // Layout for a single row
				null,                // No Cursor yet
				mFromColumns,        // Cursor columns to use
				mToFields,           // Layout fields to use
				0                  //  flag
				);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_results, menu);
		SearchManager searchManager =
				(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView =
				(SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setIconified(false);
		searchView.setSearchableInfo(
				searchManager.getSearchableInfo(getComponentName()));
		searchView.setOnQueryTextListener(new OnQueryTextListener() { 

			@Override 
			public boolean onQueryTextChange(String querytext) {
				query = querytext;
				if(i==0){
					getLoaderManager().initLoader(LOADER, null, SearchResultsActivity.this);
					i=1;
				}else
					getLoaderManager().restartLoader(LOADER, null, SearchResultsActivity.this);

				return true; 
			}

			@Override
			public boolean onQueryTextSubmit(String querytext) {
				query = querytext;
				getLoaderManager().restartLoader(LOADER, null, SearchResultsActivity.this);
				return true; 
			} 

		});
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch(id){
		case LOADER:
			String[] proj = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.DATA};
			String selection =  MediaStore.Audio.Media.TITLE + " LIKE ? or "+ MediaStore.Audio.Media.ALBUM + " LIKE ? or "+MediaStore.Audio.Media.ARTIST + " LIKE ? " ;
			String[] selectionArgs = {"%"+query+"%","%"+query+"%","%"+query+"%"};
			return new CursorLoader(this,
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, selection, selectionArgs, null);
		default:
			return null;
		}
	}
	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		if(cursor.getCount()< 1)
			empty.setText("No Matches Found");
		else empty.setVisibility(View.GONE);
	}
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.changeCursor(null);
	}
}
