package com.filmes.crud.controller;

import org.springframework.web.bind.annotation.*;

import com.filmes.crud.service.PatternSearchService;

@RestController
@RequestMapping("/busca-textual")
@CrossOrigin("*")
public class PatternSearchController {

    private final PatternSearchService patternSearchService;

    public PatternSearchController() throws Exception {
        this.patternSearchService = new PatternSearchService();
    }

    @GetMapping("/{algoritmo}/filmes")
    public Object buscarFilmes(
            @PathVariable String algoritmo,
            @RequestParam String termo) {

        try {
            if (!algoritmo.equalsIgnoreCase("kmp") && !algoritmo.equalsIgnoreCase("bm")) {
                return new ErrorResponse("Algoritmo inválido. Use kmp ou bm.");
            }

            return patternSearchService.buscarFilmes(termo, algoritmo);

        } catch (Exception e) {
            e.printStackTrace();
            return new ErrorResponse("Erro na busca textual: " + e.getMessage());  
        }
    }

    static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}