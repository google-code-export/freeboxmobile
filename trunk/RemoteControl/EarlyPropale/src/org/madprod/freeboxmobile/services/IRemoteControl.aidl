package org.madprod.freeboxmobile.services;
import org.madprod.freeboxmobile.services.IRemoteControlServiceCallback;

interface IRemoteControl {
    
    void sendCommand(in String cmd, in boolean longClick, in int repeat, in int box);
    void sendChannel(in int channel, in int box);
    void sendParentalCode(in int code, in int box);
    boolean isBoxActivated(in int box);
    
  	void registerCallback(IRemoteControlServiceCallback cb); 
  	void unregisterCallback(IRemoteControlServiceCallback cb); 	
}