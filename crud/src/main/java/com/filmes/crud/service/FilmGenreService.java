package com.filmes.crud.service;

import java.util.ArrayList;
import java.util.List;

import com.filmes.crud.dao.FilmDAO;
import com.filmes.crud.dao.FilmGenreDAO;
import com.filmes.crud.dao.GenreDAO;
import com.filmes.crud.model.Film;
import com.filmes.crud.model.FilmGenre;
import com.filmes.crud.model.Genre;

public class FilmGenreService {

    private final FilmGenreDAO filmGenreDao;
    private final FilmDAO filmDao;
    private final GenreDAO genreDao;

    public FilmGenreService() throws Exception {
        filmGenreDao = new FilmGenreDAO();
        filmDao = new FilmDAO();
        genreDao = new GenreDAO();
    }

    public List<FilmGenre> listAll() throws Exception {
        List<FilmGenre> lista = filmGenreDao.readAllToList();

        if (lista == null) {
            return new ArrayList<>();
        }

        return lista;
    }

    public FilmGenre read(int id) throws Exception {
        if (id <= 0) {
            throw new Exception("ID da relação inválido.");
        }

        return filmGenreDao.read(id);
    }

    public int create(FilmGenre fg) throws Exception {
        validarRelacao(fg);

        Film film = filmDao.read(fg.getFilmID());

        if (film == null) {
            throw new Exception("Filme não encontrado.");
        }

        Genre genre = genreDao.read(fg.getGenreID());

        if (genre == null) {
            throw new Exception("Gênero não encontrado.");
        }

        return filmGenreDao.create(fg);
    }

