package org.madprod.freeboxmobile;

import static org.madprod.freeboxmobile.StaticConstants.TAG;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Random;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class InvWidget extends AppWidgetProvider implements Constants
{
	GoogleAnalyticsTracker tracker = null;

	@Override
	public void onEnabled(Context context)
	{
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.start(ANALYTICS_MAIN_TRACKER, 20, context);
		tracker.trackPageView("Widget/Innovations");
	}
	
	@Override
	public void onDisabled(Context context)
	{
		if (tracker != null)
			tracker.stop();
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		context.startService(new Intent(context, UpdateService.class));
	}

	public static class UpdateService extends Service
	{
		@Override
		public void onStart(Intent intent, int startId)
		{
			// Build the widget update for today
			RemoteViews updateViews = buildUpdate(this);

			// Push update for this widget to the home screen
			ComponentName thisWidget = new ComponentName(this, InvWidget.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			manager.updateAppWidget(thisWidget, updateViews);
		}

		public RemoteViews buildUpdate(Context context)
		{
			GoogleAnalyticsTracker tracker;
			tracker = GoogleAnalyticsTracker.getInstance();
			tracker.start(ANALYTICS_MAIN_TRACKER, 20, context);
			tracker.trackPageView("Widget/InnovationsMAJ");

			Resources res = context.getResources();
			Random r = new java.util.Random( );
			String[] texts = res.getStringArray(R.array.inv_widget_texts);
			int ind = r.nextInt(texts.length) / 4;
			ind *= 4;
			Log.d(TAG, "SIZE : "+texts.length+" - "+ind);

			RemoteViews updateViews = null;

			updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_inv_word);
			
			updateViews.setTextViewText(R.id.word_title, texts[ind+1]);
			updateViews.setTextViewText(R.id.word_type, "["+texts[ind]+"]");
			updateViews.setTextViewText(R.id.definition, texts[ind+2]+"\n");

			String definePage;
			if (texts[ind + 3].contains("pdf"))
			{
				definePage = "http://docs.google.com/viewer?url="+texts[ind+3];
			}
			else
			{
				definePage = texts[ind+3];
			}
			Intent defineIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(definePage));
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* no requestCode */, defineIntent, 0 /* no flags */);
			updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
			return updateViews;
		}
		
		@Override
		public IBinder onBind(Intent intent)
		{
			return null;
		}
	}
}
