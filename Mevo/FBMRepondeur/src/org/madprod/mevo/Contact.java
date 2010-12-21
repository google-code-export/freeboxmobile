package org.madprod.mevo;

import android.graphics.Bitmap;

public class Contact {

	private Long id;
	private String name;
	private String label;
	private String phone;
	private int phoneType;
	private Bitmap image;
	private boolean found = false;
	
	public Contact() {
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setFound(boolean found) {
		this.found = found;
	}
	
	public boolean isFound() {
		return found;
	}
	
	public Bitmap getImage() {
		return image;
	}
	
	public void setImage(Bitmap image) {
		this.image = image;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhoneType(int phoneType) {
		this.phoneType = phoneType;
	}
	
	public int getPhoneType() {
		return phoneType;
	}
	
}
