package com.filmes.crud.service;

import java.util.ArrayList;
import java.util.List;

import com.filmes.crud.dao.FilmDAO;
import com.filmes.crud.dao.ReviewDAO;
import com.filmes.crud.dao.UserDAO;
import com.filmes.crud.model.Film;
import com.filmes.crud.model.Review;
import com.filmes.crud.model.User;

public class ReviewService {

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private final ReviewDAO reviewDao;
    private final FilmDAO filmDao;
    private final UserDAO userDao;

    public ReviewService() throws Exception {
        reviewDao = new ReviewDAO();
        filmDao = new FilmDAO();
        userDao = new UserDAO();
    }

    public List<Review> listReviewsToList() throws Exception {
        reviewDao.deleteReviewsDeFilmesInexistentes();

        List<Review> reviews = reviewDao.readAllToList();

        if (reviews == null) {
            return new ArrayList<>();
        }

        preencherUsernames(reviews);

        return reviews;
    }

    public List<Review> listReviewsByUser(int userId) throws Exception {
        if (userId <= 0) {
            throw new Exception("ID do usuário inválido.");
        }

        User user = buscarUsuario(userId);
        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        List<Review> reviews = reviewDao.readByUserID(userId);

        if (reviews == null) {
            return new ArrayList<>();
        }

        preencherUsernames(reviews);

        return reviews;
    }

