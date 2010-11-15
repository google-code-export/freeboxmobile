package org.madprod.freeboxmobile.services;

interface IRemoteControlServiceCallback {
	void dataChanged(in int status, in String message);     
}