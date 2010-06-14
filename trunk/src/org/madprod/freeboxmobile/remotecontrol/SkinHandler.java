package org.madprod.freeboxmobile.remotecontrol;

import java.util.LinkedList;
import java.util.List;

import org.madprod.freeboxmobile.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class SkinHandler extends DefaultHandler implements Constants{

	private List<Skin> skins = null;
	private Skin skin = null;
	private StringBuffer buffer;

	public SkinHandler() {
		super();
	}
	
	
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		
		if(localName.equals("skins")){
			Log.d(TAG, "SkinHandler : begin balise skins");
			skins = new LinkedList<Skin>();
		}else if(localName.equals("skin")){
			Log.d(TAG, "SkinHandler : begin balise skin");
			skin = new Skin();
		}else{
			buffer = new StringBuffer();
			Log.d(TAG, "SkinHandler : begin balise autre : "+localName);

		}
	}
	
	public List<Skin> getSkins() {
		return skins;
	}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {

		if(localName.equals("name")){
			Log.d(TAG, "SkinHandler : end balise name");
			skin.setName(buffer.toString());
		}else if(localName.equals("url")){
			Log.d(TAG, "SkinHandler : end balise url");
			skin.setPath(buffer.toString());
		}else if(localName.equals("type")){
			Log.d(TAG, "SkinHandler : end balise type");
			skin.setType(buffer.toString());
		}else if(localName.equals("skin")){
			Log.d(TAG, "SkinHandler : end balise skin");
			skins.add(skin);
		}else if(localName.equals("default")){
			Log.d(TAG, "SkinHandler : end balise default");
			skin.setDefault(buffer.toString());
		}else{
			Log.d(TAG, "SkinHandler : end balise autre : "+localName);
		}
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String lecture = new String(ch,start,length);
		Log.d(TAG, "SkinHandler : element lu : "+lecture);
		if(buffer != null) buffer.append(lecture);  
	}
	
}
