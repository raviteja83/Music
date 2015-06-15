package com.example.music.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.music.R;


public class MySimpleAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;

	public MySimpleAdapter(Context context, String[] values) {
		super(context, R.layout.customlist, values);
		this.context = context;
		this.values = values;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.customlist,null);
		} 
		TextView textView = (TextView) convertView.findViewById(R.id.text1);
		ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
		textView.setText(values[position]);
		imageView.setImageResource(R.drawable.ic_launcher);
		return convertView; 
	}


}
