package CRUD.model;

public class Genre {
	
	int ID;
	String name;
	
	public Genre() {}
	
	public Genre(int ID, String name) {
		this.ID = ID;
		this.name = name;
	}
	
	public int getID() { return ID; }
	public String getName () { return name; }
	
	public void setID (int ID) {this.ID = ID; }
	public void setName (String name) { this.name = name; }
}
