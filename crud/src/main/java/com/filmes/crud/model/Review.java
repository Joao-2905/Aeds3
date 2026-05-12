package com.filmes.crud.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Review implements Comparable<Review> {

    private int id;
    private int userID;
    private int filmID;
    private byte rating;
    private String note;

    // 🔥 NOVO CAMPO
    private String username;

    public Review() {
        this.note = "";
        this.username = "";
    }

    public Review(int id, int userID, int filmID, byte rating, String note) {
        this.id = id;
        this.userID = userID;
        this.filmID = filmID;
        this.rating = rating;
        this.note = (note == null) ? "" : note;
        this.username = "";
    }

    public Review(int userID, int filmID, byte rating, String note) {
        this.userID = userID;
        this.filmID = filmID;
        this.rating = rating;
        this.note = (note == null) ? "" : note;
        this.username = "";
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

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getFilmID() {
        return filmID;
    }

    public void setFilmID(int filmID) {
        this.filmID = filmID;
    }

    public byte getRating() {
        return rating;
    }

    public void setRating(byte rating) {
        this.rating = rating;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = (note == null) ? "" : note;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = (username == null) ? "" : username;
    }

    // =========================================================
    // SERIALIZAÇÃO
    // =========================================================

    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(id);
        dos.writeInt(userID);
        dos.writeInt(filmID);
        dos.writeByte(rating);
        dos.writeUTF(note);
        dos.writeUTF(username);
    }

    public void read(DataInputStream dis) throws IOException {
        id = dis.readInt();
        userID = dis.readInt();
        filmID = dis.readInt();
        rating = dis.readByte();
        note = dis.readUTF();
        username = dis.readUTF();
    }

    // =========================================================
    // ORDENAÇÃO EXTERNA
    // =========================================================

    @Override
    public int compareTo(Review other) {

        // Ordena por nota (maior primeiro)
        int ratingCompare = Byte.compare(other.rating, this.rating);

        if (ratingCompare != 0) {
            return ratingCompare;
        }

        // Desempate por ID
        return Integer.compare(this.id, other.id);
    }

    // =========================================================
    // EXIBIÇÃO
    // =========================================================

    @Override
    public String toString() {
        return "Review {" +
                "id=" + id +
                ", userID=" + userID +
                ", filmID=" + filmID +
                ", rating=" + rating +
                ", note='" + note + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}