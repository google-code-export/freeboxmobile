package org.madprod.freeboxmobile.services;



import android.os.Parcel;
import android.os.Parcelable;

public class MevoMessage implements Parcelable{

	private static final long serialVersionUID = 663585476779879096L;
	private Long id;
	private int status;
	private String presence;
	private String source;
	private String date;
	private String link;
	private String delete;
	private String name;
	private String length;
	private String fileName;
	
	public MevoMessage(Long _id, int _status, String _presence, String _source, String _date, String _link, String _delete, String _name, String _length, String _fileName){
		id = _id;
		status = _status;
		presence = _presence;
		source = _source;
		date = _date;
		link = _link;
		delete = _delete;
		name = _name;
		length = _length;
		fileName = _fileName;
	}
	
	public MevoMessage(Parcel in){
		readFromParcel(in);
	}
	
	public static final Parcelable.Creator<MevoMessage> CREATOR = new Parcelable.Creator<MevoMessage>() {
		public MevoMessage createFromParcel(Parcel in) {
			return new MevoMessage(in);
		}

		public MevoMessage[] newArray(int size) {
			return new MevoMessage[size];
		}

	};
	
	private void readFromParcel(Parcel in) {
		id = in.readLong();
		status = in.readInt();
		presence = in.readString();
		source = in.readString();
		date = in.readString();
		link = in.readString();
		delete = in.readString();
		name = in.readString();
		length = in.readString();
		fileName = in.readString();
		
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeInt(status);
		dest.writeString(presence);
		dest.writeString(source);
		dest.writeString(date);
		dest.writeString(link);
		dest.writeString(delete);
		dest.writeString(name);
		dest.writeString(length);
		dest.writeString(fileName);
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getDelete() {
		return delete;
	}
	
	public String getLength() {
		return length;
	}
	
	public String getLink() {
		return link;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPresence() {
		return presence;
	}
	
	public String getSource() {
		return source;
	}

	public int getStatus() {
		return status;
	}
	
	public String getFileName() {
		return fileName;
	}

}
