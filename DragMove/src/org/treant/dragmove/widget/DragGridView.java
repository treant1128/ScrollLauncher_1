package org.treant.dragmove.widget;

import java.util.HashMap;

import org.treant.dragmove.R;
import org.treant.dragmove.adapter.GridViewAdapter;
import org.treant.dragmove.util.AnimationListenerImpl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class DragGridView extends GridView{

	private Context mContext;
	/**
	 * 每个格子移动时起始的位置    移动后马上被holdPosition更新    多个格子移动时会被依次更新
	 */
	private int dragPosition;
	/**
	 * 拖动时坐标位于List中的位置  不落在任意一项中时值为-1
	 */
	private int dropPosition;
	
	//Move Image Parameters
	private int halfBitmapWidth;
	private int halfBitmapHeight;
	private ImageView dragImageView=null;
	private WindowManager windowManager=null;
	private WindowManager.LayoutParams windowParams;
	
	//Calculate Deviation
	private boolean isCountDeviation=false;
	private int mLongClickX;
	private int mLongClickY;
	private int DeviationX;
	private int DeviationY; // Deviation between setOnItemLongClickListener and onTouchEvent
	
	private boolean isActionUp=false; //whether let go after create animation
	private int contentViewTop=0;
	private int margin_left;
	private int margin_top;
	
	private void init(Context context){
		mContext=context;
	}
	public DragGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}
	public DragGridView(Context context){
		super(context);
		init(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(ev.getAction()==MotionEvent.ACTION_DOWN){
			return this.setOnItemLongClickListener(ev);
		}
		return super.onInterceptTouchEvent(ev);
	}
	public boolean setOnItemLongClickListener(final MotionEvent event){
		setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				mLongClickX=(int) event.getX();//点击处相对于控件自身左上角的坐标
				mLongClickY=(int) event.getY();//每次随着点击位置的坐标不同而改变    影响长按动画的移动参数
//				Log.i("mLongClickX", ""+mLongClickX);Log.i("mLongClickY", ""+mLongClickY);
				dragPosition=dropPosition=position;
				isActionUp=false;
				((GridViewAdapter)getAdapter()).setMovingState(true);
				ViewGroup itemView=(ViewGroup) getChildAt(dragPosition-getFirstVisiblePosition());//dragPosition-getFirstVisiblePosition()是屏幕中的第几个？(start by 0)
				LinearLayout.LayoutParams lp= (LinearLayout.LayoutParams) (itemView.findViewById(R.id.g_one)).getLayoutParams();
				margin_left=lp.leftMargin;    margin_top=lp.topMargin;
				
				itemView.destroyDrawingCache();
				itemView.setDrawingCacheEnabled(true);
				itemView.setDrawingCacheBackgroundColor(0x000000);
				
				Bitmap bm=Bitmap.createBitmap(itemView.getDrawingCache(true));//copy bitmap
				Bitmap bitmap=Bitmap.createBitmap(bm, lp.leftMargin, lp.topMargin, 
						bm.getWidth()-lp.leftMargin-lp.rightMargin, bm.getHeight()-lp.topMargin-lp.bottomMargin);
				
				showCreateDragImageAnimation(itemView, bitmap);
				return false;
			}
			
		});
		return super.onInterceptTouchEvent(event);
		
	}
	/**
	 * 生成浮动图片动画的过程
	 * @param itemView
	 * @param bitmap
	 */
	private void showCreateDragImageAnimation(final ViewGroup itemView, final Bitmap bitmap){
		halfBitmapWidth=bitmap.getWidth()/2;  halfBitmapHeight=bitmap.getHeight()/2;
		TranslateAnimation animation=new TranslateAnimation(0, mLongClickX-halfBitmapWidth-itemView.getLeft(), 
				0, mLongClickY-halfBitmapHeight-getContentViewTop()-(getContentViewTop()+itemView.getTop()));	//坐标移动的距离可以自己adjust
		//每个方格子的itemView.get**上下左右是确定的， 跟点击位置无关    综合起来移动动画只和点击坐标mLongClickX/Y有关
//		Log.i("半宽恒定?", halfBitmapWidth+"");Log.i("半高恒定?", halfBitmapHeight+"");
//		Log.i("toDeltaX", mLongClickX - halfBitmapWidth - itemView.getLeft()+"");
		Log.i("toDeltaY", mLongClickY - halfBitmapHeight - getContentViewTop() -(itemView.getTop() + getContentViewTop())+"");
//		Log.i("left-top-right-bottom", itemView.getLeft()+"-"+itemView.getTop()+"-"+itemView.getRight()+"-"+itemView.getBottom());
		animation.setFillAfter(false);//If fillAfter is true, the transformation that this animation performed will persist when it is finished.
		animation.setDuration(1000);
		animation.setAnimationListener(new AnimationListenerImpl(){
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				if(!isActionUp){
					createBitmapInWindow(bitmap, mLongClickX, mLongClickY);
					itemView.setVisibility(View.GONE);
				}
				super.onAnimationEnd(animation);
			}
		});
		itemView.startAnimation(animation);
		
	}
	/**
	 * 创建浮动的图片dragImageView
	 * @param bitmap
	 * @param x
	 * @param y
	 */
	private void createBitmapInWindow(Bitmap bitmap, int x, int y){
		windowParams=new WindowManager.LayoutParams();
		windowParams.gravity=Gravity.TOP|Gravity.LEFT;
		windowParams.width=WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.height=WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.x=x-halfBitmapWidth;
		windowParams.y=y-getContentViewTop()-halfBitmapHeight;
		windowParams.alpha=0.8f;
		ImageView image=new ImageView(getContext());
		image.setImageBitmap(bitmap);
		windowManager=(WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		if(dragImageView!=null){
			windowManager.removeView(dragImageView);
		}
		windowManager.addView(image, windowParams);
		dragImageView=image;  ////再这里把生成的图片iv赋给dragImageView后  onTouchEvent才奏效
	}

	/**
	 * 获取状态栏高度
	 * android:theme="@android:style/Theme.Black.NoTitleBar"会使得window.findViewById(Window.ID_ANDROID_CONTENT).getTop()==0
	 * @return
	 */
	private int getContentViewTop(){
		if(contentViewTop==0){
			Window window=((Activity)mContext).getWindow();
			contentViewTop=window.findViewById(Window.ID_ANDROID_CONTENT).getTop(); Log.i("没进入if", "没进入if"+contentViewTop);
			if(contentViewTop==0){
				Rect rect=new Rect(); 
				window.getDecorView().getWindowVisibleDisplayFrame(rect);
				contentViewTop=rect.top; Log.i("进入", "进入"+contentViewTop);
			}
		}Log.i("StateBarHeight",contentViewTop+"");
		return contentViewTop;
	}
	
	/**
	 * 处理Touch的Move UP事件   Down在上面处理
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		int x=(int) ev.getX();
		int y=(int) ev.getY();
		switch(ev.getAction()){
		case MotionEvent.ACTION_MOVE:
			if(dragImageView!=null){   //没有浮动图片dragImageView生成不起作用
				if(!isCountDeviation){
					DeviationX=x-mLongClickX;
					DeviationY=y-mLongClickY;
					isCountDeviation=true;   //Deviation只在第一次时确定  随后windowParams.x/y 跟随getX/Y()同样变化
				}
				onDrag(x,y);
				onItemsMove(x,y);
				//其实不用定义DeviationX/Y系统常量
			}
			break;
		case MotionEvent.ACTION_UP:Log.i("执行到UP", "执行到UP");
			isActionUp=true;
			if (dragImageView != null) {
				animationMap.clear();    
				showDropAnimation(x, y);
			}
			break;
		}
		return super.onTouchEvent(ev);
	}
	/**
	 * 放手动画
	 * @param x
	 * @param y
	 */
	private void showDropAnimation(int x, int y){
		ViewGroup moveView=(ViewGroup) getChildAt(dragPosition);
		//绝对屏幕的坐标：现在->(x，y)        目标->(halfBitmapWidth+moveView.getLeft(), halfBitmapHeight+moveView.getTop()) 
		//相对于moveView的坐标：现在->(?, ?)  目标和moveView重合(0,0) 计算差值(x-halfBitmapWidth-moveView.getLeft(),y-halfBitmapHeight-moveView.getTop())
		TranslateAnimation animation=new TranslateAnimation(x-halfBitmapWidth-moveView.getLeft(), 0,
				y-halfBitmapHeight-moveView.getTop(), 0);
		animation.setFillAfter(false);
		animation.setDuration(300);
		animation.setAnimationListener(new AnimationListenerImpl(){
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				super.onAnimationStart(animation);
				if(dragImageView!=null){
					Log.i("dd", "##############################");
					windowManager.removeView(dragImageView);//移除浮动图片
					dragImageView=null;
					}
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				super.onAnimationEnd(animation);
				GridViewAdapter adapter=(GridViewAdapter)getAdapter();
				adapter.setMovingState(false);
				adapter.notifyDataSetChanged();
			}
		});
		
		moveView.startAnimation(animation);
	}
	/**
	 * 拖动图片时windowParams跟着变化  实现图片移动
	 * @param x
	 * @param y
	 */
	private void onDrag(int x, int y){Log.i("执行到1", "执行到1");
		if(dragImageView!=null){     //判断多此一举
			windowParams.alpha=0.8f;  //和之前一样
			windowParams.x=x-DeviationX-halfBitmapWidth;
			windowParams.y=y-DeviationY-getContentViewTop()-halfBitmapHeight;
//			windowParams.x=x-halfBitmapWidth;
//			windowParams.y=y-getContentViewTop()-halfBitmapHeight;
			windowManager.updateViewLayout(dragImageView, windowParams);
		}
	}
	/**
	 * 多个方格子一起动
	 * @param x
	 * @param y
	 */
	private void onItemsMove(int x, int y){
		dropPosition=pointToPosition(x,y);   //当前移动点位于List中的位置   下面还当做exchange方法中的gonePosition参数
		if(dropPosition==AbsListView.INVALID_POSITION){ //All valid positions are in the range 0 to 1 less than the number of items in the current adapter 
			return;   //drop没有落在其他格子上 
		}
		int MoveNum=dropPosition-dragPosition;  //
		if(MoveNum!=0&&!isMovingFastConflict(MoveNum)){  //drag!=drop  
			int itemMoveNum=Math.abs(MoveNum);//参与移动格子数
			for(int i=0;i<itemMoveNum;i++){
				int holdPosition=(MoveNum>0)?dragPosition+1:dragPosition-1;
//				if(MoveNum>0){ //向后移动
//					holdPosition=dragPosition+1;
//				}else{ //向前移动
//					holdPosition=dragPosition-1;
//				}
				
				((GridViewAdapter)getAdapter()).exchange(holdPosition, dragPosition, dropPosition);
				View moveView=getChildAt(holdPosition);//目标位置的格子视图
				Animation animation=this.getMoveAnimation(moveView.getLeft(), moveView.getTop(),
						getChildAt(dragPosition).getLeft(), getChildAt(dragPosition).getTop());
				animation.setAnimationListener(new NotifyDataSetListener(holdPosition));
				dragPosition=holdPosition;//循环过程中依次更新dragPosition
				moveView.startAnimation(animation);
			}
		}

	}
	/**
	 * 对即将参与移动的格子 遍历判断 
	 * 一个格子必须完全完成上次移动动画才能准备进入下次移动
	 * @param moveNum
	 * @return
	 */
	private boolean isMovingFastConflict(int moveNum){
		int itemsMoveNum=Math.abs(moveNum);
		int temp=dragPosition;   //dragPosition会随移动而变化        复制一个副本 而不影响全局变量dragPosition
		for(int i=0;i<itemsMoveNum;i++){
			int holdPosition;////移动过程中可能会落下的位置  会随dragPosition更新
			if(moveNum>0){
				holdPosition=temp+1;
			}else{
				holdPosition=temp-1;
			}
			if(animationMap.containsKey(holdPosition)){
				return true;
			}
			temp=holdPosition;
		}
		return false;
	}
	private HashMap<Integer, Boolean> animationMap=new HashMap<Integer, Boolean>();
	
	private class NotifyDataSetListener extends AnimationListenerImpl{
		private int movedPosition;
		public NotifyDataSetListener(int primaryPosition){
			this.movedPosition=primaryPosition;
		}
		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			super.onAnimationStart(animation);
			animationMap.put(movedPosition, true);//put into map when start
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			super.onAnimationEnd(animation);
			if(animationMap.containsKey(movedPosition)){
				animationMap.remove(movedPosition);//remove from map when end
			}
			if(animationMap.isEmpty()){//所有动画移动完后更新数据
				((GridViewAdapter)getAdapter()).notifyDataSetChanged();
			}
			
		}
	}
	/**
	 * 移动动画  x y位移分别为toX-x，toY-y
	 * @param x
	 * @param y
	 * @param toX
	 * @param toY
	 * @return
	 */
	private Animation getMoveAnimation(float x, float y, float toX, float toY ){
		TranslateAnimation animation=new TranslateAnimation(0, toX-x, 0, toY-y);
		animation.setFillAfter(true);
		animation.setDuration(300);
		return animation;
	}
	
}
