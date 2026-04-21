package CRUD.index;

import java.util.ArrayList;
import java.util.List;

public class Bucket {

    private int profundidadeLocal;
    private int capacidade;
    private List<IndexEntry> entradas;

    public Bucket(int profundidadeLocal, int capacidade) {
        this.profundidadeLocal = profundidadeLocal;
        this.capacidade = capacidade;
        this.entradas = new ArrayList<>();
    }

    public boolean estaCheio() {
        return entradas.size() >= capacidade;
    }

    public int getProfundidadeLocal() {
        return profundidadeLocal;
    }

    public void setProfundidadeLocal(int profundidadeLocal) {
        this.profundidadeLocal = profundidadeLocal;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public List<IndexEntry> getEntradas() {
        return entradas;
    }

    public IndexEntry buscar(int id) {
        for (IndexEntry e : entradas) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    public boolean remover(int id) {
        return entradas.removeIf(e -> e.getId() == id);
    }

    public void limpar() {
        entradas.clear();
    }
}