package com.example.music.utils;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.music.R;

public class MyProgressDialog extends ProgressDialog{
	Context mContext;
	ImageView image ;
	ObjectAnimator anim;	
	public MyProgressDialog(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flip);
		image = (ImageView)findViewById(R.id.anim_image);
		anim  = (ObjectAnimator) AnimatorInflater.loadAnimator(mContext,R.animator.flipcard);
		anim.setTarget(image);
		anim.start();
	}
}
