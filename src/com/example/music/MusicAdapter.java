package com.example.music;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MusicAdapter extends BaseAdapter {

	private List<MusicInfo> infos;
	Context context;
	
	public MusicAdapter(Context context,List<MusicInfo> infos) {
		this.context = context;
		this.infos = infos;
	}
	
	@Override
	public int getCount() {
		return infos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HoldView holdView = null;
		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item, null);
			holdView = new HoldView();
			holdView.value = (TextView) convertView.findViewById(R.id.value);
			convertView.setTag(holdView);
		}else {
			holdView = (HoldView) convertView.getTag();
		}
		holdView.value.setText(infos.get(position).name);
		return convertView;
	}

	private class HoldView {
		TextView value;
	}
}
