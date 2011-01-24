package org.madprod.mevo;

import org.madprod.mevo.tools.Constants;
import org.madprod.mevo.tools.Utils;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class MevoMessageAdapter extends ResourceCursorAdapter implements Constants{


	public MevoMessageAdapter(Context _context, int _layout, Cursor _cursor) {
		super(_context, _layout, _cursor);
	}

	@Override
	public void bindView(View _convertView, Context _context, Cursor _cursor) {
			((TextView) _convertView.findViewById(R.id.quand)).setText(Utils.convertDateTimeHR(_cursor.getString(_cursor.getColumnIndex(KEY_QUAND))));
			((TextView) _convertView.findViewById(R.id.length)).setText(_cursor.getString(_cursor.getColumnIndex(KEY_LENGTH)) + " "+ _context.getResources().getString(R.string.seconds));
			((TextView) _convertView.findViewById(R.id.source)).setText(Utils.getContactFromNumber(_context, _cursor.getString(_cursor.getColumnIndex(KEY_SOURCE))));
			((ImageView) _convertView.findViewById(R.id.boutonLecture)).setVisibility((_cursor.getInt(_cursor.getColumnIndex(KEY_STATUS))==0)?View.VISIBLE:View.INVISIBLE);
	}


}