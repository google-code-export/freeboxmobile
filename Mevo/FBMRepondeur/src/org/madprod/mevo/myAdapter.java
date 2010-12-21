package org.madprod.mevo;

import java.util.List;

import org.madprod.freeboxmobile.services.MevoMessage;
import org.madprod.mevo.tools.Constants;
import org.madprod.mevo.tools.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class myAdapter extends BaseAdapter implements Constants{

	private List<MevoMessage> messages;
	private LayoutInflater myInflater;
	private Context context;
	
	public myAdapter(Context _context, List<MevoMessage> _messages){
		this.myInflater = LayoutInflater.from(_context);
		this.messages = _messages;
		this.context = _context;
	}
	
	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public Object getItem(int position) {
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null){
			convertView = myInflater.inflate(R.layout.mevo_messages_row, null);
			holder = new ViewHolder();
			holder.date = (TextView) convertView.findViewById(R.id.quand);
			holder.length = (TextView) convertView.findViewById(R.id.length);
			holder.source = (TextView) convertView.findViewById(R.id.source);
			holder.status = (ImageView) convertView.findViewById(R.id.boutonLecture);
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.date.setText(Utils.convertDateTimeHR(messages.get(position).getDate()));
		holder.length.setText(messages.get(position).getLength() + " "+ context.getResources().getString(R.string.seconds));
		holder.source.setText(Utils.getContactFromNumber(context, messages.get(position).getSource()));
		holder.status.setVisibility((messages.get(position).getStatus()==0)?View.VISIBLE:View.INVISIBLE);
		
		return convertView;
	}

	
	public static class ViewHolder{
		TextView date;
		TextView length;	
		TextView link;	
		TextView name;	
		TextView presence;	
		TextView source;	
		ImageView status;			
	}
	
	
	
	

	
	
}
