package org.madprod.freeboxmobile.remotecontrol;

public class Skin {
	private String name;
	private String path;
	private String type;
	
	public String getName() {
		return name;
	}
	public String getPath() {
		return path;
	}
	public String getType() {
		return type;
	}
	public void setName(String name) {
		this.name = name.trim();
	}
	public void setPath(String path) {
		this.path = path.trim();
	}
	public void setType(String type) {
		this.type = type.trim();
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("Skin name = ").append(name).append(" , path = ").append(path).append(" , type = ").append(type).toString();
	}
}
