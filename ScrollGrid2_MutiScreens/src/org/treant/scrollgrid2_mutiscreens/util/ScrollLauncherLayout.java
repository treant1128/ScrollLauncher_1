package org.treant.scrollgrid2_mutiscreens.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;
/**
 * 模仿Launcher中的WorkSpace可以左右滑动
 * @author Administrator  http://blog.csdn.net/Yao_GUET date: 2011-05-04
 *
 */
public class ScrollLauncherLayout extends ViewGroup{
	
	private Scroller mScroller;
	private int mCurScreen;
	private int mDefaultScreen;
	private VelocityTracker mVelocityTracker;
	private static final int TOUCH_STATE_REST=0;
	private static final int TOUCH_STATE_SCROLLING=1;
	private static final int SNAP_VELOCITY = 600;
	private int mTouchState=TOUCH_STATE_REST;
	/**
	 * 触发移动事件的最短距离，如果小于这个距离就不触发移动控件，如viewpager就是用这个距离来判断用户是否翻页
	 */
	private int mTouchSlop;
	private float mLastMotionX;
	private PageListener pageListener;
	public interface PageListener{
		public abstract void page(int page);
	}
	public void setPageListener(PageListener pageListener){
		this.pageListener=pageListener;
	}
	/**
	 * 没有此方法就不能再XML中引用此ViewGroup
	 * @param context
	 * @param attrs
	 */
	public ScrollLauncherLayout(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	public ScrollLauncherLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mScroller=new Scroller(context);
		mCurScreen =mDefaultScreen;
		//ViewConfiguration:Contains methods to standard constants used in the UI for timeouts, sizes, and distances
		//Configuration类中的方法基本都是get***，多用于获取和UI相关的标准常量值
		ViewConfiguration viewConfiguration=ViewConfiguration.get(getContext());
		
		mTouchSlop=viewConfiguration.getScaledTouchSlop();//获得能够触发手势滑动的距离
		
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int childLeft=0;
		int childCount=getChildCount();
		for(int i=0;i<childCount;i++){
			View childView=getChildAt(i);
			if(childView.getVisibility()!=View.GONE){
				int childWidth=childView.getMeasuredWidth();
				childView.layout(childLeft, 0, childLeft+childWidth, childView.getMeasuredHeight());
				childLeft+=childWidth;
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width=View.MeasureSpec.getSize(widthMeasureSpec);
		int widthMode=View.MeasureSpec.getMode(widthMeasureSpec);
		if(widthMode!=View.MeasureSpec.EXACTLY){
			throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode");
		}
		//wrap_content 传进去的是AT_MOST 固定数值或fill_parent 传入的模式是EXACTLY
		int heightMode=View.MeasureSpec.getMode(heightMeasureSpec);
		if(heightMode!=View.MeasureSpec.EXACTLY){
			throw new IllegalStateException("ScrollLayout only can run as EXACTLY mode");
		}
		// The children are given the same width and height as the ScrollLayout
		int count=getChildCount();
		for(int i=0;i<count;i++){
			//The actual measurement work of a view is performed in onMeasure(int, int), called by this method.
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		scrollTo(mCurScreen*width, 0);//This will cause a call to onScrollChanged(int, int, int, int) and the view will be invalidated.
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(Configure.isMoving){
			return false;// intercept and dispatch event
		}
		int action=ev.getAction();
		if(action==MotionEvent.ACTION_MOVE && mTouchState!=TOUCH_STATE_REST){
			return true;   // 拦截事件  调用onTouchEvent方法
		}
		float x=ev.getX();
		switch(action){
		case MotionEvent.ACTION_DOWN:
			mLastMotionX=x;
			mTouchState=mScroller.isFinished()?TOUCH_STATE_REST:TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:
			int xDiff=(int) Math.abs(mLastMotionX-x);
			if(xDiff>mTouchSlop){//根据移动距离判定是否触发scroll状态
				mTouchState=TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState=TOUCH_STATE_REST;
			break;
		}
		return mTouchState!=TOUCH_STATE_REST; //如果不是REST，就是在Scrolling，则返回true  
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(mVelocityTracker==null){
			//Be sure to call recycle() when done. only maintain an active object while tracking a movement, 
			//so that the VelocityTracker can be re-used elsewhere.
			mVelocityTracker=VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		int action=event.getAction();
		float x=event.getX();
		switch(action){
		case MotionEvent.ACTION_DOWN:
			if(!mScroller.isFinished()){Log.i("ACTION_DOWN","移动中被刹车"+x);
				mScroller.abortAnimation();  //正在滚动时DOWN后 停止
			}
			mLastMotionX=x;
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX=(int)(mLastMotionX-x);//当前X位置-起点
			mLastMotionX=x;//随时更新mLastMotionX
			scrollBy(deltaX, 0);
			break;
		case MotionEvent.ACTION_UP:  //根据ACTION_UP的临界速度判断手势的快速滑动
			VelocityTracker velocityTracker=mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000, Float.MAX_VALUE);//1 pixel per millisecond/ 1000 pixels per second
		    // must first call computeCurrentVelocity(int) before calling this function.
			int velocityX=(int) velocityTracker.getXVelocity();
			if(velocityX>SNAP_VELOCITY&&mCurScreen>0){//(速度大于+600( 向右滑动、屏幕左移))&&不是第一页
				// fling enough to move left
				snapToScreen(mCurScreen-1);
				--Configure.currentPage;
				pageListener.page(Configure.currentPage);
			}else if(velocityX<-SNAP_VELOCITY&&mCurScreen<getChildCount()-1){//(速度小于-600( 向左滑动、屏幕右移))&&不是最后一页
				snapToScreen(mCurScreen+1);
				Configure.currentPage++;
				pageListener.page(Configure.currentPage);
			}else{
				//没有快速滑动   根据静态位置判定是滑动还是归位    根据偏移值判断目标屏是哪个？  
				snapToDestination();Log.i("根据静态位置判定", "根据静态位置判定");
				Configure.currentPage=mCurScreen;
			}
			//Return a VelocityTracker object back to be re-used by others. You must not touch the object after calling this function.
			if(mVelocityTracker!=null){
				mVelocityTracker.recycle();
				mVelocityTracker=null;
			}
			//修正mTouchState状态
			mTouchState=TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState=TOUCH_STATE_REST;
			break;
		}
		return true;//True if the event was handled
	}
	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
	//	super.computeScroll();
		// computeScrollOffset()--the system execute the offset for you-> call this method you want to know the new location , 
		// if it returns true , the animation is not yet finished ,the loc will be altered to provide the new location;
		if(mScroller.computeScrollOffset()){
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}
	/**
	 *  屏幕移动
	 * @param whichScreen
	 */
	public void snapToScreen(int whichScreen){Log.i("Snap执行","执行了snap");
		whichScreen=Math.max(0, Math.min(whichScreen, getChildCount()-1));//0和getChildCount())-1分别代表第一/最后一页序号
	//	whichScreen=Math.min(Math.max(0, whichScreen), getChildCount()-1);//多此一举
		/**                        getScrollX()
		 * Return the scrolled left position of this view. This is the left edge of the displayed part of your view. 
		 * You do not need to draw any pixels farther left, since those are outside of the frame of your view on screen.
		 */
		Log.i("DasiDingGou","getScrollX()="+getScrollX()+"---whichScreen="+whichScreen+"---getWidth()="+getWidth());
		if(getScrollX()!=whichScreen*getWidth()){
			int delta=whichScreen*getWidth()-getScrollX();
			//Start scrolling by providing a starting point and the distance to travel
			mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta)*5);//Math.abs(delta)*2换屏所用时间和位移成正比  系数可自行指定
			mCurScreen=whichScreen;
			invalidate();Log.i("mCurScreen", "mCurScreen="+mCurScreen);
		}	
	}
	
	private void snapToDestination(){
		int screenWidth=getWidth();
		//假设当前滑屏偏移值getScrollX()加上每个屏幕一半的宽度，除以每个屏幕的宽度
		int whichScreen=(getScrollX()+screenWidth/2)/screenWidth; Log.i("snapToDestination", whichScreen+"--"+screenWidth);
		snapToScreen(whichScreen);
	}
}