    public List<Review> listReviewsByFilm(int filmId) throws Exception {
        if (filmId <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        Film film = buscarFilme(filmId);
        if (film == null) {
            throw new Exception("Filme não encontrado.");
        }

        List<Review> reviews = reviewDao.readByFilmID(filmId);

        if (reviews == null) {
            return new ArrayList<>();
        }

        preencherUsernames(reviews);

        return reviews;
    }

    public Review findOwnReviewByFilm(int userId, int filmId) throws Exception {
        if (userId <= 0) {
            throw new Exception("ID do usuário inválido.");
        }

        if (filmId <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        User user = buscarUsuario(userId);
        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        Film film = buscarFilme(filmId);
        if (film == null) {
            throw new Exception("Filme não encontrado.");
        }

        Review review = reviewDao.readByUserAndFilmID(userId, filmId);

        if (review != null) {
            review.setUsername(buscarUsername(review.getUserID()));
        }

        return review;
    }

    public List<Review> listOtherReviewsFromSameFilm(int userId, int filmId) throws Exception {
        List<Review> todas = listReviewsByFilm(filmId);
        List<Review> outras = new ArrayList<>();

        for (Review review : todas) {
            if (review != null && review.getUserID() != userId) {
                review.setUsername(buscarUsername(review.getUserID()));
                outras.add(review);
            }
        }

        return outras;
    }

    public Review readReview(int id) throws Exception {
        if (id <= 0) {
            throw new Exception("ID da review inválido.");
        }

        Review review = reviewDao.read(id);

        if (review != null) {
            review.setUsername(buscarUsername(review.getUserID()));
        }

        return review;
    }

    public int createReview(Review review) throws Exception {
        validarReview(review);

        User user = buscarUsuario(review.getUserID());
        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        Film film = buscarFilme(review.getFilmID());
        if (film == null) {
            throw new Exception("Filme não encontrado.");
        }

        Review existente = reviewDao.readByUserAndFilmID(review.getUserID(), review.getFilmID());
        if (existente != null) {
            throw new Exception("Você já avaliou este filme. Atualize a avaliação existente.");
        }

        int newId = reviewDao.create(review);
        recalcularNotaDoFilme(review.getFilmID());

        return newId;
    }

    public boolean updateReview(Review review) throws Exception {
        if (review == null) {
            throw new Exception("Review inválida.");
        }

        if (review.getID() <= 0) {
            throw new Exception("ID da review inválido.");
        }

        Review antiga = reviewDao.read(review.getID());
        if (antiga == null) {
            throw new Exception("Review não encontrada.");
        }

        validarReview(review);

        User user = buscarUsuario(review.getUserID());
        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        Film filmNovo = buscarFilme(review.getFilmID());
        if (filmNovo == null) {
            throw new Exception("Filme não encontrado.");
        }

        Review duplicada = reviewDao.readByUserAndFilmID(review.getUserID(), review.getFilmID());
        if (duplicada != null && duplicada.getID() != review.getID()) {
            throw new Exception("Você já possui outra avaliação para este filme.");
        }

        boolean ok = reviewDao.update(review);

        if (ok) {
            recalcularNotaDoFilme(antiga.getFilmID());

            if (antiga.getFilmID() != review.getFilmID()) {
                recalcularNotaDoFilme(review.getFilmID());
            } else {
                recalcularNotaDoFilme(review.getFilmID());
            }
        }

        return ok;
    }

    public boolean updateOwnReview(int reviewId, int userId, Review review) throws Exception {
        if (reviewId <= 0) {
            throw new Exception("ID da review inválido.");
        }

        if (userId <= 0) {
            throw new Exception("ID do usuário inválido.");
        }

        if (review == null) {
            throw new Exception("Review inválida.");
        }

        Review antiga = reviewDao.read(reviewId);
        if (antiga == null) {
            throw new Exception("Review não encontrada.");
        }

        if (antiga.getUserID() != userId) {
            throw new Exception("Você só pode atualizar a sua própria avaliação.");
        }

        review.setID(reviewId);
        review.setUserID(userId);

        return updateReview(review);
    }

    public boolean deleteReview(int id) throws Exception {
        if (id <= 0) {
            throw new Exception("ID da review inválido.");
        }

        Review review = reviewDao.read(id);
        if (review == null) {
            return false;
        }

        boolean ok = reviewDao.delete(id);

        if (ok) {
            recalcularNotaDoFilme(review.getFilmID());
        }

        return ok;
    }

    public boolean deleteOwnReview(int reviewId, int userId) throws Exception {
        if (reviewId <= 0) {
            throw new Exception("ID da review inválido.");
        }

        if (userId <= 0) {
            throw new Exception("ID do usuário inválido.");
        }

        Review review = reviewDao.read(reviewId);
        if (review == null) {
            throw new Exception("Review não encontrada.");
        }

        if (review.getUserID() != userId) {
            throw new Exception("Você só pode excluir a sua própria avaliação.");
        }

        return deleteReview(reviewId);
    }

    // =========================================================
    // LISTA INVERTIDA
    // =========================================================

    public List<Integer> buscarIdsReviewsPorFilme(int filmId) throws Exception {
        if (filmId <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        Film film = buscarFilme(filmId);
        if (film == null) {
            throw new Exception("Filme não encontrado.");
        }

        List<Integer> ids = reviewDao.buscarIdsReviewsPorFilme(filmId);

        if (ids == null) {
            return new ArrayList<>();
        }

        return ids;
    }

    public List<Integer> buscarIdsReviewsPorUsuario(int userId) throws Exception {
        if (userId <= 0) {
            throw new Exception("ID do usuário inválido.");
        }

        User user = buscarUsuario(userId);
        if (user == null) {
            throw new Exception("Usuário não encontrado.");
        }

        List<Integer> ids = reviewDao.buscarIdsReviewsPorUsuario(userId);

        if (ids == null) {
            return new ArrayList<>();
        }

        return ids;
    }

    public void reconstruirListaInvertida() throws Exception {
        reviewDao.reconstruirListaInvertida();
    }

    // =========================================================
    // ORDENAÇÃO EXTERNA POR INTERCALAÇÃO
    // =========================================================

    public List<Review> listarOrdenadoPorNotaCrescente() throws Exception {
        List<Review> reviews = reviewDao.listarOrdenadoPorNotaCrescente();

        if (reviews == null) {
            return new ArrayList<>();
        }

        preencherUsernames(reviews);

        return reviews;
    }

    public List<Review> listarOrdenadoPorNotaDecrescente() throws Exception {
        List<Review> reviews = reviewDao.listarOrdenadoPorNotaDecrescente();

        if (reviews == null) {
            return new ArrayList<>();
        }

        preencherUsernames(reviews);

        return reviews;
    }

    public void gerarArquivoOrdenadoPorNota() throws Exception {
        reviewDao.gerarArquivoOrdenadoPorNota();
    }

    // =========================================================
    // ÁRVORE B+ POR NOTA/RATING
    // =========================================================

    public List<Review> buscarReviewsPorNotaBMais(int rating) throws Exception {
        validarNotaConsulta(rating);

        List<Review> reviews = reviewDao.buscarPorNotaBMais(rating);

        if (reviews == null) {
            return new ArrayList<>();
        }

        preencherUsernames(reviews);

        return reviews;
    }

    public List<Review> buscarReviewsPorIntervaloNotaBMais(int inicio, int fim) throws Exception {
        validarNotaConsulta(inicio);
        validarNotaConsulta(fim);

        List<Review> reviews = reviewDao.buscarPorIntervaloNotaBMais(inicio, fim);

        if (reviews == null) {
            return new ArrayList<>();
        }

        preencherUsernames(reviews);

        return reviews;
    }

    public List<Review> listarOrdenadoPorNotaCrescenteBMais() throws Exception {
        List<Review> reviews = reviewDao.listarOrdenadoPorNotaCrescenteBMais();

        if (reviews == null) {
            return new ArrayList<>();
        }

        preencherUsernames(reviews);

        return reviews;
    }

    public List<Review> listarOrdenadoPorNotaDecrescenteBMais() throws Exception {
        List<Review> reviews = reviewDao.listarOrdenadoPorNotaDecrescenteBMais();

        if (reviews == null) {
            return new ArrayList<>();
        }

        preencherUsernames(reviews);

        return reviews;
    }

    public void reconstruirArvoreBMaisPorNota() throws Exception {
        reviewDao.reconstruirArvoreBMaisPorNota();
    }

    public void exibirArvoreBMaisPorNota() throws Exception {
        reviewDao.exibirArvoreBMaisPorNota();
    }

    private void validarReview(Review review) throws Exception {
        if (review == null) {
            throw new Exception("Review inválida.");
        }

        if (review.getUserID() <= 0) {
            throw new Exception("ID do usuário inválido.");
        }

        if (review.getFilmID() <= 0) {
            throw new Exception("ID do filme inválido.");
        }

        if (review.getRating() < 0 || review.getRating() > 10) {
            throw new Exception("A nota deve estar entre 0 e 10.");
        }

        if (review.getNote() == null || review.getNote().trim().isEmpty()) {
            throw new Exception("O comentário não pode ser vazio.");
        }

        review.setNote(review.getNote().trim());
    }

    private void validarNotaConsulta(int nota) throws Exception {
        if (nota < 0 || nota > 10) {
            throw new Exception("A nota deve estar entre 0 e 10.");
        }
    }

    private User buscarUsuario(int userID) throws Exception {
        if (userID <= 0) {
            return null;
        }

        return userDao.read(userID);
    }

    private Film buscarFilme(int filmID) throws Exception {
        if (filmID <= 0) {
            return null;
        }

        return filmDao.read(filmID);
    }

    private String buscarUsername(int userID) throws Exception {
        User user = buscarUsuario(userID);
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "Desconhecido";
        }
        return user.getUsername().trim();
    }

    private void preencherUsernames(List<Review> reviews) throws Exception {
        if (reviews == null) {
            return;
        }

        for (Review review : reviews) {
            if (review != null) {
                review.setUsername(buscarUsername(review.getUserID()));
            }
        }
    }

    private void recalcularNotaDoFilme(int filmID) throws Exception {
        Film film = buscarFilme(filmID);

        if (film == null) {
            return;
        }

        List<Review> reviews = reviewDao.readByFilmID(filmID);

        if (reviews == null) {
            film.setRating(0);
            film.setTotalReviews(0);
            filmDao.update(film);
            return;
        }

        int quantidade = 0;
        int soma = 0;

        for (Review r : reviews) {
            if (r != null && r.getFilmID() == filmID) {
                soma += r.getRating();
                quantidade++;
            }
        }

        if (quantidade == 0) {
            film.setRating(0);
            film.setTotalReviews(0);
        } else {
            float media = (float) soma / quantidade;
            film.setRating(media);
            film.setTotalReviews(quantidade);
        }

        filmDao.update(film);
    }
}