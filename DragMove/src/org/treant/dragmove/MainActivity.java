package org.treant.dragmove;

import java.util.ArrayList;
import java.util.List;

import org.treant.dragmove.adapter.GridViewAdapter;
import org.treant.dragmove.util.Configure;
import org.treant.dragmove.widget.DragGridView;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {
	/**
	 * 格子列数
	 */
	public static final int NUM_COLUMNS = 3;
	private DragGridView dragGridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Configure.init(MainActivity.this);
		dragGridView = (DragGridView) findViewById(R.id.gridView);
		dragGridView.setNumColumns(NUM_COLUMNS);
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < 9; i++) {
			list.add("" + i);
		}
		dragGridView.setAdapter(new GridViewAdapter(this, list));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
