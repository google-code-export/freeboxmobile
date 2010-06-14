package org.madprod.freeboxmobile.remotecontrol;

public class InfoSkin {
	private String className;
	private String description;
	
	public String getClassName() {
		return className;
	}
	public String getDescription() {
		return description;
	}
	public void setName(String _className) {
		this.className = _className.trim();
	}
	public void setDescription(String _description) {
		this.description = _description.trim();
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("className = ").append(className).append(" , description = ").append(description).toString();
	}
}
