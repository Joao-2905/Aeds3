package CRUD.model;

public class Review {

    private int id;
    private int userID;
    private int filmID;
    private byte rating;
    private String note;

    public Review() {}

    public Review(int id, int userID, int filmID, byte rating, String note) {
        this.id = id;
        this.userID = userID;
        this.filmID = filmID;
        this.rating = rating;
        this.note = note;
    }

    public Review(int userID, int filmID, byte rating, String note) {
        this.userID = userID;
        this.filmID = filmID;
        this.rating = rating;
        this.note = note;
    }

    public int getID() {
        return id;
    }

    public int getUserID() {
        return userID;
    }

    public int getFilmID() {
        return filmID;
    }

    public byte getRating() {
        return rating;
    }

    public String getNote() {
        return note;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setFilmID(int filmID) {
        this.filmID = filmID;
    }

    public void setRating(byte rating) {
        this.rating = rating;
    }

    public void setNote(String note) {
        this.note = note;
    }
}