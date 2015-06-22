package com.example.music.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.music.activities.MainActivity;
import com.example.music.fragments.PagerFragment;

public class MyFragmentAdapter  extends FragmentStatePagerAdapter  {
	public MyFragmentAdapter(FragmentManager fm) {
		super(fm);
	}
	
	@Override
	public int getCount() {
		return MainActivity.songList.size();
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	@Override
	public Fragment getItem(int pos) {
		return PagerFragment.newInstance(pos);
	}
}
