package com.filmes.crud.service;

import java.util.ArrayList;
import java.util.List;

import com.filmes.crud.model.Film;
import com.filmes.crud.model.Genre;
import com.filmes.crud.padroes.BoyerMoore;
import com.filmes.crud.padroes.KMP;
import com.filmes.crud.padroes.ResultadoBuscaTexto;

public class PatternSearchService {

    private final FilmService filmService;

    public PatternSearchService() throws Exception {
        this.filmService = new FilmService();
    }

    public List<ResultadoBuscaFilme> buscarFilmes(String termo, String algoritmo) throws Exception {
        List<ResultadoBuscaFilme> resultados = new ArrayList<>();

        if (termo == null || termo.trim().isEmpty()) {
            return resultados;
        }

        for (Film film : filmService.listFilmsToList()) {
            if (film == null) {
                continue;
            }

            ResultadoBuscaFilme resultadoFinal = null;

            resultadoFinal = juntarResultado(resultadoFinal,
                    buscarNoCampo(film, "Título", film.getTitle(), termo, algoritmo));

            resultadoFinal = juntarResultado(resultadoFinal,
                    buscarNoCampo(film, "Descrição", film.getDescription(), termo, algoritmo));

            List<Genre> generos = filmService.listarGenerosDoFilme(film.getID());

            for (Genre genero : generos) {
                if (genero != null) {
                    resultadoFinal = juntarResultado(resultadoFinal,buscarNoCampo(film, "Gêneros", genero.getName(), termo, algoritmo));
                }
            }

            String[] diretores = film.getDirectors();

            if (diretores != null) {
                for (String diretor : diretores) {
                    resultadoFinal = juntarResultado(resultadoFinal,buscarNoCampo(film, "Diretores", diretor, termo, algoritmo));
                }
            }

            if (resultadoFinal != null) {
                resultados.add(resultadoFinal);
            }
        }

        return resultados;
    }

    private ResultadoBuscaFilme juntarResultado(ResultadoBuscaFilme principal, ResultadoBuscaFilme novo) {
        if (novo == null) {
            return principal;
        }

        if (principal == null) {
            return novo;
        }

        principal.quantidadeOcorrencias += novo.quantidadeOcorrencias;
        principal.comparacoes += novo.comparacoes;
        principal.posicoes.addAll(novo.posicoes);

        return principal;
    }

    private ResultadoBuscaFilme buscarNoCampo(Film film, String campo, String texto, String termo, String algoritmo) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }

        ResultadoBuscaTexto resultado;

        if ("kmp".equalsIgnoreCase(algoritmo)) {
            resultado = KMP.buscar(texto, termo);
        } else {
            resultado = BoyerMoore.buscar(texto, termo);
        }

        List<Integer> posicoes = resultado.getPosicoes();
        int comparacoes = resultado.getComparacoes();

        if (posicoes == null || posicoes.isEmpty()) {
            return null;
        }

        return new ResultadoBuscaFilme(film.getID(),film.getTitle(),
                campo,algoritmo.equalsIgnoreCase("kmp") ? "KMP" : "Boyer-Moore", 
                termo,
                new ArrayList<>(posicoes),
                comparacoes
        );
    }

    public static class ResultadoBuscaFilme {
        public int filmID;
        public String titulo;
        public String campo;
        public String algoritmo;
        public String termo;
        public int quantidadeOcorrencias;
        public List<Integer> posicoes;
        public int comparacoes;

        public ResultadoBuscaFilme(int filmID, String titulo, String campo, String algoritmo, String termo, List<Integer> posicoes, int comparacoes) {
            this.filmID = filmID;
            this.titulo = titulo;
            this.campo = campo;
            this.algoritmo = algoritmo;
            this.termo = termo;
            this.posicoes = posicoes;
            this.quantidadeOcorrencias = posicoes.size();
            this.comparacoes = comparacoes;
        }
    }
}