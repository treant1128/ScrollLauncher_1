package org.treant.scrollgrid2_mutiscreens.util;

import java.util.List;

import org.treant.scrollgrid2_mutiscreens.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DragAdapter extends BaseAdapter {
		
	private Context context;
	private List<String>list;
	public DragAdapter(Context context, List<String>list){
		this.context=context;
		this.list=list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list!=null?list.size():0;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		arg1=LayoutInflater.from(context).inflate(R.layout.item_layout, null);
		TextView text=(TextView)arg1.findViewById(R.id.txt_userAge);
		text.setText("Void->"+list.get(arg0));
		return arg1;
	}

	public void exchangePosition(int dragPosition, int dropPosition) {
		// TODO Auto-generated method stub
		String dragString=list.get(dragPosition);
		String dropString=list.get(dropPosition);
		list.add(dropPosition, dragString);
		list.remove(dropPosition+1);
		list.add(dragPosition, dropString);
		list.remove(dragPosition+1);
		notifyDataSetChanged();
	}

}
