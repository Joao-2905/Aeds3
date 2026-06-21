package com.filmes.crud.padroes;

import java.util.ArrayList;
import java.util.List;

public class KMP {

    public static ResultadoBuscaTexto buscar(String texto, String padrao) {
        List<Integer> posicoes = new ArrayList<>();
        int comparacoes = 0; 

        if (texto == null || padrao == null || padrao.isBlank()) {
            return new ResultadoBuscaTexto(posicoes, comparacoes);
        }

        texto = texto.toLowerCase();
        padrao = padrao.toLowerCase();

        int[] falha = tabelaFalha(padrao);

        int i = 0;
        int j = 0;

        while (i < texto.length()) {
            comparacoes++; 

            if (texto.charAt(i) == padrao.charAt(j)) {
                i++;
                j++;

                if (j == padrao.length()) {
                    posicoes.add(i - j);
                    j = falha[j - 1];
                }
            } else if (j > 0) {
                j = falha[j - 1];
            } else {
                i++;
            }
        }

        return new ResultadoBuscaTexto(posicoes, comparacoes); 
    }

    public static int[] tabelaFalha(String padrao) {
        int[] falha = new int[padrao.length()];
        int j = 0;
        for (int i = 1; i < padrao.length(); i++) {
            while (j > 0 && padrao.charAt(i) != padrao.charAt(j)) {
                j = falha[j - 1];
            }

            if (padrao.charAt(i) == padrao.charAt(j)) {
                j++;
                falha[i] = j;
            }
        }

        return falha;
    }
}