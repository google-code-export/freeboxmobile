package org.madprod.freeboxmobile.services;
import org.madprod.freeboxmobile.services.IRemoteControlServiceCallback;
import org.madprod.freeboxmobile.services.MevoMessage;

interface IMevo{
    
	int checkMessages();
    MevoMessage[] getListOfMessages();
    int deleteMessage(in MevoMessage message);
    int setMessageRead(in MevoMessage message);
    int setMessageUnRead(in MevoMessage message);
    
  	void registerCallback(IRemoteControlServiceCallback cb); 
  	void unregisterCallback(IRemoteControlServiceCallback cb); 	
}