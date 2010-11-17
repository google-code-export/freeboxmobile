package org.madprod.freeboxmobile.services;
import org.madprod.freeboxmobile.services.IRemoteControlServiceCallback;
import org.madprod.freeboxmobile.services.Channel;

interface IGuideTV{
    
    Channel[] getListOfChannels(int numberBox);
    
  	void registerCallback(IRemoteControlServiceCallback cb); 
  	void unregisterCallback(IRemoteControlServiceCallback cb); 	
}