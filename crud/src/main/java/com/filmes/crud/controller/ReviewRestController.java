package com.filmes.crud.controller;

import com.filmes.crud.model.Review;
import com.filmes.crud.service.ReviewService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@CrossOrigin("*")
public class ReviewRestController {

    private final ReviewService reviewService;

    public ReviewRestController() throws Exception {
        this.reviewService = new ReviewService();
    }

    @GetMapping
    public List<Review> listar() throws Exception {
        return reviewService.listReviewsToList();
    }

    @GetMapping("/usuario/{userId}")
    public Object listarPorUsuario(@PathVariable int userId) {
        try {
            return reviewService.listReviewsByUser(userId);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar avaliações do usuário."));
        }
    }

    @GetMapping("/filme/{filmId}")
    public Object listarPorFilme(@PathVariable int filmId) {
        try {
            return reviewService.listReviewsByFilm(filmId);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar avaliações do filme."));
        }
    }

    @GetMapping("/usuario/{userId}/filme/{filmId}")
    public Object buscarMinhaPorFilme(@PathVariable int userId, @PathVariable int filmId) {
        try {
            Review review = reviewService.findOwnReviewByFilm(userId, filmId);

            if (review == null) {
                return new ErrorResponse("Avaliação não encontrada.");
            }

            return review;

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao buscar avaliação."));
        }
    }

    @GetMapping("/{id}")
    public Object buscar(@PathVariable int id) {
        try {
            Review review = reviewService.readReview(id);

            if (review == null) {
                return new ErrorResponse("Review não encontrada.");
            }

            return review;

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro interno ao buscar review."));
        }
    }

    @PostMapping
    public Object criar(@RequestBody Review review) {
        try {
            int id = reviewService.createReview(review);
            Review criada = reviewService.readReview(id);
            return criada;

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao criar review."));
        }
    }

