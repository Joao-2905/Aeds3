package com.filmes.crud.service;

import java.util.ArrayList;
import java.util.List;

import com.filmes.crud.dao.FilmDAO;
import com.filmes.crud.dao.FilmGenreDAO;
import com.filmes.crud.dao.GenreDAO;
import com.filmes.crud.dao.ReviewDAO;
import com.filmes.crud.model.Film;
import com.filmes.crud.model.FilmGenre;
import com.filmes.crud.model.Genre;

public class FilmService {

    private final FilmDAO filmDao;
    private final GenreDAO genreDao;

    public FilmService() throws Exception {
        filmDao = new FilmDAO();
        genreDao = new GenreDAO();
    }

    public int createFilm(String title, String description, int releaseDate, String[] directors, int genreID) throws Exception {
        return createFilm(title, description, releaseDate, directors, new int[] { genreID });
    }

    public int createFilm(String title, String description, int releaseDate, String[] directors, int[] genreIDs) throws Exception {
        title = normalizarTexto(title);
        description = normalizarTexto(description);
        directors = normalizarDiretores(directors);
        genreIDs = normalizarGenreIDs(genreIDs);

        validarTitulo(title);
        validarDescricao(description);
        validarData(releaseDate);
        validarDiretores(directors);
        validarGeneros(genreIDs);

        int genreIDPrincipal = genreIDs[0];

        Film film = new Film(title, description, releaseDate, directors, genreIDPrincipal);
        film.setGenreIDs(genreIDs);
        film.setRating(0);
        film.setTotalReviews(0);

        int idCriado = filmDao.create(film);

        salvarRelacoesFilmGenre(idCriado, genreIDs);

        return idCriado;
    }

