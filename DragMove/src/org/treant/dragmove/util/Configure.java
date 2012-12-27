package org.treant.dragmove.util;

import android.app.Activity;
import android.util.DisplayMetrics;

public class Configure {
	public static int screenWidth = 0;
	public static int screenHeight = 0;
	public static float screenDensity = 0;

	public static void init(Activity activity) {
		if (screenWidth == 0 || screenHeight == 0 || screenDensity == 0) {
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			Configure.screenWidth = dm.widthPixels;
			Configure.screenHeight = dm.heightPixels;
			Configure.screenDensity = dm.density;
		}
	}
}
