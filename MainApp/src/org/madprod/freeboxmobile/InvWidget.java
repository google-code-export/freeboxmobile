package org.madprod.freeboxmobile;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
		Log.d(TAG, "ON ENABLED !");
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
		Log.d("FBM WIDGET", "ONPUDATE !");
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
			Log.d("FBM WIDGET", "BUILD UPDATE !");
			// Pick out month names from resources
			Resources res = context.getResources();
			Random r=new java.util.Random( );
			String[] texts = res.getStringArray(R.array.inv_widget_texts);
			int ind = r.nextInt(texts.length);
			if ((ind % 2) == 1)
			{
				ind--;
			}
//			ind = 33*2;
			Log.d("FBM WIDGET", "SIZE : "+texts.length+" - "+ind);
			
			// Find current month and day
//			Time today = new Time();
//			today.setToNow();
			
			// Build today's page title, like "Wiktionary:Word of the day/March 21"
//			String pageName = res.getString(R.string.template_wotd_title, monthNames[today.month], today.monthDay);
			RemoteViews updateViews = null;
//			String pageContent = "";
            
//			try
//			{
				// Try querying the Wiktionary API for today's word
//				SimpleWikiHelper.prepareUserAgent(context);
//				pageContent = SimpleWikiHelper.getPageContent(pageName, false);
//			}
//			catch (ApiException e)
//			{
//				Log.e("WordWidget", "Couldn't contact API", e);
//			}
//			catch (ParseException e)
//			{
//				Log.e("WordWidget", "Couldn't parse API response", e);
//			}
            
			// Use a regular expression to parse out the word and its definition
//			Pattern pattern = Pattern.compile(SimpleWikiHelper.WORD_OF_DAY_REGEX);
//			Matcher matcher = pattern.matcher(pageContent);
//			if (matcher.find())
			if (true)
			{
				// Build an update that holds the updated widget contents
				updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_inv_word);
				
				String wordTitle = "Titre";
				updateViews.setTextViewText(R.id.word_title, wordTitle);
				updateViews.setTextViewText(R.id.word_type, texts[ind]);
				updateViews.setTextViewText(R.id.definition, texts[ind+1]+"\n");

				// When user clicks on widget, launch to Wiktionary definition page
//				String definePage = res.getString(R.string.template_define_url, Uri.encode(wordTitle));
//				Intent defineIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(definePage));
//				PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* no requestCode */, defineIntent, 0 /* no flags */);
//				updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
            }
			else
			{
				// Didn't find word of day, so show error message
				updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_inv_message);
				CharSequence errorMessage = context.getText(R.string.widget_error);
				updateViews.setTextViewText(R.id.message, errorMessage);
			}
			return updateViews;
		}
		
		@Override
		public IBinder onBind(Intent intent)
		{
			// We don't need to bind to this service
			return null;
		}
	}
}
