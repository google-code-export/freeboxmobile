/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/olivier/workspace_fbm/FBMobile/freeboxmobile/Mevo/FBMRepondeur/src/org/madprod/freeboxmobile/services/IMevo.aidl
 */
package org.madprod.freeboxmobile.services;
public interface IMevo extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.madprod.freeboxmobile.services.IMevo
{
private static final java.lang.String DESCRIPTOR = "org.madprod.freeboxmobile.services.IMevo";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.madprod.freeboxmobile.services.IMevo interface,
 * generating a proxy if needed.
 */
public static org.madprod.freeboxmobile.services.IMevo asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.madprod.freeboxmobile.services.IMevo))) {
return ((org.madprod.freeboxmobile.services.IMevo)iin);
}
return new org.madprod.freeboxmobile.services.IMevo.Stub.Proxy(obj);
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
case TRANSACTION_checkMessages:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.checkMessages();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getListOfMessages:
{
data.enforceInterface(DESCRIPTOR);
org.madprod.freeboxmobile.services.MevoMessage[] _result = this.getListOfMessages();
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_deleteMessage:
{
data.enforceInterface(DESCRIPTOR);
org.madprod.freeboxmobile.services.MevoMessage _arg0;
if ((0!=data.readInt())) {
_arg0 = org.madprod.freeboxmobile.services.MevoMessage.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
int _result = this.deleteMessage(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setMessageRead:
{
data.enforceInterface(DESCRIPTOR);
org.madprod.freeboxmobile.services.MevoMessage _arg0;
if ((0!=data.readInt())) {
_arg0 = org.madprod.freeboxmobile.services.MevoMessage.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
int _result = this.setMessageRead(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setMessageUnRead:
{
data.enforceInterface(DESCRIPTOR);
org.madprod.freeboxmobile.services.MevoMessage _arg0;
if ((0!=data.readInt())) {
_arg0 = org.madprod.freeboxmobile.services.MevoMessage.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
int _result = this.setMessageUnRead(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
org.madprod.freeboxmobile.services.IRemoteControlServiceCallback _arg0;
_arg0 = org.madprod.freeboxmobile.services.IRemoteControlServiceCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
org.madprod.freeboxmobile.services.IRemoteControlServiceCallback _arg0;
_arg0 = org.madprod.freeboxmobile.services.IRemoteControlServiceCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.madprod.freeboxmobile.services.IMevo
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
public int checkMessages() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_checkMessages, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public org.madprod.freeboxmobile.services.MevoMessage[] getListOfMessages() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.madprod.freeboxmobile.services.MevoMessage[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getListOfMessages, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(org.madprod.freeboxmobile.services.MevoMessage.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int deleteMessage(org.madprod.freeboxmobile.services.MevoMessage message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((message!=null)) {
_data.writeInt(1);
message.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_deleteMessage, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int setMessageRead(org.madprod.freeboxmobile.services.MevoMessage message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((message!=null)) {
_data.writeInt(1);
message.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setMessageRead, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int setMessageUnRead(org.madprod.freeboxmobile.services.MevoMessage message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((message!=null)) {
_data.writeInt(1);
message.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setMessageUnRead, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void registerCallback(org.madprod.freeboxmobile.services.IRemoteControlServiceCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void unregisterCallback(org.madprod.freeboxmobile.services.IRemoteControlServiceCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_checkMessages = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getListOfMessages = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_deleteMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_setMessageRead = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_setMessageUnRead = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
public int checkMessages() throws android.os.RemoteException;
public org.madprod.freeboxmobile.services.MevoMessage[] getListOfMessages() throws android.os.RemoteException;
public int deleteMessage(org.madprod.freeboxmobile.services.MevoMessage message) throws android.os.RemoteException;
public int setMessageRead(org.madprod.freeboxmobile.services.MevoMessage message) throws android.os.RemoteException;
public int setMessageUnRead(org.madprod.freeboxmobile.services.MevoMessage message) throws android.os.RemoteException;
public void registerCallback(org.madprod.freeboxmobile.services.IRemoteControlServiceCallback cb) throws android.os.RemoteException;
public void unregisterCallback(org.madprod.freeboxmobile.services.IRemoteControlServiceCallback cb) throws android.os.RemoteException;
}