    public int createFilm(Film film) throws Exception {
        if (film == null) {
            throw new Exception("Filme inválido.");
        }

        int[] genreIDs = film.getGenreIDs();

        if (genreIDs == null || genreIDs.length == 0) {
            genreIDs = new int[] { film.getGenreID() };
        }

        return createFilm(
                film.getTitle(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDirectors(),
                genreIDs
        );
    }

    public Film readFilm(int id) throws Exception {
        if (id <= 0) {
            throw new Exception("ID inválido.");
        }

        Film film = filmDao.read(id);

        if (film != null) {
            preencherGenerosDoFilme(film);
        }

        return film;
    }

    public List<Film> listFilmsToList() throws Exception {
        List<Film> films = filmDao.readAllToList();

        if (films == null) {
            return new ArrayList<>();
        }

        for (Film film : films) {
            if (film != null) {
                preencherGenerosDoFilme(film);
            }
        }

        return films;
    }

    public boolean deleteFilm(int id) throws Exception {
        if (id <= 0) {
            throw new Exception("ID inválido.");
        }

        Film existente = filmDao.read(id);
        if (existente == null) {
            throw new Exception("Filme não encontrado.");
        }

        ReviewDAO reviewDAO = new ReviewDAO();
        reviewDAO.deleteByFilmID(id);

        FilmGenreDAO filmGenreDAO = new FilmGenreDAO();
        filmGenreDAO.deleteByFilmID(id);

        return filmDao.delete(id);
    }

    public boolean updateFilm(Film film) throws Exception {
        if (film == null) {
            throw new Exception("Filme inválido.");
        }

        if (film.getID() <= 0) {
            throw new Exception("ID inválido.");
        }

        int[] genreIDs = film.getGenreIDs();

        if (genreIDs == null || genreIDs.length == 0) {
            genreIDs = new int[] { film.getGenreID() };
        }

        genreIDs = normalizarGenreIDs(genreIDs);

        film.setTitle(normalizarTexto(film.getTitle()));
        film.setDescription(normalizarTexto(film.getDescription()));
        film.setDirectors(normalizarDiretores(film.getDirectors()));
        film.setGenreIDs(genreIDs);

        validarTitulo(film.getTitle());
        validarDescricao(film.getDescription());
        validarData(film.getReleaseDate());
        validarDiretores(film.getDirectors());
        validarGeneros(genreIDs);

        Film existente = filmDao.read(film.getID());
        if (existente == null) {
            throw new Exception("Filme não encontrado.");
        }

        film.setGenreID(genreIDs[0]);
        film.setRating(existente.getRating());
        film.setTotalReviews(existente.getTotalReviews());

        boolean ok = filmDao.update(film);

        if (ok) {
            FilmGenreDAO filmGenreDAO = new FilmGenreDAO();
            filmGenreDAO.deleteByFilmID(film.getID());
            salvarRelacoesFilmGenre(film.getID(), genreIDs);
        }

        return ok;
    }

    public void listFilms() throws Exception {
        filmDao.listAll();
    }

    public void exibirIndice() throws Exception {
        filmDao.exibirIndice();
    }

    public boolean removerRelacoesDeGeneroDoFilme(int filmID) throws Exception {
        if (filmID <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        Film existente = filmDao.read(filmID);

        if (existente == null) {
            throw new Exception("Filme não encontrado.");
        }

        FilmGenreDAO filmGenreDAO = new FilmGenreDAO();
        return filmGenreDAO.deleteByFilmID(filmID);
    }

    public List<FilmGenre> listarRelacoesDeGeneroDoFilme(int filmID) throws Exception {
        if (filmID <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        Film existente = filmDao.read(filmID);

        if (existente == null) {
            throw new Exception("Filme não encontrado.");
        }

        FilmGenreDAO filmGenreDAO = new FilmGenreDAO();
        List<FilmGenre> relacoes = filmGenreDAO.readByFilmID(filmID);

        if (relacoes == null) {
            return new ArrayList<>();
        }

        return relacoes;
    }

    public List<Genre> listarGenerosDoFilme(int filmID) throws Exception {
        List<FilmGenre> relacoes = listarRelacoesDeGeneroDoFilme(filmID);
        List<Genre> generos = new ArrayList<>();

        for (FilmGenre relacao : relacoes) {
            if (relacao != null) {
                Genre genre = genreDao.read(relacao.getGenreID());

                if (genre != null) {
                    generos.add(genre);
                }
            }
        }

        return generos;
    }

    public List<Integer> listarIdsGenerosDoFilme(int filmID) throws Exception {
        List<FilmGenre> relacoes = listarRelacoesDeGeneroDoFilme(filmID);
        List<Integer> ids = new ArrayList<>();

        for (FilmGenre relacao : relacoes) {
            if (relacao != null && relacao.getGenreID() > 0 && !ids.contains(relacao.getGenreID())) {
                ids.add(relacao.getGenreID());
            }
        }

        return ids;
    }

    private void salvarRelacoesFilmGenre(int filmID, int[] genreIDs) throws Exception {
        if (filmID <= 0 || genreIDs == null || genreIDs.length == 0) {
            return;
        }

        FilmGenreDAO filmGenreDAO = new FilmGenreDAO();

        for (int genreID : genreIDs) {
            if (genreID > 0) {
                FilmGenre fg = new FilmGenre(filmID, genreID);
                filmGenreDAO.create(fg);
            }
        }
    }

    private void preencherGenerosDoFilme(Film film) throws Exception {
        if (film == null || film.getID() <= 0) {
            return;
        }

        FilmGenreDAO filmGenreDAO = new FilmGenreDAO();
        List<FilmGenre> relacoes = filmGenreDAO.readByFilmID(film.getID());

        if (relacoes == null || relacoes.isEmpty()) {
            if (film.getGenreID() > 0) {
                film.setGenreIDs(new int[] { film.getGenreID() });
            }
            return;
        }

        List<Integer> ids = new ArrayList<>();

        for (FilmGenre fg : relacoes) {
            if (fg != null && fg.getGenreID() > 0 && !ids.contains(fg.getGenreID())) {
                ids.add(fg.getGenreID());
            }
        }

        int[] genreIDs = new int[ids.size()];

        for (int i = 0; i < ids.size(); i++) {
            genreIDs[i] = ids.get(i);
        }

        film.setGenreIDs(genreIDs);
    }

    private void validarTitulo(String title) throws Exception {
        if (title == null || title.trim().isEmpty()) {
            throw new Exception("Título não pode ser vazio.");
        }
    }

    private void validarDescricao(String description) throws Exception {
        if (description == null || description.trim().isEmpty()) {
            throw new Exception("Descrição não pode ser vazia.");
        }
    }

    private void validarData(int releaseDate) throws Exception {
        if (String.valueOf(releaseDate).length() != 8) {
            throw new Exception("A data deve estar no formato AAAAMMDD.");
        }

        if (!dataValida(releaseDate)) {
            throw new Exception("Data inválida. O ano deve estar entre 1900 e 2026.");
        }
    }

    private void validarDiretores(String[] directors) throws Exception {
        if (directors == null || directors.length == 0) {
            throw new Exception("O filme precisa ter pelo menos um diretor.");
        }

        for (String diretor : directors) {
            if (diretor == null || diretor.trim().isEmpty()) {
                throw new Exception("Nome de diretor inválido.");
            }
        }
    }

    private void validarGenero(int genreID) throws Exception {
        if (genreID <= 0) {
            throw new Exception("Gênero obrigatório.");
        }

        if (genreDao.read(genreID) == null) {
            throw new Exception("Gênero não encontrado.");
        }
    }

    private void validarGeneros(int[] genreIDs) throws Exception {
        if (genreIDs == null || genreIDs.length == 0) {
            throw new Exception("O filme precisa ter pelo menos um gênero.");
        }

        for (int genreID : genreIDs) {
            validarGenero(genreID);
        }
    }

    private boolean dataValida(int data) {
        int ano = data / 10000;
        int mes = (data / 100) % 100;
        int dia = data % 100;

        if (ano < 1900 || ano > 2026) {
            return false;
        }

        if (mes < 1 || mes > 12) {
            return false;
        }

        int[] diasMes = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        if ((ano % 4 == 0 && ano % 100 != 0) || (ano % 400 == 0)) {
            diasMes[1] = 29;
        }

        return dia >= 1 && dia <= diasMes[mes - 1];
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String[] normalizarDiretores(String[] directors) {
        if (directors == null) {
            return new String[0];
        }

        List<String> lista = new ArrayList<>();

        for (String d : directors) {
            if (d == null) {
                continue;
            }

            String[] partes = d.split(",");

            for (String p : partes) {
                String nome = p == null ? "" : p.trim();
                if (!nome.isEmpty()) {
                    lista.add(nome);
                }
            }
        }

        return lista.toArray(new String[0]);
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