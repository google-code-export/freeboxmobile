package org.madprod.freeboxmobile.tv;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class Chaine implements Comparable <Chaine>, TvConstants
{
	private Integer channelId;
	private String logoUrl;
	private String name;
	private String currentProgName = null;
	private String currentProgDesc = null;
	
	public static final String M_URL = "url";
	public static final String M_MIME = "mime";
	
	public static final int STREAM_TYPE_TVFREEBOX = 1;
	public static final int STREAM_TYPE_INTERNET = 2;
	public static final int STREAM_TYPE_MULTIPOSTE_LD = 3;
	public static final int STREAM_TYPE_MULTIPOSTE_SD = 4;
	public static final int STREAM_TYPE_MULTIPOSTE_HD = 5;
	public static final int STREAM_TYPE_MULTIPOSTE_AUTO = 6;
	public static final int STREAM_TYPE_MULTIPOSTE_TNTSD = 7;
	public static final int STREAM_TYPE_MULTIPOSTE_TNTHD = 8;
	public static final int STREAM_TYPE_MULTIPOSTE_3D = 9;

	public static String[] STREAM_NAME= null;
	public static Integer[] STREAM_TYPE= null;
	
	private Map<Integer, Map<String, String>> streamsList = null;

	static
	{
		/* Ci dessous certains flux sont commentés afin qu'ils ne soient pas listés
		 * Il ne sert à rien de les lister (sauf à complexifier l'affichage)
		 * car ils ne sont pas pris en charge sur Android par aucun player
		 * (pour l'instant)
		 */
		if (!MainActivity.modeFull)
		{
			final String[] SNAME={
//			"Flux PC",					// STREAM_TYPE_TVFREEBOX
			"Flux Internet",			// STREAM_TYPE_INTERNET
//			"Flux Multiposte Bas débit",// STREAM_TYPE_MULTIPOSTE_LD
			"Flux Multiposte",			// STREAM_TYPE_MULTIPOSTE_SD
//			"Flux Multiposte HD",		// STREAM_TYPE_MULTIPOSTE_HD
//			"Flux Multiposte (Auto)",	// STREAM_TYPE_MULTIPOSTE_AUTO
			"Flux Multiposte TNT",		// STREAM_TYPE_MULTIPOSTE_TNTSD
//			"Flux Multiposte TNT HD",	// STREAM_TYPE_MULTIPOSTE_TNTHD
//			"Flux Multiposte 3D",		// STREAM_TYPE_MULTIPOSTE_3D
			};
			
			final Integer[] STYPE={
//				STREAM_TYPE_TVFREEBOX,
				STREAM_TYPE_INTERNET,
//				STREAM_TYPE_MULTIPOSTE_LD,
				STREAM_TYPE_MULTIPOSTE_SD,
//				STREAM_TYPE_MULTIPOSTE_HD,
//				STREAM_TYPE_MULTIPOSTE_AUTO,
				STREAM_TYPE_MULTIPOSTE_TNTSD,
//				STREAM_TYPE_MULTIPOSTE_TNTHD,
//				STREAM_TYPE_MULTIPOSTE_3D
			};

			STREAM_NAME = SNAME;
			STREAM_TYPE = STYPE;
		}
		else
		{
			final String[] SNAME={
					"Flux PC",					// STREAM_TYPE_TVFREEBOX
					"Flux Internet",			// STREAM_TYPE_INTERNET
					"Flux Multiposte Bas débit",// STREAM_TYPE_MULTIPOSTE_LD
					"Flux Multiposte",			// STREAM_TYPE_MULTIPOSTE_SD
					"Flux Multiposte HD",		// STREAM_TYPE_MULTIPOSTE_HD
					"Flux Multiposte (Auto)",	// STREAM_TYPE_MULTIPOSTE_AUTO
					"Flux Multiposte TNT",		// STREAM_TYPE_MULTIPOSTE_TNTSD
					"Flux Multiposte TNT HD",	// STREAM_TYPE_MULTIPOSTE_TNTHD
					"Flux Multiposte 3D",		// STREAM_TYPE_MULTIPOSTE_3D
					};			
			final Integer[] STYPE={
					STREAM_TYPE_TVFREEBOX,
					STREAM_TYPE_INTERNET,
					STREAM_TYPE_MULTIPOSTE_LD,
					STREAM_TYPE_MULTIPOSTE_SD,
					STREAM_TYPE_MULTIPOSTE_HD,
					STREAM_TYPE_MULTIPOSTE_AUTO,
					STREAM_TYPE_MULTIPOSTE_TNTSD,
					STREAM_TYPE_MULTIPOSTE_TNTHD,
					STREAM_TYPE_MULTIPOSTE_3D
				};

				STREAM_NAME = SNAME;
				STREAM_TYPE = STYPE;
		}
	}
	
	Chaine(int channelId, String logoUrl, String name)
	{
		this.channelId = channelId;
		this.logoUrl = logoUrl;
		this.name = name;
		streamsList = new HashMap<Integer, Map<String, String>>();
	}
	
	@Override
	public int compareTo(Chaine another)
	{
		return (channelId - another.getChannelId());
	}

    public void addStream(int type, String streamUrl, String mimeType)
    {
    	Map<String, String> map = new HashMap<String, String>();
		map.put(M_URL, streamUrl);
		map.put(M_MIME, mimeType);
		streamsList.put(type, map);
    }

    public Integer getChannelId()
    {
    	return channelId;
    }
    
    public String getLogoUrl()
    {
    	return logoUrl;
    }
    
    public String getName()
    {
    	return name;
    }

    public void setCurrentProg(String name, String desc)
    {
    	currentProgName = name;
    	currentProgDesc = desc; 
    }

    public String getCurrentProgName()
    {
    	return currentProgName;
    }
    
    public String getCurrentProgDesc()
    {
    	return currentProgDesc;
    }
    
    public static String getStreamName(Integer type)
    {
    	Integer i = 0;
    	while (i < STREAM_NAME.length)
    	{
    		if (STREAM_TYPE[i] == type)
    			return (STREAM_NAME[i]);
    		i++;
    	}
    	return null;
    }
    
    public Map<String, String> getStream(int type)
    {
    	return streamsList.get(type);
    }
    
    public Map<String, String> getFavoriteStream(Map<Integer, Integer> streamsPrefs)
    {
    	boolean cont = true;
    	int streamType = -1;
    	int i = 0;
    	
    	while (cont)
    	{
    		if (streamsPrefs.containsKey(i))
    		{
	    		streamType = streamsPrefs.get(i);
	    		if (streamsList.containsKey(streamType))
	    		{
	    			return (streamsList.get(streamType));
	    		}
    		}
    		else
    		{
    			cont = false;
    		}
    		i++;
    	}
    	return null;
    }
}
