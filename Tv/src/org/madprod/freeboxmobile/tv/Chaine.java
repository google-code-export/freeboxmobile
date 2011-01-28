package org.madprod.freeboxmobile.tv;

import java.util.HashMap;
import java.util.Map;

public class Chaine implements Comparable <Chaine>
{
	private int channelId;
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

	/* Ci dessous certains flux sont commentés afin qu'ils ne soient pas listés
	 * Il ne sert à rien de les lister (sauf à complexifier l'affichage)
	 * car ils ne sont pas pris en charge sur Android par aucun player
	 * (pour l'instant)
	 */
	public static final String[] STREAM_NAME={
		"Flux PC",					// STREAM_TYPE_TVFREEBOX
		"Flux Internet",			// STREAM_TYPE_INTERNET
//		"Flux Multiposte Bas débit",// STREAM_TYPE_MULTIPOSTE_LD
		"Flux Multiposte",			// STREAM_TYPE_MULTIPOSTE_SD
//		"Flux Multiposte HD",		// STREAM_TYPE_MULTIPOSTE_HD
//		"Flux Multiposte (Auto)",	// STREAM_TYPE_MULTIPOSTE_AUTO
//		"Flux Multiposte TNT",		// STREAM_TYPE_MULTIPOSTE_TNTSD
//		"Flux Multiposte TNT HD",	// STREAM_TYPE_MULTIPOSTE_TNTHD
//		"Flux Multiposte 3D",		// STREAM_TYPE_MULTIPOSTE_3D
		};
	public static final Integer[] STREAM_TYPE={
		STREAM_TYPE_TVFREEBOX,
		STREAM_TYPE_INTERNET,
//		STREAM_TYPE_MULTIPOSTE_LD,
		STREAM_TYPE_MULTIPOSTE_SD,
//		STREAM_TYPE_MULTIPOSTE_HD,
//		STREAM_TYPE_MULTIPOSTE_AUTO,
//		STREAM_TYPE_MULTIPOSTE_TNTSD,
//		STREAM_TYPE_MULTIPOSTE_TNTHD,
//		STREAM_TYPE_MULTIPOSTE_3D
	};
	
	private Map<Integer, Map<String, String>> streamsList = null;

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

    public int getChannelId()
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
    
    public Map<String, String> getStream(int type)
    {
    	return streamsList.get(type);
    }
    
    public Map<Integer, Map<String, String>> getStreams()
    {
    	return streamsList;
    }
}
