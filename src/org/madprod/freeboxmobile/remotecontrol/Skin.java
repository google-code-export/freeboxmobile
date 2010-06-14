package org.madprod.freeboxmobile.remotecontrol;

public class Skin {
	private String name;
	private String path;
	private String type;
	private boolean defaultHorizontal;
	private boolean defaultVertical;
	
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
	
	public void setDefault(String defaut) {
		if (defaut.compareToIgnoreCase("horizontal") == 0) defaultHorizontal = true;
		else if (defaut.compareToIgnoreCase("vertical") == 0) defaultVertical = true;
		else if (defaut.compareToIgnoreCase("both") == 0){
			defaultHorizontal = true;
			defaultVertical = true;			
		}
	}
	
	public boolean isDefaultHorizontal() {
		return defaultHorizontal;
	}
	public boolean isDefaultVertical() {
		return defaultVertical;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("Skin name = ").append(name).append(" , path = ").append(path).append(" , type = ").append(type).toString();
	}
}
