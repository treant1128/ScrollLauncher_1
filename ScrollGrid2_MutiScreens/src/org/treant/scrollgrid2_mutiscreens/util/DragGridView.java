package org.treant.scrollgrid2_mutiscreens.util;

import org.treant.scrollgrid2_mutiscreens.R;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

public class DragGridView extends GridView {

	private int dragPosition=0;
	private int dropPosition=0;
	/**
	 * the float image created by windowManager when drag motion is on
	 */
	private ImageView dragImageView=null;
	View fromView=null;
	int stopCount=0;
	
	private G_PageListener pageListener;
	/**
	 * 拖动时跨越的页面个数  右增左减
	 */
	int moveNum;
	private G_ItemChangeListener itemChangeListener;
	
	private WindowManager windowManager;
	private WindowManager.LayoutParams windowParams;
	
	private int itemWidth, itemHeight;
//	boolean flag = false;
//
//	public void setLongFlag(boolean temp) {
//		flag = temp;
//	}
	public DragGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public DragGridView(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	public interface G_PageListener{
		abstract public void page(int page);
	}
	
	public interface G_ItemChangeListener{
		/**
		 * 
		 * @param from 起点在本页面的位置 0~PAGE_SIZE-1
		 * @param to   落点在本页面的位置 0~PAGE_SIZE-1
		 * @param count  拖动时跨越的页面数目   右跨一页增一
		 */
		public abstract void change(int from, int to, int count);
	}
	public void setPageListener(G_PageListener pageListener){
		this.pageListener=pageListener;
	}
	public void setOnItemChangeListener(G_ItemChangeListener itemChangeListener){
		this.itemChangeListener=itemChangeListener;
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event){
		if(event.getAction()==MotionEvent.ACTION_DOWN){
			return this.setOnItemLongClickListener(event);
		}
		return super.onInterceptTouchEvent(event);
	}
	private boolean setOnItemLongClickListener(final MotionEvent event){
		setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Configure.isMoving=true;//这一行很重要  这觉得ScrollLauncher中的onInterceptTouchEvent方法去向
				int mLongClickX=(int)event.getX();
				int mLongClickY=(int)event.getY();
				dragPosition=dropPosition=position;
				fromView=getChildAt(dragPosition-getFirstVisiblePosition());
				itemWidth=fromView.getWidth();
				itemHeight=fromView.getHeight();
				//Frees the resources used by the drawing cache.
				fromView.destroyDrawingCache();// frees the resources used by the drawing cache
				//When the drawing cache is enabled, the next call to getDrawingCache() or buildDrawingCache() will draw the view in a bitmap
				fromView.setDrawingCacheEnabled(true);
				Bitmap bitmap=Bitmap.createBitmap(fromView.getDrawingCache());
				startDrag(bitmap, mLongClickX, mLongClickY);
				return false;
			}
			
		});
		return super.onInterceptTouchEvent(event);
	}
	private void startDrag(final Bitmap bitmap, final int mLongClickX, final int mLongClickY){
		windowManager=(WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		Animation dispear=AnimationUtils.loadAnimation(getContext(), R.anim.fadeout);
		dispear.setAnimationListener(new AnimationListenerImpl(){
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				super.onAnimationEnd(animation);
				fromView.setVisibility(View.GONE);
				stopDrag();
				windowParams=new WindowManager.LayoutParams();
				windowParams.x=mLongClickX-itemWidth/2;
				windowParams.y=mLongClickY-itemHeight/2;
				windowParams.gravity=Gravity.LEFT|Gravity.TOP;//不设定gravity位置会严重偏移
				windowParams.width=WindowManager.LayoutParams.WRAP_CONTENT;
				windowParams.height=WindowManager.LayoutParams.WRAP_CONTENT;
				ImageView floatImage=new ImageView(getContext());
				floatImage.setImageBitmap(bitmap);
				windowManager.addView(floatImage, windowParams);
				floatImage.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.occur));
				dragImageView=floatImage;
			
			}
		});
		fromView.startAnimation(dispear);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		//AdapterView.INVALID_POSITION 
		if(dragImageView!=null&&dragPosition!=AdapterView.INVALID_POSITION){
			int x=(int)ev.getX();
			int y=(int)ev.getY();
			switch(ev.getAction()){
			case MotionEvent.ACTION_MOVE:
				onDrag(x, y);
				break;
			case MotionEvent.ACTION_UP:
				stopDrag();//松手前清空dragImageView
				onDrop(x, y);
				break;
			}
		}
		return super.onTouchEvent(ev);
	}
	
	private void onDrag(int x, int y){
		if(dragImageView!=null){
			windowParams.alpha=0.6f;
			windowParams.x=x-itemWidth/2-moveNum*Configure.screenWidth;
			windowParams.y=y-itemHeight/2;
			windowManager.updateViewLayout(dragImageView, windowParams);
		}
		Log.i("Why stopCount no change", "--moveNum="+moveNum);
		Log.i("iii","moveNum="+moveNum+"-滑块距左屏的距离：->"+x+"<->"+Configure.isChangingPage+"<->"+Configure.screenDensity+"-"+Configure.screenWidth);
		//Configure.screenDensity=1.0   Configure.screenWidth=320;  Configure.screenWidth-20*Configure.screenDensity=300.0
		if((x>=(moveNum+1)*(Configure.screenWidth-20*Configure.screenDensity)||   //移动坐标event。getX（）超过300(距离右边界20像素处的边缘)
				x<=moveNum*(Configure.screenWidth-20*Configure.screenDensity))&&  
				!Configure.isChangingPage){ //isChangingPage需要及时归位
			stopCount++;  //拦截次数累加     判断是否为执意换页
		}else{
			stopCount=0;  //清零 重置
		}Log.i("stopCount", stopCount+"");
		if(stopCount>Configure.boundaryInterceptTimes){ //满足执意换页条件
			stopCount=0;
			//并且 不是最后一页  //x>=(moveNum+1)*(Configure.screenWidth-20*Configure.screenDensity)无须判断 因为满足stopCount大于10， 必然满足x的判断条件
			if(x>=(moveNum+1)*(Configure.screenWidth-20*Configure.screenDensity)&&Configure.currentPage<Configure.countPage-1){
				//isChangeingPage的状态 通过匿名内部类中实现的page方法中的Handler延迟异步更改回false    否则只能跨屏一次，不能连续跨屏拖动 
				Configure.isChangingPage=true; 
				pageListener.page((++Configure.currentPage));
				moveNum++;
			}else if(x<=moveNum*(Configure.screenWidth-20*Configure.screenDensity)&&Configure.currentPage>0){//不是第一页（currentPage=0）
				Configure.isChangingPage=true;
				pageListener.page(--Configure.currentPage);
				moveNum--;
			}
		}
	}
	
	private void onDrop(int x, int y){
		Configure.isMoving=false;
		int holdPosition=pointToPosition(x-moveNum*Configure.screenWidth, y);
		if(holdPosition!=AdapterView.INVALID_POSITION){
			dropPosition=holdPosition;
		}
		if(moveNum!=0){  //换页发生了
			itemChangeListener.change(dragPosition, dropPosition, moveNum);
			moveNum=0;
			return;
		}
		moveNum=0;
		View toView=getChildAt(dropPosition-getFirstVisiblePosition());//toView执行平移dropPosition到dragPosition
		Animation emerge=null;
		
		if(dragPosition%2==0){//drag from left arrow0  2  4  6...
			emerge=getEmergeAnimation(dropPosition%2==dragPosition%2?0:1,dropPosition/2-dragPosition/2 );//drop at left/right   x move 0/1
			if(dragPosition!=dropPosition){ //exchange happens 
				toView.startAnimation(getTranslateAnimation(dropPosition%2==dragPosition%2?0:-1,dragPosition/2-dropPosition/2));
			}
		}else{//drag from right 1  3  5  7
			emerge=getEmergeAnimation(dropPosition%2==dragPosition%2?0:-1,dropPosition/2-dragPosition/2);
			if(dragPosition!=dropPosition){
				toView.startAnimation(getTranslateAnimation(dropPosition%2==dragPosition%2?0:1,dragPosition/2-dropPosition/2));
			}
		}
		fromView.startAnimation(emerge);
		emerge.setAnimationListener(new AnimationListenerImpl(){
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				super.onAnimationEnd(animation);
				((DragAdapter)getAdapter()).exchangePosition(dragPosition, dropPosition);
			}
		});
	}
	private Animation getTranslateAnimation(float x, float y){
		TranslateAnimation animation=new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, x, 
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, y);
		animation.setFillAfter(true);
		animation.setDuration(5500);
		return animation;
	}
	private Animation getEmergeAnimation(float x, float y){
		AnimationSet set=new AnimationSet(true);
		TranslateAnimation translate=new TranslateAnimation(Animation.RELATIVE_TO_SELF, x, Animation.RELATIVE_TO_SELF, x,
				Animation.RELATIVE_TO_SELF, y, Animation.RELATIVE_TO_SELF, y);
		translate.setFillAfter(true); translate.setDuration(5500);
		AlphaAnimation alpha=new AlphaAnimation(0.1f, 1.0f);
		alpha.setFillAfter(true); alpha.setDuration(5500);
		ScaleAnimation scale=new ScaleAnimation(1.2f, 1.0f, 1.2f, 1.0f);
		scale.setFillAfter(true); scale.setDuration(5500);
		set.setInterpolator(new AccelerateInterpolator());
		set.addAnimation(translate); set.addAnimation(alpha); set.addAnimation(scale);
		return set;
	}
	/**
	 * remove dragImageView before created a new dragImageView
	 */
	private void stopDrag(){
		if(dragImageView!=null){
			windowManager.removeView(dragImageView);
			dragImageView=null;
		}
	}
}
