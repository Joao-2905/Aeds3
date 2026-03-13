package CRUD.model;

public class Film {

	private int id;
	private String title;
	private String description;
	private int releaseDate;
	private String[] directors;
	private float rating;
	private int totalReviews;

	public Film() {}

	public Film(int id, String title, String description, int releaseDate,
	            String[] directors, float rating, int totalReviews) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.releaseDate = releaseDate;
		this.directors = directors;
		this.rating = rating;
		this.totalReviews = totalReviews;
	}

	// construtor para criação (sem id)
	public Film(String title, String description, int releaseDate,
	            String[] directors) {
		this.title = title;
		this.description = description;
		this.releaseDate = releaseDate;
		this.directors = directors;
		this.rating = 0;
		this.totalReviews = 0;
	}

	public int getID() { return id; }
	public String getTitle() { return title; }
	public String getDescription() { return description; }
	public int getReleaseDate() { return releaseDate; }
	public String[] getDirectors() { return directors; }
	public float getRating() { return rating; }
	public int getTotalReviews() { return totalReviews; }

	public void setID(int id) { this.id = id; }
	public void setTitle(String title) { this.title = title; }
	public void setDescription(String description) { this.description = description; }
	public void setReleaseDate(int releaseDate) { this.releaseDate = releaseDate; }
	public void setDirectors(String[] directors) { this.directors = directors; }
	public void setRating(float rating) { this.rating = rating; }
	public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }

}