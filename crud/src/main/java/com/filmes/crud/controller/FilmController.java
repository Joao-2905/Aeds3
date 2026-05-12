package com.filmes.crud.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.filmes.crud.model.Film;
import com.filmes.crud.service.FilmService;

@RestController
@RequestMapping("/filmes")
@CrossOrigin("*")
public class FilmController {

    private final FilmService filmService;

    public FilmController() throws Exception {
        filmService = new FilmService();
    }

    @GetMapping
    public List<Film> listar() throws Exception {
        return filmService.listFilmsToList();
    }

    @PostMapping
    public Object criar(@RequestBody Film film) {
        try {
            int id = filmService.createFilm(film);
            return filmService.readFilm(id);

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(mensagemOuPadrao(e, "Erro interno ao cadastrar filme."));
        }
    }

    @GetMapping("/{id}")
    public Object buscar(@PathVariable int id) {
        try {
            Film film = filmService.readFilm(id);

            if (film == null) {
                return new ErrorResponse("Filme não encontrado.");
            }

            return film;

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(mensagemOuPadrao(e, "Erro interno ao buscar filme."));
        }
    }

    @PutMapping("/{id}")
    public Object atualizar(@PathVariable int id, @RequestBody Film film) {
        try {
            film.setID(id);

            boolean ok = filmService.updateFilm(film);

            if (ok) {
                return filmService.readFilm(id);
            }

            return new ErrorResponse("Filme não encontrado.");

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(mensagemOuPadrao(e, "Erro interno ao atualizar filme."));
        }
    }

    @DeleteMapping("/{id}")
    public Object excluir(@PathVariable int id) {
        try {
            boolean ok = filmService.deleteFilm(id);

            if (ok) {
                return new SuccessResponse("Filme excluído com sucesso.");
            }

            return new ErrorResponse("Filme não encontrado.");

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(mensagemOuPadrao(e, "Erro interno ao excluir filme."));
        }
    }

    // =========================================================
    // RELACIONAMENTO N:N FILME-GÊNERO
    // =========================================================

    @GetMapping("/{id}/generos")
    public Object listarRelacoesGenerosDoFilme(@PathVariable int id) {
        try {
            return filmService.listarRelacoesDeGeneroDoFilme(id);

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar relações de gênero do filme."));
        }
    }

    @GetMapping("/{id}/generos/detalhes")
    public Object listarGenerosDetalhadosDoFilme(@PathVariable int id) {
        try {
            return filmService.listarGenerosDoFilme(id);

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar gêneros detalhados do filme."));
        }
    }

    @GetMapping("/{id}/generos/ids")
    public Object listarIdsGenerosDoFilme(@PathVariable int id) {
        try {
            return filmService.listarIdsGenerosDoFilme(id);

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar IDs dos gêneros do filme."));
        }
    }

    @DeleteMapping("/{id}/generos")
    public Object removerGenerosDoFilme(@PathVariable int id) {
        try {
            boolean ok = filmService.removerRelacoesDeGeneroDoFilme(id);

            if (ok) {
                return new SuccessResponse("Relações de gênero do filme removidas com sucesso.");
            }

            return new ErrorResponse("Nenhuma relação encontrada para este filme.");

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse(mensagemOuPadrao(e, "Erro ao remover relações de gênero do filme."));
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
}