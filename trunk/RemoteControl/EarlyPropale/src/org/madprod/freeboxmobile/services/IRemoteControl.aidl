package org.madprod.freeboxmobile.services;
import org.madprod.freeboxmobile.services.IRemoteControlServiceCallback;

interface IRemoteControl {
    
    void sendCommand(in String cmd, in boolean longClick, in int repeat);
  	void registerCallback(IRemoteControlServiceCallback cb); 
  	void unregisterCallback(IRemoteControlServiceCallback cb); 	
}