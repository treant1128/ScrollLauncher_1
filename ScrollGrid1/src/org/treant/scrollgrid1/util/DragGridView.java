package org.treant.scrollgrid1.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

public class DragGridView extends GridView {
	
	private int dragPosition;
	private int dropPosition;
	/**
	 * 拖拽时生成的浮动图片
	 */
	private ImageView dragImageView;
	private WindowManager windowManager;
	private WindowManager.LayoutParams windowParams;
	
	private int itemWidth, itemHeight;
	private boolean flag=false;
	public DragGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public DragGridView(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	public void setLongFlag(boolean temp){
		this.flag=temp;
	}
	
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(ev.getAction()==MotionEvent.ACTION_DOWN){
			this.setOnItemLongClickListener(ev);
		}
		return super.onInterceptTouchEvent(ev);
	}
	private boolean setOnItemLongClickListener(final MotionEvent event){
		this.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				int mLongClickX=(int) event.getX();//点击处相对于控件自身左上角的坐标
				int mLongClickY=(int) event.getY();//每次随着点击位置的坐标不同而改变    影响长按动画的移动参数
				
				dragPosition=dropPosition=arg2;
				View itemView=getChildAt(dragPosition-getFirstVisiblePosition());
				itemWidth=itemView.getWidth();
				itemHeight=itemView.getHeight();
				
				itemView.destroyDrawingCache();
				itemView.setDrawingCacheEnabled(true);//When the drawing cache is enabled, the next call to getDrawingCache() or buildDrawingCache() will draw the view in a bitmap
				Bitmap bitmap=Bitmap.createBitmap(itemView.getDrawingCache(true));
				startDrag(bitmap, mLongClickX, mLongClickY);
				
				
				return false;
			}
			
		});
		return super.onInterceptTouchEvent(event);
	}
	
	private void startDrag(Bitmap bitmap, int mLongClickX, int mLongClickY){
		stopDrag();
		windowParams=new WindowManager.LayoutParams();
		windowParams.gravity=Gravity.TOP|Gravity.LEFT;
		windowParams.width=WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.height=WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.x=mLongClickX-itemWidth/2;
		windowParams.y=mLongClickY-itemHeight/2;
//		windowParams.alpha=0.6f;
		ImageView iv=new ImageView(getContext());
		iv.setImageBitmap(bitmap);
		windowManager=(WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(iv, windowParams);
		dragImageView=iv;
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(dragImageView!=null&&dragPosition!=AdapterView.INVALID_POSITION){
			int x=(int)ev.getX();
			int y=(int)ev.getY();
			switch(ev.getAction()){
			case MotionEvent.ACTION_MOVE:  Log.i("move", "move move");
				onDrag(x, y);
				break;
			case MotionEvent.ACTION_UP:   Log.i("UP", "upupup");
				stopDrag();
				onDrop(x, y);
				break;
			}
		}
		return super.onTouchEvent(ev);
	}
	private void onDrag(int x, int y){
		if(dragImageView!=null){
			windowParams.x=x-itemWidth/2;
			windowParams.y=y-itemHeight/2;
			windowParams.alpha=0.6f;
			windowManager.updateViewLayout(dragImageView, windowParams);
		}
	}
	/**
	 * 放手时
	 * @param x
	 * @param y
	 */
	private void onDrop(int x, int y){
		//The position of the item which contains the specified point, or INVALID_POSITION if the point does not intersect an item
		int holdPosition=pointToPosition(x, y);////当前移动点位于List中的位置  么有落在任何一个格子中时为-1
		if(holdPosition!=AdapterView.INVALID_POSITION){
			dropPosition=holdPosition;
		}
		Log.i("", "here444444444444444444");
		if(dragPosition!=dropPosition){//发生了拖动   drag!=drop
			View fromView=getChildAt(dragPosition-getFirstVisiblePosition());
			View toView=getChildAt(dropPosition-getFirstVisiblePosition());
			final DragGridAdapter adapter=(DragGridAdapter)getAdapter();;
			Animation from=null;
			if(dragPosition%2==0){// drag from left  0,2,4,6
				from=obtainAnimation(dropPosition%2==0?0:1,dropPosition/2-dragPosition/2);//x move 0/1 when drop at left/right   y move deviation/2
				fromView.startAnimation(from);
				toView.startAnimation(obtainAnimation(dropPosition%2==0?0:-1,dragPosition/2-dropPosition/2));
			}else{//drag from right  1  3  5  7
				from=obtainAnimation(dropPosition%2==1?0:-1,dropPosition/2-dragPosition/2);
				fromView.startAnimation(from);
				toView.startAnimation(obtainAnimation(dropPosition%2==1?0:1,dragPosition/2-dropPosition/2));
			}
			from.setAnimationListener(new AnimationListenerImpl(){
				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					super.onAnimationEnd(animation);
					adapter.exchangePosition(dragPosition, dropPosition);
				}
			});
		}
	
	}
	
	private Animation obtainAnimation(float x, float y){
		TranslateAnimation animation=new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, x,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, y);
		animation.setFillAfter(true);
		animation.setDuration(5000);
		animation.setInterpolator(new OvershootInterpolator(2));
		return animation;
	}
	/**
	 * 生成前 先清空
	 */
	private void stopDrag(){
		if(dragImageView!=null){
			windowManager.removeView(dragImageView);
			dragImageView=null;
		}
	}
}
