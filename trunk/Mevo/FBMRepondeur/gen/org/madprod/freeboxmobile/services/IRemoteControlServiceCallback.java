/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/olivier/workspace_fbm/FBMobile/freeboxmobile/Mevo/FBMRepondeur/src/org/madprod/freeboxmobile/services/IRemoteControlServiceCallback.aidl
 */
package org.madprod.freeboxmobile.services;
public interface IRemoteControlServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.madprod.freeboxmobile.services.IRemoteControlServiceCallback
{
private static final java.lang.String DESCRIPTOR = "org.madprod.freeboxmobile.services.IRemoteControlServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.madprod.freeboxmobile.services.IRemoteControlServiceCallback interface,
 * generating a proxy if needed.
 */
public static org.madprod.freeboxmobile.services.IRemoteControlServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.madprod.freeboxmobile.services.IRemoteControlServiceCallback))) {
return ((org.madprod.freeboxmobile.services.IRemoteControlServiceCallback)iin);
}
return new org.madprod.freeboxmobile.services.IRemoteControlServiceCallback.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_dataChanged:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
this.dataChanged(_arg0, _arg1);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.madprod.freeboxmobile.services.IRemoteControlServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void dataChanged(int status, java.lang.String message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(status);
_data.writeString(message);
mRemote.transact(Stub.TRANSACTION_dataChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_dataChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void dataChanged(int status, java.lang.String message) throws android.os.RemoteException;
}
