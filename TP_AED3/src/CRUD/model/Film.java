package CRUD.model;

public class Film {

    private int ID;
    private String title;
    private String director;
    private String description;
    private int[] genreID;

    public Film() {
        this.genreID = new int[0];
    }

    public Film(int ID, String title, String director, String description, int[] genreID) {
        this.ID = ID;
        this.title = title;
        this.director = director;
        this.description = description;
        this.genreID = (genreID != null) ? genreID.clone() : new int[0];
    }

    public int getID() {
        return ID;
    }

    public int[] getGenreID() {
        return (this.genreID != null) ? this.genreID.clone() : new int[0];
    }

    public String getTitle() {
        return title;
    }

    public String getDirector() {
        return director;
    }

    public String getDescription() {
        return description;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setGenreID(int[] genreID) {
        this.genreID = (genreID != null) ? genreID.clone() : new int[0];
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}