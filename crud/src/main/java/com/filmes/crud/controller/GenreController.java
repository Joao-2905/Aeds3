package com.filmes.crud.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.filmes.crud.model.Genre;
import com.filmes.crud.service.FilmGenreService;
import com.filmes.crud.service.GenreService;

@RestController
@RequestMapping("/generos")
@CrossOrigin("*")
public class GenreController {

    private final GenreService genreService;
    private final FilmGenreService filmGenreService;

    public GenreController() throws Exception {
        genreService = new GenreService();
        filmGenreService = new FilmGenreService();
    }

    @GetMapping
    public ResponseEntity<List<Genre>> listar() {
        try {
            return ResponseEntity.ok(genreService.listGenresToList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable int id) {
        try {
            Genre genre = genreService.readGenre(id);

            if (genre == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Gênero não encontrado."));
            }

            return ResponseEntity.ok(genre);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagemOuPadrao(e, "Erro ao buscar gênero.")));
        }
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Genre genre) {
        try {
            int id = genreService.createGenre(genre.getName());
            Genre criado = genreService.readGenre(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagemOuPadrao(e, "Erro ao cadastrar gênero.")));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable int id, @RequestBody Genre genre) {
        try {
            genre.setID(id);

            boolean ok = genreService.updateGenre(genre);
            if (!ok) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Gênero não encontrado."));
            }

            return ResponseEntity.ok(genreService.readGenre(id));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagemOuPadrao(e, "Erro ao atualizar gênero.")));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable int id) {
        try {
            boolean ok = genreService.deleteGenre(id);

            if (!ok) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Gênero não encontrado."));
            }

            return ResponseEntity.ok(new SuccessResponse("Gênero excluído com sucesso."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagemOuPadrao(e, "Erro ao excluir gênero.")));
        }
    }

    // =========================================================
    // RELACIONAMENTO N:N FILME-GÊNERO
    // =========================================================

    @GetMapping("/{id}/filmes")
    public ResponseEntity<?> listarFilmesDoGenero(@PathVariable int id) {
        try {
            return ResponseEntity.ok(filmGenreService.listByGenreID(id));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar filmes do gênero.")));
        }
    }

    @GetMapping("/{id}/filmes/ids")
    public ResponseEntity<?> listarIdsRelacoesDoGenero(@PathVariable int id) {
        try {
            return ResponseEntity.ok(filmGenreService.buscarIdsRelacoesPorGenero(id));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagemOuPadrao(e, "Erro ao listar IDs das relações do gênero.")));
        }
    }

    @DeleteMapping("/{id}/filmes")
    public ResponseEntity<?> removerFilmesDoGenero(@PathVariable int id) {
        try {
            boolean ok = genreService.removerRelacoesDoGenero(id);

            if (!ok) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Nenhuma relação encontrada para este gênero."));
            }

            return ResponseEntity.ok(new SuccessResponse("Relações do gênero removidas com sucesso."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(mensagemOuPadrao(e, "Erro ao remover relações do gênero.")));
        }
    }

    private static String mensagemOuPadrao(Exception e, String padrao) {
        return (e.getMessage() == null || e.getMessage().isBlank()) ? padrao : e.getMessage();
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