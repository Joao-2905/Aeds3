package CRUD.model;

public class Review {
	
	int ID;
	int userID;
	int filmID;
	short rating;
	String note;
	
	public Review(){}
	
	public Review(int ID, int userID, int filmID, short rating, String note){
		this.ID = ID;
		this.userID = userID;
		this.filmID = filmID;
		this.rating = rating;
		this.note = note;
	}
	
	public int getID() { return ID; }
	public int getUserID () { return userID; }
	public int getFilmID() { return filmID; }
	public short getRating() { return rating; }
	public String getNote() { return note; }
	
	public void setID(int ID) { this.ID = ID; }
	public void setUserID(int userID) { this.userID = userID; }
	public void setFilmID (int movieID) { this.filmID = movieID; }
	public void setRating(short rating) { this.rating = rating; }
	public void setNote(String note) { this.note = note; }
}
