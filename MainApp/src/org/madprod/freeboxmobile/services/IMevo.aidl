package org.madprod.freeboxmobile.services;

interface IMevo{
    
	void checkMessages();
    void deleteMessage(in long messageId);
    void setMessageRead(in long messageId);
    void setMessageUnRead(in long messageId);
    String getMessageFile(in long messageId);
   
}