package com.filmes.crud.padroes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoyerMoore {

    public static ResultadoBuscaTexto buscar(String texto, String padrao) {
        List<Integer> posicoes = new ArrayList<>();
        int comparacoes = 0; 

        if (texto == null || padrao == null || padrao.isBlank()) {
            return new ResultadoBuscaTexto(posicoes, comparacoes);
        }

        texto = texto.toLowerCase();
        padrao = padrao.toLowerCase();

        int n = texto.length();
        int m = padrao.length();

        if(m > n){
            return new ResultadoBuscaTexto(posicoes, comparacoes);
        }

        Map<Character, Integer> caractereRuim = tabelaCaractereRuim(padrao);
        int[] bomSufixo = tabelaBomSufixo(padrao);

        int deslocamento = 0;

        while (deslocamento <= n - m) {
            int j = m - 1;
            while (j >= 0) {
                comparacoes++; 
                if (padrao.charAt(j) != texto.charAt(deslocamento + j)) {
                    break;
                }
                j--;
            }

            if (j < 0) {
                posicoes.add(deslocamento);
                deslocamento += bomSufixo[0];
            } else {
                char ruim = texto.charAt(deslocamento + j);

                int ultima = caractereRuim.getOrDefault(ruim, -1);
                int deslocRuim = Math.max(1, j - ultima);
                int deslocBom = bomSufixo[j];

                deslocamento += Math.max(deslocRuim, deslocBom);
            }
        }

        return new ResultadoBuscaTexto(posicoes, comparacoes); 
    }

    private static Map<Character, Integer> tabelaCaractereRuim(String padrao) {
        Map<Character, Integer> tabela = new HashMap<>();

        for (int i = 0; i < padrao.length() - 1; i++) {
            tabela.put(padrao.charAt(i), i);
        }

        return tabela;
    }

    private static int[] tabelaBomSufixo(String padrao) {
        int m = padrao.length();

        int[] deslocamento = new int[m + 1];
        int[] borda = new int[m + 1];

        int i = m;
        int j = m + 1;

        borda[i] = j;

        while (i > 0) {
            while (j <= m && padrao.charAt(i - 1) != padrao.charAt(j - 1)) {
                if (deslocamento[j] == 0) {
                    deslocamento[j] = j - i;
                }

                j = borda[j];
            }

            i--;
            j--;
            borda[i] = j;
        }

        j = borda[0];

        for (i = 0; i <= m; i++) {
            if (deslocamento[i] == 0) {
                deslocamento[i] = j;
            }

            if (i == j) {
                j = borda[j];
            }
        }

        int[] resultado = new int[m];

        for (i = 0; i < m; i++) {
            resultado[i] = deslocamento[i + 1];
        }

        return resultado;
    }
}