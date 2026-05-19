package com.filmes.crud.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.filmes.crud.model.FilmGenre;
import com.filmes.crud.service.FilmGenreService;

@RestController
@RequestMapping("/film-genres")
@CrossOrigin("*")
public class FilmGenreRestController {

    private final FilmGenreService filmGenreService;

    public FilmGenreRestController() throws Exception {
        this.filmGenreService = new FilmGenreService();
    }

    @GetMapping
    public Object listar() {
        try {
            return filmGenreService.listAll();

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao listar relações filme-gênero."));
        }
    }

    @GetMapping("/{id}")
    public Object buscar(@PathVariable int id) {
        try {
            FilmGenre fg = filmGenreService.read(id);

            if (fg == null) {
                return new ErrorResponse(
                        "Relação filme-gênero não encontrada.");
            }

            return fg;

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao buscar relação filme-gênero."));
        }
    }

    // =========================================================
    // RELAÇÕES POR FILME
    // =========================================================

    @GetMapping("/filme/{filmID}")
    public Object listarPorFilme(@PathVariable int filmID) {
        try {
            return filmGenreService.listByFilmID(filmID);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao listar gêneros do filme."));
        }
    }

    @GetMapping("/filme/{filmID}/generos")
    public Object listarGenerosDoFilme(@PathVariable int filmID) {
        try {
            return filmGenreService.listGenresByFilmID(filmID);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao listar gêneros do filme."));
        }
    }

    @GetMapping("/filme/{filmID}/generos/ids")
    public Object listarIdsGenerosDoFilme(@PathVariable int filmID) {
        try {
            return filmGenreService.listGenreIDsByFilmID(filmID);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao listar IDs dos gêneros do filme."));
        }
    }

    // =========================================================
    // RELAÇÕES POR GÊNERO
    // =========================================================

    @GetMapping("/genero/{genreID}")
    public Object listarPorGenero(@PathVariable int genreID) {
        try {
            return filmGenreService.listByGenreID(genreID);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao listar filmes do gênero."));
        }
    }

    @GetMapping("/genero/{genreID}/filmes")
    public Object listarFilmesDoGenero(@PathVariable int genreID) {
        try {
            return filmGenreService.listFilmsByGenreID(genreID);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao listar filmes do gênero."));
        }
    }

    @GetMapping("/genero/{genreID}/filmes/ids")
    public Object listarIdsFilmesDoGenero(@PathVariable int genreID) {
        try {
            return filmGenreService.listFilmIDsByGenreID(genreID);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao listar IDs dos filmes do gênero."));
        }
    }

    // =========================================================
    // BUSCA ESPECÍFICA
    // =========================================================

    @GetMapping("/filme/{filmID}/genero/{genreID}")
    public Object buscarPorFilmeEGenero(
            @PathVariable int filmID,
            @PathVariable int genreID) {

        try {
            FilmGenre fg = filmGenreService.findByFilmAndGenre(
                    filmID,
                    genreID
            );

            if (fg == null) {
                return new ErrorResponse(
                        "Relação filme-gênero não encontrada.");
            }

            return fg;

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao buscar relação filme-gênero."));
        }
    }

    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public Object criar(@RequestBody FilmGenre fg) {
        try {
            int id = filmGenreService.create(fg);

            FilmGenre criada = filmGenreService.read(id);

            return criada;

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao criar relação filme-gênero."));
        }
    }

    // =========================================================
    // CREATE MANY
    // =========================================================

    @PostMapping("/multiplos")
    public Object criarMultiplos(@RequestBody Map<String, Object> body) {

        try {

            Object filmIDObj = body.get("filmID");
            Object genreIDsObj = body.get("genreIDs");

            if (filmIDObj == null || genreIDsObj == null) {
                return new ErrorResponse(
                        "filmID e genreIDs são obrigatórios.");
            }

            int filmID = Integer.parseInt(filmIDObj.toString());

            List<?> lista = (List<?>) genreIDsObj;

            int[] genreIDs = new int[lista.size()];

            for (int i = 0; i < lista.size(); i++) {
                genreIDs[i] = Integer.parseInt(lista.get(i).toString());
            }

            return filmGenreService.createMany(filmID, genreIDs);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao criar múltiplas relações."));
        }
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{id}")
    public Object atualizar(
            @PathVariable int id,
            @RequestBody FilmGenre fg) {

        try {
            fg.setID(id);

            boolean ok = filmGenreService.update(fg);

            if (ok) {
                return filmGenreService.read(id);
            }

            return new ErrorResponse(
                    "Relação filme-gênero não encontrada.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao atualizar relação filme-gênero."));
        }
    }