    @PutMapping("/{id}")
    public Object atualizar(@PathVariable int id, @RequestBody Review review) {
        try {
            review.setID(id);
            boolean ok = reviewService.updateReview(review);

            if (ok) {
                Review atualizada = reviewService.readReview(id);
                return atualizada;
            }

            return new ErrorResponse("Review não encontrada.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao atualizar review."));
        }
    }

    @PutMapping("/{id}/usuario/{userId}")
    public Object atualizarPropria(@PathVariable int id, @PathVariable int userId, @RequestBody Review review) {
        try {
            boolean ok = reviewService.updateOwnReview(id, userId, review);

            if (ok) {
                Review atualizada = reviewService.readReview(id);
                return atualizada;
            }

            return new ErrorResponse("Review não encontrada.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao atualizar sua avaliação."));
        }
    }

    @DeleteMapping("/{id}")
    public Object deletar(@PathVariable int id) {
        try {
            boolean ok = reviewService.deleteReview(id);

            if (ok) {
                return new SuccessResponse("Review deletada com sucesso.");
            }

            return new ErrorResponse("Review não encontrada.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao deletar review."));
        }
    }

    @DeleteMapping("/{id}/usuario/{userId}")
    public Object deletarPropria(@PathVariable int id, @PathVariable int userId) {
        try {
            boolean ok = reviewService.deleteOwnReview(id, userId);

            if (ok) {
                return new SuccessResponse("Avaliação excluída com sucesso.");
            }

            return new ErrorResponse("Review não encontrada.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao excluir sua avaliação."));
        }
    }

    // =========================================================
    // LISTA INVERTIDA
    // =========================================================

    @GetMapping("/indice/filme/{filmId}")
    public Object listarPorFilmeComIndice(@PathVariable int filmId) {
        try {
            return reviewService.listReviewsByFilm(filmId);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao consultar avaliações por filme usando índice."));
        }
    }

    @GetMapping("/indice/usuario/{userId}")
    public Object listarPorUsuarioComIndice(@PathVariable int userId) {
        try {
            return reviewService.listReviewsByUser(userId);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao consultar avaliações por usuário usando índice."));
        }
    }

    @GetMapping("/indice/filme/{filmId}/ids")
    public Object listarIdsPorFilme(@PathVariable int filmId) {
        try {
            return reviewService.buscarIdsReviewsPorFilme(filmId);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao buscar IDs das avaliações por filme."));
        }
    }

    @GetMapping("/indice/usuario/{userId}/ids")
    public Object listarIdsPorUsuario(@PathVariable int userId) {
        try {
            return reviewService.buscarIdsReviewsPorUsuario(userId);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao buscar IDs das avaliações por usuário."));
        }
    }

    @PostMapping("/indice/reconstruir")
    public Object reconstruirListaInvertida() {
        try {
            reviewService.reconstruirListaInvertida();
            return new SuccessResponse("Lista invertida reconstruída com sucesso.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao reconstruir lista invertida."));
        }
    }

    // =========================================================
    // ORDENAÇÃO EXTERNA POR INTERCALAÇÃO
    // =========================================================

    @GetMapping("/ordenado/crescente")
    public Object listarOrdenadoCrescente() {
        try {
            return reviewService.listarOrdenadoPorNotaCrescente();

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar avaliações em ordem crescente."));
        }
    }

    @GetMapping("/ordenado/decrescente")
    public Object listarOrdenadoDecrescente() {
        try {
            return reviewService.listarOrdenadoPorNotaDecrescente();

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar avaliações em ordem decrescente."));
        }
    }

    @PostMapping("/ordenado/gerar")
    public Object gerarArquivoOrdenado() {
        try {
            reviewService.gerarArquivoOrdenadoPorNota();
            return new SuccessResponse("Arquivo review_ordenado.bin gerado com sucesso.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao gerar arquivo ordenado."));
        }
    }

    // =========================================================
    // ÁRVORE B+ POR NOTA/RATING
    // =========================================================

    @GetMapping("/bmais/rating/{rating}")
    public Object buscarPorNotaBMais(@PathVariable int rating) {
        try {
            return reviewService.buscarReviewsPorNotaBMais(rating);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao buscar avaliações pela Árvore B+."));
        }
    }

    @GetMapping("/bmais/intervalo/{inicio}/{fim}")
    public Object buscarPorIntervaloBMais(@PathVariable int inicio, @PathVariable int fim) {
        try {
            return reviewService.buscarReviewsPorIntervaloNotaBMais(inicio, fim);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao buscar avaliações por intervalo na Árvore B+."));
        }
    }

    @GetMapping("/bmais/ordenado/crescente")
    public Object listarOrdenadoCrescenteBMais() {
        try {
            return reviewService.listarOrdenadoPorNotaCrescenteBMais();

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar avaliações em ordem crescente pela Árvore B+."));
        }
    }

    @GetMapping("/bmais/ordenado/decrescente")
    public Object listarOrdenadoDecrescenteBMais() {
        try {
            return reviewService.listarOrdenadoPorNotaDecrescenteBMais();

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar avaliações em ordem decrescente pela Árvore B+."));
        }
    }

    @PostMapping("/bmais/reconstruir")
    public Object reconstruirBMais() {
        try {
            reviewService.reconstruirArvoreBMaisPorNota();
            return new SuccessResponse("Árvore B+ de reviews por nota reconstruída com sucesso.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao reconstruir Árvore B+."));
        }
    }

    @GetMapping("/bmais/exibir")
    public Object exibirBMais() {
        try {
            reviewService.exibirArvoreBMaisPorNota();
            return new SuccessResponse("Árvore B+ exibida no terminal do servidor.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao exibir Árvore B+."));
        }
    }

    private String mensagemOuPadrao(Exception e, String padrao) {
        String mensagem = e.getMessage();
        if (mensagem == null || mensagem.isBlank()) {
            return padrao;
        }
        return mensagem;
    }

    static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }

    static class SuccessResponse {
        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }
    }

    // =========================================================
// ORDENAÇÃO EXTERNA POR INTERCALAÇÃO
// =========================================================

    @GetMapping("/externalsort/crescente")
    public Object listarExternalSortCrescente() {
        try {
            return reviewService.listarOrdenadoPorNotaCrescente();

        } catch (Exception e) {
            return new ErrorResponse(
                mensagemOuPadrao(e, "Erro na ordenação externa crescente.")
            );
        }
    }

    @GetMapping("/externalsort/decrescente")
    public Object listarExternalSortDecrescente() {
        try {
            return reviewService.listarOrdenadoPorNotaDecrescente();

        } catch (Exception e) {
            return new ErrorResponse(
                mensagemOuPadrao(e, "Erro na ordenação externa decrescente.")
            );
        }
    }
}