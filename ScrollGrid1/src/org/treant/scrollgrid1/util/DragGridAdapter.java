package org.treant.scrollgrid1.util;

import java.util.List;

import org.treant.scrollgrid1.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DragGridAdapter extends BaseAdapter {

	private Context context;
	private List<String> list;

	public DragGridAdapter(Context context, List<String> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list != null ? list.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		// TODO Auto-generated method stub
//		ViewHolder vh = null;
//		if (convertView == null) {
//			convertView = LayoutInflater.from(context).inflate(
//					R.layout.item_layout, null);
//			vh = new ViewHolder();
//			vh.text = (TextView) convertView.findViewById(R.id.txt_userAge);
//			convertView.setTag(vh);
//		} else {
//			vh = (ViewHolder) convertView.getTag();
//		}
//		vh.text.setText("ÐòºÅ=" + list.get(position));
//		return convertView;
//	}
//
//	class ViewHolder {
//		TextView text;
//	}
	@Override
	public View getView(int position ,View convertView, ViewGroup parent){
		convertView=LayoutInflater.from(context).inflate(R.layout.item_layout, null);
		TextView tv=(TextView)convertView.findViewById(R.id.txt_userAge);
		tv.setText("ABS:"+list.get(position));
		return convertView;
	}

	public void exchangePosition(int dragPosition, int dropPosition) {
		// TODO Auto-generated method stub
		String dragString = list.get(dragPosition);
		String dropString = list.get(dropPosition);
		list.add(dragPosition, dropString);
		list.remove(dragPosition + 1);
		list.add(dropPosition, dragString);
		list.remove(dropPosition + 1);
		notifyDataSetChanged();//Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself.
	}
}
