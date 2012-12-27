package org.treant.scrollgrid1;

import java.util.ArrayList;

import org.treant.scrollgrid1.util.AnimationListenerImpl;
import org.treant.scrollgrid1.util.DragGridAdapter;
import org.treant.scrollgrid1.util.DragGridView;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private DragGridView gridView;
	private ImageView img;
	TranslateAnimation left, right;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// requestWindowFeature(Window.Feature)
		gridView = (DragGridView) this.findViewById(R.id.myGridView);
		img = (ImageView) this.findViewById(R.id.run_image);
		ArrayList<String> data = new ArrayList<String>();
		for (int i = 0; i < 8; i++) {
			data.add("m*y" + i);
		}
		DragGridAdapter adapter = new DragGridAdapter(MainActivity.this, data);
		gridView.setAdapter(adapter);
		runBackgroundAnimation();
	}

	private void runBackgroundAnimation() {
		right = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1f,
				Animation.RELATIVE_TO_PARENT, -2f,
				Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT,
				0);
		right.setDuration(25000);
		right.setFillAfter(true);
		right.setAnimationListener(new AnimationListenerImpl() {
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				super.onAnimationEnd(animation);
				img.startAnimation(left);
			}
		});
		left = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -2f,
				Animation.RELATIVE_TO_PARENT, -1f,
				Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT,
				0);
		left.setDuration(25000);
		left.setFillAfter(true);
		left.setAnimationListener(new AnimationListenerImpl() {
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				super.onAnimationEnd(animation);
				img.startAnimation(right);
			}
		});
		img.startAnimation(right);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
