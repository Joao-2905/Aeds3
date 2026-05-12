package com.filmes.crud.service;

import java.util.ArrayList;
import java.util.List;

import com.filmes.crud.dao.FilmGenreDAO;
import com.filmes.crud.dao.GenreDAO;
import com.filmes.crud.model.Genre;

public class GenreService {

    private final GenreDAO genreDao;

    public GenreService() throws Exception {
        genreDao = new GenreDAO();
    }

    public int createGenre(String name) throws Exception {
        name = normalizarNome(name);

        validarNome(name);

        if (genreDao.readByName(name) != null) {
            throw new Exception("Gênero já cadastrado.");
        }

        Genre genre = new Genre(name);
        return genreDao.create(genre);
    }

    public Genre readGenre(int id) throws Exception {
        if (id <= 0) {
            throw new Exception("ID do gênero inválido.");
        }

        return genreDao.read(id);
    }

    public List<Genre> listGenresToList() throws Exception {
        List<Genre> genres = genreDao.readAllToList();

        if (genres == null) {
            return new ArrayList<>();
        }

        return genres;
    }

    public boolean updateGenre(Genre genre) throws Exception {
        if (genre == null) {
            throw new Exception("Gênero inválido.");
        }

        if (genre.getID() <= 0) {
            throw new Exception("ID do gênero inválido.");
        }

        genre.setName(normalizarNome(genre.getName()));

        validarNome(genre.getName());

        Genre atual = genreDao.read(genre.getID());

        if (atual == null) {
            throw new Exception("Gênero não encontrado.");
        }

        Genre existente = genreDao.readByName(genre.getName());

        if (existente != null && existente.getID() != genre.getID()) {
            throw new Exception("Já existe outro gênero com esse nome.");
        }

        return genreDao.update(genre);
    }

    public boolean deleteGenre(int id) throws Exception {
        if (id <= 0) {
            throw new Exception("ID do gênero inválido.");
        }

        Genre existente = genreDao.read(id);

        if (existente == null) {
            throw new Exception("Gênero não encontrado.");
        }

        FilmGenreDAO filmGenreDAO = new FilmGenreDAO();
        filmGenreDAO.deleteByGenreID(id);

        return genreDao.delete(id);
    }

    // =========================================================
    // RELACIONAMENTO N:N FILME-GÊNERO
    // =========================================================

    public boolean removerRelacoesDoGenero(int genreID) throws Exception {
        if (genreID <= 0) {
            throw new Exception("ID do gênero inválido.");
        }

        Genre existente = genreDao.read(genreID);

        if (existente == null) {
            throw new Exception("Gênero não encontrado.");
        }

        FilmGenreDAO filmGenreDAO = new FilmGenreDAO();
        return filmGenreDAO.deleteByGenreID(genreID);
    }

    public List<Integer> buscarIdsRelacoesDoGenero(int genreID) throws Exception {
        if (genreID <= 0) {
            throw new Exception("ID do gênero inválido.");
        }

        Genre existente = genreDao.read(genreID);

        if (existente == null) {
            throw new Exception("Gênero não encontrado.");
        }

        FilmGenreDAO filmGenreDAO = new FilmGenreDAO();
        List<Integer> ids = filmGenreDAO.buscarIdsRelacoesPorGenero(genreID);

        if (ids == null) {
            return new ArrayList<>();
        }

        return ids;
    }

    private void validarNome(String name) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new Exception("Nome do gênero inválido.");
        }
    }

    private String normalizarNome(String name) {
        return name == null ? "" : name.trim();
    }
}