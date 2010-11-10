package org.madprod.freeboxmobile.remotecontrol;


import org.madprod.freeboxmobile.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class InfoSkinHandler extends DefaultHandler implements Constants{

	private InfoSkin infoSkin = null;
	private StringBuffer buffer;

	public InfoSkinHandler() {
		super();
	}
	
	
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		
		buffer = new StringBuffer();
		if(localName.equals("skin")){
			Log.d(TAG, "InfoSkinHandler : begin balise skin");
			infoSkin = new InfoSkin();
		}else if(localName.equals("class")){
			Log.d(TAG, "InfoSkinHandler : begin balise class");
		}else if(localName.equals("description")){
			Log.d(TAG, "InfoSkinHandler : begin balise description");
		}else{
			Log.d(TAG, "InfoSkinHandler : begin balise autre : "+localName);
		}
	}
	
	public InfoSkin getInfoSkin() {
		return infoSkin;
	}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {

		if(localName.equals("class")){
			Log.d(TAG, "InfoSkinHandler : end balise class");
			infoSkin.setName(buffer.toString().trim());
		}else if(localName.equals("description")){
			Log.d(TAG, "InfoSkinHandler : end balise description");
			infoSkin.setDescription(buffer.toString().trim());
		}else{
			Log.d(TAG, "SkinHandler : end balise autre : "+localName);
		}
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String lecture = new String(ch,start,length);
		Log.d(TAG, "InfoSkinHandler : element lu : "+lecture);
		if(buffer != null) buffer.append(lecture);  
	}
	
}