    public List<Integer> createMany(int filmID, int[] genreIDs) throws Exception {
        if (filmID <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        Film film = filmDao.read(filmID);

        if (film == null) {
            throw new Exception("Filme não encontrado.");
        }

        int[] idsNormalizados = normalizarGenreIDs(genreIDs);

        if (idsNormalizados.length == 0) {
            throw new Exception("Selecione pelo menos um gênero.");
        }

        List<Integer> idsRelacoesCriadas = new ArrayList<>();

        for (int genreID : idsNormalizados) {
            Genre genre = genreDao.read(genreID);

            if (genre == null) {
                throw new Exception("Gênero não encontrado: " + genreID);
            }

            FilmGenre fg = new FilmGenre(filmID, genreID);
            int idRelacao = filmGenreDao.create(fg);
            idsRelacoesCriadas.add(idRelacao);
        }

        return idsRelacoesCriadas;
    }

    public boolean update(FilmGenre fg) throws Exception {
        validarRelacao(fg);

        if (fg.getID() <= 0) {
            throw new Exception("ID da relação inválido.");
        }

        FilmGenre antiga = filmGenreDao.read(fg.getID());

        if (antiga == null) {
            return false;
        }

        Film film = filmDao.read(fg.getFilmID());

        if (film == null) {
            throw new Exception("Filme não encontrado.");
        }

        Genre genre = genreDao.read(fg.getGenreID());

        if (genre == null) {
            throw new Exception("Gênero não encontrado.");
        }

        return filmGenreDao.update(fg);
    }

    public boolean replaceGenresOfFilm(int filmID, int[] genreIDs) throws Exception {
        if (filmID <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        Film film = filmDao.read(filmID);

        if (film == null) {
            throw new Exception("Filme não encontrado.");
        }

        int[] idsNormalizados = normalizarGenreIDs(genreIDs);

        if (idsNormalizados.length == 0) {
            throw new Exception("Selecione pelo menos um gênero.");
        }

        for (int genreID : idsNormalizados) {
            Genre genre = genreDao.read(genreID);

            if (genre == null) {
                throw new Exception("Gênero não encontrado: " + genreID);
            }
        }

        filmGenreDao.deleteByFilmID(filmID);

        for (int genreID : idsNormalizados) {
            filmGenreDao.create(new FilmGenre(filmID, genreID));
        }

        return true;
    }

    public boolean delete(int id) throws Exception {
        if (id <= 0) {
            throw new Exception("ID da relação inválido.");
        }

        return filmGenreDao.delete(id);
    }

    public boolean deleteByFilmID(int filmID) throws Exception {
        if (filmID <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        return filmGenreDao.deleteByFilmID(filmID);
    }

    public boolean deleteByGenreID(int genreID) throws Exception {
        if (genreID <= 0) {
            throw new Exception("ID do gênero inválido.");
        }

        return filmGenreDao.deleteByGenreID(genreID);
    }

    public boolean deleteByFilmAndGenre(int filmID, int genreID) throws Exception {
        if (filmID <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        if (genreID <= 0) {
            throw new Exception("ID do gênero inválido.");
        }

        return filmGenreDao.deleteByFilmAndGenre(filmID, genreID);
    }

    public List<FilmGenre> listByFilmID(int filmID) throws Exception {
        if (filmID <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        Film film = filmDao.read(filmID);

        if (film == null) {
            throw new Exception("Filme não encontrado.");
        }

        List<FilmGenre> lista = filmGenreDao.readByFilmID(filmID);

        if (lista == null) {
            return new ArrayList<>();
        }

        return lista;
    }

    public List<FilmGenre> listByGenreID(int genreID) throws Exception {
        if (genreID <= 0) {
            throw new Exception("ID do gênero inválido.");
        }

        Genre genre = genreDao.read(genreID);

        if (genre == null) {
            throw new Exception("Gênero não encontrado.");
        }

        List<FilmGenre> lista = filmGenreDao.readByGenreID(genreID);

        if (lista == null) {
            return new ArrayList<>();
        }

        return lista;
    }

    public List<Genre> listGenresByFilmID(int filmID) throws Exception {
        List<FilmGenre> relacoes = listByFilmID(filmID);
        List<Genre> generos = new ArrayList<>();

        for (FilmGenre fg : relacoes) {
            if (fg != null) {
                Genre genre = genreDao.read(fg.getGenreID());

                if (genre != null) {
                    generos.add(genre);
                }
            }
        }

        return generos;
    }

    public List<Film> listFilmsByGenreID(int genreID) throws Exception {
        List<FilmGenre> relacoes = listByGenreID(genreID);
        List<Film> filmes = new ArrayList<>();

        for (FilmGenre fg : relacoes) {
            if (fg != null) {
                Film film = filmDao.read(fg.getFilmID());

                if (film != null) {
                    filmes.add(film);
                }
            }
        }

        return filmes;
    }

    public List<Integer> listGenreIDsByFilmID(int filmID) throws Exception {
        List<FilmGenre> relacoes = listByFilmID(filmID);
        List<Integer> ids = new ArrayList<>();

        for (FilmGenre fg : relacoes) {
            if (fg != null && fg.getGenreID() > 0 && !ids.contains(fg.getGenreID())) {
                ids.add(fg.getGenreID());
            }
        }

        return ids;
    }

    public List<Integer> listFilmIDsByGenreID(int genreID) throws Exception {
        List<FilmGenre> relacoes = listByGenreID(genreID);
        List<Integer> ids = new ArrayList<>();

        for (FilmGenre fg : relacoes) {
            if (fg != null && fg.getFilmID() > 0 && !ids.contains(fg.getFilmID())) {
                ids.add(fg.getFilmID());
            }
        }

        return ids;
    }

    public FilmGenre findByFilmAndGenre(int filmID, int genreID) throws Exception {
        if (filmID <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        if (genreID <= 0) {
            throw new Exception("ID do gênero inválido.");
        }

        return filmGenreDao.readByFilmAndGenre(filmID, genreID);
    }

    public List<Integer> buscarIdsRelacoesPorFilme(int filmID) throws Exception {
        if (filmID <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        List<Integer> ids = filmGenreDao.buscarIdsRelacoesPorFilme(filmID);

        if (ids == null) {
            return new ArrayList<>();
        }

        return ids;
    }

    public List<Integer> buscarIdsRelacoesPorGenero(int genreID) throws Exception {
        if (genreID <= 0) {
            throw new Exception("ID do gênero inválido.");
        }

        List<Integer> ids = filmGenreDao.buscarIdsRelacoesPorGenero(genreID);

        if (ids == null) {
            return new ArrayList<>();
        }

        return ids;
    }

    public void reconstruirListaInvertida() throws Exception {
        filmGenreDao.reconstruirListaInvertida();
    }

    private void validarRelacao(FilmGenre fg) throws Exception {
        if (fg == null) {
            throw new Exception("Relação filme-gênero inválida.");
        }

        if (fg.getFilmID() <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        if (fg.getGenreID() <= 0) {
            throw new Exception("ID do gênero inválido.");
        }
    }

    private int[] normalizarGenreIDs(int[] genreIDs) {
        if (genreIDs == null || genreIDs.length == 0) {
            return new int[0];
        }

        List<Integer> lista = new ArrayList<>();

        for (int id : genreIDs) {
            if (id > 0 && !lista.contains(id)) {
                lista.add(id);
            }
        }

        int[] resultado = new int[lista.size()];

        for (int i = 0; i < lista.size(); i++) {
            resultado[i] = lista.get(i);
        }

        return resultado;
    }
}