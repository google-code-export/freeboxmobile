package org.madprod.freeboxmobile.services;



import android.os.Parcel;
import android.os.Parcelable;

public class Channel implements Parcelable{

	private static final long serialVersionUID = 663585476779879096L;
	private String chainesImgs;
	private String chainesIds;
	private String chainesCanal;
	private String chainesName;
	
	public Channel(){
		
	}
	
	public Channel(Parcel in){
		readFromParcel(in);
	}
	
	public static final Parcelable.Creator<Channel> CREATOR = new Parcelable.Creator<Channel>() {
		public Channel createFromParcel(Parcel in) {
			return new Channel(in);
		}

		public Channel[] newArray(int size) {
			return new Channel[size];
		}

	};
	
	private void readFromParcel(Parcel in) {
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
	}
	

}
