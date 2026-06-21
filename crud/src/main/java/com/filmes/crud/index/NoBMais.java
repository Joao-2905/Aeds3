package com.filmes.crud.index;

import java.util.ArrayList;
import java.util.List;

public class NoBMais{

    boolean folha;
    List<Integer> chaves;
    List<NoBMais> filhos;
    List<List<Integer>> valores;
    NoBMais proximo;

    public NoBMais(boolean folha) {
        this.folha = folha;
        this.chaves = new ArrayList<>();
        this.filhos = new ArrayList<>();
        this.valores = new ArrayList<>();
        this.proximo = null;
    }

    public boolean isFolha() {
        return folha;
    }

    public List<Integer> getChaves() {
        return chaves;
    }

    public List<NoBMais> getFilhos() {
        return filhos;
    }

    public List<List<Integer>> getValores() {
        return valores;
    }

    public NoBMais getProximo() {
        return proximo;
    }
}