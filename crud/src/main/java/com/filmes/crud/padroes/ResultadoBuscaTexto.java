package com.filmes.crud.padroes;
import java.util.*;

public class ResultadoBuscaTexto {
    private List<Integer> posicoes;
    private int comparacoes;

    public ResultadoBuscaTexto(List<Integer> posicoes, int comparacoes) {
        this.posicoes = posicoes;
        this.comparacoes = comparacoes;
    }

    public List<Integer> getPosicoes() {
        return posicoes;
    }


    public int getComparacoes() {
        return comparacoes;
    }
}
