package org.madprod.freeboxmobile.tv;

import java.util.HashMap;
import java.util.Map;

public class Chaine implements Comparable <Chaine>
{
	private int channelId;
	private String logoUrl;
	private String name;
	
	public static final String M_URL = "url";
	public static final String M_MIME = "mime";
	
	public static final int STREAM_TYPE_TVFREEBOX = 1;
	public static final int STREAM_TYPE_INTERNET = 2;
	public static final int STREAM_TYPE_MULTIPOSTE_LD = 3;
	public static final int STREAM_TYPE_MULTIPOSTE_SD = 4;
	public static final int STREAM_TYPE_MULTIPOSTE_HD = 5;
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
    
    public Map<String, String> getStream(int type)
    {
    	return streamsList.get(type);
    }
    
    public Map<Integer, Map<String, String>> getStreams()
    {
    	return streamsList;
    }
}
