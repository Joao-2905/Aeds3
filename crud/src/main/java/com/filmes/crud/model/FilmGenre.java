package com.filmes.crud.model;

public class FilmGenre {

    private int id;
    private int filmID;
    private int genreID;

    public FilmGenre() {
    }

    public FilmGenre(int id, int filmID, int genreID) {
        this.id = id;
        this.filmID = filmID;
        this.genreID = genreID;
    }

    public FilmGenre(int filmID, int genreID) {
        this.filmID = filmID;
        this.genreID = genreID;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFilmID() {
        return filmID;
    }

    public void setFilmID(int filmID) {
        this.filmID = filmID;
    }

    public int getFilmId() {
        return filmID;
    }

    public void setFilmId(int filmId) {
        this.filmID = filmId;
    }

    public int getFilmeID() {
        return filmID;
    }

    public void setFilmeID(int filmeID) {
        this.filmID = filmeID;
    }

    public int getFilmeId() {
        return filmID;
    }

    public void setFilmeId(int filmeId) {
        this.filmID = filmeId;
    }

    public int getGenreID() {
        return genreID;
    }

    public void setGenreID(int genreID) {
        this.genreID = genreID;
    }

    public int getGenreId() {
        return genreID;
    }

    public void setGenreId(int genreId) {
        this.genreID = genreId;
    }

    public int getGeneroID() {
        return genreID;
    }

    public void setGeneroID(int generoID) {
        this.genreID = generoID;
    }

    public int getGeneroId() {
        return genreID;
    }

    public void setGeneroId(int generoId) {
        this.genreID = generoId;
    }

    @Override
    public String toString() {
        return "FilmGenre {" +
                "id=" + id +
                ", filmID=" + filmID +
                ", genreID=" + genreID +
                '}';
    }
}