    // =========================================================
    // REPLACE GENRES OF FILM
    // =========================================================

    @PutMapping("/filme/{filmID}/generos")
    public Object substituirGenerosDoFilme(
            @PathVariable int filmID,
            @RequestBody Map<String, Object> body) {

        try {

            Object genreIDsObj = body.get("genreIDs");

            if (genreIDsObj == null) {
                return new ErrorResponse(
                        "genreIDs é obrigatório.");
            }

            List<?> lista = (List<?>) genreIDsObj;

            int[] genreIDs = new int[lista.size()];

            for (int i = 0; i < lista.size(); i++) {
                genreIDs[i] = Integer.parseInt(lista.get(i).toString());
            }

            boolean ok = filmGenreService.replaceGenresOfFilm(
                    filmID,
                    genreIDs
            );

            if (ok) {
                return new SuccessResponse(
                        "Gêneros do filme atualizados com sucesso.");
            }

            return new ErrorResponse(
                    "Erro ao atualizar gêneros do filme.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao atualizar gêneros do filme."));
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{id}")
    public Object deletar(@PathVariable int id) {
        try {
            boolean ok = filmGenreService.delete(id);

            if (ok) {
                return new SuccessResponse(
                        "Relação filme-gênero deletada com sucesso.");
            }

            return new ErrorResponse(
                    "Relação filme-gênero não encontrada.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao deletar relação filme-gênero."));
        }
    }

    @DeleteMapping("/filme/{filmID}")
    public Object deletarPorFilme(@PathVariable int filmID) {
        try {
            boolean ok = filmGenreService.deleteByFilmID(filmID);

            if (ok) {
                return new SuccessResponse(
                        "Relações do filme deletadas com sucesso.");
            }

            return new ErrorResponse(
                    "Nenhuma relação encontrada para este filme.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao deletar relações do filme."));
        }
    }

    @DeleteMapping("/genero/{genreID}")
    public Object deletarPorGenero(@PathVariable int genreID) {
        try {
            boolean ok = filmGenreService.deleteByGenreID(genreID);

            if (ok) {
                return new SuccessResponse(
                        "Relações do gênero deletadas com sucesso.");
            }

            return new ErrorResponse(
                    "Nenhuma relação encontrada para este gênero.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao deletar relações do gênero."));
        }
    }

    @DeleteMapping("/filme/{filmID}/genero/{genreID}")
    public Object deletarPorFilmeEGenero(
            @PathVariable int filmID,
            @PathVariable int genreID) {

        try {
            boolean ok = filmGenreService.deleteByFilmAndGenre(
                    filmID,
                    genreID
            );

            if (ok) {
                return new SuccessResponse(
                        "Relação filme-gênero deletada com sucesso.");
            }

            return new ErrorResponse(
                    "Relação filme-gênero não encontrada.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao deletar relação filme-gênero."));
        }
    }

    // =========================================================
    // LISTA INVERTIDA
    // =========================================================

    @GetMapping("/indice/filme/{filmID}/ids")
    public Object buscarIdsPorFilme(@PathVariable int filmID) {
        try {
            return filmGenreService.buscarIdsRelacoesPorFilme(filmID);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao buscar IDs das relações por filme."));
        }
    }

    @GetMapping("/indice/genero/{genreID}/ids")
    public Object buscarIdsPorGenero(@PathVariable int genreID) {
        try {
            return filmGenreService.buscarIdsRelacoesPorGenero(genreID);

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao buscar IDs das relações por gênero."));
        }
    }

    @PostMapping("/indice/reconstruir")
    public Object reconstruirListaInvertida() {
        try {
            filmGenreService.reconstruirListaInvertida();

            return new SuccessResponse(
                    "Lista invertida reconstruída com sucesso.");

        } catch (Exception e) {
            return new ErrorResponse(mensagemOuPadrao(e,
                    "Erro ao reconstruir lista invertida."));
        }
    }

    // =========================================================
    // AUXILIARES
    // =========================================================

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