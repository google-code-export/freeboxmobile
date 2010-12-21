package org.madprod.mevo.icons;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.widget.ImageView;


public class IconView extends ImageView{

	public IconView(Context context) {
		super(context);
	}

	public IconView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IconView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	Path path = new Path();

	@Override
	protected void onDraw(Canvas canvas) {
		if (getDrawable() != null){
			RectF initRect = new RectF(getDrawable().copyBounds());
			RectF finalRect = new RectF();
			getImageMatrix().mapRect(finalRect, initRect);
		
			float width = finalRect.right-finalRect.left;
			float height = finalRect.bottom-finalRect.top;
		
			path.addRoundRect(finalRect, width*10/100, height*10/100, Direction.CCW);
			
		}
		canvas.clipPath(path);
		
		super.onDraw(canvas);
		canvas.save();

	}

}
