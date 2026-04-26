package CRUD.controller;

import CRUD.dao.GenreDAO;
import CRUD.model.Genre;

import java.io.IOException;

public class GenreController {

    private GenreDAO dao;

    public GenreController() throws IOException {
        dao = new GenreDAO();
    }

    // =============================
    // CREATE
    // =============================
    public int createGenre(String nome) throws IOException {

        Genre g = new Genre(0, nome);
        return dao.create(g);
    }

    // =============================
    // READ
    // =============================
    public Genre readGenre(int id) throws IOException {
        return dao.read(id);
    }

    // =============================
    // UPDATE
    // =============================
    public boolean updateGenre(Genre g) throws IOException {
        return dao.update(g);
    }

    // =============================
    // DELETE
    // =============================
    public boolean deleteGenre(int id) throws IOException {
        return dao.delete(id);
    }

    // =============================
    // LIST
    // =============================
    public void listGenres() throws IOException {
        dao.listAll();
    }

    // =============================
    // HASH DEBUG
    // =============================
    public void exibirIndice() throws IOException {
        dao.exibirIndice(); // 🔥 importante: precisa existir no DAO
    }
}