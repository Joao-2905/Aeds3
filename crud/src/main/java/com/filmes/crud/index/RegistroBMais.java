package com.filmes.crud.index;

public class RegistroBMais {

    private int chave;
    private int idRegistro;

    public RegistroBMais(int chave, int idRegistro) {
        this.chave = chave;
        this.idRegistro = idRegistro;
    }

    public int getChave() {
        return chave;
    }

    public int getIdRegistro() {
        return idRegistro;
    }
}